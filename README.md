# **JDBC Preferences API**

Implementation of the Java `Preferences` API for database storage via JDBC.
This project has been tested with the following databases:

- **Derby**
- **DuckDB**
- **H2 Database**
- **HSQLDB**
- **MSSQL**
- **PostgreSQL**
- **SQLite**

In the case of PostgreSQL, the database must exist beforehand.


## **Features**

- Support for different databases through JDBC
- Configuration via system properties


## **Technologies Used**

- **Java** (JDK 17 or higher)
- **JDBC** (Java Database Connectivity)
- **Java Preferences API**


## **How to Set Up and Run**

Add the following dependency into yout project:

```xml
<dependency>
    <groupId>br.dev.jadl.preferences</groupId>
    <artifactId>jdbc-preferences</artifactId>
    <version>1.0.0</version>
</dependency>
```


### **Configuration via System Properties**

The configuration should be specified using system properties. These properties can be defined for a specific scope
(`user` or `system`), if no scope is defined, the settings will apply to both scopes.

- `br.dev.jadl.prefs.JDBCPreferences.{scope}.url` - The JDBC connection URL for the database (e.g., `jdbc:mysql://localhost:3306/db`).
- `br.dev.jadl.prefs.JDBCPreferences.{scope}.table` - The name of the table where the preferences will be stored (default is `preferences`).


### **Running your application**

You can define the system properties from the command line, through a configuration file, or dynamically at runtime.

#### Running from the command line


```bash
# Running a fat jar directly from the command line
java -Djava.util.prefs.PreferencesFactory=br.dev.jadl.prefs.JDBCPreferencesFactory \
     -Dbr.dev.jadl.prefs.JDBCPreferences.user.url=jdbc:sqlite:./userprefs.sqlite \
     -Dbr.dev.jadl.prefs.JDBCPreferences.system.url=jdbc:sqlite:./sysprefs.sqlite \
     -jar your-application.jar
```

#### From a configuration file


```text
# Defines the Preferences API implementation to be used
-Djava.util.prefs.PreferencesFactory=br.dev.jadl.prefs.JDBCPreferencesFactory

# No scope defined so it will be used for both
# user and system
-Dbr.dev.jadl.prefs.JDBCPreferences.url=jdbc:sqlite:./prefs.sqlite

# Configure table used by system preferences
-Dbr.dev.jadl.prefs.JDBCPreferences.system.table=preferences

# Configure table used by user preferences
-Dbr.dev.jadl.prefs.JDBCPreferences.user.table=users
```

```bash
java @<filename> -jar application.jar
```

#### Dynamically at runtime

```java
import java.util.prefs.Preferences;

import static java.lang.System.Logger.Level.INFO;

public class Main {

    private static final System.Logger logger = System.getLogger(Main.class.getCanonicalName());

    public static void main(String[] args) {

        System.setProperty(PreferencesFactory.class.getCanonicalName(), "br.dev.jadl.prefs.JDBCPreferencesFactory");
        System.setProperty("br.dev.jadl.prefs.JDBCPreferences.url", "jdbc:sqlite:./preferences.sqlite");
        Preferences prefs = Preferences.userRoot().node("theme");

        // Retrieve the preference or the default value
        String theme = prefs.get("dark", "my-awesome-theme");

        logger.log(INFO, "Loading {0} theme", theme);
    }
}
```

## Known issues

- So far, it is not possible to store preferences by user.
- Other implementations are being developed in parallel (MongoDBPreferences and PropertiesPreferences), and user-specific storage should be consistent with all other implementations.


## **Contributing**

If you encounter any issues or would like to contribute to improving this project, feel free to open an issue or submit
a pull request. We welcome your contributions!

## **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
