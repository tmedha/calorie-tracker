package com.calorietracker.model;

import java.time.LocalDate;

/** A body-weight measurement for a given day, stored canonically in kilograms. */
public record WeightEntry(LocalDate date, double weightKg) {
}
