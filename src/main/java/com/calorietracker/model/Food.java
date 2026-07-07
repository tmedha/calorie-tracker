package com.calorietracker.model;

import com.calorietracker.util.NumberFmt;

/**
 * A food item in the database. All macro/calorie values are stated <em>per serving</em>,
 * where one serving is {@code servingSize} of {@code servingUnit} (e.g. 100 g, 1 piece).
 */
public class Food {
    private int id;
    private String name;
    private double servingSize;
    private String servingUnit;
    private double calories;
    private double proteinG;
    private double carbsG;
    private double fatG;
    private boolean seed;

    public Food() {
    }

    public Food(int id, String name, double servingSize, String servingUnit,
                double calories, double proteinG, double carbsG, double fatG, boolean seed) {
        this.id = id;
        this.name = name;
        this.servingSize = servingSize;
        this.servingUnit = servingUnit;
        this.calories = calories;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatG = fatG;
        this.seed = seed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getServingSize() {
        return servingSize;
    }

    public void setServingSize(double servingSize) {
        this.servingSize = servingSize;
    }

    public String getServingUnit() {
        return servingUnit;
    }

    public void setServingUnit(String servingUnit) {
        this.servingUnit = servingUnit;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProteinG() {
        return proteinG;
    }

    public void setProteinG(double proteinG) {
        this.proteinG = proteinG;
    }

    public double getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(double carbsG) {
        this.carbsG = carbsG;
    }

    public double getFatG() {
        return fatG;
    }

    public void setFatG(double fatG) {
        this.fatG = fatG;
    }

    public boolean isSeed() {
        return seed;
    }

    public void setSeed(boolean seed) {
        this.seed = seed;
    }

    /** Human-readable serving label, e.g. "100 g" or "1 piece". */
    public String servingLabel() {
        return NumberFmt.trim(servingSize) + " " + servingUnit;
    }

    @Override
    public String toString() {
        return name + " (" + servingLabel() + ")";
    }
}
