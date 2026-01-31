package com.faithapp.controllers;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;

public class ZikrTrackerController {
    private static final Logger logger = LoggerFactory.getLogger(ZikrTrackerController.class);
    
    @FXML private Label timeLabel;
    @FXML private VBox morningZikrContainer;
    @FXML private VBox eveningZikrContainer;
    @FXML private ProgressBar morningProgress;
    @FXML private ProgressBar eveningProgress;
    @FXML private Label morningProgressLabel;
    @FXML private Label eveningProgressLabel;
    @FXML private CheckBox morningCompletedCheck;
    @FXML private CheckBox eveningCompletedCheck;
    @FXML private ComboBox<String> morningReminderTime;
    @FXML private ComboBox<String> eveningReminderTime;
    @FXML private CheckBox morningReminderEnabled;
    @FXML private CheckBox eveningReminderEnabled;
    
    private Timer timer;
    private Preferences prefs;
    private List<ZikrItem> morningZikrs;
    private List<ZikrItem> eveningZikrs;
    
    public void initialize() {
        prefs = Preferences.userNodeForPackage(ZikrTrackerController.class);
        setupClock();
        setupReminderTimes();
        loadSavedPreferences();
        initializeZikrLists();
        displayZikrs();
        setupReminders();
        
        logger.info("ZikrTracker initialized");
    }
    
