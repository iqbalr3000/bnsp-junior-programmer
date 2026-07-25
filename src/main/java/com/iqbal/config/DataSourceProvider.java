package com.iqbal.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DataSourceProvider {

    private static HikariDataSource dataSource;

    private DataSourceProvider() {
    }

    public static synchronized HikariDataSource get() {
        if (dataSource == null) {
            AppConfig config = AppConfig.get();
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getDbUrl());
            hikariConfig.setUsername(config.getDbUsername());
            hikariConfig.setPassword(config.getDbPassword());
            hikariConfig.setPoolName("hospital-pool");
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);
            dataSource = new HikariDataSource(hikariConfig);
        }
        return dataSource;
    }

    public static synchronized void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }
}
