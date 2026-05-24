package nips.dev.springqueryutils.query;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

/**
 * Утилита «какой JDBC-драйвер сейчас в classpath» — для отладки, не для бизнес-логики.
 *
 * <p>{@link nips.dev.springqueryutils.template.AbstractCRUDLService} это не вызывает.
 *
 * @author nip
 * @since 0.0.1
 */
public class DriverUtils {


    public static DatabaseType getActiveDatabaseType() {
        try {
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                DatabaseType registered = DatabaseType.fromDriverClass(driver.getClass().getName());
                if (registered != DatabaseType.OTHER) {
                    return registered;
                }
            }
        } catch (Exception ignored) {
            // fall through to classpath probe
        }

        for (DatabaseType type : DatabaseType.values()) {
            if (type != DatabaseType.OTHER && isDriverAvailable(type.getDriverClass())) {
                return type;
            }
        }

        return DatabaseType.OTHER;
    }


    public static boolean isDriverAvailable(String driverClassName) {
        try {
            Class.forName(driverClassName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    public static boolean isDatabaseAvailable(DatabaseType type) {
        if (type == DatabaseType.OTHER) {
            return false;
        }
        return isDriverAvailable(type.getDriverClass());
    }


    public static void printAvailableDrivers() {
        try {
            System.out.println("Available JDBC Drivers:");
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                System.out.println(" - " + driver.getClass().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
