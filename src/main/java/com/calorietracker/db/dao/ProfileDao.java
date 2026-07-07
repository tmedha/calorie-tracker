package com.calorietracker.db.dao;

import com.calorietracker.db.Database;
import com.calorietracker.model.ActivityLevel;
import com.calorietracker.model.Goal;
import com.calorietracker.model.Sex;
import com.calorietracker.model.UnitSystem;
import com.calorietracker.model.UserProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/** Access to the single-row {@code profiles} table (id = 1). */
public class ProfileDao {

    private static final int PROFILE_ID = 1;

    private final Database database;

    public ProfileDao(Database database) {
        this.database = database;
    }

    /** Loads the profile; the row is guaranteed to exist by schema initialization. */
    public UserProfile load() {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM profiles WHERE id = ?")) {
            ps.setInt(1, PROFILE_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return new UserProfile();
                }
                UserProfile p = new UserProfile();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setSex(Sex.valueOf(rs.getString("sex")));
                String birth = rs.getString("birth_date");
                p.setBirthDate(birth == null || birth.isBlank() ? null : LocalDate.parse(birth));
                p.setHeightCm(rs.getDouble("height_cm"));
                p.setWeightKg(rs.getDouble("weight_kg"));
                p.setActivityLevel(ActivityLevel.valueOf(rs.getString("activity_level")));
                p.setGoal(Goal.valueOf(rs.getString("goal")));
                p.setUnitSystem(UnitSystem.valueOf(rs.getString("unit_system")));
                p.setCalorieTarget(rs.getInt("calorie_target"));
                p.setProteinTargetG(rs.getInt("protein_target_g"));
                p.setCarbsTargetG(rs.getInt("carbs_target_g"));
                p.setFatTargetG(rs.getInt("fat_target_g"));
                return p;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load profile", e);
        }
    }

    public void save(UserProfile p) {
        String sql = "UPDATE profiles SET name = ?, sex = ?, birth_date = ?, height_cm = ?, "
                + "weight_kg = ?, activity_level = ?, goal = ?, unit_system = ?, calorie_target = ?, "
                + "protein_target_g = ?, carbs_target_g = ?, fat_target_g = ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getSex().name());
            ps.setString(3, p.getBirthDate() == null ? null : p.getBirthDate().toString());
            ps.setDouble(4, p.getHeightCm());
            ps.setDouble(5, p.getWeightKg());
            ps.setString(6, p.getActivityLevel().name());
            ps.setString(7, p.getGoal().name());
            ps.setString(8, p.getUnitSystem().name());
            ps.setInt(9, p.getCalorieTarget());
            ps.setInt(10, p.getProteinTargetG());
            ps.setInt(11, p.getCarbsTargetG());
            ps.setInt(12, p.getFatTargetG());
            ps.setInt(13, PROFILE_ID);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save profile", e);
        }
    }

    private Connection conn() {
        return database.getConnection();
    }
}
