package nips.dev.springqueryutils.query;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

public class DriverUtils {


    public static DatabaseType getActiveDatabaseType() {
        try {
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                java.sql.Driver driver = drivers.nextElement();
                for (DatabaseType type : DatabaseType.values()) {
                    if (type != DatabaseType.OTHER &&
                            driver.getClass().getName().equals(type.getDriverClass())) {
                        return type;
                    }
                }
            }
        } catch (Exception ignore) {
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
            Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                java.sql.Driver driver = drivers.nextElement();
                System.out.println(" - " + driver.getClass().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
