package com.calorietracker;

import com.calorietracker.model.Food;
import com.calorietracker.service.MealSuggester;
import com.calorietracker.service.MealSuggester.MealSuggestion;
import com.calorietracker.service.MealSuggester.SuggestionItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MealSuggesterTest {

    private Food food(String name, double cal, double p, double c, double f) {
        return new Food(0, name, 1, "serving", cal, p, c, f, false);
    }

    private int totalServings(MealSuggestion s) {
        return s.items().stream().mapToInt(SuggestionItem::servings).sum();
    }

    @Test
    void hitsCalorieTargetExactlyWhenPossible() {
        List<Food> foods = List.of(food("A", 100, 10, 0, 0), food("B", 200, 20, 0, 0));
        MealSuggestion s = MealSuggester.suggest(foods, 300, 30, 0, 0, 4);

        assertTrue(s.feasible());
        assertEquals(300, s.totals().calories(), 1e-9);
        assertEquals(30, s.totals().proteinG(), 1e-9);
    }

    @Test
    void neverExceedsTheCalorieBudget() {
        List<Food> foods = List.of(food("A", 100, 10, 0, 0), food("B", 175, 12, 5, 3));
        MealSuggestion s = MealSuggester.suggest(foods, 500, 40, 10, 5, 4);

        assertTrue(s.feasible());
        assertTrue(s.totals().calories() <= 500, "solution must stay within the calorie budget");
    }

    @Test
    void respectsMaxServingsPerFood() {
        List<Food> foods = List.of(food("A", 100, 10, 0, 0));
        MealSuggestion s = MealSuggester.suggest(foods, 1000, 200, 0, 0, 3);

        assertTrue(s.feasible());
        assertEquals(1, s.items().size());
        assertEquals(3, s.items().get(0).servings(), "must not exceed the per-food serving cap");
        assertEquals(300, s.totals().calories(), 1e-9);
    }

    @Test
    void steersTowardTheDesiredMacro() {
        // Same calories, but one food is pure protein and the other pure carbs.
        List<Food> foods = List.of(
                food("Protein", 100, 25, 0, 0),
                food("Carbs", 100, 0, 25, 0));
        MealSuggestion s = MealSuggester.suggest(foods, 400, 100, 0, 0, 4);

        assertTrue(s.feasible());
        assertEquals(1, s.items().size());
        assertEquals("Protein", s.items().get(0).food().getName());
        assertEquals(100, s.totals().proteinG(), 1e-9);
    }

    @Test
    void infeasibleWhenNoFoodsOrNoBudget() {
        assertFalse(MealSuggester.suggest(List.of(), 500, 30, 30, 30, 4).feasible());
        assertFalse(MealSuggester.suggest(
                List.of(food("A", 100, 10, 0, 0)), 0, 30, 0, 0, 4).feasible());
    }

    @Test
    void skipsFoodsThatCannotFitInTheBudget() {
        // Only the small food can fit within 150 kcal.
        List<Food> foods = List.of(food("Big", 500, 40, 0, 0), food("Small", 50, 5, 0, 0));
        MealSuggestion s = MealSuggester.suggest(foods, 150, 15, 0, 0, 4);

        assertTrue(s.feasible());
        assertTrue(s.items().stream().allMatch(i -> i.food().getName().equals("Small")));
        assertTrue(s.totals().calories() <= 150);
        assertTrue(totalServings(s) >= 1);
    }
}
