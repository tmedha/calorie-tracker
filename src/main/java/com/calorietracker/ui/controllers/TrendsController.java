package com.calorietracker.ui.controllers;

import com.calorietracker.model.UnitSystem;
import com.calorietracker.model.WeightEntry;
import com.calorietracker.service.NutritionService;
import com.calorietracker.service.UnitConverter;
import com.calorietracker.ui.AppContext;
import com.calorietracker.util.NumberFmt;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/** Trends view: a 7-day calorie bar chart and a weight-over-time line chart with logging. */
public class TrendsController {

    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("M/d");

    private final AppContext context;
    private UnitSystem unitSystem;

    @FXML private Label weeklyCaption;
    @FXML private VBox weeklyChartHolder;
    @FXML private TextField weightField;
    @FXML private Label weightUnitLabel;
    @FXML private Label latestValue;
    @FXML private Label changeValue;
    @FXML private VBox weightChartHolder;
    @FXML private Label weightEmptyHint;

    public TrendsController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        unitSystem = context.profile().getUnitSystem();
        weightUnitLabel.setText(weightUnit());
        buildWeeklyChart();
        refreshWeight();
    }

    // ---------- Weekly calories ----------

    private void buildWeeklyChart() {
        int target = context.profile().getCalorieTarget();
        weeklyCaption.setText(target > 0
                ? "Daily calories vs. your " + target + " kcal target"
                : "Daily calories · set a target in Profile to compare");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("kcal");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setBarGap(2);
        chart.setCategoryGap(14);
        chart.setPrefHeight(230);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            double calories = NutritionService.total(context.logEntries().findByDate(day)).calories();
            String label = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            series.getData().add(new XYChart.Data<>(label, Math.round(calories)));
        }
        chart.getData().add(series);

        // Recolor bars that exceed the target (a warning state, single series).
        if (target > 0) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node bar = data.getNode();
                if (bar != null && data.getYValue().doubleValue() > target) {
                    bar.getStyleClass().add("over-target");
                }
            }
        }

        weeklyChartHolder.getChildren().setAll(chart);
    }

    // ---------- Weight ----------

    private void refreshWeight() {
        List<WeightEntry> history = context.weights().findAll();
        boolean hasData = !history.isEmpty();
        weightEmptyHint.setVisible(!hasData);
        weightEmptyHint.setManaged(!hasData);
        weightChartHolder.setVisible(hasData);
        weightChartHolder.setManaged(hasData);

        if (!hasData) {
            latestValue.setText("—");
            changeValue.setText("—");
            changeValue.setStyle("");
            weightChartHolder.getChildren().clear();
            return;
        }

        double firstKg = history.get(0).weightKg();
        double lastKg = history.get(history.size() - 1).weightKg();
        latestValue.setText(NumberFmt.trim(round1(toDisplay(lastKg))) + " " + weightUnit());

        double deltaDisplay = toDisplay(lastKg) - toDisplay(firstKg);
        if (Math.abs(deltaDisplay) < 0.05) {
            changeValue.setText("±0 " + weightUnit());
            changeValue.setStyle("-fx-text-fill: -muted;");
        } else {
            String sign = deltaDisplay > 0 ? "+" : "−";
            changeValue.setText(sign + NumberFmt.trim(round1(Math.abs(deltaDisplay))) + " " + weightUnit());
            changeValue.setStyle(deltaDisplay > 0 ? "-fx-text-fill: #b91c1c;" : "-fx-text-fill: #15803d;");
        }

        buildWeightChart(history);
    }

    private void buildWeightChart(List<WeightEntry> history) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(false);
        yAxis.setLabel(weightUnit());

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(250);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (WeightEntry entry : history) {
            series.getData().add(new XYChart.Data<>(
                    entry.date().format(DAY_LABEL), round1(toDisplay(entry.weightKg()))));
        }
        chart.getData().add(series);
        weightChartHolder.getChildren().setAll(chart);
    }

    @FXML
    private void logWeight() {
        double entered = parse(weightField.getText());
        if (entered <= 0) {
            return;
        }
        double kg = unitSystem == UnitSystem.IMPERIAL ? UnitConverter.lbToKg(entered) : entered;
        context.weights().upsert(LocalDate.now(), kg);

        // Keep the profile's current weight in sync so BMR stays accurate.
        context.profile().setWeightKg(kg);
        context.saveProfile();

        weightField.clear();
        refreshWeight();
    }

    // ---------- helpers ----------

    private double toDisplay(double kg) {
        return unitSystem == UnitSystem.IMPERIAL ? UnitConverter.kgToLb(kg) : kg;
    }

    private String weightUnit() {
        return unitSystem == UnitSystem.IMPERIAL ? "lb" : "kg";
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }

    private static double parse(String text) {
        try {
            return text == null || text.isBlank() ? 0 : Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
