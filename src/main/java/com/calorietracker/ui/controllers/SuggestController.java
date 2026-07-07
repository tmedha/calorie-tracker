package com.calorietracker.ui.controllers;

import com.calorietracker.model.Food;
import com.calorietracker.model.MealType;
import com.calorietracker.model.UserProfile;
import com.calorietracker.service.MealSuggester;
import com.calorietracker.service.MealSuggester.MealSuggestion;
import com.calorietracker.service.MealSuggester.SuggestionItem;
import com.calorietracker.service.NutritionService;
import com.calorietracker.service.NutritionService.MacroTotals;
import com.calorietracker.ui.AppContext;
import com.calorietracker.util.NumberFmt;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Meal Suggester screen: enter targets, run the knapsack solver, review the fit, and log it. */
public class SuggestController {

    private final AppContext context;

    @FXML private TextField caloriesField;
    @FXML private TextField proteinField;
    @FXML private TextField carbsField;
    @FXML private TextField fatField;
    @FXML private TextField maxServingsField;
    @FXML private Label statusLabel;
    @FXML private VBox resultBox;

    private MealSuggestion lastSuggestion;
    private int targetCalories;
    private double targetProtein;
    private double targetCarbs;
    private double targetFat;

    public SuggestController(AppContext context) {
        this.context = context;
    }

    @FXML
    public void initialize() {
        prefillFromRemaining();
        maxServingsField.setText("4");
        statusLabel.setText("");
        showHint("Set your targets and click “Build meal” to generate a suggestion.");
    }

    /** Prefills the target fields with what's left of today's goals. */
    private void prefillFromRemaining() {
        UserProfile p = context.profile();
        MacroTotals eaten = NutritionService.total(context.logEntries().findByDate(LocalDate.now()));
        caloriesField.setText(Integer.toString(remaining(p.getCalorieTarget(), eaten.calories())));
        proteinField.setText(Integer.toString(remaining(p.getProteinTargetG(), eaten.proteinG())));
        carbsField.setText(Integer.toString(remaining(p.getCarbsTargetG(), eaten.carbsG())));
        fatField.setText(Integer.toString(remaining(p.getFatTargetG(), eaten.fatG())));
    }

    private static int remaining(int target, double eaten) {
        return Math.max(0, target - (int) Math.round(eaten));
    }

    @FXML
    private void build() {
        targetCalories = parseInt(caloriesField);
        targetProtein = parseDouble(proteinField);
        targetCarbs = parseDouble(carbsField);
        targetFat = parseDouble(fatField);
        int maxServings = Math.max(1, parseInt(maxServingsField));

        if (targetCalories <= 0) {
            statusLabel.setText("Enter a calorie target above 0.");
            showHint("Set your targets and click “Build meal” to generate a suggestion.");
            return;
        }
        List<Food> foods = context.foods().findAll();
        if (foods.isEmpty()) {
            statusLabel.setText("");
            showHint("Add some foods in the Food Database first, then come back to build a meal.");
            return;
        }

        lastSuggestion = MealSuggester.suggest(foods, targetCalories,
                targetProtein, targetCarbs, targetFat, maxServings);
        statusLabel.setText("Considered " + foods.size() + " foods.");
        renderResult(lastSuggestion);
    }

    private void renderResult(MealSuggestion suggestion) {
        resultBox.getChildren().clear();
        if (!suggestion.feasible()) {
            showHint("No combination fits those targets. Try raising the calories or the max servings.");
            return;
        }

        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label title = new Label("Suggested meal");
        title.getStyleClass().add("section-title");
        Label score = new Label(matchScore(suggestion.totals()) + "% match");
        score.getStyleClass().add("match-score");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10, title, spacer, score);
        header.setStyle("-fx-alignment: center-left;");

        Label caption = new Label("Built with a bounded-knapsack DP over your "
                + context.foods().findAll().size() + " saved foods.");
        caption.getStyleClass().add("muted");

        card.getChildren().addAll(header, caption);
        for (SuggestionItem item : suggestion.items()) {
            card.getChildren().add(itemRow(item));
        }
        card.getChildren().add(new Separator());

        MacroTotals t = suggestion.totals();
        card.getChildren().add(matchBar("Calories", t.calories(), targetCalories, "calorie-bar", "kcal"));
        card.getChildren().add(matchBar("Protein", t.proteinG(), targetProtein, "protein-bar", "g"));
        card.getChildren().add(matchBar("Carbs", t.carbsG(), targetCarbs, "carbs-bar", "g"));
        card.getChildren().add(matchBar("Fat", t.fatG(), targetFat, "fat-bar", "g"));

