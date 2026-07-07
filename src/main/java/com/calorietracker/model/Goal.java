package com.calorietracker.model;

/** Weight goal and the calorie adjustment applied to maintenance (TDEE). */
public enum Goal {
    LOSE("Lose weight", -500),
    MAINTAIN("Maintain weight", 0),
    GAIN("Gain weight", 300);

    private final String label;
    private final int calorieAdjustment;

    Goal(String label, int calorieAdjustment) {
        this.label = label;
        this.calorieAdjustment = calorieAdjustment;
    }

    public String getLabel() {
        return label;
    }

    public int getCalorieAdjustment() {
        return calorieAdjustment;
    }

    @Override
    public String toString() {
        return label;
    }
}
