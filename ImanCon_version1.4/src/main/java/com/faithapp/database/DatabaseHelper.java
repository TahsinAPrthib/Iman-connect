package com.faithapp.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.models.RamadanEntry;
import com.faithapp.models.TasbihEntry;
import com.faithapp.models.User;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Alert;

public class DatabaseHelper {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseHelper.class);
    private static final SimpleObjectProperty<Connection> connectionProperty = new SimpleObjectProperty<>();
    private static final ScheduledExecutorService backupScheduler = Executors.newSingleThreadScheduledExecutor();
    
    static {
        initializeDatabase();
        scheduleBackups();
    }
    
    private static void initializeDatabase() {
        // Create the users table first
        createTables();

        // Create Islamic tracking tables
        String createRamadanTable = """
            CREATE TABLE IF NOT EXISTS ramadan_fasting (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                year INTEGER NOT NULL,
                day_number INTEGER NOT NULL,
                fasted BOOLEAN DEFAULT FALSE,
                notes TEXT,
                good_deeds TEXT,
                quran_pages INTEGER DEFAULT 0,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id),
                UNIQUE(user_id, year, day_number)
            )
        """;

        String createSalahTable = """
            CREATE TABLE IF NOT EXISTS salah_tracker (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                prayer_date DATE NOT NULL,
                fajr BOOLEAN DEFAULT FALSE,
                dhuhr BOOLEAN DEFAULT FALSE,
                asr BOOLEAN DEFAULT FALSE,
                maghrib BOOLEAN DEFAULT FALSE,
                isha BOOLEAN DEFAULT FALSE,
                notes TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id),
                UNIQUE(user_id, prayer_date)
            )
        """;

        String createQuranTable = """
            CREATE TABLE IF NOT EXISTS quran_tracker (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                date DATE NOT NULL,
                surah_number INTEGER NOT NULL,
                ayah_from INTEGER NOT NULL,
                ayah_to INTEGER NOT NULL,
                duration_minutes INTEGER,
                notes TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """;

        String createZikrTable = """
            CREATE TABLE IF NOT EXISTS zikr_tracker (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                date DATE NOT NULL,
                period TEXT CHECK(period IN ('morning', 'evening')) NOT NULL,
                completed BOOLEAN DEFAULT FALSE,
                notes TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id),
                UNIQUE(user_id, date, period)
            )
        """;

        String createTasbihTable = """
            CREATE TABLE IF NOT EXISTS tasbih_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                entry_date DATE NOT NULL,
                dhikr_name TEXT NOT NULL,
                count INTEGER NOT NULL,
                cycles INTEGER NOT NULL,
                total_count INTEGER NOT NULL,
                notes TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """;

        // Create Fatwa system tables
        String createScholarsTable = """
            CREATE TABLE IF NOT EXISTS scholars (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                full_name TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                specialization TEXT,
                qualifications TEXT,
                bio TEXT,
                gender TEXT DEFAULT 'Male',
                is_verified BOOLEAN DEFAULT FALSE,
                is_online BOOLEAN DEFAULT FALSE,
                last_seen DATETIME,
                profile_picture_path TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """;

        String createFatwaQuestionsTable = """
            CREATE TABLE IF NOT EXISTS fatwa_questions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                scholar_id INTEGER NOT NULL,
                question_title TEXT NOT NULL,
                question_text TEXT NOT NULL,
                category TEXT,
                priority TEXT DEFAULT 'normal',
                status TEXT DEFAULT 'pending' CHECK(status IN ('pending', 'answered', 'rejected')),
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (scholar_id) REFERENCES scholars(id)
            )
        """;

        String createFatwaAnswersTable = """
            CREATE TABLE IF NOT EXISTS fatwa_answers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                question_id INTEGER NOT NULL,
                scholar_id INTEGER NOT NULL,
                answer_text TEXT NOT NULL,
                references_text TEXT,
                is_public BOOLEAN DEFAULT TRUE,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (question_id) REFERENCES fatwa_questions(id),
                FOREIGN KEY (scholar_id) REFERENCES scholars(id)
            )
        """;

        // Create Community messaging tables
        String createCommunityMessagesTable = """
            CREATE TABLE IF NOT EXISTS community_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                message_text TEXT NOT NULL,
                community_type TEXT NOT NULL CHECK(community_type IN ('male', 'female')),
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
        """;

        String createPersonalMessagesTable = """
            CREATE TABLE IF NOT EXISTS personal_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender_id INTEGER NOT NULL,
                receiver_id INTEGER NOT NULL,
                message_text TEXT NOT NULL,
                is_read BOOLEAN DEFAULT FALSE,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (sender_id) REFERENCES users(id),
                FOREIGN KEY (receiver_id) REFERENCES users(id)
            )
        """;

        try (Connection conn = ConnectionPool.getConnection()) {
            // Create Islamic tracking tables
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createRamadanTable);
                stmt.execute(createSalahTable);
                stmt.execute(createQuranTable);
                stmt.execute(createZikrTable);
                stmt.execute(createTasbihTable);
                stmt.execute(createScholarsTable);
                stmt.execute(createFatwaQuestionsTable);
                stmt.execute(createFatwaAnswersTable);
                stmt.execute(createCommunityMessagesTable);
                stmt.execute(createPersonalMessagesTable);
                logger.info("Created Islamic tracking, Fatwa, and Community messaging tables successfully");
            }
            
            // Update existing ramadan_fasting table if needed
            updateRamadanTableSchema(conn);
            
            // Add gender column to scholars table if it doesn't exist
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE scholars ADD COLUMN gender TEXT DEFAULT 'Male'");
                logger.info("Added gender column to scholars table");
            } catch (SQLException e) {
                // Column already exists, ignore
                logger.debug("Gender column already exists in scholars table");
            }
            
            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.error("Error initializing database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    private static void updateRamadanTableSchema(Connection conn) {
        try {
            // Check if good_deeds column exists
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT good_deeds FROM ramadan_fasting LIMIT 1");
                logger.info("good_deeds column already exists in ramadan_fasting table");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                logger.info("Adding good_deeds column to ramadan_fasting table");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE ramadan_fasting ADD COLUMN good_deeds TEXT");
                }
            }
            
            // Check if quran_pages column exists
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT quran_pages FROM ramadan_fasting LIMIT 1");
                logger.info("quran_pages column already exists in ramadan_fasting table");
            } catch (SQLException e) {
                // Column doesn't exist, add it
                logger.info("Adding quran_pages column to ramadan_fasting table");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE ramadan_fasting ADD COLUMN quran_pages INTEGER DEFAULT 0");
                }
            }
        } catch (SQLException e) {
            logger.error("Error updating ramadan_fasting table schema", e);
        }
    }
    
    private static void scheduleBackups() {
        // Schedule daily backups at midnight
        backupScheduler.scheduleAtFixedRate(() -> {
            try {
                ConnectionPool.backupDatabase();
            } catch (Exception e) {
                logger.error("Scheduled backup failed", e);
            }
        }, 1, 24, TimeUnit.HOURS);
    }
    
    private static void createTables() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                gender TEXT DEFAULT 'Male',
                profile_picture_path TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            
            // Add gender column if it doesn't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN gender TEXT DEFAULT 'Male'");
            } catch (SQLException e) {
                // Column already exists, ignore
                logger.debug("Gender column already exists in users table");
            }
            
            logger.info("Database tables created successfully");
        } catch (SQLException e) {
            logger.error("Failed to create tables", e);
            Platform.runLater(() -> showError("Database Error", "Failed to create tables: " + e.getMessage()));
        }
    }
    
    public static CompletableFuture<Boolean> registerUser(String fullName, String email, String username, String password, String gender) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO users (full_name, email, username, password_hash, gender) VALUES (?, ?, ?, ?, ?)";
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                logger.info("Attempting to register user: {}", username);
                
                pstmt.setString(1, fullName);
                pstmt.setString(2, email);
                pstmt.setString(3, username);
                pstmt.setString(4, password); // For testing, store password directly. In production, use proper hashing
                pstmt.setString(5, gender);
                
                int result = pstmt.executeUpdate();
                if (result > 0) {
                    logger.info("User registered successfully: {}", username);
                    return true;
                }
                logger.warn("Failed to register user: {}, no rows affected", username);
                return false;
            } catch (SQLException e) {
                logger.error("Failed to register user: {} - Error: {}", username, e.getMessage(), e);
                Platform.runLater(() -> showError("Registration Error", 
                    e.getMessage().contains("UNIQUE constraint") ? 
                    "Username or email already exists" : 
                    "Failed to register user: " + e.getMessage()));
                return false;
            }
        });
    }
    
    public static CompletableFuture<Boolean> checkUserExists(String username, String email) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, username);
                pstmt.setString(2, email);
                
                ResultSet rs = pstmt.executeQuery();
                return rs.getInt(1) > 0;
            } catch (SQLException e) {
                logger.error("Failed to check user existence: {}", username, e);
                Platform.runLater(() -> showError("Database Error", "Failed to check user existence: " + e.getMessage()));
                return false;
            }
        });
    }
    
    public static CompletableFuture<Boolean> validateLogin(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT password_hash FROM users WHERE username = ?";
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    String storedPassword = rs.getString("password_hash");
                    boolean valid = password.equals(storedPassword); // For testing. In production, use proper password verification
                    if (valid) {
                        logger.info("User logged in successfully: {}", username);
                    } else {
                        logger.warn("Invalid login attempt for user: {}", username);
                    }
                    return valid;
                }
                return false;
            } catch (SQLException e) {
                logger.error("Failed to validate login: {}", username, e);
                Platform.runLater(() -> showError("Login Error", "Failed to validate login: " + e.getMessage()));
                return false;
            }
        });
    }
    
    public static CompletableFuture<String> getFullName(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ConnectionPool.getConnection()) {
                String sql = "SELECT full_name FROM users WHERE username = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    logger.info("Executing getFullName query for username: {}", username);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        String fullName = rs.getString("full_name");
                        logger.info("Found full name for user {}: {}", username, fullName);
                        return fullName;
                    }
                    logger.warn("No user found with username: {}", username);
                }
            } catch (SQLException e) {
                logger.error("Error getting full name for user: {}", username, e);
                Platform.runLater(() -> showError("Database Error", "Failed to get user data: " + e.getMessage()));
            }
            return null;
        });
    }

    public static CompletableFuture<User> getUserByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ConnectionPool.getConnection()) {
                String sql = "SELECT id, username, full_name, email, gender, created_at, profile_picture_path FROM users WHERE username = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, username);
                    logger.info("Executing getUserByUsername query for username: {}", username);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("gender"),
                            rs.getString("created_at") != null ? rs.getString("created_at") : "Today",
                            rs.getString("profile_picture_path")
                        );
                        logger.info("Found user: {} with gender: {}", user.getFullName(), user.getGender());
                        return user;
                    }
                    logger.warn("No user found with username: {}", username);
                }
            } catch (SQLException e) {
                logger.error("Error getting user by username: {}", username, e);
                Platform.runLater(() -> showError("Database Error", "Failed to get user data: " + e.getMessage()));
            }
            return null;
        });
    }
    
    public static CompletableFuture<String> updateProfilePicture(String username, String picturePath) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE users SET profile_picture_path = ? WHERE username = ?";
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, picturePath);
                stmt.setString(2, username);
                
                int updated = stmt.executeUpdate();
                if (updated > 0) {
                    logger.info("Updated profile picture path for user: {} to: {}", username, picturePath);
                    return picturePath;
                } else {
                    logger.error("No user found with username: {}", username);
                    throw new RuntimeException("User not found");
                }
            } catch (SQLException e) {
                logger.error("Error updating profile picture path for user: " + username, e);
                throw new RuntimeException("Failed to update profile picture", e);
            }
        });
    }
    
    public static String getProfilePicturePath(String username) {
        String sql = "SELECT profile_picture_path FROM users WHERE username = ?";
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String path = rs.getString("profile_picture_path");
                logger.info("Retrieved profile picture path for user {}: {}", username, path);
                return path;
            }
            
            logger.info("No profile picture path found for user: {}", username);
            return null;
        } catch (SQLException e) {
            logger.error("Error getting profile picture path for user: " + username, e);
            return null;
        }
    }
    
    private static void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.show();
        });
    }
    
    public static void shutdown() {
        backupScheduler.shutdown();
        try {
            if (!backupScheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                backupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            backupScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        ConnectionPool.closePool();
    }

    // New methods for Islamic tracking features

    public static CompletableFuture<Boolean> trackRamadanFast(int userId, int year, int day, boolean completed, String notes) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ConnectionPool.getConnection()) {
                String sql = "INSERT INTO ramadan_fasts (user_id, year, day, completed, notes) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, year);
                    stmt.setInt(3, day);
                    stmt.setBoolean(4, completed);
                    stmt.setString(5, notes);
                    return stmt.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                logger.error("Error tracking Ramadan fast", e);
                return false;
            }
        });
    }

    public static CompletableFuture<Boolean> saveRamadanEntry(int userId, LocalDate date, boolean fasted, 
                                                             String fastingNotes, String goodDeeds, int quranPages) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ConnectionPool.getConnection()) {
                String sql = "INSERT OR REPLACE INTO ramadan_fasting (user_id, year, day_number, fasted, notes, good_deeds, quran_pages) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, date.getYear());
                    stmt.setInt(3, date.getDayOfYear());
                    stmt.setBoolean(4, fasted);
                    stmt.setString(5, fastingNotes);
                    stmt.setString(6, goodDeeds);
                    stmt.setInt(7, quranPages);
                    return stmt.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                logger.error("Error saving Ramadan entry", e);
                return false;
            }
        });
    }

    public static CompletableFuture<List<RamadanEntry>> getRamadanEntries(int userId, int year) {
        return CompletableFuture.supplyAsync(() -> {
            List<RamadanEntry> entries = new ArrayList<>();
            try (Connection conn = ConnectionPool.getConnection()) {
                String sql = "SELECT id, user_id, year, day_number, fasted, notes, good_deeds, quran_pages " +
                           "FROM ramadan_fasting WHERE user_id = ? AND year = ? ORDER BY day_number";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, year);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            LocalDate date = LocalDate.ofYearDay(year, rs.getInt("day_number"));
                            RamadanEntry entry = new RamadanEntry(
                                rs.getInt("id"),
                                rs.getInt("user_id"),
                                date,
                                rs.getBoolean("fasted"),
                                rs.getString("notes"),
                                rs.getString("good_deeds"),
                                rs.getInt("quran_pages")
                            );
                            entries.add(entry);
                        }
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting Ramadan entries", e);
            }
            return entries;
        });
    }

    public static CompletableFuture<Boolean> trackSalah(int userId, Date date, boolean fajr, boolean dhuhr,
                                                       boolean asr, boolean maghrib, boolean isha, String notes) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ConnectionPool.getConnection()) {
                String sql = "INSERT INTO salah_entries (user_id, prayer_date, fajr, dhuhr, asr, maghrib, isha, notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setDate(2, date);
                    stmt.setBoolean(3, fajr);
                    stmt.setBoolean(4, dhuhr);
                    stmt.setBoolean(5, asr);
                    stmt.setBoolean(6, maghrib);
                    stmt.setBoolean(7, isha);
                    stmt.setString(8, notes);
                    return stmt.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                logger.error("Error tracking Salah", e);
                return false;
            }
        });
    }

    public static CompletableFuture<Boolean> trackTasbih(int userId, Date date, String dhikrName, 
                                                        int count, int cycles, int totalCount, String notes) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ConnectionPool.getConnection()) {
                String sql = "INSERT INTO tasbih_entries (user_id, entry_date, dhikr_name, count, cycles, total_count, notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setDate(2, date);
                    stmt.setString(3, dhikrName);
                    stmt.setInt(4, count);
                    stmt.setInt(5, cycles);
                    stmt.setInt(6, totalCount);
                    stmt.setString(7, notes);
                    return stmt.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                logger.error("Error tracking Tasbih", e);
                return false;
            }
        });
    }

    public static CompletableFuture<List<TasbihEntry>> getTasbihEntries(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            List<TasbihEntry> entries = new ArrayList<>();
            try (Connection conn = ConnectionPool.getConnection()) {
                String sql = "SELECT id, user_id, entry_date, dhikr_name, count, cycles, total_count, notes " +
                           "FROM tasbih_entries WHERE user_id = ? ORDER BY entry_date DESC";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            TasbihEntry entry = new TasbihEntry(
                                rs.getInt("id"),
                                rs.getInt("user_id"),
                                rs.getDate("entry_date").toLocalDate(),
                                rs.getString("dhikr_name"),
                                rs.getInt("count"),
                                rs.getInt("cycles"),
                                rs.getInt("total_count"),
                                rs.getString("notes")
                            );
                            entries.add(entry);
                        }
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting Tasbih entries", e);
            }
            return entries;
        });
    }

    public static CompletableFuture<Boolean> trackQuranReading(int userId, Date date, int surah,
                                                             int ayahFrom, int ayahTo, int duration, String notes) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ConnectionPool.getConnection()) {
                String sql = "INSERT INTO quran_entries (user_id, reading_date, surah, ayah_from, ayah_to, duration, notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setDate(2, date);
                    stmt.setInt(3, surah);
                    stmt.setInt(4, ayahFrom);
                    stmt.setInt(5, ayahTo);
                    stmt.setInt(6, duration);
                    stmt.setString(7, notes);
                    return stmt.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                logger.error("Error tracking Quran reading", e);
                return false;
            }
        });
    }

    public static CompletableFuture<Boolean> trackZikr(int userId, Date date, String period, boolean completed, String notes) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ConnectionPool.getConnection()) {
                String sql = "INSERT INTO zikr_entries (user_id, zikr_date, period, completed, notes) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setDate(2, date);
                    stmt.setString(3, period);
                    stmt.setBoolean(4, completed);
                    stmt.setString(5, notes);
                    return stmt.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                logger.error("Error tracking Zikr", e);
                return false;
            }
        });
    }

    public static CompletableFuture<List<User>> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            List<User> users = new ArrayList<>();
            String sql = "SELECT id, username, full_name, email, gender, created_at, profile_picture_path FROM users ORDER BY created_at DESC";
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("gender"),
                            rs.getString("created_at") != null ? rs.getString("created_at") : "Today",
                            rs.getString("profile_picture_path")
                        );
                        users.add(user);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting all users", e);
            }
            return users;
        });
    }

    public static CompletableFuture<Boolean> updatePassword(String username, String newPassword) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, newPassword); // In production, use proper password hashing
                pstmt.setString(2, username);
                
                int result = pstmt.executeUpdate();
                if (result > 0) {
                    logger.info("Password updated successfully for user: {}", username);
                    return true;
                }
                logger.warn("Failed to update password for user: {}, no rows affected", username);
                return false;
            } catch (SQLException e) {
                logger.error("Failed to update password for user: {} - Error: {}", username, e.getMessage(), e);
                throw new RuntimeException("Failed to update password", e);
            }
        });
    }

    public static CompletableFuture<List<com.faithapp.models.ZikrEntry>> getZikrEntries(int userId, int year) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            List<com.faithapp.models.ZikrEntry> entries = new ArrayList<>();
            try (Connection conn = ConnectionPool.getConnection()) {
                String sql = "SELECT id, zikr_date, period, completed, notes FROM zikr_entries WHERE user_id = ? AND strftime('%Y', zikr_date) = ? ORDER BY zikr_date DESC";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, String.valueOf(year));
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            entries.add(new com.faithapp.models.ZikrEntry(
                                rs.getInt("id"),
                                rs.getDate("zikr_date").toLocalDate(),
                                rs.getString("period"),
                                rs.getBoolean("completed"),
                                rs.getString("notes")
                            ));
                        }
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting Zikr entries", e);
            }
            return entries;
        });
    }

    // ========== SCHOLAR METHODS ==========
    
    public static CompletableFuture<Boolean> registerScholar(String fullName, String email, String username, 
                                                            String password, String specialization, 
                                                            String qualifications, String bio, String gender) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ConnectionPool.getConnection()) {
                // First create a user record
                String createUserSql = "INSERT INTO users (full_name, email, username, password_hash, gender) VALUES (?, ?, ?, ?, ?)";
                int userId = -1;
                
                try (PreparedStatement stmt = conn.prepareStatement(createUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, fullName);
                    stmt.setString(2, email);
                    stmt.setString(3, username);
                    stmt.setString(4, password); // In production, use proper password hashing
                    stmt.setString(5, gender);
                    
                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows > 0) {
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                userId = rs.getInt(1);
                            }
                        }
                    }
                }
                
                if (userId == -1) {
                    logger.error("Failed to create user record for scholar: {}", username);
                    return false;
                }
                
                // Then create scholar record
                String createScholarSql = """
                    INSERT INTO scholars (user_id, full_name, email, username, password_hash, 
                                        specialization, qualifications, bio, gender) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(createScholarSql)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, fullName);
                    stmt.setString(3, email);
                    stmt.setString(4, username);
                    stmt.setString(5, password);
                    stmt.setString(6, specialization);
                    stmt.setString(7, qualifications);
                    stmt.setString(8, bio);
                    stmt.setString(9, gender);
                    
                    int result = stmt.executeUpdate();
                    if (result > 0) {
                        logger.info("Scholar registered successfully: {}", username);
                        return true;
                    }
                }
                
                logger.warn("Failed to register scholar: {}", username);
                return false;
            } catch (SQLException e) {
                logger.error("Error registering scholar: {}", username, e);
                return false;
            }
        });
    }

    public static CompletableFuture<Boolean> validateScholarLogin(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM scholars WHERE username = ? AND password_hash = ?";
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        boolean isValid = count > 0;
                        
                        if (isValid) {
                            // Update last seen
                            updateScholarLastSeen(conn, username);
                        }
                        
                        logger.info("Scholar login validation for {}: {}", username, isValid);
                        return isValid;
                    }
                }
            } catch (SQLException e) {
                logger.error("Error validating scholar login for: {}", username, e);
            }
            return false;
        });
    }

    private static void updateScholarLastSeen(Connection conn, String username) {
        try {
            String sql = "UPDATE scholars SET last_seen = CURRENT_TIMESTAMP, is_online = TRUE WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.error("Error updating scholar last seen", e);
        }
    }

    public static CompletableFuture<com.faithapp.models.Scholar> getScholarByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                SELECT id, user_id, full_name, email, username, specialization, qualifications, 
                       bio, gender, is_verified, is_online, last_seen, profile_picture_path, created_at 
                FROM scholars WHERE username = ?
            """;
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new com.faithapp.models.Scholar(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("username"),
                            rs.getString("specialization"),
                            rs.getString("qualifications"),
                            rs.getString("bio"),
                            rs.getString("gender"),
                            rs.getBoolean("is_verified"),
                            rs.getBoolean("is_online"),
                            rs.getString("profile_picture_path"),
                            rs.getString("created_at")
                        );
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting scholar by username: {}", username, e);
            }
            return null;
        });
    }

    public static CompletableFuture<List<com.faithapp.models.Scholar>> getAllScholars() {
        return CompletableFuture.supplyAsync(() -> {
            List<com.faithapp.models.Scholar> scholars = new ArrayList<>();
            String sql = """
                SELECT id, user_id, full_name, email, username, specialization, qualifications, 
                       bio, gender, is_verified, is_online, last_seen, profile_picture_path, created_at 
                FROM scholars ORDER BY created_at DESC
            """;
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        scholars.add(new com.faithapp.models.Scholar(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("username"),
                            rs.getString("specialization"),
                            rs.getString("qualifications"),
                            rs.getString("bio"),
                            rs.getString("gender"),
                            rs.getBoolean("is_verified"),
                            rs.getBoolean("is_online"),
                            rs.getString("profile_picture_path"),
                            rs.getString("created_at")
                        ));
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting all scholars", e);
            }
            return scholars;
        });
    }

    // ========== FATWA METHODS ==========
    
    public static CompletableFuture<Boolean> submitFatwaQuestion(int userId, int scholarId, 
                                                                String questionTitle, String questionText, 
                                                                String category, String priority) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ConnectionPool.getConnection()) {
                String sql = """
                    INSERT INTO fatwa_questions (user_id, scholar_id, question_title, question_text, 
                                               category, priority, status) 
                    VALUES (?, ?, ?, ?, ?, ?, 'pending')
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, scholarId);
                    stmt.setString(3, questionTitle);
                    stmt.setString(4, questionText);
                    stmt.setString(5, category);
                    stmt.setString(6, priority);
                    
                    int result = stmt.executeUpdate();
                    if (result > 0) {
                        logger.info("Fatwa question submitted successfully by user: {}", userId);
                        return true;
                    }
                }
            } catch (SQLException e) {
                logger.error("Error submitting fatwa question", e);
            }
            return false;
        });
    }

    public static CompletableFuture<List<com.faithapp.models.FatwaQuestion>> getFatwaQuestionsForScholar(int scholarId) {
        return CompletableFuture.supplyAsync(() -> {
            List<com.faithapp.models.FatwaQuestion> questions = new ArrayList<>();
            String sql = """
                SELECT fq.id, fq.user_id, fq.scholar_id, fq.question_title, fq.question_text, 
                       fq.category, fq.priority, fq.status, fq.created_at, fq.updated_at,
                       u.username as user_name, s.full_name as scholar_name
                FROM fatwa_questions fq
                JOIN users u ON fq.user_id = u.id
                JOIN scholars s ON fq.scholar_id = s.id
                WHERE fq.scholar_id = ? 
                ORDER BY fq.created_at DESC
            """;
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, scholarId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        questions.add(new com.faithapp.models.FatwaQuestion(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("scholar_id"),
                            rs.getString("question_title"),
                            rs.getString("question_text"),
                            rs.getString("category"),
                            rs.getString("priority"),
                            rs.getString("status"),
                            rs.getString("user_name"),
                            rs.getString("scholar_name")
                        ));
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting fatwa questions for scholar: {}", scholarId, e);
            }
            return questions;
        });
    }

    public static CompletableFuture<List<com.faithapp.models.FatwaQuestion>> getFatwaQuestionsForUser(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            List<com.faithapp.models.FatwaQuestion> questions = new ArrayList<>();
            String sql = """
                SELECT fq.id, fq.user_id, fq.scholar_id, fq.question_title, fq.question_text, 
                       fq.category, fq.priority, fq.status, fq.created_at, fq.updated_at,
                       u.username as user_name, s.full_name as scholar_name
                FROM fatwa_questions fq
                JOIN users u ON fq.user_id = u.id
                JOIN scholars s ON fq.scholar_id = s.id
                WHERE fq.user_id = ? 
                ORDER BY fq.created_at DESC
            """;
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        questions.add(new com.faithapp.models.FatwaQuestion(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("scholar_id"),
                            rs.getString("question_title"),
                            rs.getString("question_text"),
                            rs.getString("category"),
                            rs.getString("priority"),
                            rs.getString("status"),
                            rs.getString("user_name"),
                            rs.getString("scholar_name")
                        ));
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting fatwa questions for user: {}", userId, e);
            }
            return questions;
        });
    }

    public static CompletableFuture<Boolean> submitFatwaAnswer(int questionId, int scholarId, 
                                                              String answerText, String referencesText, 
                                                              boolean isPublic) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ConnectionPool.getConnection()) {
                // Insert the answer
                String insertAnswerSql = """
                    INSERT INTO fatwa_answers (question_id, scholar_id, answer_text, references_text, is_public) 
                    VALUES (?, ?, ?, ?, ?)
                """;
                
                try (PreparedStatement stmt = conn.prepareStatement(insertAnswerSql)) {
                    stmt.setInt(1, questionId);
                    stmt.setInt(2, scholarId);
                    stmt.setString(3, answerText);
                    stmt.setString(4, referencesText);
                    stmt.setBoolean(5, isPublic);
                    
                    int result = stmt.executeUpdate();
                    if (result > 0) {
                        // Update question status to answered
                        String updateQuestionSql = "UPDATE fatwa_questions SET status = 'answered', updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuestionSql)) {
                            updateStmt.setInt(1, questionId);
                            updateStmt.executeUpdate();
                        }
                        
                        logger.info("Fatwa answer submitted successfully for question: {}", questionId);
                        return true;
                    }
                }
            } catch (SQLException e) {
                logger.error("Error submitting fatwa answer", e);
            }
            return false;
        });
    }

    public static CompletableFuture<com.faithapp.models.FatwaAnswer> getFatwaAnswer(int questionId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                SELECT fa.id, fa.question_id, fa.scholar_id, fa.answer_text, fa.references_text, 
                       fa.is_public, fa.created_at, s.full_name as scholar_name
                FROM fatwa_answers fa
                JOIN scholars s ON fa.scholar_id = s.id
                WHERE fa.question_id = ?
            """;
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, questionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Get the timestamp from the database
                        java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                        java.time.LocalDateTime createdAt = timestamp != null ? timestamp.toLocalDateTime() : null;
                        
                        return new com.faithapp.models.FatwaAnswer(
                            rs.getInt("id"),
                            rs.getInt("question_id"),
                            rs.getInt("scholar_id"),
                            rs.getString("answer_text"),
                            rs.getString("references_text"),
                            rs.getBoolean("is_public"),
                            createdAt,
                            rs.getString("scholar_name")
                        );
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting fatwa answer for question: {}", questionId, e);
            }
            return null;
        });
    }

    public static CompletableFuture<Boolean> checkScholarExists(String username, String email) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM scholars WHERE username = ? OR email = ?";
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, username);
                pstmt.setString(2, email);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        boolean exists = count > 0;
                        logger.info("Scholar existence check for {}: {}", username, exists);
                        return exists;
                    }
                }
            } catch (SQLException e) {
                logger.error("Error checking scholar existence for: {}", username, e);
            }
            return false;
        });
    }

    // Community Messaging Methods
    public static CompletableFuture<Boolean> postCommunityMessage(int userId, String messageText, String communityType) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO community_messages (user_id, message_text, community_type) VALUES (?, ?, ?)";
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, userId);
                pstmt.setString(2, messageText);
                pstmt.setString(3, communityType);
                
                int result = pstmt.executeUpdate();
                if (result > 0) {
                    logger.info("Community message posted successfully by user: {} in {} community", userId, communityType);
                    return true;
                }
            } catch (SQLException e) {
                logger.error("Error posting community message", e);
            }
            return false;
        });
    }

    public static CompletableFuture<List<com.faithapp.models.CommunityMessage>> getCommunityMessages(String communityType) {
        return CompletableFuture.supplyAsync(() -> {
            List<com.faithapp.models.CommunityMessage> messages = new ArrayList<>();
            String sql = """
                SELECT cm.id, cm.user_id, cm.message_text, cm.community_type, cm.created_at,
                       u.full_name, u.gender, u.profile_picture_path
                FROM community_messages cm
                JOIN users u ON cm.user_id = u.id
                WHERE cm.community_type = ?
                ORDER BY cm.created_at DESC
                LIMIT 100
            """;
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, communityType);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        messages.add(new com.faithapp.models.CommunityMessage(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("gender"),
                            rs.getString("message_text"),
                            rs.getString("community_type"),
                            rs.getString("created_at"),
                            rs.getString("profile_picture_path")
                        ));
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting community messages for: {}", communityType, e);
            }
            return messages;
        });
    }

    public static CompletableFuture<Boolean> sendPersonalMessage(int senderId, int receiverId, String messageText) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO personal_messages (sender_id, receiver_id, message_text) VALUES (?, ?, ?)";
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, senderId);
                pstmt.setInt(2, receiverId);
                pstmt.setString(3, messageText);
                
                int result = pstmt.executeUpdate();
                if (result > 0) {
                    logger.info("Personal message sent successfully from user: {} to user: {}", senderId, receiverId);
                    return true;
                }
            } catch (SQLException e) {
                logger.error("Error sending personal message", e);
            }
            return false;
        });
    }

    public static CompletableFuture<List<com.faithapp.models.PersonalMessage>> getPersonalMessages(int userId1, int userId2) {
        return CompletableFuture.supplyAsync(() -> {
            List<com.faithapp.models.PersonalMessage> messages = new ArrayList<>();
            String sql = """
                SELECT pm.id, pm.sender_id, pm.receiver_id, pm.message_text, pm.is_read, pm.created_at,
                       sender.full_name as sender_name, receiver.full_name as receiver_name,
                       sender.profile_picture_path as sender_profile, receiver.profile_picture_path as receiver_profile
                FROM personal_messages pm
                JOIN users sender ON pm.sender_id = sender.id
                JOIN users receiver ON pm.receiver_id = receiver.id
                WHERE (pm.sender_id = ? AND pm.receiver_id = ?) OR (pm.sender_id = ? AND pm.receiver_id = ?)
                ORDER BY pm.created_at ASC
            """;
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, userId1);
                stmt.setInt(2, userId2);
                stmt.setInt(3, userId2);
                stmt.setInt(4, userId1);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        messages.add(new com.faithapp.models.PersonalMessage(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            rs.getInt("receiver_id"),
                            rs.getString("sender_name"),
                            rs.getString("receiver_name"),
                            rs.getString("message_text"),
                            rs.getString("created_at"),
                            rs.getBoolean("is_read"),
                            rs.getString("sender_profile"),
                            rs.getString("receiver_profile")
                        ));
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting personal messages between users: {} and {}", userId1, userId2, e);
            }
            return messages;
        });
    }

    public static CompletableFuture<List<com.faithapp.models.User>> getUsersForMessaging(int currentUserId, String currentUserGender) {
        return CompletableFuture.supplyAsync(() -> {
            List<com.faithapp.models.User> users = new ArrayList<>();
            String sql = """
                SELECT id, username, full_name, email, gender, created_at, profile_picture_path
                FROM users
                WHERE id != ? AND gender = ?
                ORDER BY full_name ASC
            """;
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, currentUserId);
                stmt.setString(2, currentUserGender);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(new com.faithapp.models.User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("gender"),
                            rs.getString("created_at") != null ? rs.getString("created_at") : "Today",
                            rs.getString("profile_picture_path")
                        ));
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting users for messaging", e);
            }
            return users;
        });
    }

    public static CompletableFuture<Boolean> markMessageAsRead(int messageId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE personal_messages SET is_read = TRUE WHERE id = ?";
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, messageId);
                int result = pstmt.executeUpdate();
                return result > 0;
            } catch (SQLException e) {
                logger.error("Error marking message as read", e);
                return false;
            }
        });
    }

    public static CompletableFuture<Integer> getUnreadMessageCount(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM personal_messages WHERE receiver_id = ? AND is_read = FALSE";
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, userId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                logger.error("Error getting unread message count", e);
            }
            return 0;
        });
    }

    public static CompletableFuture<Boolean> initializeCommunities() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = ConnectionPool.getConnection()) {
                // Check if communities already have messages
                String checkSql = "SELECT COUNT(*) FROM community_messages";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        logger.info("Communities already initialized with messages");
                        return true;
                    }
                }

                // Get admin user (first user in the system)
                String adminSql = "SELECT id FROM users LIMIT 1";
                int adminUserId = 1; // Default admin user ID
                try (PreparedStatement adminStmt = conn.prepareStatement(adminSql)) {
                    ResultSet rs = adminStmt.executeQuery();
                    if (rs.next()) {
                        adminUserId = rs.getInt("id");
                    }
                }

                // Insert welcome messages for Male Community
                String[] maleMessages = {
                    "Assalamu alaikum brothers! Welcome to our Male Community. Let's support each other in our Islamic journey.",
                    "May Allah bless us all. Feel free to share your thoughts, ask questions, and connect with fellow brothers.",
                    "Remember to maintain Islamic etiquette in our discussions. Respect and kindness are key values.",
                    "This is a safe space for brothers to discuss Islamic topics, share experiences, and grow together in faith."
                };

                // Insert welcome messages for Female Community
                String[] femaleMessages = {
                    "Assalamu alaikum sisters! Welcome to our Female Community. Let's support each other in our Islamic journey.",
                    "May Allah bless us all. Feel free to share your thoughts, ask questions, and connect with fellow sisters.",
                    "Remember to maintain Islamic etiquette in our discussions. Respect and kindness are key values.",
                    "This is a safe space for sisters to discuss Islamic topics, share experiences, and grow together in faith."
                };

                String insertSql = "INSERT INTO community_messages (user_id, message_text, community_type) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    
                    // Insert male community messages
                    for (String message : maleMessages) {
                        insertStmt.setInt(1, adminUserId);
                        insertStmt.setString(2, message);
                        insertStmt.setString(3, "male");
                        insertStmt.executeUpdate();
                    }

                    // Insert female community messages
                    for (String message : femaleMessages) {
                        insertStmt.setInt(1, adminUserId);
                        insertStmt.setString(2, message);
                        insertStmt.setString(3, "female");
                        insertStmt.executeUpdate();
                    }

                    logger.info("Communities initialized with welcome messages");
                    return true;
                }
            } catch (SQLException e) {
                logger.error("Error initializing communities", e);
                return false;
            }
        });
    }
    
    public static CompletableFuture<List<User>> getUsersByGender(String gender) {
        return CompletableFuture.supplyAsync(() -> {
            List<User> users = new ArrayList<>();
            String sql = "SELECT id, username, full_name, email, gender, profile_picture_path FROM users WHERE gender = ? ORDER BY full_name";
            
            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, gender);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("gender"),
                            "Today", // Default created_at
                            rs.getString("profile_picture_path")
                        );
                        users.add(user);
                    }
                }
                logger.info("Found {} users with gender: {}", users.size(), gender);
            } catch (SQLException e) {
                logger.error("Error getting users by gender", e);
            }
            return users;
        });
    }
} 