package com.janumart.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.janumart.exception.AppException;

/**
 * Application-wide HikariCP connection pool. Initialized once by the
 * {@code AppContextListener} and closed on shutdown. Holds the schema/seed
 * bootstrap logic that runs on first startup.
 * Never create a new pool per request.
 */
public final class DBUtil {

    private static final Logger log = LoggerFactory.getLogger(DBUtil.class);

    private static HikariDataSource dataSource;

    private DBUtil() {
    }

    public static synchronized void init(Properties props) {
        if (dataSource != null) {
            return;
        }
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(props.getProperty("db.driver", "org.h2.Driver"));
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.user", "sa"));
        config.setPassword(props.getProperty("db.password", ""));
        config.setMaximumPoolSize(intValue(props.getProperty("pool.maximumPoolSize"), 10));
        config.setMinimumIdle(intValue(props.getProperty("pool.minimumIdle"), 2));
        config.setConnectionTimeout(longValue(props.getProperty("pool.connectionTimeout"), 30_000));
        config.setIdleTimeout(longValue(props.getProperty("pool.idleTimeout"), 600_000));
        config.setMaxLifetime(longValue(props.getProperty("pool.maxLifetime"), 1_800_000));
        config.setPoolName("JanuMartPool");
        dataSource = new HikariDataSource(config);
        log.info("HikariCP pool initialised ({}).", config.getJdbcUrl());
    }

    public static Connection getConnection() {
        if (dataSource == null) {
            throw new AppException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database is not initialized");
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new AppException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not open a database connection");
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static synchronized void closePool() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
            log.info("HikariCP pool closed.");
        }
    }

    /**
     * Applies schema.sql (idempotent) and, when the database is brand new,
     * seed.sql with demo data.
     */
    public static void bootstrap(Logger log) {
        try (Connection c = getConnection()) {
            runScript(c, "/schema.sql");
            log.info("Database schema ready.");
            if (countUsers() == 0) {
                runScript(c, "/seed.sql");
                log.info("Seed data loaded (fresh database).");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not bootstrap the database", e);
        }
    }

    private static void runScript(Connection c, String resource) throws SQLException {
        try (InputStream in = DBUtil.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + resource);
            }
            RunScript.execute(c, new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Failed to run script " + resource, e);
        }
    }

    private static int countUsers() throws SQLException {
        try (Connection c = getConnection(); Statement st = c.createStatement();
             var rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static int intValue(String v, int def) {
        try {
            return v == null ? def : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static long longValue(String v, long def) {
        try {
            return v == null ? def : Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}