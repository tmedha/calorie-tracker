package com.calorietracker.service;

import com.calorietracker.model.FoodLogEntry;

import java.util.Collection;

/** Aggregates logged food into calorie and macro totals. */
public final class NutritionService {

    private NutritionService() {
    }

    /** Sums calories and macros across the given entries. */
    public static MacroTotals total(Collection<FoodLogEntry> entries) {
        double calories = 0, protein = 0, carbs = 0, fat = 0;
        for (FoodLogEntry e : entries) {
            calories += e.getCalories();
            protein += e.getProteinG();
            carbs += e.getCarbsG();
            fat += e.getFatG();
        }
        return new MacroTotals(calories, protein, carbs, fat);
    }

    /** Calorie and macro totals for a set of logged foods. */
    public record MacroTotals(double calories, double proteinG, double carbsG, double fatG) {

        public static MacroTotals empty() {
            return new MacroTotals(0, 0, 0, 0);
        }

        /** Fraction of {@code target} these calories represent, clamped to [0, 1]. */
        public double calorieProgress(int target) {
            return progress(calories, target);
        }

        public static double progress(double value, int target) {
            if (target <= 0) {
                return 0;
            }
            return Math.min(1.0, value / target);
        }
    }
}
