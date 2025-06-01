package br.dev.jadl.prefs;

import static java.util.logging.Level.OFF;
import static java.util.logging.Logger.GLOBAL_LOGGER_NAME;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MSSQLServerContainer;

public class MSSQLJDBCPreferencesTest extends PreferencesTest {
    
    private static final String prefix = JDBCPreferences.class.getCanonicalName();
    private static final String password = "Required-Strong-P4ssw0rd";

    @SuppressWarnings("resource")
    private static final MSSQLServerContainer<?> mssql = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:latest")
        .acceptLicense()
        .withUrlParam("username", "sa")
        .withUrlParam("password", password)
        .withPassword(password);

    private static String url;

    @BeforeAll
    public static void init() {
        // Required to suppress log messages as mssqlserver uses JUL;
        LogManager.getLogManager().reset();
        Logger.getLogger(GLOBAL_LOGGER_NAME).setLevel(OFF);

        Assertions.assertDoesNotThrow(() -> mssql.start());
        
        url = mssql.getJdbcUrl();
        System.setProperty(String.format("%s.url", prefix), url);
    }

    @BeforeEach
    public void setup() {
        System.setProperty(String.format("%s.table", prefix), "preferences");
    }

    @AfterEach
    public void cleanup() {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement()) {
            
            statement.execute("TRUNCATE TABLE preferences");
        
        } catch (SQLException e) {
            // Safe to ignore: the table might not have been created during the test
        }
    }

    @AfterAll
    public static void teardown() {
        Assertions.assertDoesNotThrow(() -> mssql.close());
        mssql.close();
    }
}
