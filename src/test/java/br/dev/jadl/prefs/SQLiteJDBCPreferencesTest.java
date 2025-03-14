package br.dev.jadl.prefs;

import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;

public class SQLiteJDBCPreferencesTest extends JDBCPreferencesTest {

    @TempDir
    private Path path;

    @Override
    protected void config() {
        final String prefix = JDBCPreferences.class.getCanonicalName();
        final String url = String.format("jdbc:sqlite:%s", path.resolve("database"));
        System.setProperty(String.format("%s.url", prefix), url);
    }
}
