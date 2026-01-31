package com.faithapp.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.User;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TasbihCounterController {
    private static final Logger logger = LoggerFactory.getLogger(TasbihCounterController.class);
    private static final String TASBIH_DATA_FILE = "tasbih_data.txt";

    @FXML private Label arabicDhikrLabel;
    @FXML private Label englishDhikrLabel;
    @FXML private Label banglaTranslationLabel;
    @FXML private Label counterLabel;
    @FXML private Label cycleLabel;
    @FXML private Label totalCountLabel;
    @FXML private Label totalCyclesLabel;
    @FXML private Button countButton;
    @FXML private Button resetButton;
    @FXML private Button backButton;
    @FXML private Button saveButton;
    @FXML private Button showSummaryButton;
    @FXML private Button fullSummaryButton;
    @FXML private ProgressBar progressBar;
    @FXML private Circle counterCircle;
    @FXML private ComboBox<String> dhikrComboBox;
    @FXML private TextArea notesTextArea;
    @FXML private DatePicker entryDatePicker;
    
    private int count = 0;
    private int cycles = 0;
    private int totalCount = 0;
    private static final int MILESTONE = 33;
    
    private User currentUser;

    private String[][] dhikrList = {
        {"سُبْحَانَ اللّهِ", "SubhanAllah", "আল্লাহ পবিত্র মহান"},
        {"ٱلْحَمْدُ لِلَّٰهِ", "Alhamdulillah", "সমস্ত প্রশংসা আল্লাহর জন্য"},
        {"اللَّهُ أَكْبَرُ", "Allahu Akbar", "আল্লাহ সর্বশ্রেষ্ঠ"},
        {"لَا إِلَٰهَ إِلَّا ٱللَّٰهُ", "La ilaha illallah", "আল্লাহ ছাড়া কোন উপাস্য নেই"},
        {"لا حول ولا قوة إلا بالله", "La hawla wala quwwata illa billah", "আল্লাহর সাহায্য ছাড়া কোন শক্তি নেই"},
        {"أَسْتَغْفِرُ اللّٰهَ", "Astaghfirullah", "আমি আল্লাহর কাছে ক্ষমা চাই"},
        {"اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", "Allahumma Salli Ala Muhammad", "হে আল্লাহ, মুহাম্মদের উপর দরুদ পাঠাও"}
    };
    
    private int currentDhikrIndex = 0;

    @FXML
    public void initialize() {
        // Initialize ComboBox with dhikr options
        String[] dhikrNames = new String[dhikrList.length];
        for (int i = 0; i < dhikrList.length; i++) {
            dhikrNames[i] = dhikrList[i][1]; // English names
        }
        dhikrComboBox.setItems(FXCollections.observableArrayList(dhikrNames));
        dhikrComboBox.getSelectionModel().select(0);
        
        // Initialize date picker
        entryDatePicker.setValue(LocalDate.now());
        
        updateLabels();
        updateProgress();
        updateDhikrLabels();
        loadTodaysData();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    private void loadTasbihHistory() {
        // History is now handled in the summary page
        // This method is kept for potential future use
        if (currentUser != null) {
            logger.info("Tasbih history loading handled in summary page for user: {}", currentUser.getUsername());
        }
    }

    @FXML
    private void handleDhikrChange() {
        currentDhikrIndex = dhikrComboBox.getSelectionModel().getSelectedIndex();
        updateDhikrLabels();
    }

    @FXML
    private void handleCount() {
        count++;
        totalCount++;
        
        // Animate the counter circle
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), counterCircle);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);
        scaleTransition.play();

        // Check for milestone (every 33)
        if (count % MILESTONE == 0) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Milestone Reached");
                alert.setHeaderText("MashaAllah!");
                alert.setContentText("You have completed " + count + " dhikr!");
                alert.show();
            });
        }

        updateLabels();
        updateProgress();
    }

    @FXML
    private void handleReset() {
        count = 0;
        updateLabels();
        updateProgress();
    }
    
    @FXML
    private void handleSave() {
        if (currentUser == null) {
            showError("Error", "Please log in first");
            return;
        }
        
        if (count == 0) {
            showError("Error", "Please count some dhikr before saving");
            return;
        }
        
        String dhikrName = dhikrList[currentDhikrIndex][1];
        LocalDate date = entryDatePicker.getValue();
        String notes = notesTextArea.getText();
        
        if (date == null) {
            showError("Error", "Please select a date");
            return;
        }
        
        // Calculate the cycle number for this save operation
        int currentCycle = cycles + 1;
        
        DatabaseHelper.trackTasbih(currentUser.getId(), java.sql.Date.valueOf(date), 
                                  dhikrName, count, currentCycle, totalCount, notes)
            .thenAccept(success -> {
                Platform.runLater(() -> {
                    if (success) {
                        showSuccess("Success", "Tasbih entry saved successfully as Cycle " + currentCycle + "!");
                        // Increment cycles and reset count for next session
                        cycles = currentCycle;
                        count = 0;
                        totalCount = 0;
                        updateLabels();
                        updateProgress();
                    } else {
                        showError("Error", "Failed to save tasbih entry");
                    }
                });
            });
    }
    
    @FXML
    private void handleShowSummary() {
        // Summary functionality moved to full summary page
        handleFullSummaryPage();
    }
    
    @FXML
    private void handleFullSummaryPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TasbihSummary.fxml"));
            Parent summaryPage = loader.load();
            
            // Pass the current user to the summary controller
            TasbihSummaryController summaryController = loader.getController();
            summaryController.setCurrentUser(currentUser);
            
            Stage summaryStage = new Stage();
            summaryStage.setTitle("Tasbih Summary");
            summaryStage.setScene(new Scene(summaryPage));
            summaryStage.setMaximized(true);
            summaryStage.show();
            
            logger.info("Tasbih summary page opened");
        } catch (Exception e) {
            logger.error("Error opening tasbih summary page: {}", e.getMessage(), e);
            showError("Error", "Failed to open summary page: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
    
    private void loadTodaysData() {
        try {
            File tasbihDataFile = new File(TASBIH_DATA_FILE);
            if (!tasbihDataFile.exists()) {
                return;
            }
            
            LocalDate today = LocalDate.now();
            
            try (BufferedReader br = new BufferedReader(new FileReader(tasbihDataFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(today.toString() + ",")) {
                        // Found today's data
                        String[] parts = line.split(",");
                        if (parts.length >= 4) {
                            count = Integer.parseInt(parts[1]);
                            cycles = Integer.parseInt(parts[2]);
                            totalCount = Integer.parseInt(parts[3]);
                            updateLabels();
                            updateProgress();
                        }
                        break;
                    }
                }
            }
            
            logger.info("Loaded today's tasbih data");
            
        } catch (Exception e) {
            logger.error("Error loading tasbih data", e);
        }
    }
    
    private void saveTodaysDataToFile() {
        try {
            LocalDate today = LocalDate.now();
            
            StringBuilder sb = new StringBuilder();
            sb.append(today).append(",");
            sb.append(count).append(",");
            sb.append(cycles).append(",");
            sb.append(totalCount);
            
            List<String> lines = new ArrayList<>();
            boolean replaced = false;
            
            // Read existing lines, replace today's if exists
            File file = new File(TASBIH_DATA_FILE);
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
                } catch (IOException e) {
                    logger.error("Error reading tasbih data file", e);
                }
            }
            
            if (!replaced) {
                lines.add(sb.toString());
            }
            
            // Write back to file
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                for (String l : lines) {
                    bw.write(l + "\n");
                }
            }
            
            logger.info("Saved tasbih data: count={}, cycles={}, total={}", count, cycles, totalCount);
            
        } catch (Exception e) {
            logger.error("Error saving tasbih data", e);
        }
    }
    
    private void updateLabels() {
        counterLabel.setText(String.valueOf(count));
        cycleLabel.setText("Next Cycle: " + (cycles + 1));
        totalCountLabel.setText("Current Count: " + count);
        totalCyclesLabel.setText("Completed Cycles: " + cycles);
    }

    private void updateProgress() {
        double progress = (double) count / MILESTONE;
        progressBar.setProgress(progress);
    }

    private void updateDhikrLabels() {
        arabicDhikrLabel.setText(dhikrList[currentDhikrIndex][0]);
        englishDhikrLabel.setText(dhikrList[currentDhikrIndex][1]);
        banglaTranslationLabel.setText(dhikrList[currentDhikrIndex][2]);
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 