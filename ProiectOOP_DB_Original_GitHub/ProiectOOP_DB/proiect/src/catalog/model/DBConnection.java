package catalog.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/CatalogOnline");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "postgres");

    public static Connection getConnection() throws SQLException {
        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.isEmpty()) {
            throw new SQLException("DB_PASSWORD environment variable is not set.");
        }
        return DriverManager.getConnection(URL, USER, password);
    }
}
