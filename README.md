# Calorie Tracker

A desktop app for tracking calories and macros, built with **JavaFX 25** and **SQLite**.

## Features

- **BMR & calorie targets** — Mifflin–St Jeor BMR, activity-based TDEE, and a goal-adjusted daily
  calorie target with a suggested 30/40/30 macro split (all editable).
- **Daily food logging by meal** — Breakfast / Lunch / Dinner / Snacks, with a date navigator to
  review any day.
- **Macro tracking** — a summary card showing calories eaten vs. target (remaining/over) and
  protein / carbs / fat progress toward your targets.
- **Custom food database** — full create/edit/delete, searchable, pre-seeded with ~45 common foods.
- **Metric or imperial** — toggle in Settings; body metrics are stored canonically in metric and
  converted for display.

Data is stored locally in `~/.calorietracker/data.db`.

## Requirements

- Java 21+ (developed and tested on Temurin 25)
- Maven (`brew install maven`)

## Run

```bash
mvn javafx:run
```

## Test

```bash
mvn test
```

## Project structure

```
src/main/java/com/calorietracker/
  App.java                  Sidebar shell + view navigation
  model/                    Domain types (Food, FoodLogEntry, UserProfile, enums)
  db/                       Database (schema init + seeding) and JDBC DAOs
  service/                  BmrCalculator, UnitConverter, NutritionService
  ui/                       AppContext, dialogs, and FXML controllers
src/main/resources/
  fxml/                     Dashboard, Food Database, Profile, Settings views
  css/app.css               Design system (colors, cards, sidebar, macro bars)
  db/                       schema.sql, seed_foods.sql
```
