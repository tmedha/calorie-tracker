package com.calorietracker;

/**
 * Entry point for the shaded jar and jpackage installer.
 * Must not extend javafx.application.Application: when a fat jar's
 * Main-Class does, the JavaFX launcher's module-path check misfires
 * outside a modular runtime image and fails with "JavaFX runtime
 * components are missing", even though the classes are on the classpath.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
