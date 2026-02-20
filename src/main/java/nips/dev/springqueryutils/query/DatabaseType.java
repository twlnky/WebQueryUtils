package nips.dev.springqueryutils.query;

import lombok.Getter;

@Getter
public enum DatabaseType {
    POSTGRESQL("org.postgresql.Driver"),
    MYSQL("com.mysql.cj.jdbc.Driver"),
    H2("org.h2.Driver"),
    SQLITE("org.sqlite.JDBC"),
    OTHER("");

    private final String driverClass;

    DatabaseType(String driverClass) {
        this.driverClass = driverClass;
    }

    public static DatabaseType fromDriverClass(String driverClassName) {
        for (DatabaseType type : values()) {
            if (type.driverClass.equals(driverClassName)) {
                return type;
            }
        }
        return OTHER;
    }
}