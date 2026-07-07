package com.calorietracker.service;

import com.calorietracker.model.Food;
import com.calorietracker.service.NutritionService.MacroTotals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a meal from the user's saved foods using a <b>bounded-knapsack dynamic program</b>.
 *
 * <p>The calorie budget is the knapsack capacity; each serving of a food is an item whose
 * "weight" is its calories and whose "value" leans toward the user's desired macro profile.
 * The DP fills the budget without exceeding it while maximizing macro alignment, reconstructs
 * the exact servings via backpointers, and breaks near-ties by macro closeness so it chases
 * protein / carbs / fat as well. Runs in {@code O(n · capacity · maxServings)} — effectively
 * instant for a personal food list.
 */
public final class MealSuggester {

    /** Accept solutions whose calories land within this fraction below the budget. */
    private static final double TOLERANCE = 0.10;
    /** Cells whose value is within this fraction of the best are "near-optimal" and macro-ranked. */
    private static final double NEAR_OPTIMAL = 0.02;
    /** Guards against absurd inputs blowing up the DP array. */
    private static final int MAX_CAPACITY = 20_000;
    private static final int MAX_SERVINGS = 20;

    private MealSuggester() {
    }

    /** One food and how many servings of it the suggester chose. */
    public record SuggestionItem(Food food, int servings) {

        public double calories() {
            return food.getCalories() * servings;
        }
    }

    /** The suggested meal: chosen items, their combined totals, and whether a fit was found. */
    public record MealSuggestion(List<SuggestionItem> items, MacroTotals totals, boolean feasible) {

        static MealSuggestion infeasible() {
            return new MealSuggestion(List.of(), MacroTotals.empty(), false);
        }
    }

    /**
     * @param foods            candidate foods (typically the whole saved database)
     * @param remainingCalories the calorie budget = knapsack capacity
     * @param proteinTarget    desired grams (0 = don't care)
     * @param carbsTarget      desired grams (0 = don't care)
     * @param fatTarget        desired grams (0 = don't care)
     * @param maxServingsPerFood upper bound on servings of any single food
     */
    public static MealSuggestion suggest(List<Food> foods, int remainingCalories,
                                         double proteinTarget, double carbsTarget, double fatTarget,
                                         int maxServingsPerFood) {
        if (foods == null || foods.isEmpty() || remainingCalories <= 0) {
            return MealSuggestion.infeasible();
        }
        int cap = Math.min(remainingCalories, MAX_CAPACITY);
        int maxServings = Math.max(1, Math.min(maxServingsPerFood, MAX_SERVINGS));

        // Macro-alignment weights, normalized so the value function tracks the wanted profile.
        double sum = proteinTarget + carbsTarget + fatTarget;
        double wP = sum > 0 ? proteinTarget / sum : 1.0 / 3;
        double wC = sum > 0 ? carbsTarget / sum : 1.0 / 3;
        double wF = sum > 0 ? fatTarget / sum : 1.0 / 3;

        // Exact-weight 0/1 knapsack over K serving-copies of each food.
        // best[c] = max macro value for a selection summing to exactly c calories.
        double[] best = new double[cap + 1];
        Arrays.fill(best, Double.NEGATIVE_INFINITY);
        best[0] = 0;
        int[] pickFood = new int[cap + 1];
        int[] prevCap = new int[cap + 1];
        Arrays.fill(pickFood, -1);
        Arrays.fill(prevCap, -1);

        for (int i = 0; i < foods.size(); i++) {
            Food food = foods.get(i);
            int weight = (int) Math.round(food.getCalories());
            if (weight <= 0 || weight > cap) {
                continue; // zero-calorie or too-big-to-fit foods are skipped
            }
            double value = wP * food.getProteinG() + wC * food.getCarbsG() + wF * food.getFatG();
            // Each of the up-to-K servings is an independent 0/1 item.
            for (int s = 0; s < maxServings; s++) {
                for (int c = cap; c >= weight; c--) {
                    if (best[c - weight] == Double.NEGATIVE_INFINITY) {
                        continue;
                    }
                    double candidate = best[c - weight] + value;
                    if (candidate > best[c] + 1e-12) {
                        best[c] = candidate;
                        pickFood[c] = i;
                        prevCap[c] = c - weight;
                    }
                }
            }
        }

        int chosenCap = selectCapacity(best, cap, foods, pickFood, prevCap,
                proteinTarget, carbsTarget, fatTarget);
        if (chosenCap <= 0) {
            return MealSuggestion.infeasible();
        }

        List<SuggestionItem> items = reconstruct(chosenCap, foods, pickFood, prevCap);
        if (items.isEmpty()) {
            return MealSuggestion.infeasible();
        }
        return new MealSuggestion(items, totalOf(items), true);
    }

