# NutriPlan

NutriPlan is a fully offline **native Android meal planner** built with Kotlin and Jetpack Compose.
It lets you manage recipes, build a weekly meal plan, generate a categorized shopping list,
track nutrition, and back up your data — all without an internet connection.

> The repository also contains an earlier web prototype (`index.html`, `github-lepesek.html`).
> The production application is the native Android project described below.

## Tech stack

| Area | Choice |
|------|--------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean-ish layering (data / domain / presentation) |
| Database | Room |
| Dependency Injection | Hilt |
| Async | Kotlin Coroutines + Flow |
| Serialization | kotlinx.serialization (JSON export/import) |
| Logging | Timber (centralized `Logger` helper) |
| Min SDK | Android 10 (API 29) |
| Target / Compile SDK | 35 |

## Features

- **Recipe management** — full CRUD with name, meal type, calories, macros and a list of
  ingredients (name, quantity, unit, auto-detected category). Live search.
- **Weekly planner** — Monday–Sunday, each day split into Breakfast, Morning Snack, Lunch,
  Afternoon Snack and Dinner. Assign any number of recipes per slot.
- **Shopping list** — generated automatically from the weekly plan. Identical ingredients are
  merged (e.g. `3 × 180 g Chicken breast → 540 g`), grouped into 9 categories. Mark purchased,
  delete items, clear purchased, clear all.
- **Nutrition** — calories / protein / carbs / fat totals per meal, per day and per week.
- **Export / Import** — JSON backup file via the Storage Access Framework.
- **Languages** — Hungarian, English, Romanian (switchable in Settings, applied offline).
- **Dark mode** — Light / Dark / System.
- **50 default recipes** seeded automatically on first launch.

## Logging

Every important action is logged via Timber through the centralized `com.nutriplan.app.util.Logger`
helper, using consistent tags (`NutriPlan/App`, `NutriPlan/Database`, `NutriPlan/Recipe`,
`NutriPlan/Planner`, `NutriPlan/Shopping`, `NutriPlan/Nutrition`, `NutriPlan/Backup`, …).
Startup, database init, inserts, meal assignments, shopping list generation, calculations and
export/import all produce detailed Logcat output.

## Project structure

```
app/src/main/java/com/nutriplan/app/
├── NutriPlanApplication.kt        # Hilt app, Timber init, first-launch seeding
├── MainActivity.kt                # Single activity, theme + locale handling
├── util/                          # Logger, LocaleHelper
├── data/
│   ├── local/                     # Room entities, DAOs, relations, database
│   ├── mapper/                    # Entity ↔ domain mapping
│   ├── repository/                # Repository implementations
│   ├── preferences/               # SettingsManager (theme + language)
│   ├── backup/                    # JSON DTOs
│   └── seed/                      # Default recipes + seeder
├── domain/
│   ├── model/                     # Domain models + enums
│   ├── repository/                # Repository interfaces
│   ├── usecase/                   # Use cases (recipe, planner, shopping, nutrition, backup)
│   └── util/                      # Ingredient categorizer
├── di/                            # Hilt modules
└── presentation/
    ├── theme/                     # Compose Material 3 theme
    ├── navigation/                # Routes + bottom navigation
    ├── components/                # Reusable composables
    ├── recipe/ planner/ shopping/ nutrition/ settings/   # Screens + ViewModels
    └── util/                      # UI label mapping, dialogs
```

## Building

Open the project in **Android Studio (Ladybug or newer)** and run the `app` configuration,
or from the command line:

```bash
./gradlew assembleDebug
```

> Building requires access to Google's Maven repository (`dl.google.com`) and the Android SDK
> to download the Android Gradle Plugin and AndroidX dependencies.
