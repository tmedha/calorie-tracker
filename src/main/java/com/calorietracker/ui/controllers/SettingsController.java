package com.calorietracker.ui.controllers;

import com.calorietracker.model.UnitSystem;
import com.calorietracker.ui.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

/** Settings screen: choose the display unit system. */
public class SettingsController {

    private final AppContext context;

    @FXML private ComboBox<UnitSystem> unitCombo;
    @FXML private Label unitHint;
    @FXML private Label statusLabel;

    public SettingsController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        unitCombo.getItems().setAll(UnitSystem.values());
        unitCombo.setValue(context.profile().getUnitSystem());
        unitCombo.valueProperty().addListener((obs, old, val) -> updateHint(val));
        updateHint(unitCombo.getValue());
        statusLabel.setText("");
    }

    @FXML
    private void save() {
        context.profile().setUnitSystem(unitCombo.getValue());
        context.saveProfile();
        statusLabel.setText("Saved.");
    }

    private void updateHint(UnitSystem system) {
        statusLabel.setText("");
        if (system == UnitSystem.IMPERIAL) {
            unitHint.setText("Body weight and height on the Profile screen will use pounds and feet/inches.");
        } else {
            unitHint.setText("Body weight and height on the Profile screen will use kilograms and centimeters.");
        }
    }
}
