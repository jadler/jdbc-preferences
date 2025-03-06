package br.dev.jadl.prefs;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

public class JDBCPreferences extends AbstractPreferences {

    private static final System.Logger logger = System.getLogger(JDBCPreferences.class.getCanonicalName());

    private final String url;
    private final String table;

    private final boolean userNode;

    JDBCPreferences(final String url, final String table, final boolean userNode) {
        this(null, "", url, table, userNode);

        try (Connection connection = DriverManager.getConnection(this.url)) {
            this.create(connection);
            this.insert(connection, "", null);
        } catch (final SQLException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }
    }

    private JDBCPreferences(final AbstractPreferences parent, final String name, final String url, final String table, final boolean userNode) {
        super(parent, name);
        this.url = url;
        this.table = table;
        this.userNode = userNode;
    }

    @Override
    public boolean isUserNode() {
        return this.userNode;
    }

    @Override
    protected void putSpi(final String key, final String value) {
        try (Connection connection = DriverManager.getConnection(this.url)) {
            this.insert(connection, key, value);
        } catch (final SQLException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String getSpi(final String key) {
        final String query = """
                SELECT "value" FROM %s WHERE "node" = ? AND "key" = ?
                """.formatted(this.table);

        try (Connection connection = DriverManager.getConnection(this.url);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, this.absolutePath());
            statement.setString(2, key);

            final ResultSet result = statement.executeQuery();

            if (result.next()) {
                return result.getString("value");
            }
        } catch (final SQLException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void removeSpi(final String key) {
        final String query = """
                DELETE FROM %s WHERE "node" = ? AND "key" = ?
                """.formatted(this.table);

        try (Connection connection = DriverManager.getConnection(this.url);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, this.absolutePath());
            statement.setString(2, key);

            statement.executeUpdate();

        } catch (final SQLException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        final String query = """
                DELETE FROM %s WHERE "node" LIKE ?
                """.formatted(this.table);

        try (Connection connection = DriverManager.getConnection(this.url);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, this.absolutePath() + "%");

            statement.executeUpdate();

        } catch (final SQLException e) {
            throw new BackingStoreException(e);
        }
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        final String query = """
                SELECT "key" FROM %s WHERE "node" = ? AND "key" IS NOT NULL
                """.formatted(this.table);

        try (Connection connection = DriverManager.getConnection(this.url);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, this.absolutePath());

            final ResultSet result = statement.executeQuery();
            final Collection<String> keys = new ArrayList<>();
            while (result.next()) {
                keys.add(result.getString("key"));
            }

            return keys.stream()
                .filter(Predicate.not(String::isBlank))
                .toArray(String[]::new);

        } catch (final SQLException e) {
            throw new BackingStoreException(e);
        }
    }

    @Override
    protected String[] childrenNamesSpi() {

        final String query = """
                SELECT "node" FROM %s
                WHERE "key" = ?
                  AND "node" <> ?
                  AND "node" LIKE ?
                  AND "node" NOT LIKE ?
                """.formatted(this.table);

        try (Connection connection = DriverManager.getConnection(this.url);
             PreparedStatement statement = connection.prepareStatement(query)) {

            final String prefix = (this.parent() == null) ? "" : this.absolutePath();

            statement.setString(1, "");
            statement.setString(2, this.absolutePath());
            statement.setString(3, prefix + "/%");
            statement.setString(4, prefix + "/%/%");

            final ResultSet result = statement.executeQuery();
            final Collection<String> children = new ArrayList<>();
            while (result.next()) {
                final String node = result
                    .getString("node")
                    .replaceFirst(prefix + "/", "");

                children.add(node);
            }

            return children.toArray(String[]::new);

        } catch (final SQLException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }

        return new String[0];
    }

    @Override
    protected AbstractPreferences childSpi(final String node) {
        final JDBCPreferences child = new JDBCPreferences(this, node, this.url, this.table, this.userNode);

        try (Connection connection = DriverManager.getConnection(this.url)) {
            child.insert(connection, "", null);
        } catch (final SQLException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }

        return child;
    }

    @Override
    protected void syncSpi() {
        // No-op as the data is immediately persisted
    }

    @Override
    protected void flushSpi() {
        // No-op as the data is immediately persisted
    }

    private void create(final Connection connection) {
        try (Statement statement = connection.createStatement()) {

            final String create = """
                CREATE TABLE %s (
                        "node" VARCHAR(255) NOT NULL,
                        "key" VARCHAR(255) NOT NULL,
                        "value" VARCHAR(8000),
                        PRIMARY KEY ("node", "key"),
                        CONSTRAINT chk_key_value CHECK (
                            ("key" = '' AND "value" IS NULL)
                            OR ("key" <> '' AND "value" IS NULL)
                            OR ("key" <> '' AND "value" IS NOT NULL)))
                """.formatted(this.table);

            statement.execute(create);

            final String index = """
                CREATE INDEX idx_node ON %s ("node")
                """.formatted(this.table);

            statement.execute(index);
        } catch (final SQLException e) {
            // Table and index already exist
            logger.log(WARNING, e.getMessage());
        }
    }

    private boolean exists(final Connection connection, final String key) {
        final String query = """
                SELECT count(*) FROM %s WHERE "node" = ? AND "key" = ?
                """.formatted(this.table);

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, this.absolutePath());
            statement.setString(2, key);

            final ResultSet result = statement.executeQuery();

            return result.next() && result.getInt(1) > 0;
        } catch (final SQLException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private void insert(final Connection connection, final String key, final String value) {
        if (this.exists(connection, key)) {
            this.update(connection, key, value);
            return;
        }

        final String query = """
                INSERT INTO %s ("node", "key", "value") VALUES (?, ?, ?)
                """.formatted(this.table);

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, this.absolutePath());
            statement.setString(2, key);
            statement.setString(3, value);

            statement.executeUpdate();

        } catch (final SQLException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }
    }

    private void update(final Connection connection, final String key, final String value) {
        final String query = """
                UPDATE %s SET "value" = ? WHERE "node" = ? AND "key" = ?
                """.formatted(this.table);

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, value);
            statement.setString(2, this.absolutePath());
            statement.setString(3, key);

            statement.executeUpdate();

        } catch (final SQLException e) {
            logger.log(ERROR, e.getMessage());
            if (logger.isLoggable(TRACE)) {
                e.printStackTrace();
            }
        }
    }
}
