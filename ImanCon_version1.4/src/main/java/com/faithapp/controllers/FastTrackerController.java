package com.faithapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;

public class FastTrackerController {
    private static final Logger logger = LoggerFactory.getLogger(FastTrackerController.class);

    @FXML private DatePicker datePicker;
    @FXML private RadioButton fastCompletedRadio;
    @FXML private RadioButton fastMissedRadio;
    @FXML private RadioButton fastExemptRadio;
    @FXML private TextArea notesArea;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    @FXML private Label totalFastsLabel;
    @FXML private Label completedFastsLabel;
    @FXML private Label missedFastsLabel;
    @FXML private Label exemptFastsLabel;
    @FXML private ProgressBar monthlyProgress;
    
    // New labels for the redesigned UI
    @FXML private Label successRateLabel;
    @FXML private Label totalFastsLabel2;
    @FXML private Label completedFastsLabel2;
    @FXML private Label missedFastsLabel2;
    @FXML private Label exemptFastsLabel2;
    @FXML private Label monthlyProgressLabel;
    @FXML private Label monthlyPercentageLabel;

    private ToggleGroup fastStatus;
    private int totalFasts = 0;
    private int completedFasts = 0;
    private int missedFasts = 0;
    private int exemptFasts = 0;

    @FXML
    public void initialize() {
        try {
            // Initialize toggle group
            fastStatus = new ToggleGroup();
            fastCompletedRadio.setToggleGroup(fastStatus);
            fastMissedRadio.setToggleGroup(fastStatus);
            fastExemptRadio.setToggleGroup(fastStatus);
            
            // Set default date to today
            datePicker.setValue(LocalDate.now());
            
            // Load initial data
            loadFastStatus(LocalDate.now());
            
            // Defer statistics update to ensure all UI elements are injected
            Platform.runLater(() -> {
                try {
                    updateStatistics();
                    logger.info("Fast Tracker initialized successfully");
                } catch (Exception e) {
                    logger.error("Error updating statistics: {}", e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            logger.error("Error initializing Fast Tracker: {}", e.getMessage(), e);
        }
    }

    @FXML
    private void handleSave() {
        LocalDate selectedDate = datePicker.getValue();
        RadioButton selectedStatus = (RadioButton) fastStatus.getSelectedToggle();
        String notes = notesArea.getText();

        if (selectedStatus == null) {
            showError("Error", "Please select a fast status");
            return;
        }

        // TODO: Save to database
        saveFastStatus(selectedDate, selectedStatus.getText(), notes);
        updateStatistics();
        showInfo("Success", "Fast status saved successfully");
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBackToDashboard() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleViewDetailedReport() {
        StringBuilder report = new StringBuilder();
        report.append("📊 FASTING DETAILED REPORT\n");
        report.append("========================\n\n");
        report.append("📈 Overall Statistics:\n");
        report.append("• Total Fasts: ").append(totalFasts).append("\n");
        report.append("• Completed: ").append(completedFasts).append("\n");
        report.append("• Missed: ").append(missedFasts).append("\n");
        report.append("• Exempt: ").append(exemptFasts).append("\n");
        report.append("• Success Rate: ").append(String.format("%.1f%%", 
            totalFasts > 0 ? (double) completedFasts / totalFasts * 100 : 0)).append("\n\n");
        
        report.append("🎯 Monthly Progress:\n");
        report.append("• This Month: ").append(completedFasts).append("/30\n");
        report.append("• Monthly Goal: 30 fasts\n");
        report.append("• Progress: ").append(String.format("%.1f%%", 
            (double) completedFasts / 30 * 100)).append("\n\n");
        
        report.append("💡 Recommendations:\n");
        if (completedFasts < 15) {
            report.append("• Try to increase your fasting consistency\n");
            report.append("• Set smaller daily goals to build momentum\n");
        } else if (completedFasts < 25) {
            report.append("• Good progress! Keep up the consistency\n");
            report.append("• Consider adding more voluntary fasts\n");
        } else {
            report.append("• Excellent fasting record! MashaAllah!\n");
            report.append("• Consider mentoring others in fasting\n");
        }

        showDetailedReport("Fasting Detailed Report", report.toString());
    }

    @FXML
    private void handleSetMonthlyGoal() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(30));
        dialog.setTitle("Set Monthly Goal");
        dialog.setHeaderText("Set Your Monthly Fasting Goal");
        dialog.setContentText("Enter number of fasts to complete this month:");

        dialog.showAndWait().ifPresent(goal -> {
            try {
                int monthlyGoal = Integer.parseInt(goal);
                if (monthlyGoal > 0 && monthlyGoal <= 31) {
                    // TODO: Save to database
                    showInfo("Goal Set", "Monthly goal set to " + monthlyGoal + " fasts");
                    updateMonthlyGoal(monthlyGoal);
                } else {
                    showError("Invalid Goal", "Please enter a number between 1 and 31");
                }
            } catch (NumberFormatException e) {
                showError("Invalid Input", "Please enter a valid number");
            }
        });
    }

    @FXML
    private void handleExportData() {
        StringBuilder exportData = new StringBuilder();
        exportData.append("Fast Tracker Export Data\n");
        exportData.append("Generated: ").append(LocalDate.now()).append("\n\n");
        exportData.append("Total Fasts: ").append(totalFasts).append("\n");
        exportData.append("Completed: ").append(completedFasts).append("\n");
        exportData.append("Missed: ").append(missedFasts).append("\n");
        exportData.append("Exempt: ").append(exemptFasts).append("\n");
        exportData.append("Success Rate: ").append(String.format("%.1f%%", 
            totalFasts > 0 ? (double) completedFasts / totalFasts * 100 : 0)).append("\n");

        // Create a text area to show the export data
        TextArea textArea = new TextArea(exportData.toString());
        textArea.setEditable(false);
        textArea.setPrefRowCount(15);
        textArea.setPrefColumnCount(50);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Data");
        alert.setHeaderText("Your Fasting Data");
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void loadFastStatus(LocalDate date) {
        // TODO: Load from database
        // For now, just reset the form
        fastStatus.selectToggle(null);
        notesArea.clear();
    }

    private void saveFastStatus(LocalDate date, String status, String notes) {
        // TODO: Implement database save
        // For now, just update counters
        switch (status) {
            case "✅ Completed Fast":
                completedFasts++;
                break;
            case "❌ Missed Fast":
                missedFasts++;
                break;
            case "🏥 Exempt (Health/Other)":
                exemptFasts++;
                break;
        }
        totalFasts++;
        updateStatistics();
    }

    private void updateStatistics() {
        // Update all labels with null checks
        if (totalFastsLabel != null) totalFastsLabel.setText(String.valueOf(totalFasts));
        if (completedFastsLabel != null) completedFastsLabel.setText(String.valueOf(completedFasts));
        if (missedFastsLabel != null) missedFastsLabel.setText(String.valueOf(missedFasts));
        if (exemptFastsLabel != null) exemptFastsLabel.setText(String.valueOf(exemptFasts));
        
        // Update duplicate labels
        if (totalFastsLabel2 != null) totalFastsLabel2.setText(String.valueOf(totalFasts));
        if (completedFastsLabel2 != null) completedFastsLabel2.setText(String.valueOf(completedFasts));
        if (missedFastsLabel2 != null) missedFastsLabel2.setText(String.valueOf(missedFasts));
        if (exemptFastsLabel2 != null) exemptFastsLabel2.setText(String.valueOf(exemptFasts));

        // Calculate and update success rate
        double successRate = totalFasts > 0 ? (double) completedFasts / totalFasts * 100 : 0;
        if (successRateLabel != null) {
            successRateLabel.setText(String.format("%.1f%%", successRate));
        }

        // Update progress bar
        double progress = totalFasts > 0 ? (double) completedFasts / totalFasts : 0;
        if (monthlyProgress != null) {
            monthlyProgress.setProgress(progress);
        }
        
        // Update monthly progress labels
        if (monthlyProgressLabel != null) {
            monthlyProgressLabel.setText(completedFasts + "/30");
        }
        if (monthlyPercentageLabel != null) {
            monthlyPercentageLabel.setText(String.format("%.1f%%", successRate));
        }
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

    private void showDetailedReport(String title, String content) {
        Platform.runLater(() -> {
            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setPrefRowCount(20);
            textArea.setPrefColumnCount(60);
            textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText("Your Detailed Fasting Report");
            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
        });
    }

    private void updateMonthlyGoal(int goal) {
        // TODO: Save monthly goal to database
        // For now, just log it
        logger.info("Monthly goal updated to: {} fasts", goal);
    }
} 