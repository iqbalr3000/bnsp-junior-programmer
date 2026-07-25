package com.iqbal.config;

import java.io.IOException;
import java.io.InputStream;
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
        applyEnvOverride(merged, "db.url", "DB_URL");
        applyEnvOverride(merged, "db.username", "DB_USERNAME");
        applyEnvOverride(merged, "db.password", "DB_PASSWORD");
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

    private static void applyEnvOverride(Properties target, String key, String envVarName) {
        String value = System.getenv(envVarName);
        if (value != null && !value.isBlank()) {
            target.setProperty(key, value);
        }
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

    private String require(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Konfigurasi wajib '" + key + "' tidak ditemukan untuk environment '" + env + "'");
        }
        return value;
    }
}
