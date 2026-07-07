package com.calorietracker.db.dao;

import com.calorietracker.db.Database;
import com.calorietracker.model.Food;
import com.calorietracker.model.FoodLogEntry;
import com.calorietracker.model.MealType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Access to {@code log_entries}, joined with {@code foods} for display. */
public class LogEntryDao {

    private final Database database;

    public LogEntryDao(Database database) {
        this.database = database;
    }

    /** All entries logged on a given day, each with its resolved food. */
    public List<FoodLogEntry> findByDate(LocalDate date) {
        String sql = "SELECT e.id AS entry_id, e.log_date, e.meal_type, e.servings, f.* "
                + "FROM log_entries e JOIN foods f ON f.id = e.food_id "
                + "WHERE e.log_date = ? ORDER BY e.id";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<FoodLogEntry> entries = new ArrayList<>();
                while (rs.next()) {
                    Food food = FoodDao.map(rs);
                    entries.add(new FoodLogEntry(
                            rs.getInt("entry_id"),
                            LocalDate.parse(rs.getString("log_date")),
                            MealType.valueOf(rs.getString("meal_type")),
                            rs.getDouble("servings"),
                            food));
                }
                return entries;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load log for " + date, e);
        }
    }

    public FoodLogEntry insert(LocalDate date, MealType mealType, int foodId, double servings) {
        String sql = "INSERT INTO log_entries (log_date, meal_type, food_id, servings) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, date.toString());
            ps.setString(2, mealType.name());
            ps.setInt(3, foodId);
            ps.setDouble(4, servings);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : 0;
                FoodLogEntry entry = new FoodLogEntry();
                entry.setId(id);
                entry.setLogDate(date);
                entry.setMealType(mealType);
                entry.setServings(servings);
                return entry;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to add log entry", e);
        }
    }

    public void updateServings(int entryId, double servings) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE log_entries SET servings = ? WHERE id = ?")) {
            ps.setDouble(1, servings);
            ps.setInt(2, entryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update log entry " + entryId, e);
        }
    }

    public void delete(int entryId) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM log_entries WHERE id = ?")) {
            ps.setInt(1, entryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete log entry " + entryId, e);
        }
    }

    private Connection conn() {
        return database.getConnection();
    }
}
