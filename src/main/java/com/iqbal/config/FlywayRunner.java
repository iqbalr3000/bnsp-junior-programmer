package com.iqbal.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

public final class FlywayRunner {

    private FlywayRunner() {
    }

    public static void migrate(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
    }
}
