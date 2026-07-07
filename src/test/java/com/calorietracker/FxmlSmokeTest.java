package com.calorietracker;

import com.calorietracker.db.Database;
import com.calorietracker.ui.AppContext;
import com.calorietracker.ui.controllers.DashboardController;
import com.calorietracker.ui.controllers.FoodDbController;
import com.calorietracker.ui.controllers.ProfileController;
import com.calorietracker.ui.controllers.SettingsController;
import com.calorietracker.ui.controllers.SuggestController;
import com.calorietracker.ui.controllers.TrendsController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Loads each FXML view with a real controller to catch FXML typos and binding errors.
 * Skipped automatically when no JavaFX toolkit/display is available (e.g. headless CI).
 */
class FxmlSmokeTest {

    @BeforeAll
    static void bootToolkit() {
        try {
            CountDownLatch started = new CountDownLatch(1);
            Platform.setImplicitExit(false);
            Platform.startup(started::countDown);
            Assumptions.assumeTrue(started.await(10, TimeUnit.SECONDS), "JavaFX toolkit did not start");
        } catch (IllegalStateException alreadyRunning) {
            // Toolkit already initialized by a previous test — fine.
        } catch (Throwable t) {
            Assumptions.abort("JavaFX toolkit unavailable: " + t.getMessage());
        }
    }

    @Test
    void allViewsLoadWithoutError(@TempDir Path dir) throws Exception {
        try (Database database = new Database(dir.resolve("smoke.db"))) {
            database.init();
            AppContext ctx = new AppContext(database);
            loadOnFxThread("/fxml/dashboard.fxml", new DashboardController(ctx));
            loadOnFxThread("/fxml/food_db.fxml", new FoodDbController(ctx));
            loadOnFxThread("/fxml/suggest.fxml", new SuggestController(ctx));
            loadOnFxThread("/fxml/trends.fxml", new TrendsController(ctx));
            loadOnFxThread("/fxml/profile.fxml", new ProfileController(ctx));
            loadOnFxThread("/fxml/settings.fxml", new SettingsController(ctx));
        }
    }

    private void loadOnFxThread(String fxml, Object controller) throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                loader.setController(controller);
                loader.load();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                done.countDown();
            }
        });
        if (!done.await(10, TimeUnit.SECONDS)) {
            fail("Timed out loading " + fxml);
        }
        if (error.get() != null) {
            fail("Failed to load " + fxml + ": " + error.get(), error.get());
        }
    }
}
