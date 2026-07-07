package com.calorietracker.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the single SQLite connection for the app's lifetime, initializes the schema,
 * and loads the starter food database on first run.
 */
public class Database implements AutoCloseable {

    private final Path dbPath;
    private Connection connection;

    /** Uses the default location: {@code ~/.calorietracker/data.db}. */
    public Database() {
        this(Path.of(System.getProperty("user.home"), ".calorietracker", "data.db"));
    }

    public Database(Path dbPath) {
        this.dbPath = dbPath;
    }

    /** Opens the connection, applies the schema, and seeds foods if the table is empty. */
    public void init() {
        try {
            if (dbPath.getParent() != null) {
                Files.createDirectories(dbPath.getParent());
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            runScript(readResource("/db/schema.sql"));
            if (isFoodsEmpty()) {
                runScript(readResource("/db/seed_foods.sql"));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to prepare database directory", e);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    public Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("Database not initialized; call init() first");
        }
        return connection;
    }

    private boolean isFoodsEmpty() throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM foods")) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    /** Executes a semicolon-separated SQL script statement by statement. */
    private void runScript(String script) throws SQLException {
        try (Statement st = connection.createStatement()) {
            for (String raw : script.split(";")) {
                String sql = stripComments(raw).trim();
                if (!sql.isEmpty()) {
                    st.execute(sql);
                }
            }
        }
    }

    private static String stripComments(String sql) {
        StringBuilder sb = new StringBuilder();
        for (String line : sql.split("\n")) {
            String trimmed = line.stripLeading();
            if (!trimmed.startsWith("--")) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private static String readResource(String path) {
        try (InputStream in = Database.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource: " + path);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read resource: " + path, e);
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // closing on shutdown; nothing actionable
            }
        }
    }
}
