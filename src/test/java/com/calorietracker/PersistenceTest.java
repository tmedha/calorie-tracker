package com.calorietracker;

import com.calorietracker.db.Database;
import com.calorietracker.db.dao.FoodDao;
import com.calorietracker.db.dao.LogEntryDao;
import com.calorietracker.db.dao.ProfileDao;
import com.calorietracker.db.dao.WeightEntryDao;
import com.calorietracker.model.Food;
import com.calorietracker.model.FoodLogEntry;
import com.calorietracker.model.Goal;
import com.calorietracker.model.MealType;
import com.calorietracker.model.Sex;
import com.calorietracker.model.UnitSystem;
import com.calorietracker.model.UserProfile;
import com.calorietracker.model.WeightEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** End-to-end check of schema init, seeding, and DAO CRUD against a real SQLite file. */
class PersistenceTest {

    @TempDir
    Path tempDir;

    private Database database;

    @BeforeEach
    void setUp() {
        database = new Database(tempDir.resolve("test.db"));
        database.init();
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    @Test
    void seedsFoodsOnFirstInit() {
        List<Food> foods = new FoodDao(database).findAll();
        assertTrue(foods.size() >= 40, "expected the starter foods to be seeded");
        assertTrue(foods.stream().anyMatch(f -> f.getName().equals("Chicken breast, cooked")));
    }

    @Test
    void searchFiltersByName() {
        List<Food> results = new FoodDao(database).search("egg");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(f -> f.getName().toLowerCase().contains("egg")));
    }

    @Test
    void foodCrudRoundTrips() {
        FoodDao dao = new FoodDao(database);
        Food food = dao.insert(new Food(0, "Test Bar", 1, "piece", 200, 10, 25, 8, false));
        assertTrue(food.getId() > 0);

        food.setCalories(210);
        dao.update(food);
        assertEquals(210, dao.findById(food.getId()).getCalories(), 1e-9);

        dao.delete(food.getId());
        assertNull(dao.findById(food.getId()));
    }

    @Test
    void logEntriesAreScopedByDate() {
        FoodDao foodDao = new FoodDao(database);
        LogEntryDao logDao = new LogEntryDao(database);
        int foodId = foodDao.findAll().get(0).getId();

        LocalDate today = LocalDate.of(2026, 7, 6);
        LocalDate other = today.minusDays(1);
        logDao.insert(today, MealType.BREAKFAST, foodId, 2);
        logDao.insert(other, MealType.DINNER, foodId, 1);

        List<FoodLogEntry> todays = logDao.findByDate(today);
        assertEquals(1, todays.size());
        assertEquals(MealType.BREAKFAST, todays.get(0).getMealType());
        assertEquals(2, todays.get(0).getServings(), 1e-9);
        assertNotNull(todays.get(0).getFood());
        assertEquals(1, logDao.findByDate(other).size());
    }

    @Test
    void deletingFoodCascadesToLogEntries() {
        FoodDao foodDao = new FoodDao(database);
        LogEntryDao logDao = new LogEntryDao(database);
        Food food = foodDao.insert(new Food(0, "Temp", 100, "g", 50, 1, 1, 1, false));
        LocalDate day = LocalDate.of(2026, 7, 6);
        logDao.insert(day, MealType.LUNCH, food.getId(), 1);

        foodDao.delete(food.getId());
        assertTrue(logDao.findByDate(day).isEmpty(), "log entries should cascade-delete with the food");
    }

    @Test
    void profileSaveAndLoad() {
        ProfileDao dao = new ProfileDao(database);
        UserProfile p = dao.load();
        assertNotNull(p);

        p.setName("Alex");
        p.setSex(Sex.FEMALE);
        p.setBirthDate(LocalDate.of(1996, 1, 15));
        p.setHeightCm(165);
        p.setWeightKg(65);
        p.setGoal(Goal.LOSE);
        p.setUnitSystem(UnitSystem.IMPERIAL);
        p.setCalorieTarget(1800);
        p.setProteinTargetG(135);
        dao.save(p);

        UserProfile loaded = dao.load();
        assertEquals("Alex", loaded.getName());
        assertEquals(Sex.FEMALE, loaded.getSex());
        assertEquals(LocalDate.of(1996, 1, 15), loaded.getBirthDate());
        assertEquals(165, loaded.getHeightCm(), 1e-9);
        assertEquals(Goal.LOSE, loaded.getGoal());
        assertEquals(UnitSystem.IMPERIAL, loaded.getUnitSystem());
        assertEquals(1800, loaded.getCalorieTarget());
        assertEquals(135, loaded.getProteinTargetG());
    }

    @Test
    void weightUpsertKeepsOnePerDayAndTracksHistory() {
        WeightEntryDao dao = new WeightEntryDao(database);
        LocalDate d1 = LocalDate.of(2026, 7, 1);
        LocalDate d2 = LocalDate.of(2026, 7, 6);

        dao.upsert(d1, 80.0);
        dao.upsert(d2, 79.0);
        dao.upsert(d2, 78.5); // same day -> replaces, not a second row

        List<WeightEntry> all = dao.findAll();
        assertEquals(2, all.size());
        assertEquals(d1, all.get(0).date(), "history should be ordered oldest-first");
        assertEquals(78.5, dao.latest().weightKg(), 1e-9);
        assertEquals(d2, dao.latest().date());
    }

    @Test
    void distinctLoggedDatesReflectsLoggedDays() {
        FoodDao foodDao = new FoodDao(database);
        LogEntryDao logDao = new LogEntryDao(database);
        int foodId = foodDao.findAll().get(0).getId();
        LocalDate a = LocalDate.of(2026, 7, 6);
        LocalDate b = LocalDate.of(2026, 7, 5);

        logDao.insert(a, MealType.BREAKFAST, foodId, 1);
        logDao.insert(a, MealType.LUNCH, foodId, 1); // same day, still one distinct date
        logDao.insert(b, MealType.DINNER, foodId, 1);

        assertEquals(Set.of(a, b), logDao.distinctLoggedDates());
    }

    private static void assertNull(Object o) {
        assertTrue(o == null, "expected null");
    }
}
