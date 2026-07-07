package com.calorietracker.db.dao;

import com.calorietracker.db.Database;
import com.calorietracker.model.Food;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** CRUD access to the {@code foods} table. */
public class FoodDao {

    private final Database database;

    public FoodDao(Database database) {
        this.database = database;
    }

    /** All foods ordered by name; optionally filtered by a case-insensitive name search. */
    public List<Food> search(String query) {
        String sql = "SELECT * FROM foods";
        boolean filtered = query != null && !query.isBlank();
        if (filtered) {
            sql += " WHERE name LIKE ?";
        }
        sql += " ORDER BY name COLLATE NOCASE";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            if (filtered) {
                ps.setString(1, "%" + query.trim() + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Food> foods = new ArrayList<>();
                while (rs.next()) {
                    foods.add(map(rs));
                }
                return foods;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load foods", e);
        }
    }

    public List<Food> findAll() {
        return search(null);
    }

    public Food findById(int id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM foods WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load food " + id, e);
        }
    }

    /** Inserts a new food and returns it with the generated id set. */
    public Food insert(Food food) {
        String sql = "INSERT INTO foods (name, serving_size, serving_unit, calories, "
                + "protein_g, carbs_g, fat_g, is_seed) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindFields(ps, food);
            ps.setInt(8, food.isSeed() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    food.setId(keys.getInt(1));
                }
            }
            return food;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert food", e);
        }
    }

    public void update(Food food) {
        String sql = "UPDATE foods SET name = ?, serving_size = ?, serving_unit = ?, "
                + "calories = ?, protein_g = ?, carbs_g = ?, fat_g = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            bindFields(ps, food);
            ps.setInt(8, food.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update food " + food.getId(), e);
        }
    }

    /** Deletes a food; cascades to its log entries. */
    public void delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM foods WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete food " + id, e);
        }
    }

    private void bindFields(PreparedStatement ps, Food food) throws SQLException {
        ps.setString(1, food.getName());
        ps.setDouble(2, food.getServingSize());
        ps.setString(3, food.getServingUnit());
        ps.setDouble(4, food.getCalories());
        ps.setDouble(5, food.getProteinG());
        ps.setDouble(6, food.getCarbsG());
        ps.setDouble(7, food.getFatG());
    }

    static Food map(ResultSet rs) throws SQLException {
        return new Food(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("serving_size"),
                rs.getString("serving_unit"),
                rs.getDouble("calories"),
                rs.getDouble("protein_g"),
                rs.getDouble("carbs_g"),
                rs.getDouble("fat_g"),
                rs.getInt("is_seed") == 1);
    }

    private Connection conn() {
        return database.getConnection();
    }
}