    private void setupClock() {
        updateTime();
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateTime());
            }
        }, 0, 60000); // Update every minute
    }
    
    private void updateTime() {
        LocalDateTime now = LocalDateTime.now();
        timeLabel.setText(now.format(DateTimeFormatter.ofPattern("HH:mm")));
    }
    
    private void setupReminderTimes() {
        List<String> times = new ArrayList<>();
        LocalTime time = LocalTime.of(0, 0);
        while (!time.equals(LocalTime.of(23, 30))) {
            times.add(time.format(DateTimeFormatter.ofPattern("HH:mm")));
            time = time.plusMinutes(30);
        }
        times.add("23:30");
        
        morningReminderTime.setItems(FXCollections.observableArrayList(times));
        eveningReminderTime.setItems(FXCollections.observableArrayList(times));
        
        morningReminderTime.setValue("05:00");
        eveningReminderTime.setValue("16:00");
    }
    
    private void loadSavedPreferences() {
        morningReminderEnabled.setSelected(prefs.getBoolean("morningReminderEnabled", true));
        eveningReminderEnabled.setSelected(prefs.getBoolean("eveningReminderEnabled", true));
        morningReminderTime.setValue(prefs.get("morningReminderTime", "05:00"));
        eveningReminderTime.setValue(prefs.get("eveningReminderTime", "16:00"));
    }
    
    private void initializeZikrLists() {
        morningZikrs = new ArrayList<>();
        eveningZikrs = new ArrayList<>();
        
        // Morning Zikrs
        morningZikrs.add(new ZikrItem(
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            "Glory and praise be to Allah",
            33
        ));
        morningZikrs.add(new ZikrItem(
            "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            "I seek forgiveness from Allah and turn to Him in repentance",
            33
        ));
        morningZikrs.add(new ZikrItem(
            "لا إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لا شَرِيكَ لَهُ",
            "There is no deity worthy of worship except Allah alone, who has no partner",
            33
        ));
        
        // Evening Zikrs
        eveningZikrs.addAll(morningZikrs); // Same zikrs for evening
    }
    
    private void displayZikrs() {
        displayZikrList(morningZikrs, morningZikrContainer, morningProgress, morningProgressLabel);
        displayZikrList(eveningZikrs, eveningZikrContainer, eveningProgress, eveningProgressLabel);
    }
    
    private void displayZikrList(List<ZikrItem> zikrs, VBox container, ProgressBar progress, Label progressLabel) {
        container.getChildren().clear();
        int totalCompleted = 0;
        
        for (ZikrItem zikr : zikrs) {
            VBox zikrBox = new VBox(5);
            zikrBox.getStyleClass().add("zikr-item");
            
            Label arabicText = new Label(zikr.arabic);
            arabicText.getStyleClass().add("zikr-text");
            
            Label translationText = new Label(zikr.translation);
            translationText.getStyleClass().add("zikr-translation");
            
            Spinner<Integer> countSpinner = new Spinner<>(0, zikr.targetCount, 0);
            countSpinner.setEditable(true);
            countSpinner.valueProperty().addListener((obs, old, newValue) -> {
                zikr.currentCount = newValue;
                updateProgress(zikrs, progress, progressLabel);
            });
            
            Label countLabel = new Label("Target: " + zikr.targetCount);
            countLabel.getStyleClass().add("zikr-count");
            
            zikrBox.getChildren().addAll(arabicText, translationText, countLabel, countSpinner);
            container.getChildren().add(zikrBox);
            
            if (zikr.currentCount >= zikr.targetCount) {
                totalCompleted++;
            }
        }
        
        updateProgress(zikrs, progress, progressLabel);
    }
    
    private void updateProgress(List<ZikrItem> zikrs, ProgressBar progress, Label progressLabel) {
        int totalCompleted = 0;
        int totalTargets = 0;
        
        for (ZikrItem zikr : zikrs) {
            totalCompleted += Math.min(zikr.currentCount, zikr.targetCount);
            totalTargets += zikr.targetCount;
        }
        
        double progressValue = (double) totalCompleted / totalTargets;
        progress.setProgress(progressValue);
        progressLabel.setText(String.format("Progress: %d/%d", totalCompleted, totalTargets));
    }
    
    @FXML
    private void handleSaveReminders() {
        prefs.putBoolean("morningReminderEnabled", morningReminderEnabled.isSelected());
        prefs.putBoolean("eveningReminderEnabled", eveningReminderEnabled.isSelected());
        prefs.put("morningReminderTime", morningReminderTime.getValue());
        prefs.put("eveningReminderTime", eveningReminderTime.getValue());
        
        setupReminders();
        showInfo("Success", "Reminder settings saved successfully!");
    }
    
    private void setupReminders() {
        if (morningReminderEnabled.isSelected()) {
            scheduleReminder("Morning Zikr Time", morningReminderTime.getValue());
        }
        if (eveningReminderEnabled.isSelected()) {
            scheduleReminder("Evening Zikr Time", eveningReminderTime.getValue());
        }
    }
    
    private void scheduleReminder(String title, String time) {
        LocalTime reminderTime = LocalTime.parse(time);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextReminder = LocalDateTime.of(now.toLocalDate(), reminderTime);
        
        if (now.toLocalTime().isAfter(reminderTime)) {
            nextReminder = nextReminder.plusDays(1);
        }
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> showReminder(title));
            }
        }, Date.from(nextReminder.atZone(ZoneId.systemDefault()).toInstant()), 24 * 60 * 60 * 1000); // Repeat daily
    }
    
    private void showReminder(String title) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Zikr Reminder");
            alert.setHeaderText(title);
            alert.setContentText("It's time for your daily Zikr. Open the Zikr Tracker to begin.");
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
    
    private static class ZikrItem {
        String arabic;
        String translation;
        int targetCount;
        int currentCount;
        
        ZikrItem(String arabic, String translation, int targetCount) {
            this.arabic = arabic;
            this.translation = translation;
            this.targetCount = targetCount;
            this.currentCount = 0;
        }
    }
    
    @FXML
    private void handleBackToDashboard() {
        try {
            javafx.stage.Stage currentStage = (javafx.stage.Stage) timeLabel.getScene().getWindow();
            currentStage.close();
            
            // Return to the original dashboard window
            javafx.stage.Stage mainDashboard = com.faithapp.controllers.DashboardController.getMainDashboardStage();
            if (mainDashboard != null) {
                mainDashboard.show();
                mainDashboard.toFront();
                logger.info("Returned to original dashboard from Zikr Tracker");
            } else {
                logger.warn("Main dashboard stage not found, creating new dashboard");
                // Fallback: create new dashboard if main reference is lost
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                javafx.scene.Parent dashboard = loader.load();
                
                javafx.stage.Stage dashboardStage = new javafx.stage.Stage();
                dashboardStage.setTitle("ImanConnect - Dashboard");
                dashboardStage.setScene(new javafx.scene.Scene(dashboard));
                dashboardStage.setMaximized(true);
                dashboardStage.show();
            }
        } catch (Exception e) {
            logger.error("Error returning to dashboard: {}", e.getMessage(), e);
            showError("Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleViewSummary() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/zikr_summary.fxml"));
            javafx.scene.Parent summaryRoot = loader.load();
            ZikrSummaryController summaryController = loader.getController();
            if (this.getClass().getDeclaredField("currentUser") != null) {
                java.lang.reflect.Field userField = this.getClass().getDeclaredField("currentUser");
                userField.setAccessible(true);
                Object user = userField.get(this);
                summaryController.setCurrentUser((com.faithapp.models.User) user);
            }
            javafx.stage.Stage summaryStage = new javafx.stage.Stage();
            summaryStage.setTitle("Zikr Summary");
            summaryStage.setScene(new javafx.scene.Scene(summaryRoot));
            summaryStage.setMaximized(true);
            summaryStage.show();
        } catch (Exception e) {
            logger.error("Error opening Zikr summary: {}", e.getMessage(), e);
            showError("Error", "Failed to open Zikr summary: " + e.getMessage());
        }
    }
} 