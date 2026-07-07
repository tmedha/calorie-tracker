package com.calorietracker;

import com.calorietracker.service.StreakService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreakServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 6);

    @Test
    void countsConsecutiveDaysEndingToday() {
        Set<LocalDate> dates = Set.of(TODAY, TODAY.minusDays(1), TODAY.minusDays(2));
        assertEquals(3, StreakService.currentStreak(dates, TODAY));
    }

    @Test
    void yesterdayGraceKeepsStreakWhenTodayNotYetLogged() {
        Set<LocalDate> dates = Set.of(TODAY.minusDays(1), TODAY.minusDays(2));
        assertEquals(2, StreakService.currentStreak(dates, TODAY));
    }

    @Test
    void brokenWhenNeitherTodayNorYesterdayLogged() {
        Set<LocalDate> dates = Set.of(TODAY.minusDays(2), TODAY.minusDays(3));
        assertEquals(0, StreakService.currentStreak(dates, TODAY));
    }

    @Test
    void stopsAtAGap() {
        // today and 2 days ago logged, but yesterday missing -> only today counts
        Set<LocalDate> dates = Set.of(TODAY, TODAY.minusDays(2), TODAY.minusDays(3));
        assertEquals(1, StreakService.currentStreak(dates, TODAY));
    }

    @Test
    void emptyIsZero() {
        assertEquals(0, StreakService.currentStreak(Set.of(), TODAY));
    }
}
