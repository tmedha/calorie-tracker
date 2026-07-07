CREATE TABLE IF NOT EXISTS profiles (
    id               INTEGER PRIMARY KEY,
    name             TEXT,
    sex              TEXT    NOT NULL DEFAULT 'MALE',
    birth_date       TEXT,
    height_cm        REAL    NOT NULL DEFAULT 0,
    weight_kg        REAL    NOT NULL DEFAULT 0,
    activity_level   TEXT    NOT NULL DEFAULT 'MODERATE',
    goal             TEXT    NOT NULL DEFAULT 'MAINTAIN',
    unit_system      TEXT    NOT NULL DEFAULT 'METRIC',
    calorie_target   INTEGER NOT NULL DEFAULT 0,
    protein_target_g INTEGER NOT NULL DEFAULT 0,
    carbs_target_g   INTEGER NOT NULL DEFAULT 0,
    fat_target_g     INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS foods (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    name         TEXT    NOT NULL,
    serving_size REAL    NOT NULL DEFAULT 100,
    serving_unit TEXT    NOT NULL DEFAULT 'g',
    calories     REAL    NOT NULL DEFAULT 0,
    protein_g    REAL    NOT NULL DEFAULT 0,
    carbs_g      REAL    NOT NULL DEFAULT 0,
    fat_g        REAL    NOT NULL DEFAULT 0,
    is_seed      INTEGER NOT NULL DEFAULT 0,
    created_at   TEXT    NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS log_entries (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    log_date   TEXT    NOT NULL,
    meal_type  TEXT    NOT NULL,
    food_id    INTEGER NOT NULL,
    servings   REAL    NOT NULL DEFAULT 1,
    created_at TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_log_entries_date ON log_entries(log_date);

INSERT OR IGNORE INTO profiles (id) VALUES (1);
