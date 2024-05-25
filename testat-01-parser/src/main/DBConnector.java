/**
 * The DBConnector class manages database connections using environment settings for configuration. It attempts to
 * establish a connection to the database and logs any connection errors using the DBLogger.
 * <p>
 * Fields:
 * - DATABASE_URL: URL of the database, loaded from environment variables.
 * - DATABASE_USER: Username for the database, loaded from environment variables.
 * - DATABASE_PW: Password for the database, loaded from environment variables.
 * <p>
 * Methods:
 * - connect(): Attempts to establish and return a connection to the database using the configured URL, username,
 *   and password. Logs any SQLException encountered and returns null if the connection fails.
 */

package main;

import main.logger.DBLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {
    static final String DATABASE_URL = Helpers.loadEnv("DATABASE_URL");
    static final String DATABASE_USER = Helpers.loadEnv("DATABASE_USER");
    static final String DATABASE_PW = Helpers.loadEnv("DATABASE_PW");

    public static Connection connect() {

        try {
            return DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PW);
        } catch (SQLException e) {
            DBLogger.LOG.severe(e.getMessage());
            return null;
        }
    }
}
