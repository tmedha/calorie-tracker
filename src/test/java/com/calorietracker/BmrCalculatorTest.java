package com.calorietracker;

import com.calorietracker.model.ActivityLevel;
import com.calorietracker.model.Goal;
import com.calorietracker.model.Sex;
import com.calorietracker.service.BmrCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BmrCalculatorTest {

    @Test
    void maleBmrMatchesMifflinStJeor() {
        // 80 kg, 180 cm, 30 y male: 10*80 + 6.25*180 - 5*30 + 5 = 1780
        assertEquals(1780.0, BmrCalculator.bmr(Sex.MALE, 80, 180, 30), 0.001);
    }

    @Test
    void femaleBmrMatchesMifflinStJeor() {
        // 65 kg, 165 cm, 30 y female: 10*65 + 6.25*165 - 5*30 - 161 = 1370.25
        assertEquals(1370.25, BmrCalculator.bmr(Sex.FEMALE, 65, 165, 30), 0.001);
    }

    @Test
    void tdeeAppliesActivityMultiplier() {
        double bmr = 1780.0;
        assertEquals(bmr * 1.55, BmrCalculator.tdee(bmr, ActivityLevel.MODERATE), 0.001);
    }

    @Test
    void calorieTargetAdjustsForGoalAndRounds() {
        // TDEE 2759 - 500 (lose) = 2259 -> rounded to nearest 10 = 2260
        assertEquals(2260, BmrCalculator.suggestedCalorieTarget(2759, Goal.LOSE));
        assertEquals(2760, BmrCalculator.suggestedCalorieTarget(2759, Goal.MAINTAIN));
    }

    @Test
    void macroSplitFollows304030() {
        // 2000 kcal: 30% protein /4 = 150 g, 40% carbs /4 = 200 g, 30% fat /9 = 67 g
        BmrCalculator.MacroTargets m = BmrCalculator.suggestedMacros(2000);
        assertEquals(150, m.proteinG());
        assertEquals(200, m.carbsG());
        assertEquals(67, m.fatG());
    }
}
