package org.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class PoolTest {
    public static void main(String[] args) {
        // 1. Define database connection settings
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/reliefmesh");
        config.setUsername("admin");
        config.setPassword("password123");
        config.setMaximumPoolSize(10);

        // 2. Initialize the Hikari connection pool
        HikariDataSource dataSource = new HikariDataSource(config);

        // 3. Test acquiring a connection from the pool
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                System.out.println("SUCCESS: HikariCP successfully connected to PostgreSQL!");
            } else {
                System.out.println("FAILED: Connection was established but is invalid.");
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Could not connect to the database.");
            e.printStackTrace();
        } finally {
            // 4. Close the pool after testing
            dataSource.close();
            System.out.println("Connection pool closed cleanly.");
        }
    }
}