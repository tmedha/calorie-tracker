package com.calorietracker.model;

/** Preferred display units. Body metrics are always stored canonically in metric. */
public enum UnitSystem {
    METRIC("Metric (kg, cm)"),
    IMPERIAL("Imperial (lb, ft/in)");

    private final String label;

    UnitSystem(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
