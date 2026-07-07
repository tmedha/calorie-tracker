package com.calorietracker.ui;

import com.calorietracker.db.Database;
import com.calorietracker.db.dao.FoodDao;
import com.calorietracker.db.dao.LogEntryDao;
import com.calorietracker.db.dao.ProfileDao;
import com.calorietracker.db.dao.WeightEntryDao;
import com.calorietracker.model.UserProfile;

/** Shared application services and cached profile, handed to every controller. */
public class AppContext {

    private final Database database;
    private final FoodDao foodDao;
    private final LogEntryDao logEntryDao;
    private final ProfileDao profileDao;
    private final WeightEntryDao weightDao;
    private UserProfile profile;

    public AppContext(Database database) {
        this.database = database;
        this.foodDao = new FoodDao(database);
        this.logEntryDao = new LogEntryDao(database);
        this.profileDao = new ProfileDao(database);
        this.weightDao = new WeightEntryDao(database);
        this.profile = profileDao.load();
    }

    public FoodDao foods() {
        return foodDao;
    }

    public LogEntryDao logEntries() {
        return logEntryDao;
    }

    public WeightEntryDao weights() {
        return weightDao;
    }

    public ProfileDao profiles() {
        return profileDao;
    }

    /** The cached profile. Mutate it, then call {@link #saveProfile()} to persist. */
    public UserProfile profile() {
        return profile;
    }

    public void saveProfile() {
        profileDao.save(profile);
    }

    /** Reloads the profile from storage (e.g. after external changes). */
    public void reloadProfile() {
        this.profile = profileDao.load();
    }

    public Database database() {
        return database;
    }
}
