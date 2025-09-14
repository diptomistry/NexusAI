package com.example.ai_assistant_backend.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Custom database configuration to handle prepared statement conflicts
 * and optimize connection management for Supabase PostgreSQL
 */
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        // Connection pool settings optimized for prepared statement conflicts
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(900000);
        config.setLeakDetectionThreshold(60000);

        // Prepared statement settings to avoid conflicts
        config.addDataSourceProperty("prepareThreshold", "0");
        config.addDataSourceProperty("cachePrepStmts", "false");
        config.addDataSourceProperty("prepStmtCacheSize", "0");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "0");

        // Connection validation and management
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        config.setRegisterMbeans(true);

        // Transaction settings
        config.setAutoCommit(false);
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");

        // Additional PostgreSQL-specific settings
        config.addDataSourceProperty("socketTimeout", "30");
        config.addDataSourceProperty("loginTimeout", "10");
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("ApplicationName", "NexusAI-Backend");

        return new HikariDataSource(config);
    }
}
