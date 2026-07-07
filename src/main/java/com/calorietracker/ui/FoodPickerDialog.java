package com.calorietracker.ui;

import com.calorietracker.db.dao.FoodDao;
import com.calorietracker.model.Food;
import com.calorietracker.util.NumberFmt;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Optional;

/** Modal dialog to search the food database and pick a food plus a number of servings. */
public final class FoodPickerDialog {

    /** A chosen food and how many servings to log. */
    public record PickResult(Food food, double servings) {
    }

    private FoodPickerDialog() {
    }

    public static Optional<PickResult> show(Region owner, FoodDao foodDao, String mealName) {
        Dialog<PickResult> dialog = new Dialog<>();
        dialog.setTitle("Add to " + mealName);
        dialog.setHeaderText("Search your foods and choose an amount");
        if (owner != null && owner.getScene() != null) {
            dialog.initOwner(owner.getScene().getWindow());
            dialog.getDialogPane().getStylesheets().addAll(owner.getScene().getStylesheets());
        }

        ButtonType addType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        TextField search = new TextField();
        search.setPromptText("Search foods…");

        ObservableList<Food> items = FXCollections.observableArrayList(foodDao.findAll());
        ListView<Food> list = new ListView<>(items);
        list.setPrefHeight(260);
        list.setCellFactory(v -> new FoodCell());

        TextField servings = new TextField("1");
        servings.setPrefWidth(80);
        Label preview = new Label();
        preview.getStyleClass().add("muted");

        HBox amountRow = new HBox(10, new Label("Servings:"), servings, preview);
        amountRow.setStyle("-fx-alignment: center-left;");

        VBox content = new VBox(10, search, list, amountRow);
        content.setPadding(new Insets(14, 4, 4, 4));
        content.setPrefWidth(420);
        VBox.setVgrow(list, Priority.ALWAYS);
        dialog.getDialogPane().setContent(content);

        Runnable updatePreview = () -> {
            Food f = list.getSelectionModel().getSelectedItem();
            double s = parse(servings.getText());
            if (f == null || s <= 0) {
                preview.setText("");
            } else {
                preview.setText("= " + NumberFmt.whole(f.getCalories() * s) + " kcal");
            }
        };

        search.textProperty().addListener((o, a, b) -> {
            items.setAll(foodDao.search(b));
            updatePreview.run();
        });
        list.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> updatePreview.run());
        servings.textProperty().addListener((o, a, b) -> updatePreview.run());

        dialog.setResultConverter(button -> {
            if (button != addType) {
                return null;
            }
            Food f = list.getSelectionModel().getSelectedItem();
            double s = parse(servings.getText());
            if (f == null || s <= 0) {
                return null;
            }
            return new PickResult(f, s);
        });

        return dialog.showAndWait();
    }

    private static double parse(String text) {
        try {
            return text == null || text.isBlank() ? 0 : Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** List cell showing a food's name and per-serving calories/serving. */
    private static final class FoodCell extends javafx.scene.control.ListCell<Food> {
        @Override
        protected void updateItem(Food food, boolean empty) {
            super.updateItem(food, empty);
            if (empty || food == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            Label name = new Label(food.getName());
            name.getStyleClass().add("entry-name");
            Label meta = new Label(NumberFmt.whole(food.getCalories()) + " kcal · " + food.servingLabel());
            meta.getStyleClass().add("entry-meta");
            VBox box = new VBox(1, name, meta);
            setGraphic(box);
        }
    }
}
