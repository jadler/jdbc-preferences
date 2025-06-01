package br.dev.jadl.prefs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

public class PostgresJDBCPreferencesTest extends PreferencesTest {

    private static EmbeddedPostgres postgres;
    private static String url;

    @BeforeAll
    public static void init() throws IOException {
        postgres = Assertions.assertDoesNotThrow(() -> EmbeddedPostgres.start());
        url = postgres.getJdbcUrl("postgres", "postgres");
    }

    @BeforeEach
    public void setup() {
        final String prefix = JDBCPreferences.class.getCanonicalName();

        System.setProperty(String.format("%s.url", prefix), url);
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
    public static void teardown() throws MalformedURLException {
        Assertions.assertDoesNotThrow(() -> postgres.close());
    }
}
