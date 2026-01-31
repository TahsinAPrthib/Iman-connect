package com.faithapp.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class SalahTrackerController {
    private static final Logger logger = LoggerFactory.getLogger(SalahTrackerController.class);
    
    @FXML private Button backButton;
    @FXML private Label dateLabel;
    @FXML private Label completionRateLabel;
    
    // Prayer time labels
    @FXML private Label fajrTimeLabel;
    @FXML private Label dhuhrTimeLabel;
    @FXML private Label asrTimeLabel;
    @FXML private Label maghribTimeLabel;
    @FXML private Label ishaTimeLabel;
    
    // Prayer status radio buttons
    @FXML private RadioButton fajrOnTimeRadio;
    @FXML private RadioButton fajrLateRadio;
    @FXML private RadioButton fajrMissedRadio;
    
    @FXML private RadioButton dhuhrOnTimeRadio;
    @FXML private RadioButton dhuhrLateRadio;
    @FXML private RadioButton dhuhrMissedRadio;
    
    @FXML private RadioButton asrOnTimeRadio;
    @FXML private RadioButton asrLateRadio;
    @FXML private RadioButton asrMissedRadio;
    
    @FXML private RadioButton maghribOnTimeRadio;
    @FXML private RadioButton maghribLateRadio;
    @FXML private RadioButton maghribMissedRadio;
    
    @FXML private RadioButton ishaOnTimeRadio;
    @FXML private RadioButton ishaLateRadio;
    @FXML private RadioButton ishaMissedRadio;
    
    // Status labels
    @FXML private Label fajrStatusLabel;
    @FXML private Label dhuhrStatusLabel;
    @FXML private Label asrStatusLabel;
    @FXML private Label maghribStatusLabel;
    @FXML private Label ishaStatusLabel;
    
    // Stats labels
    @FXML private Label onTimeCountLabel;
    @FXML private Label lateCountLabel;
    @FXML private Label missedCountLabel;
    @FXML private ProgressBar dailyProgress;
    
    private Map<String, LocalTime> prayerTimes;
    private Map<String, ToggleGroup> prayerGroups;
    
    private static final String SALAH_DATA_FILE = "salah_data.txt";
    
    public void initialize() {
        try {
            setupDate();
            setupPrayerTimes();
            setupRadioButtonGroups();
            setupStatusListeners();
            loadTodaysPrayers();
            updateDailyStats();
        } catch (Exception e) {
            logger.error("Error initializing Salah Tracker: {}", e.getMessage(), e);
        }
    }
    
    private void setupDate() {
        if (dateLabel != null) {
            dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        }
    }
    
    private void setupPrayerTimes() {
        // TODO: Integrate with actual prayer times API
        prayerTimes = new HashMap<>();
        prayerTimes.put("Fajr", LocalTime.of(5, 30));
        prayerTimes.put("Dhuhr", LocalTime.of(13, 30));
        prayerTimes.put("Asr", LocalTime.of(16, 30));
        prayerTimes.put("Maghrib", LocalTime.of(19, 30));
        prayerTimes.put("Isha", LocalTime.of(21, 0));
        
        updatePrayerTimeLabels();
    }
    
    private void updatePrayerTimeLabels() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        if (fajrTimeLabel != null) fajrTimeLabel.setText(prayerTimes.get("Fajr").format(formatter));
        if (dhuhrTimeLabel != null) dhuhrTimeLabel.setText(prayerTimes.get("Dhuhr").format(formatter));
        if (asrTimeLabel != null) asrTimeLabel.setText(prayerTimes.get("Asr").format(formatter));
        if (maghribTimeLabel != null) maghribTimeLabel.setText(prayerTimes.get("Maghrib").format(formatter));
        if (ishaTimeLabel != null) ishaTimeLabel.setText(prayerTimes.get("Isha").format(formatter));
    }
    
    private void setupRadioButtonGroups() {
        prayerGroups = new HashMap<>();
        
        // Create toggle groups for each prayer
        setupPrayerGroup("Fajr", fajrOnTimeRadio, fajrLateRadio, fajrMissedRadio);
        setupPrayerGroup("Dhuhr", dhuhrOnTimeRadio, dhuhrLateRadio, dhuhrMissedRadio);
        setupPrayerGroup("Asr", asrOnTimeRadio, asrLateRadio, asrMissedRadio);
        setupPrayerGroup("Maghrib", maghribOnTimeRadio, maghribLateRadio, maghribMissedRadio);
        setupPrayerGroup("Isha", ishaOnTimeRadio, ishaLateRadio, ishaMissedRadio);
    }
    
    private void setupPrayerGroup(String prayer, RadioButton... radioButtons) {
        ToggleGroup group = new ToggleGroup();
        for (RadioButton radioButton : radioButtons) {
            radioButton.setToggleGroup(group);
            radioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    updatePrayerStatus(prayer);
                    updateDailyStats();
                }
            });
        }
        prayerGroups.put(prayer, group);
    }
    
    private void setupStatusListeners() {
        // Add status update listeners for each prayer
        setupPrayerStatusListener("Fajr", fajrStatusLabel, fajrOnTimeRadio, fajrLateRadio, fajrMissedRadio);
        setupPrayerStatusListener("Dhuhr", dhuhrStatusLabel, dhuhrOnTimeRadio, dhuhrLateRadio, dhuhrMissedRadio);
        setupPrayerStatusListener("Asr", asrStatusLabel, asrOnTimeRadio, asrLateRadio, asrMissedRadio);
        setupPrayerStatusListener("Maghrib", maghribStatusLabel, maghribOnTimeRadio, maghribLateRadio, maghribMissedRadio);
        setupPrayerStatusListener("Isha", ishaStatusLabel, ishaOnTimeRadio, ishaLateRadio, ishaMissedRadio);
    }
    
    private void setupPrayerStatusListener(String prayer, Label statusLabel, RadioButton onTime, RadioButton late, RadioButton missed) {
        ChangeListener<Boolean> listener = (obs, oldVal, newVal) -> {
            if (newVal) {
                updatePrayerStatus(prayer);
            }
        };
        
        onTime.selectedProperty().addListener(listener);
        late.selectedProperty().addListener(listener);
        missed.selectedProperty().addListener(listener);
    }
    
    private void updatePrayerStatus(String prayer) {
        Label statusLabel = getPrayerStatusLabel(prayer);
        RadioButton onTimeRadio = getPrayerOnTimeRadio(prayer);
        RadioButton lateRadio = getPrayerLateRadio(prayer);
        RadioButton missedRadio = getPrayerMissedRadio(prayer);
        
        if (onTimeRadio.isSelected()) {
            statusLabel.setText("✅ Completed on time!");
            statusLabel.setStyle("-fx-text-fill: #4CAF50;"); // Green
        } else if (lateRadio.isSelected()) {
            statusLabel.setText("⏰ Completed late");
            statusLabel.setStyle("-fx-text-fill: #FF9800;"); // Orange
        } else if (missedRadio.isSelected()) {
            statusLabel.setText("❌ Missed");
            statusLabel.setStyle("-fx-text-fill: #F44336;"); // Red
        } else {
            statusLabel.setText("");
        }
        
        saveAllPrayerStatusForToday();
    }
    
    private void updateDailyStats() {
        int onTime = countSelectedRadioButtons(fajrOnTimeRadio, dhuhrOnTimeRadio, asrOnTimeRadio, maghribOnTimeRadio, ishaOnTimeRadio);
        int late = countSelectedRadioButtons(fajrLateRadio, dhuhrLateRadio, asrLateRadio, maghribLateRadio, ishaLateRadio);
        int missed = countSelectedRadioButtons(fajrMissedRadio, dhuhrMissedRadio, asrMissedRadio, maghribMissedRadio, ishaMissedRadio);
        
        if (onTimeCountLabel != null) onTimeCountLabel.setText(String.valueOf(onTime));
        if (lateCountLabel != null) lateCountLabel.setText(String.valueOf(late));
        if (missedCountLabel != null) missedCountLabel.setText(String.valueOf(missed));
        
        int total = onTime + late + missed;
        double completionRate = total > 0 ? (double) (onTime + late) / 5 * 100 : 0;
        if (completionRateLabel != null) completionRateLabel.setText(String.format("%.0f%%", completionRate));
        
        if (dailyProgress != null) dailyProgress.setProgress(total > 0 ? (double) (onTime + late) / 5 : 0);
    }
    
    private int countSelectedRadioButtons(RadioButton... radioButtons) {
        int count = 0;
        for (RadioButton radioButton : radioButtons) {
            if (radioButton != null && radioButton.isSelected()) {
                count++;
            }
        }
        return count;
    }
    
    private void loadTodaysPrayers() {
        loadPrayersForDate(LocalDate.now());
    }
    
    private void loadPrayersForDate(LocalDate date) {
        // TODO: Load actual prayer data from database
        if (date.equals(LocalDate.now())) {
            // Reset all checkboxes for today
            resetAllRadioButtons();
        } else {
            // Load historical data
            // This would normally come from a database
            simulateHistoricalData();
        }
        updateDailyStats();
    }
    
    private void resetAllRadioButtons() {
        RadioButton[] allRadioButtons = {
            fajrOnTimeRadio, fajrLateRadio, fajrMissedRadio,
            dhuhrOnTimeRadio, dhuhrLateRadio, dhuhrMissedRadio,
            asrOnTimeRadio, asrLateRadio, asrMissedRadio,
            maghribOnTimeRadio, maghribLateRadio, maghribMissedRadio,
            ishaOnTimeRadio, ishaLateRadio, ishaMissedRadio
        };
        
        for (RadioButton radioButton : allRadioButtons) {
            radioButton.setSelected(false);
        }
    }
    
    private void simulateHistoricalData() {
        // This is just for demonstration
        // In reality, this would load from a database
        resetAllRadioButtons();
        fajrOnTimeRadio.setSelected(true);
        dhuhrOnTimeRadio.setSelected(true);
        asrLateRadio.setSelected(true);
        maghribOnTimeRadio.setSelected(true);
        ishaOnTimeRadio.setSelected(true);
    }
    
    private void saveAllPrayerStatusForToday() {
        LocalDate today = LocalDate.now();
        StringBuilder sb = new StringBuilder();
        sb.append(today).append(",");
        sb.append("Fajr:").append(getPrayerStatus("Fajr")).append(",");
        sb.append("Dhuhr:").append(getPrayerStatus("Dhuhr")).append(",");
        sb.append("Asr:").append(getPrayerStatus("Asr")).append(",");
        sb.append("Maghrib:").append(getPrayerStatus("Maghrib")).append(",");
        sb.append("Isha:").append(getPrayerStatus("Isha"));
        List<String> lines = new ArrayList<>();
        boolean replaced = false;
        // Read existing lines, replace today's if exists
        File file = new File(SALAH_DATA_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(today.toString() + ",")) {
                        lines.add(sb.toString());
                        replaced = true;
                    } else {
                        lines.add(line);
                    }
                }
            } catch (IOException e) { /* ignore */ }
        }
        if (!replaced) lines.add(sb.toString());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String l : lines) bw.write(l + "\n");
        } catch (IOException e) { /* ignore */ }
    }
    
    private String getPrayerStatus(String prayer) {
        RadioButton onTimeRadio = getPrayerOnTimeRadio(prayer);
        RadioButton lateRadio = getPrayerLateRadio(prayer);
        RadioButton missedRadio = getPrayerMissedRadio(prayer);
        
        if (onTimeRadio.isSelected()) return "ON_TIME";
        if (lateRadio.isSelected()) return "LATE";
        if (missedRadio.isSelected()) return "MISSED";
        return "NOT_RECORDED";
    }
    
    private Label getPrayerStatusLabel(String prayer) {
        switch (prayer) {
            case "Fajr": return fajrStatusLabel;
            case "Dhuhr": return dhuhrStatusLabel;
            case "Asr": return asrStatusLabel;
            case "Maghrib": return maghribStatusLabel;
            case "Isha": return ishaStatusLabel;
            default: throw new IllegalArgumentException("Invalid prayer: " + prayer);
        }
    }
    
    private RadioButton getPrayerOnTimeRadio(String prayer) {
        switch (prayer) {
            case "Fajr": return fajrOnTimeRadio;
            case "Dhuhr": return dhuhrOnTimeRadio;
            case "Asr": return asrOnTimeRadio;
            case "Maghrib": return maghribOnTimeRadio;
            case "Isha": return ishaOnTimeRadio;
            default: throw new IllegalArgumentException("Invalid prayer: " + prayer);
        }
    }
    
    private RadioButton getPrayerLateRadio(String prayer) {
        switch (prayer) {
            case "Fajr": return fajrLateRadio;
            case "Dhuhr": return dhuhrLateRadio;
            case "Asr": return asrLateRadio;
            case "Maghrib": return maghribLateRadio;
            case "Isha": return ishaLateRadio;
            default: throw new IllegalArgumentException("Invalid prayer: " + prayer);
        }
    }
    
    private RadioButton getPrayerMissedRadio(String prayer) {
        switch (prayer) {
            case "Fajr": return fajrMissedRadio;
            case "Dhuhr": return dhuhrMissedRadio;
            case "Asr": return asrMissedRadio;
            case "Maghrib": return maghribMissedRadio;
            case "Isha": return ishaMissedRadio;
            default: throw new IllegalArgumentException("Invalid prayer: " + prayer);
        }
    }

    @FXML
    private void handleShowSummary() {
        Map<String, int[]> summary = getSalahSummary();
        StringBuilder message = new StringBuilder();
        message.append("📊 SALAH SUMMARY\n");
        message.append("================\n\n");
        
        for (Map.Entry<String, int[]> entry : summary.entrySet()) {
            message.append(entry.getKey()).append(": ").append(formatSummary(entry.getValue())).append("\n");
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Salah Summary");
        alert.setHeaderText("Your Prayer Statistics");
        alert.setContentText(message.toString());
        alert.showAndWait();
    }

    private String formatSummary(int[] arr) {
        return String.format("%d/%d/%d", arr[0], arr[1], arr[2]);
    }

    private Map<String, int[]> getSalahSummary() {
        Map<String, int[]> result = new HashMap<>();
        int[] today = new int[3];
        int[] week = new int[3];
        int[] month = new int[3];
        LocalDate now = LocalDate.now();
        try (BufferedReader br = new BufferedReader(new FileReader(SALAH_DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                LocalDate date = LocalDate.parse(parts[0]);
                int[] counts = countStats(parts);
                if (date.equals(now)) addArr(today, counts);
                if (!date.isAfter(now) && !date.isBefore(now.minusDays(6))) addArr(week, counts);
                if (!date.isAfter(now) && !date.isBefore(now.minusDays(29))) addArr(month, counts);
            }
        } catch (IOException e) { /* ignore if file doesn't exist */ }
        result.put("today", today);
        result.put("week", week);
        result.put("month", month);
        return result;
    }

    private int[] countStats(String[] parts) {
        int[] arr = new int[3]; // [onTime, late, missed]
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].contains("ON_TIME")) arr[0]++;
            else if (parts[i].contains("LATE")) arr[1]++;
            else if (parts[i].contains("MISSED")) arr[2]++;
        }
        return arr;
    }

    private void addArr(int[] base, int[] add) {
        for (int i = 0; i < 3; i++) base[i] += add[i];
    }

    @FXML
    private void handleSaveNow() {
        saveAllPrayerStatusForToday();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Saved");
        alert.setHeaderText(null);
        alert.setContentText("Today's prayer status has been saved.");
        alert.showAndWait();
    }

    @FXML
    private void handleOpenSummaryPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SalahSummary.fxml"));
            Parent summaryPage = loader.load();
            
            Stage summaryStage = new Stage();
            summaryStage.setTitle("Salah Summary");
            summaryStage.setScene(new Scene(summaryPage));
            summaryStage.setMaximized(true);
            summaryStage.show();
            
            logger.info("Salah summary page opened");
        } catch (Exception e) {
            logger.error("Error opening salah summary page: {}", e.getMessage(), e);
            showError("Error", "Failed to open summary page: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBackToDashboard() {
        try {
            Stage currentStage = (Stage) dateLabel.getScene().getWindow();
            currentStage.close();
            
            // Return to the original dashboard window
            Stage mainDashboard = com.faithapp.controllers.DashboardController.getMainDashboardStage();
            if (mainDashboard != null) {
                // Refresh prayer stats before showing dashboard
                com.faithapp.controllers.DashboardController.refreshPrayerStats();
                
                mainDashboard.show();
                mainDashboard.toFront();
                logger.info("Returned to original dashboard from Salah Tracker with refreshed stats");
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
    
    // New UI Methods for Enhanced Features
    
    @FXML
    private void handleResetToday() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Today's Prayers");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will clear all prayer statuses for today. This action cannot be undone.");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                resetAllRadioButtons();
                updateDailyStats();
                clearTodaysData();
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Reset Complete");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Today's prayer data has been reset successfully.");
                successAlert.showAndWait();
                
                logger.info("Reset today's prayer data");
            }
        });
    }
    
    @FXML
    private void handleChangeDate() {
        TextInputDialog dialog = new TextInputDialog(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dialog.setTitle("Change Date");
        dialog.setHeaderText("Enter a date to view");
        dialog.setContentText("Date (YYYY-MM-DD):");
        
        dialog.showAndWait().ifPresent(dateString -> {
            try {
                LocalDate selectedDate = LocalDate.parse(dateString);
                dateLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
                loadPrayersForDate(selectedDate);
                
                logger.info("Changed to date: {}", selectedDate);
            } catch (Exception e) {
                showError("Invalid Date", "Please enter a valid date in YYYY-MM-DD format.");
                logger.error("Invalid date entered: {}", dateString);
            }
        });
    }
    
    @FXML
    private void handleViewHistory() {
        try {
            // Create a simple history view
            StringBuilder history = new StringBuilder();
            history.append("📊 Prayer History (Last 7 Days)\n\n");
            
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                String dateStr = date.format(DateTimeFormatter.ofPattern("MMM dd"));
                String dayName = date.getDayOfWeek().toString().substring(0, 3);
                
                // Get prayer data for this date
                Map<String, int[]> summary = getSalahSummaryForDate(date);
                int completed = summary.get("completed")[0];
                int total = 5;
                
                String status = completed == total ? "✅" : completed > 0 ? "⚠️" : "❌";
                history.append(String.format("%s %s (%s): %d/%d prayers\n", 
                    status, dateStr, dayName, completed, total));
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Prayer History");
            alert.setHeaderText("Last 7 Days Overview");
            alert.setContentText(history.toString());
            alert.showAndWait();
            
            logger.info("Displayed prayer history");
        } catch (Exception e) {
            logger.error("Error displaying history: {}", e.getMessage(), e);
            showError("Error", "Failed to load prayer history: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSetGoals() {
        TextInputDialog dialog = new TextInputDialog("5");
        dialog.setTitle("Set Daily Prayer Goal");
        dialog.setHeaderText("Set your daily prayer completion goal");
        dialog.setContentText("Target prayers per day (1-5):");
        
        dialog.showAndWait().ifPresent(goalString -> {
            try {
                int goal = Integer.parseInt(goalString);
                if (goal >= 1 && goal <= 5) {
                    // In a real app, this would save to user preferences
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Goal Set");
                    alert.setHeaderText(null);
                    alert.setContentText("Daily prayer goal set to " + goal + " prayers per day.");
                    alert.showAndWait();
                    
                    logger.info("Set daily prayer goal to: {}", goal);
                } else {
                    showError("Invalid Goal", "Please enter a number between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                showError("Invalid Input", "Please enter a valid number.");
            }
        });
    }
    
    @FXML
    private void handleAutoSave() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Auto Save");
        alert.setHeaderText("Auto Save Feature");
        alert.setContentText("Auto save is currently enabled. Your prayer data is automatically saved whenever you make changes.");
        alert.showAndWait();
        
        logger.info("Auto save info displayed");
    }
    
    @FXML
    private void handleExportData() {
        try {
            // Create export data
            StringBuilder exportData = new StringBuilder();
            exportData.append("Prayer Tracker Export\n");
            exportData.append("Generated: ").append(LocalDate.now()).append("\n\n");
            
            // Add current day's data
            exportData.append("Today's Prayers:\n");
            String[] prayers = {"Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"};
            for (String prayer : prayers) {
                String status = getPrayerStatus(prayer);
                exportData.append(String.format("%s: %s\n", prayer, status));
            }
            
            // Add summary
            Map<String, int[]> summary = getSalahSummary();
            exportData.append("\nSummary:\n");
            exportData.append("Today: ").append(formatSummary(summary.get("today"))).append("\n");
            exportData.append("This Week: ").append(formatSummary(summary.get("week"))).append("\n");
            exportData.append("This Month: ").append(formatSummary(summary.get("month"))).append("\n");
            
            // Save to file
            String fileName = "prayer_export_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(exportData.toString());
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Complete");
            alert.setHeaderText(null);
            alert.setContentText("Prayer data exported to: " + fileName);
            alert.showAndWait();
            
            logger.info("Exported prayer data to: {}", fileName);
        } catch (Exception e) {
            logger.error("Error exporting data: {}", e.getMessage(), e);
            showError("Export Error", "Failed to export data: " + e.getMessage());
        }
    }
    
    // Helper methods for new features
    
    private void clearTodaysData() {
        File file = new File(SALAH_DATA_FILE);
        if (file.exists()) {
            try {
                List<String> lines = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!line.startsWith(LocalDate.now().toString() + ",")) {
                            lines.add(line);
                        }
                    }
                }
                
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                    for (String l : lines) {
                        bw.write(l + "\n");
                    }
                }
            } catch (IOException e) {
                logger.error("Error clearing today's data: {}", e.getMessage(), e);
            }
        }
    }
    
    private Map<String, int[]> getSalahSummaryForDate(LocalDate date) {
        Map<String, int[]> result = new HashMap<>();
        int[] completed = new int[3]; // [completed, late, missed]
        
        try (BufferedReader br = new BufferedReader(new FileReader(SALAH_DATA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(date.toString() + ",")) {
                    String[] parts = line.split(",");
                    completed = countStats(parts);
                    break;
                }
            }
        } catch (IOException e) {
            // File doesn't exist or can't be read
        }
        
        result.put("completed", completed);
        return result;
    }
} 