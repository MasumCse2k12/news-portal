package com.newsportal.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    /**
     * Repairs failed migration records before running pending migrations.
     * This clears any FAILED entries in flyway_schema_history so Flyway can
     * re-apply them. All migrations use IF NOT EXISTS / ON CONFLICT DO NOTHING
     * so re-running them is safe.
     */
    @Bean
    public FlywayMigrationStrategy repairThenMigrate() {
        return flyway -> {
            flyway.repair();   // clears FAILED records so pending migrations can run
            flyway.migrate();  // apply all pending migrations
        };
    }
}
