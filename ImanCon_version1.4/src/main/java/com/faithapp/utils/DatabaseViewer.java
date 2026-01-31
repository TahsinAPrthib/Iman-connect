package com.faithapp.utils;

import com.faithapp.database.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseViewer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseViewer.class);

    public static void viewAllUsers() {
        String sql = "SELECT id, full_name, email, username, created_at FROM users";
        
        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n=== Registered Users ===");
            System.out.println("ID | Full Name | Email | Username | Created At");
            System.out.println("----------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %s | %s%n",
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("username"),
                    rs.getString("created_at")
                );
            }
            System.out.println("----------------------------------------\n");
            
        } catch (Exception e) {
            logger.error("Error viewing users", e);
            System.err.println("Error viewing users: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        viewAllUsers();
        ConnectionPool.closePool();
    }
} 