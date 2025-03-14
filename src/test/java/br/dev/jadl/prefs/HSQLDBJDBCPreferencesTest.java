package br.dev.jadl.prefs;

import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;

public class HSQLDBJDBCPreferencesTest extends JDBCPreferencesTest {

    @TempDir
    private Path path;

    @Override
    protected void config() {
        final String prefix = JDBCPreferences.class.getCanonicalName();
        final String url = String.format("jdbc:hsqldb:%s;shutdown=true", path.resolve("database"));
        System.setProperty(String.format("%s.url", prefix), url);
    }
}
