package com.faithapp.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.models.User;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    // Static reference to the main dashboard window
    private static Stage mainDashboardStage;
    private static DashboardController mainDashboardController;
    
    @FXML private Label userNameLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Button salahTrackerButton;
    @FXML private Button quranTrackerButton;
    @FXML private Button ramadanTrackerButton;
    @FXML private Button zikrTrackerButton;
    @FXML private Button tasbihCounterButton;
    @FXML private Button fastTrackerButton;
    @FXML private Button fatwaTrackerButton;
    @FXML private Button communityTrackerButton;
    @FXML private Button logoutButton;
    @FXML private Button settingsButton;
    @FXML private Button changePhotoButton;
    
    @FXML private Circle profilePhotoCircle;
    @FXML private ImageView profilePhotoView;
    @FXML private Label prayerStatsLabel;
    @FXML private Label prayerDetailsLabel;
    @FXML private Label quranStatsLabel;
    @FXML private ProgressBar prayerProgress;
    @FXML private ProgressBar quranProgress;
    
    private User currentUser;
    private static final String PROFILE_PHOTOS_DIR = "profile_photos";
    private Timer dateTimeTimer;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

    public void initialize() {
        // Store reference to this controller instance
        mainDashboardController = this;
        
        // Set up profile photo hover effect
        profilePhotoCircle.setOnMouseEntered(e -> changePhotoButton.setVisible(true));
        profilePhotoCircle.setOnMouseExited(e -> changePhotoButton.setVisible(false));
        changePhotoButton.setOnMouseEntered(e -> changePhotoButton.setVisible(true));
        changePhotoButton.setOnMouseExited(e -> changePhotoButton.setVisible(false));
        
        // Initialize progress bars
        prayerProgress.setStyle("-fx-accent: #00BFA5;");
        quranProgress.setStyle("-fx-accent: #00BFA5;");
        
        // Initialize date and time display
        initializeDateTimeDisplay();
        
        // Store reference to main dashboard window
        Platform.runLater(() -> {
            mainDashboardStage = (Stage) userNameLabel.getScene().getWindow();
            if (mainDashboardStage != null) {
                mainDashboardStage.setMinWidth(800);
                mainDashboardStage.setMinHeight(600);
                mainDashboardStage.setResizable(true);
                mainDashboardStage.setMaximized(false); // Reset state
                mainDashboardStage.setMaximized(true);  // Force maximize
                
                // Add focus listener to refresh stats when dashboard becomes visible
                mainDashboardStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (isNowFocused) {
                        loadPrayerStatsFromFile();
                        loadQuranStatsFromFile();
                    }
                });
            }
        });
    }
    
    private void initializeDateTimeDisplay() {
        // Set initial date and time
        updateDateTimeDisplay();
        
        // Create timer to update time every second
        dateTimeTimer = new Timer(true);
        dateTimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateDateTimeDisplay());
            }
        }, 1000, 1000); // Update every 1 second
    }
    
    private void updateDateTimeDisplay() {
        try {
            LocalDateTime now = LocalDateTime.now();
            String currentDate = now.format(dateFormatter);
            String currentTime = now.format(timeFormatter);
            
            currentDateLabel.setText(currentDate);
            currentTimeLabel.setText(currentTime);
        } catch (Exception e) {
            logger.error("Error updating date/time display", e);
            currentDateLabel.setText("Date unavailable");
            currentTimeLabel.setText("Time unavailable");
        }
    }
    
    // Static method to get the main dashboard window
    public static Stage getMainDashboardStage() {
        return mainDashboardStage;
    }
    
    // Static method to refresh prayer stats
    public static void refreshPrayerStats() {
        if (mainDashboardController != null) {
            Platform.runLater(() -> mainDashboardController.loadPrayerStatsFromFile());
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        initializeUserData();
    }

    private void initializeUserData() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getFullName());
            loadProfilePhoto();
            loadUserStats();
            logger.info("Dashboard initialized for user: {}", currentUser.getUsername());
        }
    }

    private void loadProfilePhoto() {
        try {
            String photoPath = getProfilePhotoPath();
            if (Files.exists(Paths.get(photoPath))) {
                Image image = new Image(new File(photoPath).toURI().toString());
                profilePhotoView.setImage(image);
            }
            
            // Add hover functionality for change photo button
            setupPhotoButtonHover();
        } catch (Exception e) {
            logger.error("Error loading profile photo", e);
        }
    }

    private void setupPhotoButtonHover() {
        // Get the StackPane that contains the profile photo and button
        StackPane photoContainer = (StackPane) profilePhotoView.getParent();
        
        // Show button on hover
        photoContainer.setOnMouseEntered(e -> {
            changePhotoButton.setVisible(true);
        });
        
        // Hide button when mouse leaves
        photoContainer.setOnMouseExited(e -> {
            changePhotoButton.setVisible(false);
        });
        
        // Also handle hover on the button itself
        changePhotoButton.setOnMouseEntered(e -> {
            changePhotoButton.setVisible(true);
        });
        
        changePhotoButton.setOnMouseExited(e -> {
            // Check if mouse is still over the photo container
            if (!photoContainer.isHover()) {
                changePhotoButton.setVisible(false);
            }
        });
    }

    private void loadUserStats() {
        loadPrayerStatsFromFile();
        loadQuranStatsFromFile();
    }
    
    private void loadPrayerStatsFromFile() {
        try {
            File salahDataFile = new File("salah_data.txt");
            if (!salahDataFile.exists()) {
                updatePrayerStats(0, 5, 0, 0, 0);
                return;
            }
            
            LocalDate today = LocalDate.now();
            int completedPrayers = 0;
            int totalPrayers = 5; // Fajr, Dhuhr, Asr, Maghrib, Isha
            int onTimePrayers = 0;
            int latePrayers = 0;
            int missedPrayers = 0;
            
            try (BufferedReader br = new BufferedReader(new FileReader(salahDataFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(today.toString() + ",")) {
                        // Found today's data
                        String[] parts = line.split(",");
                        for (int i = 1; i < parts.length; i++) {
                            if (parts[i].contains("ON_TIME")) {
                                completedPrayers++;
                                onTimePrayers++;
                            } else if (parts[i].contains("LATE")) {
                                completedPrayers++;
                                latePrayers++;
                            } else if (parts[i].contains("MISSED")) {
                                missedPrayers++;
                            }
                        }
                        break;
                    }
                }
            }
            
            updatePrayerStats(completedPrayers, totalPrayers, onTimePrayers, latePrayers, missedPrayers);
            logger.info("Loaded prayer stats: {}/{} prayers completed today (On Time: {}, Late: {}, Missed: {})", 
                       completedPrayers, totalPrayers, onTimePrayers, latePrayers, missedPrayers);
            
        } catch (Exception e) {
            logger.error("Error loading prayer stats from file", e);
            updatePrayerStats(0, 5, 0, 0, 0); // Default to 0 completed if error
        }
    }
    
    private void updatePrayerStats(int completed, int total, int onTime, int late, int missed) {
        prayerStatsLabel.setText(String.format("Daily Prayers: %d/%d", completed, total));
        prayerDetailsLabel.setText(String.format("On Time: %d | Late: %d | Missed: %d", onTime, late, missed));
        prayerProgress.setProgress((double) completed / total);
    }

    private void loadQuranStatsFromFile() {
        try {
            File quranDataFile = new File("quran_data.txt");
            if (!quranDataFile.exists()) {
                updateQuranStats(0, 10); // Default to 0 pages read, 10 page goal
                return;
            }
            
            LocalDate today = LocalDate.now();
            int pagesRead = 0;
            int dailyGoal = 10; // Default goal
            
            try (BufferedReader br = new BufferedReader(new FileReader(quranDataFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(today.toString() + ",")) {
                        // Found today's data
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            pagesRead = Integer.parseInt(parts[1]);
                            dailyGoal = Integer.parseInt(parts[2]);
                        }
                        break;
                    }
                }
            }
            
            updateQuranStats(pagesRead, dailyGoal);
            logger.info("Loaded Quran stats: {} pages read today, goal: {} pages", pagesRead, dailyGoal);
            
        } catch (Exception e) {
            logger.error("Error loading Quran stats from file", e);
            updateQuranStats(0, 10); // Default to 0 completed if error
        }
    }

    private void updateQuranStats(int pagesRead, int dailyGoal) {
        quranStatsLabel.setText(String.format("Pages Read Today: %d/%d", pagesRead, dailyGoal));
        double progress = dailyGoal > 0 ? (double) pagesRead / dailyGoal : 0.0;
        quranProgress.setProgress(Math.min(progress, 1.0));
    }

    // Static method to refresh Quran stats
    public static void refreshQuranStats() {
        if (mainDashboardController != null) {
            Platform.runLater(() -> mainDashboardController.loadQuranStatsFromFile());
        }
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(profilePhotoView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String photoPath = getProfilePhotoPath();
                Files.createDirectories(Paths.get(PROFILE_PHOTOS_DIR));
                Files.copy(selectedFile.toPath(), Paths.get(photoPath), StandardCopyOption.REPLACE_EXISTING);
                loadProfilePhoto();
                logger.info("Profile photo updated for user: {}", currentUser.getUsername());
            } catch (Exception e) {
                logger.error("Error saving profile photo", e);
                showError("Error", "Failed to save profile photo. Please try again.");
            }
        }
    }

    private String getProfilePhotoPath() {
        return Paths.get(PROFILE_PHOTOS_DIR, currentUser.getUsername() + "_profile.jpg").toString();
    }

    @FXML
    private void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
            Parent settings = loader.load();
            
            SettingsController controller = loader.getController();
            controller.setCurrentUsername(currentUser.getUsername());
            
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");
            settingsStage.setScene(new Scene(settings));
            settingsStage.setMaximized(true);
            settingsStage.show();
            
            logger.info("Settings opened for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening settings", e);
            showError("Error", "Failed to open settings: " + e.getMessage());
        }
    }

    @FXML
    private void handleSalahTracker() {
        logger.info("Salah Tracker button clicked!");
        try {
            logger.info("Loading Salah Tracker FXML...");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/salah_tracker.fxml"));
            Parent salahTracker = loader.load();
            
            logger.info("Salah Tracker FXML loaded successfully, creating stage...");
            Stage salahStage = new Stage();
            salahStage.setTitle("Salah Tracker");
            Scene scene = new Scene(salahTracker);
            scene.getStylesheets().add(getClass().getResource("/styles/salah_tracker.css").toExternalForm());
            salahStage.setScene(scene);
            salahStage.setMaximized(true);
            salahStage.show();
            
            logger.info("Salah tracker opened for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening Salah tracker: {}", e.getMessage(), e);
            showError("Error", "Failed to open Salah tracker: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleQuranTracker() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/quran_tracker.fxml"));
            Parent quranTracker = loader.load();
            
            Stage quranStage = new Stage();
            quranStage.setTitle("Quran Tracker");
            Scene scene = new Scene(quranTracker);
            quranStage.setScene(scene);
            quranStage.setMaximized(true);
            quranStage.show();
            
            logger.info("Quran tracker opened for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening Quran tracker: {}", e.getMessage(), e);
            showError("Error", "Failed to open Quran tracker: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRamadanTracker() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/ramadan_tracker.fxml"));
            Parent ramadanTracker = loader.load();
            
            // Get the controller and set the current user
            RamadanTrackerController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage ramadanStage = new Stage();
            ramadanStage.setTitle("Ramadan Tracker");
            Scene scene = new Scene(ramadanTracker);
            ramadanStage.setScene(scene);
            ramadanStage.setMaximized(true);
            ramadanStage.show();
            
            logger.info("Ramadan tracker opened for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening Ramadan tracker: {}", e.getMessage(), e);
            showError("Error", "Failed to open Ramadan tracker: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleZikrTracker() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/zikr_tracker.fxml"));
            Parent zikrTracker = loader.load();
            
            Stage zikrStage = new Stage();
            zikrStage.setTitle("Zikr Tracker");
            Scene scene = new Scene(zikrTracker);
            zikrStage.setScene(scene);
            zikrStage.setMaximized(true);
            zikrStage.show();
            
            logger.info("Zikr tracker opened for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening Zikr tracker: {}", e.getMessage(), e);
            showError("Error", "Failed to open Zikr tracker: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleTasbihCounter() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/tasbih_counter.fxml"));
            Parent tasbihCounter = loader.load();
            
            // Get the controller and set the current user
            TasbihCounterController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage tasbihStage = new Stage();
            tasbihStage.setTitle("Tasbih Counter");
            Scene scene = new Scene(tasbihCounter);
            tasbihStage.setScene(scene);
            tasbihStage.setMaximized(true);
            tasbihStage.show();
            
            logger.info("Tasbih counter opened for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening Tasbih counter: {}", e.getMessage(), e);
            showError("Error", "Failed to open Tasbih counter: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleFastTracker() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/fast_tracker.fxml"));
            Parent fastTracker = loader.load();
            
            Stage fastStage = new Stage();
            fastStage.setTitle("Fast Tracker");
            Scene scene = new Scene(fastTracker);
            scene.getStylesheets().add(getClass().getResource("/styles/fast_tracker.css").toExternalForm());
            fastStage.setScene(scene);
            fastStage.setMaximized(true);
            fastStage.show();
            
            logger.info("Fast tracker opened for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening Fast tracker: {}", e.getMessage(), e);
            showError("Error", "Failed to open Fast tracker: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleFatwaTracker() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/fatwa_tracker.fxml"));
            Parent fatwaTracker = loader.load();
            
            // Get the controller and set the current user
            FatwaTrackerController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage fatwaStage = new Stage();
            fatwaStage.setTitle("Fatwa Tracker");
            Scene scene = new Scene(fatwaTracker);
            fatwaStage.setScene(scene);
            fatwaStage.setMaximized(true);
            fatwaStage.show();
            
            logger.info("Fatwa tracker opened for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening Fatwa tracker: {}", e.getMessage(), e);
            showError("Error", "Failed to open Fatwa tracker: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCommunityTracker() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/community_tracker.fxml"));
            Parent communityTracker = loader.load();
            
            // Get the controller and set the current user
            CommunityTrackerController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage communityStage = new Stage();
            communityStage.setTitle("Community Tracker");
            Scene scene = new Scene(communityTracker);
            scene.getStylesheets().add(getClass().getResource("/styles/community_tracker.css").toExternalForm());
            communityStage.setScene(scene);
            communityStage.setMaximized(true);
            communityStage.show();
            
            logger.info("Community tracker opened for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening Community tracker: {}", e.getMessage(), e);
            showError("Error", "Failed to open Community tracker: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            // Clean up resources
            cleanup();
            
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(login);
            stage.setScene(scene);
            
            // Set full screen mode
            stage.setFullScreen(true);
            stage.setFullScreenExitHint(""); // Disable the ESC hint message
            
            stage.show();
            logger.info("User logged out successfully: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error during logout", e);
            showError("Error", "Failed to logout. Please try again.");
        }
    }

    @FXML
    private void handleRefreshPrayerStats() {
        loadPrayerStatsFromFile();
        logger.info("Prayer stats manually refreshed");
    }

    @FXML
    private void handleRefreshQuranStats() {
        loadQuranStatsFromFile();
        logger.info("Quran stats manually refreshed");
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.show();
        });
    }

    private void showInfo(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.show();
        });
    }

    private void cleanup() {
        // Stop the date/time timer
        if (dateTimeTimer != null) {
            dateTimeTimer.cancel();
            dateTimeTimer = null;
        }
        
        // Clear static references
        mainDashboardController = null;
        mainDashboardStage = null;
    }
}