package br.dev.jadl.prefs;

import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;

public class DerbyJDBCPreferencesTest extends JDBCPreferencesTest {

    @TempDir
    private Path path;

    @Override
    protected void config() {
        System.setProperty("derby.system.home", path.toAbsolutePath().toString());

        final String prefix = JDBCPreferences.class.getCanonicalName();
        final String url = String.format("jdbc:derby:%s;create=true", path.resolve("database"));
        System.setProperty(String.format("%s.url", prefix), url);
    }
}
