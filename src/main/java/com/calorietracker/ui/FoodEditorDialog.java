package com.calorietracker.ui;

import com.calorietracker.model.Food;
import com.calorietracker.util.NumberFmt;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.util.Optional;

/**
 * Modal dialog for creating or editing a {@link Food}. All macro values are per serving.
 * Returns the populated food (id preserved when editing) or empty if cancelled.
 */
public final class FoodEditorDialog {

    private FoodEditorDialog() {
    }

    public static Optional<Food> show(Region owner, Food existing) {
        boolean editing = existing != null;
        Food food = editing ? existing : new Food();

        Dialog<Food> dialog = new Dialog<>();
        dialog.setTitle(editing ? "Edit food" : "Add food");
        dialog.setHeaderText(editing ? "Update the food's details" : "Add a food to your database");
        if (owner != null && owner.getScene() != null) {
            dialog.initOwner(owner.getScene().getWindow());
            dialog.getDialogPane().getStylesheets().addAll(owner.getScene().getStylesheets());
        }

        ButtonType saveType = new ButtonType(editing ? "Save" : "Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        TextField name = new TextField(nullToEmpty(food.getName()));
        name.setPromptText("e.g. Greek yogurt");
        TextField servingSize = new TextField(editing ? NumberFmt.trim(food.getServingSize()) : "100");
        ComboBox<String> servingUnit = new ComboBox<>();
        servingUnit.setEditable(true);
        servingUnit.getItems().addAll("g", "ml", "oz", "piece", "slice", "cup", "tbsp", "tsp");
        servingUnit.setValue(editing ? food.getServingUnit() : "g");
        TextField calories = new TextField(editing ? NumberFmt.trim(food.getCalories()) : "");
        TextField protein = new TextField(editing ? NumberFmt.trim(food.getProteinG()) : "");
        TextField carbs = new TextField(editing ? NumberFmt.trim(food.getCarbsG()) : "");
        TextField fat = new TextField(editing ? NumberFmt.trim(food.getFatG()) : "");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 4, 4, 4));
        grid.addRow(0, label("Name"), name);
        grid.addRow(1, label("Serving size"), servingSize);
        grid.addRow(2, label("Serving unit"), servingUnit);
        grid.addRow(3, label("Calories (kcal)"), calories);
        grid.addRow(4, label("Protein (g)"), protein);
        grid.addRow(5, label("Carbs (g)"), carbs);
        grid.addRow(6, label("Fat (g)"), fat);
        GridPane.setHgrow(name, javafx.scene.layout.Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != saveType) {
                return null;
            }
            if (name.getText() == null || name.getText().isBlank()) {
                return null;
            }
            food.setName(name.getText().trim());
            food.setServingSize(parse(servingSize.getText(), 1));
            String unit = servingUnit.getValue();
            food.setServingUnit(unit == null || unit.isBlank() ? "g" : unit.trim());
            food.setCalories(parse(calories.getText(), 0));
            food.setProteinG(parse(protein.getText(), 0));
            food.setCarbsG(parse(carbs.getText(), 0));
            food.setFatG(parse(fat.getText(), 0));
            return food;
        });

        return dialog.showAndWait();
    }

    private static Label label(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return l;
    }

    private static double parse(String text, double fallback) {
        try {
            return text == null || text.isBlank() ? fallback : Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
