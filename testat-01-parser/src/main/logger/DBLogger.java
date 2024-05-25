/**
 * The DBLogger class handles logging for database-related activities. It provides methods to log rejected records
 * and manage log outputs based on environment settings. Logs can be directed to the console and a specified log file.
 * <p>
 * Fields:
 * - LOG: Logger instance for logging messages.
 * - rejectedRecords: List to store rejected records.
 * - SILENT_MODE: Boolean flag for silent mode operation, set from environment variables.
 * - LOG_OUTPUT_FILE: Path to the log output file, set from environment variables.
 * <p>
 * Methods:
 * - logRejectedRecord(String entity, String attribute, String errorMessage): Logs a rejected record with details
 *   about the entity, attribute, and error message, and adds it to the rejectedRecords list.
 * - writeRejectedRecords(): Writes all rejected records to the specified log file.
 */

package main.logger;

import main.Helpers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.*;

public class DBLogger {
    public static final Logger LOG = Logger.getLogger(DBLogger.class.getName());
    private static final List<String> rejectedRecords = new ArrayList<>();

    private static final boolean SILENT_MODE = Boolean.parseBoolean(Helpers.loadEnv("SILENT_MODE"));
    private static final String LOG_OUTPUT_FILE = Helpers.loadEnv("LOG_OUTPUT_FILE");

    static {
        LogManager.getLogManager().reset();
        LOG.setLevel(Level.INFO);

        Formatter simpleFormatter = new Formatter() {
            @Override
            public synchronized String format(LogRecord record) {
                if (!SILENT_MODE) {
                    return String.format("[%1$tF %1$tT] [%2$-4s] [%3$s] %4$s%n",
                            new Date(record.getMillis()),
                            record.getLevel(),
                            record.getLoggerName(),
                            record.getMessage());
                } else {
                    return "";
                }
            }
        };

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(simpleFormatter);

        LOG.addHandler(consoleHandler);
    }

    public static void logRejectedRecord(String entity, String attribute, String errorMessage) {
        var message = String.format("%s, %s, %s", entity, attribute, errorMessage);
        LOG.severe(message);
        rejectedRecords.add(message);
    }

    public static void writeRejectedRecords() {
        try (FileWriter writer = new FileWriter(LOG_OUTPUT_FILE)) {
            rejectedRecords.add(0, "Entity, Attribute, Error Message");
            for (String record : rejectedRecords) {
                writer.write(record + System.lineSeparator());
            }
            LOG.info("Rejected records logged to " + LOG_OUTPUT_FILE);
        } catch (IOException e) {
            LOG.severe("Failed to log rejected records: " + e.getMessage());
        }
    }
}
