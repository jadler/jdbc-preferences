package br.dev.jadl.prefs;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

public class PostgresJDBCPreferencesTest extends JDBCPreferencesTest {

    private static EmbeddedPostgres postgres;
    private static String url;

    @BeforeAll
    public static void init() throws IOException {

        postgres = Assertions.assertDoesNotThrow(() -> EmbeddedPostgres.start());
        url = postgres.getJdbcUrl("postgres", "postgres");

    }

    @AfterAll
    public static void teardown() throws MalformedURLException {
        Assertions.assertDoesNotThrow(() -> postgres.close());
    }

    @Override
    protected void config() {
        final String prefix = JDBCPreferences.class.getCanonicalName();

        System.setProperty(String.format("%s.url", prefix), url);
        System.setProperty(String.format("%s.table", prefix), "preferences");
    }
}
