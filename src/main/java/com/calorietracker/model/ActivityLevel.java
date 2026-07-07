package com.calorietracker.model;

/** Activity level with the TDEE multiplier applied to BMR. */
public enum ActivityLevel {
    SEDENTARY("Sedentary (little or no exercise)", 1.2),
    LIGHT("Lightly active (1-3 days/week)", 1.375),
    MODERATE("Moderately active (3-5 days/week)", 1.55),
    ACTIVE("Very active (6-7 days/week)", 1.725),
    VERY_ACTIVE("Extra active (hard exercise, physical job)", 1.9);

    private final String label;
    private final double multiplier;

    ActivityLevel(String label, double multiplier) {
        this.label = label;
        this.multiplier = multiplier;
    }

    public String getLabel() {
        return label;
    }

    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public String toString() {
        return label;
    }
}
