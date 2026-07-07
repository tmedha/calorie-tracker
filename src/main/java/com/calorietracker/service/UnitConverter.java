package com.calorietracker.service;

/**
 * Conversions between the canonical metric storage units and imperial display units.
 * Body metrics are always persisted in kg / cm; these helpers convert only for the UI.
 */
public final class UnitConverter {

    private static final double LB_PER_KG = 2.2046226218;
    private static final double CM_PER_INCH = 2.54;
    private static final double G_PER_OZ = 28.349523125;
    private static final double CM_PER_FOOT = 30.48;

    private UnitConverter() {
    }

    public static double kgToLb(double kg) {
        return kg * LB_PER_KG;
    }

    public static double lbToKg(double lb) {
        return lb / LB_PER_KG;
    }

    public static double cmToInches(double cm) {
        return cm / CM_PER_INCH;
    }

    public static double inchesToCm(double inches) {
        return inches * CM_PER_INCH;
    }

    public static double gToOz(double grams) {
        return grams / G_PER_OZ;
    }

    public static double ozToG(double oz) {
        return oz * G_PER_OZ;
    }

    /** Splits a height in cm into whole feet and the remaining inches. */
    public static int feetPart(double cm) {
        return (int) (cm / CM_PER_FOOT);
    }

    /** Remaining inches after {@link #feetPart}, e.g. 180 cm -> 5 ft and ~10.87 in. */
    public static double inchesPart(double cm) {
        double totalInches = cmToInches(cm);
        return totalInches - feetPart(cm) * 12.0;
    }

    /** Builds a height in cm from feet and inches. */
    public static double feetInchesToCm(int feet, double inches) {
        return inchesToCm(feet * 12.0 + inches);
    }
}