    /**
     * Picks the calorie total to reconstruct: highest knapsack value within the tolerance
     * window, breaking near-ties by the smallest macro distance to the target.
     */
    private static int selectCapacity(double[] best, int cap, List<Food> foods,
                                      int[] pickFood, int[] prevCap,
                                      double pTarget, double cTarget, double fTarget) {
        int low = (int) Math.floor(cap * (1 - TOLERANCE));
        int chosen = scanWindow(best, low, cap, foods, pickFood, prevCap, pTarget, cTarget, fTarget);
        if (chosen <= 0) {
            // Nothing landed in the window — accept the best fit anywhere under budget.
            chosen = scanWindow(best, 1, cap, foods, pickFood, prevCap, pTarget, cTarget, fTarget);
        }
        return chosen;
    }

    private static int scanWindow(double[] best, int low, int high, List<Food> foods,
                                  int[] pickFood, int[] prevCap,
                                  double pTarget, double cTarget, double fTarget) {
        double maxValue = Double.NEGATIVE_INFINITY;
        for (int c = Math.max(1, low); c <= high; c++) {
            if (best[c] > maxValue) {
                maxValue = best[c];
            }
        }
        if (maxValue == Double.NEGATIVE_INFINITY) {
            return -1;
        }
        double threshold = maxValue > 0 ? maxValue * (1 - NEAR_OPTIMAL) : maxValue - 1e-9;

        int chosen = -1;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int c = Math.max(1, low); c <= high; c++) {
            if (best[c] == Double.NEGATIVE_INFINITY || best[c] < threshold) {
                continue;
            }
            MacroTotals totals = totalOf(reconstruct(c, foods, pickFood, prevCap));
            double distance = macroDistance(totals, pTarget, cTarget, fTarget);
            if (distance < bestDistance - 1e-9) {
                bestDistance = distance;
                chosen = c;
            }
        }
        return chosen;
    }

    /** Normalized squared error across calories and each macro the user actually targeted. */
    private static double macroDistance(MacroTotals totals, double pTarget, double cTarget, double fTarget) {
        double d = 0;
        d += term(totals.proteinG(), pTarget);
        d += term(totals.carbsG(), cTarget);
        d += term(totals.fatG(), fTarget);
        return d;
    }

    private static double term(double got, double target) {
        if (target <= 0) {
            return 0;
        }
        double e = (got - target) / target;
        return e * e;
    }

    private static List<SuggestionItem> reconstruct(int capacity, List<Food> foods,
                                                    int[] pickFood, int[] prevCap) {
        Map<Integer, Integer> servings = new LinkedHashMap<>();
        int c = capacity;
        while (c > 0 && pickFood[c] != -1) {
            servings.merge(pickFood[c], 1, Integer::sum);
            c = prevCap[c];
        }
        List<SuggestionItem> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : servings.entrySet()) {
            items.add(new SuggestionItem(foods.get(e.getKey()), e.getValue()));
        }
        items.sort(Comparator.comparingDouble(SuggestionItem::calories).reversed());
        return items;
    }

    private static MacroTotals totalOf(List<SuggestionItem> items) {
        double cal = 0, p = 0, cb = 0, f = 0;
        for (SuggestionItem item : items) {
            cal += item.food().getCalories() * item.servings();
            p += item.food().getProteinG() * item.servings();
            cb += item.food().getCarbsG() * item.servings();
            f += item.food().getFatG() * item.servings();
        }
        return new MacroTotals(cal, p, cb, f);
    }
}