        Button logButton = new Button("Log this meal");
        logButton.getStyleClass().add("primary-button");
        logButton.setOnAction(e -> logMeal());
        HBox actions = new HBox(logButton);
        actions.setStyle("-fx-alignment: center-left;");
        card.getChildren().add(actions);

        resultBox.getChildren().add(card);
    }

    private HBox itemRow(SuggestionItem item) {
        Food food = item.food();
        Label name = new Label(item.servings() + " × " + food.getName());
        name.getStyleClass().add("entry-name");
        Label meta = new Label(food.servingLabel() + " each  ·  "
                + NumberFmt.whole(item.food().getProteinG() * item.servings()) + "P "
                + NumberFmt.whole(item.food().getCarbsG() * item.servings()) + "C "
                + NumberFmt.whole(item.food().getFatG() * item.servings()) + "F");
        meta.getStyleClass().add("entry-meta");
        VBox info = new VBox(2, name, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label cals = new Label(NumberFmt.whole(item.calories()) + " kcal");
        cals.getStyleClass().add("entry-cals");

        HBox row = new HBox(12, info, spacer, cals);
        row.getStyleClass().add("entry-row");
        row.setStyle("-fx-alignment: center-left;");
        return row;
    }

    private VBox matchBar(String name, double got, double target, String barClass, String unit) {
        Label label = new Label(name);
        label.getStyleClass().add("macro-name");
        String valueText = target > 0
                ? NumberFmt.whole(got) + " / " + NumberFmt.whole(target) + " " + unit
                : NumberFmt.whole(got) + " " + unit;
        Label value = new Label(valueText);
        value.getStyleClass().add("entry-meta");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox head = new HBox(8, label, spacer, value);

        ProgressBar bar = new ProgressBar(target > 0 ? Math.min(1.0, got / target) : 0);
        bar.getStyleClass().add(barClass);
        bar.setMaxWidth(Double.MAX_VALUE);

        return new VBox(4, head, bar);
    }

    private void logMeal() {
        if (lastSuggestion == null || !lastSuggestion.feasible()) {
            return;
        }
        ChoiceDialog<MealType> dialog = new ChoiceDialog<>(MealType.LUNCH, MealType.values());
        dialog.setTitle("Log meal");
        dialog.setHeaderText("Add these " + lastSuggestion.items().size() + " items to which meal?");
        dialog.setContentText("Meal:");
        if (resultBox.getScene() != null) {
            dialog.initOwner(resultBox.getScene().getWindow());
            dialog.getDialogPane().getStylesheets().addAll(resultBox.getScene().getStylesheets());
        }
        Optional<MealType> choice = dialog.showAndWait();
        if (choice.isEmpty()) {
            return;
        }
        MealType meal = choice.get();
        for (SuggestionItem item : lastSuggestion.items()) {
            context.logEntries().insert(LocalDate.now(), meal, item.food().getId(), item.servings());
        }

        Alert done = new Alert(Alert.AlertType.INFORMATION,
                "Added " + lastSuggestion.items().size() + " items to " + meal.getLabel()
                        + ". Check the Dashboard to see them.");
        done.setHeaderText(null);
        done.initOwner(resultBox.getScene().getWindow());
        done.getDialogPane().getStylesheets().addAll(resultBox.getScene().getStylesheets());
        done.showAndWait();
    }

    /** Average per-target closeness (calories + each targeted macro), as a 0–100 percentage. */
    private int matchScore(MacroTotals totals) {
        double sum = closeness(totals.calories(), targetCalories);
        int count = 1;
        if (targetProtein > 0) {
            sum += closeness(totals.proteinG(), targetProtein);
            count++;
        }
        if (targetCarbs > 0) {
            sum += closeness(totals.carbsG(), targetCarbs);
            count++;
        }
        if (targetFat > 0) {
            sum += closeness(totals.fatG(), targetFat);
            count++;
        }
        return (int) Math.round(100 * sum / count);
    }

    private static double closeness(double got, double target) {
        if (target <= 0) {
            return 1;
        }
        return Math.max(0, 1 - Math.abs(got - target) / target);
    }

    private void showHint(String text) {
        resultBox.getChildren().clear();
        Label hint = new Label(text);
        hint.getStyleClass().add("empty-hint");
        hint.setWrapText(true);
        resultBox.getChildren().add(hint);
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
