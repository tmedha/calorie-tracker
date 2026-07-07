package com.calorietracker.db.dao;

import com.calorietracker.db.Database;
import com.calorietracker.model.WeightEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Access to {@code weight_entries}: one weight per day, stored in kilograms. */
public class WeightEntryDao {

    private final Database database;

    public WeightEntryDao(Database database) {
        this.database = database;
    }

    /** Inserts or replaces the weight for a given day. */
    public void upsert(LocalDate date, double weightKg) {
        String sql = "INSERT INTO weight_entries (entry_date, weight_kg) VALUES (?, ?) "
                + "ON CONFLICT(entry_date) DO UPDATE SET weight_kg = excluded.weight_kg";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, date.toString());
            ps.setDouble(2, weightKg);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save weight for " + date, e);
        }
    }

    /** All entries ordered oldest to newest. */
    public List<WeightEntry> findAll() {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT entry_date, weight_kg FROM weight_entries ORDER BY entry_date");
             ResultSet rs = ps.executeQuery()) {
            List<WeightEntry> entries = new ArrayList<>();
            while (rs.next()) {
                entries.add(new WeightEntry(LocalDate.parse(rs.getString("entry_date")),
                        rs.getDouble("weight_kg")));
            }
            return entries;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load weight history", e);
        }
    }

    /** The most recent weight entry, or null if none has been logged. */
    public WeightEntry latest() {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT entry_date, weight_kg FROM weight_entries ORDER BY entry_date DESC LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next()
                    ? new WeightEntry(LocalDate.parse(rs.getString("entry_date")), rs.getDouble("weight_kg"))
                    : null;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load latest weight", e);
        }
    }

    private Connection conn() {
        return database.getConnection();
    }
}
