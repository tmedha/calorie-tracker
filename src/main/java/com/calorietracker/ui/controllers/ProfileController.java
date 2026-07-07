package com.calorietracker.ui.controllers;

import com.calorietracker.model.ActivityLevel;
import com.calorietracker.model.Goal;
import com.calorietracker.model.Sex;
import com.calorietracker.model.UnitSystem;
import com.calorietracker.model.UserProfile;
import com.calorietracker.service.BmrCalculator;
import com.calorietracker.service.UnitConverter;
import com.calorietracker.ui.AppContext;
import com.calorietracker.util.NumberFmt;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

import java.time.LocalDate;
import java.time.Period;

/** Profile screen: collects body metrics, computes BMR/TDEE, and stores daily targets. */
public class ProfileController {

    private final AppContext context;
    private UnitSystem unitSystem;

    @FXML private TextField nameField;
    @FXML private ComboBox<Sex> sexCombo;
    @FXML private DatePicker birthPicker;
    @FXML private Node metricBox;
    @FXML private Node imperialBox;
    @FXML private TextField weightKgField;
    @FXML private TextField heightCmField;
    @FXML private TextField weightLbField;
    @FXML private TextField heightFtField;
    @FXML private TextField heightInField;
    @FXML private ComboBox<ActivityLevel> activityCombo;
    @FXML private ComboBox<Goal> goalCombo;

    @FXML private Label bmrValue;
    @FXML private Label tdeeValue;
    @FXML private Label suggestedValue;
    @FXML private Label incompleteHint;
    @FXML private TextField calorieField;
    @FXML private TextField proteinField;
    @FXML private TextField carbsField;
    @FXML private TextField fatField;
    @FXML private Label statusLabel;

    public ProfileController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        UserProfile p = context.profile();
        unitSystem = p.getUnitSystem();

        sexCombo.getItems().setAll(Sex.values());
        activityCombo.getItems().setAll(ActivityLevel.values());
        goalCombo.getItems().setAll(Goal.values());

        nameField.setText(p.getName() == null ? "" : p.getName());
        sexCombo.setValue(p.getSex());
        birthPicker.setValue(p.getBirthDate());
        activityCombo.setValue(p.getActivityLevel());
        goalCombo.setValue(p.getGoal());

        configureUnitFields(p);
        prefillTargets(p);

        // Live recompute on any input that affects the calculation.
        Runnable recompute = this::recompute;
        birthPicker.valueProperty().addListener((o, a, b) -> recompute.run());
        sexCombo.valueProperty().addListener((o, a, b) -> recompute.run());
        activityCombo.valueProperty().addListener((o, a, b) -> recompute.run());
        goalCombo.valueProperty().addListener((o, a, b) -> recompute.run());
        for (TextField f : new TextField[]{weightKgField, heightCmField,
                weightLbField, heightFtField, heightInField}) {
            f.textProperty().addListener((o, a, b) -> recompute.run());
        }

        statusLabel.setText("");
        recompute();
    }

    private void configureUnitFields(UserProfile p) {
        boolean metric = unitSystem == UnitSystem.METRIC;
        show(metricBox, metric);
        show(imperialBox, !metric);

        if (p.getWeightKg() > 0) {
            if (metric) {
                weightKgField.setText(NumberFmt.trim(p.getWeightKg()));
            } else {
                weightLbField.setText(NumberFmt.trim(Math.round(UnitConverter.kgToLb(p.getWeightKg()))));
            }
        }
        if (p.getHeightCm() > 0) {
            if (metric) {
                heightCmField.setText(NumberFmt.trim(p.getHeightCm()));
            } else {
                heightFtField.setText(Integer.toString(UnitConverter.feetPart(p.getHeightCm())));
                heightInField.setText(NumberFmt.trim(
                        Math.round(UnitConverter.inchesPart(p.getHeightCm()) * 10) / 10.0));
            }
        }
    }

    private void prefillTargets(UserProfile p) {
        if (p.getCalorieTarget() > 0) {
            calorieField.setText(Integer.toString(p.getCalorieTarget()));
            proteinField.setText(Integer.toString(p.getProteinTargetG()));
            carbsField.setText(Integer.toString(p.getCarbsTargetG()));
            fatField.setText(Integer.toString(p.getFatTargetG()));
        }
    }

    private void recompute() {
        statusLabel.setText("");
        LocalDate dob = birthPicker.getValue();
        double kg = readWeightKg();
        double cm = readHeightCm();

        if (dob == null || kg <= 0 || cm <= 0) {
            bmrValue.setText("—");
            tdeeValue.setText("—");
            suggestedValue.setText("—");
            show(incompleteHint, true);
            return;
        }
        show(incompleteHint, false);

        int age = Period.between(dob, LocalDate.now()).getYears();
        double bmr = BmrCalculator.bmr(sexCombo.getValue(), kg, cm, age);
        double tdee = BmrCalculator.tdee(bmr, activityCombo.getValue());
        int suggested = BmrCalculator.suggestedCalorieTarget(tdee, goalCombo.getValue());

        bmrValue.setText(NumberFmt.whole(bmr));
        tdeeValue.setText(NumberFmt.whole(tdee));
        suggestedValue.setText(Integer.toString(suggested));
    }

    @FXML
    private void useSuggested() {
        LocalDate dob = birthPicker.getValue();
        double kg = readWeightKg();
        double cm = readHeightCm();
        if (dob == null || kg <= 0 || cm <= 0) {
            statusLabel.setText("Enter your details first.");
            return;
        }
        int age = Period.between(dob, LocalDate.now()).getYears();
        double tdee = BmrCalculator.tdee(
                BmrCalculator.bmr(sexCombo.getValue(), kg, cm, age), activityCombo.getValue());
        int target = BmrCalculator.suggestedCalorieTarget(tdee, goalCombo.getValue());
        BmrCalculator.MacroTargets macros = BmrCalculator.suggestedMacros(target);

        calorieField.setText(Integer.toString(target));
        proteinField.setText(Integer.toString(macros.proteinG()));
        carbsField.setText(Integer.toString(macros.carbsG()));
        fatField.setText(Integer.toString(macros.fatG()));
    }

    @FXML
    private void save() {
        UserProfile p = context.profile();
        p.setName(nameField.getText() == null ? "" : nameField.getText().trim());
        p.setSex(sexCombo.getValue());
        p.setBirthDate(birthPicker.getValue());
        p.setWeightKg(readWeightKg());
        p.setHeightCm(readHeightCm());
        p.setActivityLevel(activityCombo.getValue());
        p.setGoal(goalCombo.getValue());
        p.setCalorieTarget(parseInt(calorieField));
        p.setProteinTargetG(parseInt(proteinField));
        p.setCarbsTargetG(parseInt(carbsField));
        p.setFatTargetG(parseInt(fatField));
        context.saveProfile();
        statusLabel.setText("Saved.");
    }

    private double readWeightKg() {
        if (unitSystem == UnitSystem.METRIC) {
            return parseDouble(weightKgField);
        }
        return UnitConverter.lbToKg(parseDouble(weightLbField));
    }

    private double readHeightCm() {
        if (unitSystem == UnitSystem.METRIC) {
            return parseDouble(heightCmField);
        }
        return UnitConverter.feetInchesToCm((int) parseDouble(heightFtField), parseDouble(heightInField));
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private static double parseDouble(TextInputControl field) {
        try {
            String t = field.getText();
            return t == null || t.isBlank() ? 0 : Double.parseDouble(t.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int parseInt(TextInputControl field) {
        return (int) Math.round(parseDouble(field));
    }
}
