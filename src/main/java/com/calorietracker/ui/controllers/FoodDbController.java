package com.calorietracker.ui.controllers;

import com.calorietracker.model.Food;
import com.calorietracker.ui.AppContext;
import com.calorietracker.ui.FoodEditorDialog;
import com.calorietracker.util.NumberFmt;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.Optional;

/** Food Database screen: searchable table with full create/edit/delete. */
public class FoodDbController {

    private final AppContext context;
    private final ObservableList<Food> foods = FXCollections.observableArrayList();

    @FXML private TextField searchField;
    @FXML private TableView<Food> foodTable;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    public FoodDbController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        buildColumns();
        foodTable.setItems(foods);
        foodTable.setPlaceholder(new javafx.scene.control.Label("No foods found."));
        foodTable.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<Food>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    editFood(row.getItem());
                }
            });
            return row;
        });

        searchField.textProperty().addListener((o, a, b) -> reload());

        editButton.disableProperty().bind(
                foodTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(
                foodTable.getSelectionModel().selectedItemProperty().isNull());

        reload();
    }

    private void buildColumns() {
        TableColumn<Food, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        name.setMinWidth(220);

        TableColumn<Food, String> serving = new TableColumn<>("Serving");
        serving.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().servingLabel()));
        serving.setMinWidth(110);

        TableColumn<Food, String> cals = numberColumn("Calories", f -> NumberFmt.whole(f.getCalories()));
        TableColumn<Food, String> protein = numberColumn("Protein", f -> NumberFmt.trim(f.getProteinG()) + " g");
        TableColumn<Food, String> carbs = numberColumn("Carbs", f -> NumberFmt.trim(f.getCarbsG()) + " g");
        TableColumn<Food, String> fat = numberColumn("Fat", f -> NumberFmt.trim(f.getFatG()) + " g");

        TableColumn<Food, String> source = new TableColumn<>("Source");
        source.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isSeed() ? "Built-in" : "Custom"));
        source.setMinWidth(90);

        foodTable.getColumns().setAll(java.util.List.of(name, serving, cals, protein, carbs, fat, source));
        foodTable.getColumns().forEach(col -> col.setReorderable(false));
    }

    private TableColumn<Food, String> numberColumn(String title, java.util.function.Function<Food, String> value) {
        TableColumn<Food, String> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> new SimpleStringProperty(value.apply(c.getValue())));
        col.setMinWidth(90);
        return col;
    }

    private void reload() {
        foods.setAll(context.foods().search(searchField.getText()));
    }

    @FXML
    private void addFood() {
        FoodEditorDialog.show(foodTable, null).ifPresent(food -> {
            context.foods().insert(food);
            reload();
            foodTable.getSelectionModel().select(food);
        });
    }

    @FXML
    private void editSelected() {
        Food selected = foodTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editFood(selected);
        }
    }

    private void editFood(Food food) {
        FoodEditorDialog.show(foodTable, food).ifPresent(updated -> {
            context.foods().update(updated);
            reload();
        });
    }

    @FXML
    private void deleteSelected() {
        Food selected = foodTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + selected.getName() + "\"? Any logged entries using it will also be removed.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.initOwner(foodTable.getScene().getWindow());
        confirm.getDialogPane().getStylesheets().addAll(foodTable.getScene().getStylesheets());
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            context.foods().delete(selected.getId());
            reload();
        }
    }
}
