package com.calorietracker;

import com.calorietracker.service.UnitConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UnitConverterTest {

    @Test
    void kgLbRoundTrip() {
        assertEquals(220.462, UnitConverter.kgToLb(100), 0.001);
        assertEquals(100.0, UnitConverter.lbToKg(UnitConverter.kgToLb(100)), 1e-9);
    }

    @Test
    void cmInchesConversion() {
        assertEquals(1.0, UnitConverter.cmToInches(2.54), 1e-9);
        assertEquals(2.54, UnitConverter.inchesToCm(1.0), 1e-9);
    }

    @Test
    void gramsOuncesConversion() {
        assertEquals(1.0, UnitConverter.gToOz(28.349523125), 1e-9);
        assertEquals(28.349523125, UnitConverter.ozToG(1.0), 1e-9);
    }

    @Test
    void heightSplitsIntoFeetAndInches() {
        // 180 cm = 70.866 in = 5 ft 10.866 in
        assertEquals(5, UnitConverter.feetPart(180));
        assertEquals(10.866, UnitConverter.inchesPart(180), 0.001);
    }

    @Test
    void feetInchesRebuildCm() {
        assertEquals(180.0, UnitConverter.feetInchesToCm(5, UnitConverter.inchesPart(180)), 1e-9);
    }
}
