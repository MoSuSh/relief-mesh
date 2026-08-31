package org.example;

import org.example.db.DatabaseConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PoolTest {

    public static void main(String[] args) {
        System.out.println("Starting connection pool test with 20 threads...\n");

        for (int i = 1; i <= 20; i++) {
            int threadId = i;
            Thread thread = new Thread(() -> runQuery(threadId));
            thread.start();
        }
    }

    private static void runQuery(int threadId) {
        // Runs built-in PostgreSQL sleep query to simulate 1 second of DB work
        String sql = "SELECT 1, pg_sleep(1);";

        System.out.println("Thread " + threadId + " attempting to acquire connection...");

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("✅ Thread " + threadId + " successfully executed query.");
            }

        } catch (SQLException e) {
            System.err.println("❌ Thread " + threadId + " failed: " + e.getMessage());
        }
    }
}