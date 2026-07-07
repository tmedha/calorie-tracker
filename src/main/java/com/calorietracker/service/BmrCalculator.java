package com.calorietracker.service;

import com.calorietracker.model.ActivityLevel;
import com.calorietracker.model.Goal;
import com.calorietracker.model.Sex;
import com.calorietracker.model.UserProfile;

/**
 * Basal metabolic rate and calorie/macro targets using the Mifflin-St Jeor equation.
 * All inputs are metric (kg, cm) as stored on the profile.
 */
public final class BmrCalculator {

    // Standard macro calorie densities (kcal per gram).
    private static final int KCAL_PER_G_PROTEIN = 4;
    private static final int KCAL_PER_G_CARBS = 4;
    private static final int KCAL_PER_G_FAT = 9;

    // Default macro split of total calories: 30% protein / 40% carbs / 30% fat.
    private static final double PROTEIN_RATIO = 0.30;
    private static final double CARBS_RATIO = 0.40;
    private static final double FAT_RATIO = 0.30;

    private BmrCalculator() {
    }

    /** Mifflin-St Jeor BMR in kcal/day. */
    public static double bmr(Sex sex, double weightKg, double heightCm, int age) {
        double base = 10 * weightKg + 6.25 * heightCm - 5 * age;
        return sex == Sex.FEMALE ? base - 161 : base + 5;
    }

    public static double bmr(UserProfile p) {
        return bmr(p.getSex(), p.getWeightKg(), p.getHeightCm(), p.getAge());
    }

    /** Total daily energy expenditure: BMR scaled by the activity multiplier. */
    public static double tdee(double bmr, ActivityLevel activityLevel) {
        return bmr * activityLevel.getMultiplier();
    }

    public static double tdee(UserProfile p) {
        return tdee(bmr(p), p.getActivityLevel());
    }

    /** Suggested daily calorie target: TDEE adjusted for the weight goal, rounded to 10s. */
    public static int suggestedCalorieTarget(double tdee, Goal goal) {
        double adjusted = tdee + goal.getCalorieAdjustment();
        return (int) (Math.round(adjusted / 10.0) * 10);
    }

    public static int suggestedCalorieTarget(UserProfile p) {
        return suggestedCalorieTarget(tdee(p), p.getGoal());
    }

    /** Macro gram targets derived from a calorie target using the default 30/40/30 split. */
    public static MacroTargets suggestedMacros(int calorieTarget) {
        int protein = (int) Math.round(calorieTarget * PROTEIN_RATIO / KCAL_PER_G_PROTEIN);
        int carbs = (int) Math.round(calorieTarget * CARBS_RATIO / KCAL_PER_G_CARBS);
        int fat = (int) Math.round(calorieTarget * FAT_RATIO / KCAL_PER_G_FAT);
        return new MacroTargets(protein, carbs, fat);
    }

    /** Grams of protein/carbs/fat for a daily plan. */
    public record MacroTargets(int proteinG, int carbsG, int fatG) {
    }
}
