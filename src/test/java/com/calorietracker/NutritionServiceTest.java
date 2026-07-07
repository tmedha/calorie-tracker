package com.calorietracker;

import com.calorietracker.model.Food;
import com.calorietracker.model.FoodLogEntry;
import com.calorietracker.model.MealType;
import com.calorietracker.service.NutritionService;
import com.calorietracker.service.NutritionService.MacroTotals;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NutritionServiceTest {

    private FoodLogEntry entry(double calories, double p, double c, double f, double servings) {
        Food food = new Food(1, "Test", 100, "g", calories, p, c, f, false);
        return new FoodLogEntry(1, LocalDate.now(), MealType.LUNCH, servings, food);
    }

    @Test
    void totalsScaleByServingsAndSum() {
        MacroTotals t = NutritionService.total(List.of(
                entry(100, 10, 5, 2, 2),   // -> 200 kcal, 20 p, 10 c, 4 f
                entry(150, 5, 30, 1, 1)    // -> 150 kcal, 5 p, 30 c, 1 f
        ));
        assertEquals(350, t.calories(), 1e-9);
        assertEquals(25, t.proteinG(), 1e-9);
        assertEquals(40, t.carbsG(), 1e-9);
        assertEquals(5, t.fatG(), 1e-9);
    }

    @Test
    void emptyLogTotalsZero() {
        MacroTotals t = NutritionService.total(List.of());
        assertEquals(0, t.calories(), 1e-9);
    }

    @Test
    void progressClampsToOne() {
        MacroTotals t = new MacroTotals(2500, 0, 0, 0);
        assertEquals(1.0, t.calorieProgress(2000), 1e-9);
        assertEquals(0.5, new MacroTotals(1000, 0, 0, 0).calorieProgress(2000), 1e-9);
        assertEquals(0.0, t.calorieProgress(0), 1e-9);
    }
}
