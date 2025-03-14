package br.dev.jadl.prefs;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public class JDBCPreferencesFactory implements PreferencesFactory {

    @Override
    public Preferences systemRoot() {
        return preferences("system");
    }

    @Override
    public Preferences userRoot() {
        return preferences("user");
    }

    private static Preferences preferences(final String scope) {
        final String prefix = JDBCPreferences.class.getCanonicalName();

        final String url = property(prefix, scope, "url", null);
        final String table = property(prefix, scope, "table", "preferences");

        return new JDBCPreferences(url, table, scope.equals("user"));
    }

    private static String property(final String prefix, final String scope, final String key, final String def) {
        final String scoped = String.format("%s.%s.%s", prefix, scope, key);
        final String unscoped = String.format("%s.%s", prefix, key);
        return System.getProperty(scoped, System.getProperty(unscoped, def));
    }
}
