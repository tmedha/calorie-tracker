package com.calorietracker;

import com.calorietracker.db.Database;
import com.calorietracker.ui.AppContext;
import com.calorietracker.ui.controllers.DashboardController;
import com.calorietracker.ui.controllers.FoodDbController;
import com.calorietracker.ui.controllers.ProfileController;
import com.calorietracker.ui.controllers.SettingsController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/** JavaFX entry point: builds the sidebar shell and swaps content views. */
public class App extends Application {

    private final Database database = new Database();
    private AppContext context;

    private final BorderPane shell = new BorderPane();
    private final Map<Nav, Button> navButtons = new EnumMap<>(Nav.class);

    /** The four top-level destinations, with an emoji glyph and content view supplier. */
    private enum Nav {
        DASHBOARD("Dashboard", "🏠", "/fxml/dashboard.fxml"),
        FOODS("Food Database", "🍎", "/fxml/food_db.fxml"),
        PROFILE("Profile", "👤", "/fxml/profile.fxml"),
        SETTINGS("Settings", "⚙", "/fxml/settings.fxml");

        final String label;
        final String glyph;
        final String fxml;

        Nav(String label, String glyph, String fxml) {
            this.label = label;
            this.glyph = glyph;
            this.fxml = fxml;
        }
    }

    @Override
    public void init() {
        database.init();
        context = new AppContext(database);
    }

    @Override
    public void start(Stage stage) {
        shell.setLeft(buildSidebar());
        navigate(Nav.DASHBOARD);

        Scene scene = new Scene(shell, 1080, 720);
        scene.getStylesheets().add(resource("/css/app.css"));
        stage.setTitle("Calorie Tracker");
        stage.setScene(scene);
        stage.setMinWidth(920);
        stage.setMinHeight(600);
        stage.show();
    }

    @Override
    public void stop() {
        database.close();
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");

        Label brand = new Label("◉  Calorie Tracker");
        brand.getStyleClass().add("brand");
        sidebar.getChildren().add(brand);

        for (Nav nav : Nav.values()) {
            Button button = new Button(nav.label);
            button.getStyleClass().add("nav-button");
            button.setText(nav.glyph + "   " + nav.label);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> navigate(nav));
            navButtons.put(nav, button);
            sidebar.getChildren().add(button);
        }
        return sidebar;
    }

    /** Loads a destination's view, sets it as content, and highlights its nav button. */
    private void navigate(Nav nav) {
        Supplier<Object> controllerFactory = switch (nav) {
            case DASHBOARD -> () -> new DashboardController(context);
            case FOODS -> () -> new FoodDbController(context);
            case PROFILE -> () -> new ProfileController(context);
            case SETTINGS -> () -> new SettingsController(context);
        };
        shell.setCenter(loadView(nav.fxml, controllerFactory.get()));
        navButtons.forEach((key, button) ->
                button.getStyleClass().remove("active"));
        navButtons.get(nav).getStyleClass().add("active");
    }

    private Node loadView(String fxml, Object controller) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        loader.setController(controller);
        try {
            Parent view = loader.load();
            if (view instanceof Region region) {
                VBox.setVgrow(region, Priority.ALWAYS);
            }
            return view;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load view " + fxml, e);
        }
    }

    private String resource(String path) {
        return getClass().getResource(path).toExternalForm();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
