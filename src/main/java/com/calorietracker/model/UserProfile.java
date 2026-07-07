package com.calorietracker.model;

import java.time.LocalDate;
import java.time.Period;

/**
 * The single user profile (row id = 1). Body metrics are stored canonically in metric
 * (kg / cm); {@link UnitSystem} only affects how they are displayed and entered.
 */
public class UserProfile {
    private int id = 1;
    private String name;
    private Sex sex = Sex.MALE;
    private LocalDate birthDate;
    private double heightCm;
    private double weightKg;
    private ActivityLevel activityLevel = ActivityLevel.MODERATE;
    private Goal goal = Goal.MAINTAIN;
    private UnitSystem unitSystem = UnitSystem.METRIC;
    private int calorieTarget;
    private int proteinTargetG;
    private int carbsTargetG;
    private int fatTargetG;

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

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /** Age in whole years derived from birth date; 0 if birth date is unset. */
    public int getAge() {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(double heightCm) {
        this.heightCm = heightCm;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public UnitSystem getUnitSystem() {
        return unitSystem;
    }

    public void setUnitSystem(UnitSystem unitSystem) {
        this.unitSystem = unitSystem;
    }

    public int getCalorieTarget() {
        return calorieTarget;
    }

    public void setCalorieTarget(int calorieTarget) {
        this.calorieTarget = calorieTarget;
    }

    public int getProteinTargetG() {
        return proteinTargetG;
    }

    public void setProteinTargetG(int proteinTargetG) {
        this.proteinTargetG = proteinTargetG;
    }

    public int getCarbsTargetG() {
        return carbsTargetG;
    }

    public void setCarbsTargetG(int carbsTargetG) {
        this.carbsTargetG = carbsTargetG;
    }

    public int getFatTargetG() {
        return fatTargetG;
    }

    public void setFatTargetG(int fatTargetG) {
        this.fatTargetG = fatTargetG;
    }

    /** True once the essential BMR inputs are present. */
    public boolean isComplete() {
        return birthDate != null && heightCm > 0 && weightKg > 0;
    }
}
