package com.iqbal.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class AppConfig {

    private static final String DEFAULT_ENV = "development";

    private static AppConfig instance;

    private final String env;
    private final Properties properties;

    private AppConfig() {
        this.env = resolveEnv();
        this.properties = loadLayeredProperties(env);
    }

    public static synchronized AppConfig get() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private static String resolveEnv() {
        String env = System.getenv("APP_ENV");
        return (env == null || env.isBlank()) ? DEFAULT_ENV : env;
    }

    private static Properties loadLayeredProperties(String env) {
        Properties merged = new Properties();
        loadResourceInto(merged, "application.properties");
        loadResourceInto(merged, "application-" + env + ".properties");
        Map<String, String> dotEnv = loadDotEnv();
        applyEnvOverride(merged, dotEnv, "db.url", "DB_URL");
        applyEnvOverride(merged, dotEnv, "db.username", "DB_USERNAME");
        applyEnvOverride(merged, dotEnv, "db.password", "DB_PASSWORD");
        applyEnvOverride(merged, dotEnv, "gemini.apiKey", "GEMINI_API_KEY");
        return merged;
    }

    private static void loadResourceInto(Properties target, String resourceName) {
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                return;
            }
            target.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Gagal memuat konfigurasi: " + resourceName, e);
        }
    }

    /**
     * OS environment variable menang kalau ada; ".env" di root project cuma fallback
     * untuk kemudahan development lokal supaya tidak perlu export manual tiap sesi terminal.
     */
    private static void applyEnvOverride(Properties target, Map<String, String> dotEnv, String key, String envVarName) {
        String value = System.getenv(envVarName);
        if (value == null || value.isBlank()) {
            value = dotEnv.get(envVarName);
        }
        if (value != null && !value.isBlank()) {
            target.setProperty(key, value);
        }
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> result = new HashMap<>();
        Path path = Path.of(".env");
        if (!Files.isRegularFile(path)) {
            return result;
        }
        try {
            for (String rawLine : Files.readAllLines(path)) {
                String line = rawLine.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("export ")) {
                    line = line.substring("export ".length()).strip();
                }
                int separator = line.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = line.substring(0, separator).strip();
                String value = unquote(line.substring(separator + 1).strip());
                result.put(key, value);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membaca file .env", e);
        }
        return result;
    }

    private static String unquote(String value) {
        boolean wrapped = value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")));
        return wrapped ? value.substring(1, value.length() - 1) : value;
    }

    public String getEnv() {
        return env;
    }

    public String getAppName() {
        return properties.getProperty("app.name", "Aplikasi");
    }

    public String getDbUrl() {
        return require("db.url");
    }

    public String getDbUsername() {
        return require("db.username");
    }

    public String getDbPassword() {
        return properties.getProperty("db.password", "");
    }

    /**
     * Boleh null/kosong kalau belum diset — AI Assistant adalah fitur opsional,
     * jangan sampai aplikasi gagal start total hanya karena key ini belum ada.
     */
    public String getGeminiApiKey() {
        return properties.getProperty("gemini.apiKey");
    }

    public String getGeminiModel() {
        return properties.getProperty("gemini.model", "gemini-2.5-flash");
    }

    public int getGeminiContextWindowSize() {
        return Integer.parseInt(properties.getProperty("gemini.contextWindowSize", "20"));
    }

    private String require(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Konfigurasi wajib '" + key + "' tidak ditemukan untuk environment '" + env + "'");
        }
        return value;
    }
}
