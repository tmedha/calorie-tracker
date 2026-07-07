package com.calorietracker.model;

import java.time.LocalDate;

/**
 * A single logged food for a given day and meal. Carries a resolved {@link Food}
 * so the UI can display and total it without a second lookup.
 */
public class FoodLogEntry {
    private int id;
    private LocalDate logDate;
    private MealType mealType;
    private double servings;
    private Food food;

    public FoodLogEntry() {
    }

    public FoodLogEntry(int id, LocalDate logDate, MealType mealType, double servings, Food food) {
        this.id = id;
        this.logDate = logDate;
        this.mealType = mealType;
        this.servings = servings;
        this.food = food;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public double getServings() {
        return servings;
    }

    public void setServings(double servings) {
        this.servings = servings;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public double getCalories() {
        return food.getCalories() * servings;
    }

    public double getProteinG() {
        return food.getProteinG() * servings;
    }

    public double getCarbsG() {
        return food.getCarbsG() * servings;
    }

    public double getFatG() {
        return food.getFatG() * servings;
    }
}
