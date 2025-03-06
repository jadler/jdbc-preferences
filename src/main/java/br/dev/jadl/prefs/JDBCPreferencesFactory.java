package br.dev.jadl.prefs;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/**
 * A JDBC-based preferences factory that implements the {@link PreferencesFactory} interface.
 * <p>
 * This class allows JDBCPreferences to be installed as the Preferences implementations via the
 * java.util.prefs.PreferencesFactory system property.
 * <p>
 * Usage:
 * <pre>
 * java -Djava.util.prefs.PreferencesFactory=br.dev.jadl.prefs.JDBCPreferencesFactory \
 *      -Dbr.dev.jadl.prefs.JDBCPreferences.user.url=jdbc:sqlite:./userprefs.sqlite \
 *      -Dbr.dev.jadl.prefs.JDBCPreferences.system.url=jdbc:sqlite:./sysprefs.sqlite \
 *      -Dbr.dev.jadl.prefs.JDBCPreferences.table=preferences:w
 * </pre>
 * 
 * @see PreferencesFactory
 * @see JDBCPreferences
 */
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
