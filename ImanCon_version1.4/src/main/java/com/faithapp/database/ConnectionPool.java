package com.faithapp.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private static final String DB_FILE = "imanconnect.db";
    private static HikariDataSource dataSource;
    
    static {
        try {
            initializePool();
            try (Connection conn = getConnection()) {
                initializeDatabase(conn);
            }
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    private static void initializePool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + DB_FILE);
        config.setMaximumPoolSize(10);
        
        dataSource = new HikariDataSource(config);
        logger.info("Connection pool initialized successfully");
    }

    private static void initializeDatabase(Connection conn) throws SQLException {
        String[] tables = {
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                profile_picture_path TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS ramadan_fasts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                year INTEGER NOT NULL,
                day INTEGER NOT NULL,
                completed BOOLEAN NOT NULL,
                notes TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id),
                UNIQUE(user_id, year, day)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS salah_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                prayer_date DATE NOT NULL,
                fajr BOOLEAN NOT NULL,
                dhuhr BOOLEAN NOT NULL,
                asr BOOLEAN NOT NULL,
                maghrib BOOLEAN NOT NULL,
                isha BOOLEAN NOT NULL,
                notes TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id),
                UNIQUE(user_id, prayer_date)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS quran_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                reading_date DATE NOT NULL,
                surah INTEGER NOT NULL,
                ayah_from INTEGER NOT NULL,
                ayah_to INTEGER NOT NULL,
                duration INTEGER NOT NULL,
                notes TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS zikr_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                zikr_date DATE NOT NULL,
                period TEXT NOT NULL,
                completed BOOLEAN NOT NULL,
                notes TEXT,
                FOREIGN KEY (user_id) REFERENCES users(id),
                UNIQUE(user_id, zikr_date, period)
            )
            """
        };

        for (String sql : tables) {
            try (var stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        }
        logger.info("Created Islamic tracking tables successfully");
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Connection pool closed successfully");
        }
    }

    // Backup the database
    public static void backupDatabase() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "backup_" + timestamp + "_" + DB_FILE;
        
        try {
            Files.copy(
                Path.of(DB_FILE),
                Path.of("backups", backupFileName),
                StandardCopyOption.REPLACE_EXISTING
            );
            logger.info("Database backup created successfully: {}", backupFileName);
            
            // Clean up old backups (keep last 5)
            File backupDir = new File("backups");
            if (backupDir.exists()) {
                File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("backup_"));
                if (backups != null && backups.length > 5) {
                    // Sort by last modified time (oldest first)
                    java.util.Arrays.sort(backups, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));
                    // Delete oldest files until we have 5 left
                    for (int i = 0; i < backups.length - 5; i++) {
                        if (backups[i].delete()) {
                            logger.info("Deleted old backup: {}", backups[i].getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to create database backup", e);
        }
    }
} 