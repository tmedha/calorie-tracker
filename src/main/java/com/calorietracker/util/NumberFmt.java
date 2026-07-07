package com.calorietracker.util;

/** Small helpers for displaying numbers without noisy trailing zeros. */
public final class NumberFmt {

    private NumberFmt() {
    }

    /** Formats a value dropping a trailing ".0" (e.g. 100.0 -> "100", 1.5 -> "1.5"). */
    public static String trim(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return Long.toString((long) value);
        }
        return stripZeros(String.format("%.2f", value));
    }

    /** Rounds to whole number for display (e.g. calories, macros in grams). */
    public static String whole(double value) {
        return Long.toString(Math.round(value));
    }

    private static String stripZeros(String s) {
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return s;
    }
}
