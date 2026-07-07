package com.calorietracker.ui.controllers;

import com.calorietracker.model.FoodLogEntry;
import com.calorietracker.model.MealType;
import com.calorietracker.model.UserProfile;
import com.calorietracker.service.NutritionService;
import com.calorietracker.service.NutritionService.MacroTotals;
import com.calorietracker.service.StreakService;
import com.calorietracker.ui.AppContext;
import com.calorietracker.ui.FoodPickerDialog;
import com.calorietracker.ui.controls.CalorieRing;
import com.calorietracker.util.NumberFmt;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Dashboard: date navigation, a daily calorie/macro summary, and per-meal logging. */
public class DashboardController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");

    private final AppContext context;

    @FXML private Label dateSubtitle;
    @FXML private javafx.scene.control.DatePicker datePicker;
    @FXML private CalorieRing calorieRing;
    @FXML private Label caloriesSub;
    @FXML private Node streakChip;
    @FXML private Label streakLabel;
    @FXML private Label proteinLabel;
    @FXML private ProgressBar proteinBar;
    @FXML private Label carbsLabel;
    @FXML private ProgressBar carbsBar;
    @FXML private Label fatLabel;
    @FXML private ProgressBar fatBar;
    @FXML private VBox mealsBox;

    public DashboardController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
        datePicker.valueProperty().addListener((o, a, b) -> refresh());
        refresh();
    }

    @FXML
    private void previousDay() {
        datePicker.setValue(currentDate().minusDays(1));
    }

    @FXML
    private void nextDay() {
        datePicker.setValue(currentDate().plusDays(1));
    }

    @FXML
    private void goToday() {
        datePicker.setValue(LocalDate.now());
    }

    private LocalDate currentDate() {
        return datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue();
    }

    private void refresh() {
        LocalDate date = currentDate();
        List<FoodLogEntry> entries = context.logEntries().findByDate(date);

        String prefix = date.equals(LocalDate.now()) ? "Today · "
                : date.equals(LocalDate.now().minusDays(1)) ? "Yesterday · " : "";
        dateSubtitle.setText(prefix + date.format(DATE_FMT));

        updateSummary(NutritionService.total(entries));
        updateStreak();
        buildMeals(entries);
    }

    private void updateSummary(MacroTotals totals) {
        UserProfile p = context.profile();

        int calTarget = p.getCalorieTarget();
        calorieRing.setValues(totals.calories(), calTarget);
        if (calTarget > 0) {
            int remaining = calTarget - (int) Math.round(totals.calories());
            caloriesSub.setText(remaining >= 0 ? remaining + " kcal left" : (-remaining) + " kcal over");
        } else {
            caloriesSub.setText("Set a calorie target in Profile");
        }

        updateMacro(proteinLabel, proteinBar, totals.proteinG(), p.getProteinTargetG());
        updateMacro(carbsLabel, carbsBar, totals.carbsG(), p.getCarbsTargetG());
        updateMacro(fatLabel, fatBar, totals.fatG(), p.getFatTargetG());
    }

    private void updateStreak() {
        int streak = StreakService.currentStreak(
                context.logEntries().distinctLoggedDates(), LocalDate.now());
        boolean show = streak > 0;
        streakChip.setVisible(show);
        streakChip.setManaged(show);
        if (show) {
            streakLabel.setText("🔥 " + streak + "-day streak");
        }
    }

    private void updateMacro(Label label, ProgressBar bar, double consumed, int target) {
        if (target > 0) {
            label.setText(NumberFmt.whole(consumed) + " / " + target + " g");
            bar.setProgress(MacroTotals.progress(consumed, target));
        } else {
            label.setText(NumberFmt.whole(consumed) + " g");
            bar.setProgress(0);
        }
    }

    private void buildMeals(List<FoodLogEntry> allEntries) {
        mealsBox.getChildren().clear();
        for (MealType meal : MealType.values()) {
            List<FoodLogEntry> mealEntries = new ArrayList<>();
            for (FoodLogEntry e : allEntries) {
                if (e.getMealType() == meal) {
                    mealEntries.add(e);
                }
            }
            mealsBox.getChildren().add(buildMealSection(meal, mealEntries));
        }
    }

    private VBox buildMealSection(MealType meal, List<FoodLogEntry> entries) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");

        double mealCalories = NutritionService.total(entries).calories();

        Label title = new Label(meal.getLabel());
        title.getStyleClass().add("meal-header");
        Label kcal = new Label(NumberFmt.whole(mealCalories) + " kcal");
        kcal.getStyleClass().add("muted");
        Button add = new Button("＋ Add food");
        add.getStyleClass().add("ghost-button");
        add.setOnAction(e -> openPicker(meal));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, title, spacer, kcal, add);
        header.setStyle("-fx-alignment: center-left;");
        card.getChildren().add(header);

        if (entries.isEmpty()) {
            Label empty = new Label("No items yet.");
            empty.getStyleClass().add("empty-hint");
            card.getChildren().add(empty);
        } else {
            for (FoodLogEntry entry : entries) {
                card.getChildren().add(buildEntryRow(entry));
            }
        }
        return card;
    }

    private HBox buildEntryRow(FoodLogEntry entry) {
        Label name = new Label(entry.getFood().getName());
        name.getStyleClass().add("entry-name");

        String meta = NumberFmt.trim(entry.getServings()) + " × " + entry.getFood().servingLabel()
                + "  ·  " + NumberFmt.whole(entry.getProteinG()) + "P "
                + NumberFmt.whole(entry.getCarbsG()) + "C "
                + NumberFmt.whole(entry.getFatG()) + "F";
        Label metaLabel = new Label(meta);
        metaLabel.getStyleClass().add("entry-meta");

        VBox info = new VBox(2, name, metaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label cals = new Label(NumberFmt.whole(entry.getCalories()) + " kcal");
        cals.getStyleClass().add("entry-cals");

        Button remove = new Button("✕");
        remove.getStyleClass().add("danger-link");
        remove.setOnAction(e -> {
            context.logEntries().delete(entry.getId());
            refresh();
        });

        HBox row = new HBox(12, info, spacer, cals, remove);
        row.getStyleClass().add("entry-row");
        row.setStyle("-fx-alignment: center-left;");
        return row;
    }

    private void openPicker(MealType meal) {
        FoodPickerDialog.show(mealsBox, context.foods(), meal.getLabel()).ifPresent(result -> {
            context.logEntries().insert(currentDate(), meal, result.food().getId(), result.servings());
            refresh();
        });
    }
}
