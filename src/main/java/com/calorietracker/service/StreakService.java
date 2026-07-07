package com.calorietracker.service;

import java.time.LocalDate;
import java.util.Set;

/** Computes the current daily-logging streak from the set of dates that have log entries. */
public final class StreakService {

    private StreakService() {
    }

    /**
     * Length of the run of consecutive logged days ending at {@code today} (or, if today has
     * no entries yet, ending at yesterday — a grace day so the streak survives an un-logged
     * morning). Returns 0 if neither today nor yesterday was logged.
     */
    public static int currentStreak(Set<LocalDate> loggedDates, LocalDate today) {
        if (loggedDates == null || loggedDates.isEmpty()) {
            return 0;
        }
        LocalDate cursor;
        if (loggedDates.contains(today)) {
            cursor = today;
        } else if (loggedDates.contains(today.minusDays(1))) {
            cursor = today.minusDays(1);
        } else {
            return 0;
        }

        int streak = 0;
        while (loggedDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
