package com.faithapp.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.User;
import com.faithapp.services.PrayerTimeService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RamadanTrackerController {
    private static final Logger logger = LoggerFactory.getLogger(RamadanTrackerController.class);
    
    @FXML private Label dateLabel;
    @FXML private Label ramadanDayLabel;
    
    // Fasting tracker
    @FXML private Label suhoorLabel;
    @FXML private Label iftarLabel;
    @FXML private CheckBox fastingCheck;
    @FXML private TextArea fastingNotes;
    
    // Good deeds
    @FXML private TextField deedInput;
    @FXML private VBox deedsContainer;
    
    // Quran reading
    @FXML private Spinner<Integer> pagesReadSpinner;
    @FXML private ProgressBar quranProgress;
    @FXML private Label quranGoalLabel;
    
    // Buttons
    @FXML private Button saveButton;
    @FXML private Button summaryButton;
    
    // Monthly overview
    @FXML private GridPane daysGrid;
    
    private LocalDate ramadanStart;
    private ObservableList<String> goodDeeds;
    private Map<LocalDate, DayProgress> monthlyProgress;
    private User currentUser;
    
    @FXML
    public void initialize() {
        try {
            setupDate();
            setupControls();
            setupPrayerTimes();
            setupMonthlyGrid();
            loadProgress();
            logger.info("RamadanTracker initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing RamadanTracker", e);
            throw new RuntimeException("Failed to initialize RamadanTracker", e);
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    private void setupDate() {
        LocalDate today = LocalDate.now();
        dateLabel.setText(today.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        
        // TODO: Calculate actual Ramadan start date
        ramadanStart = LocalDate.of(2024, 3, 11); // Example date for Ramadan 2024
        long daysSinceStart = today.toEpochDay() - ramadanStart.toEpochDay() + 1;
        ramadanDayLabel.setText("Ramadan Day: " + daysSinceStart);
    }
    
    private void setupControls() {
        // Setup pages spinner
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
        pagesReadSpinner.setValueFactory(valueFactory);
        
        // Initialize good deeds list
        goodDeeds = FXCollections.observableArrayList();
        
        // Initialize progress map
        monthlyProgress = new HashMap<>();
    }
    
    private void setupPrayerTimes() {
        // Get prayer times for default location (Dhaka)
        PrayerTimeService.PrayerTimes times = PrayerTimeService.getPrayerTimes("Dhaka", "Dhaka");
        
        suhoorLabel.setText(times.format(times.getSuhoor()));
        iftarLabel.setText(times.format(times.getIftar()));
    }
    
    private void setupMonthlyGrid() {
        // Add day labels
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #667eea;");
            daysGrid.add(dayLabel, i, 0);
        }
        
        // Add day cells
        LocalDate date = ramadanStart;
        int row = 1;
        int col = date.getDayOfWeek().getValue() % 7;
        
        for (int day = 1; day <= 30; day++) {
            VBox dayCell = createDayCell(day, date);
            daysGrid.add(dayCell, col, row);
            
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
            
            date = date.plusDays(1);
        }
    }
    
    private VBox createDayCell(int day, LocalDate date) {
        VBox cell = new VBox(5);
        cell.getStyleClass().add("day-cell");
        cell.setAlignment(Pos.CENTER);
        
        Label dayLabel = new Label(String.valueOf(day));
        Label statusLabel = new Label("⚪"); // Default status
        
        DayProgress progress = monthlyProgress.get(date);
        if (progress != null && progress.isCompleted()) {
            statusLabel.setText("🟢");
            cell.getStyleClass().add("completed");
        }
        
        cell.getChildren().addAll(dayLabel, statusLabel);
        return cell;
    }
    
    @FXML
    private void handleAddDeed() {
        String deed = deedInput.getText().trim();
        logger.info("Adding deed: '{}'", deed);
        if (!deed.isEmpty()) {
            goodDeeds.add(deed);
            updateDeedsDisplay();
            deedInput.clear();
            logger.info("Deed added successfully. Total deeds: {}", goodDeeds.size());
        } else {
            logger.warn("Attempted to add empty deed");
        }
    }
    
    private void updateDeedsDisplay() {
        deedsContainer.getChildren().clear();
        for (String deed : goodDeeds) {
            HBox deedBox = new HBox(10);
            deedBox.setAlignment(Pos.CENTER_LEFT);
            
            Label deedLabel = new Label(deed);
            Button removeBtn = new Button("✕");
            removeBtn.setOnAction(e -> {
                goodDeeds.remove(deed);
                updateDeedsDisplay();
            });
            
            deedBox.getChildren().addAll(deedLabel, removeBtn);
            deedsContainer.getChildren().add(deedBox);
        }
    }
    
    @FXML
    private void handleUpdatePages() {
        int pages = pagesReadSpinner.getValue();
        double progress = pages / 20.0; // Assuming daily goal of 20 pages
        quranProgress.setProgress(progress);
        quranGoalLabel.setText(String.format("Daily Goal: %d/20 pages", pages));
        
        // Update monthly progress
        updateDayProgress();
    }
    
    @FXML
    private void handleSave() {
        if (currentUser == null) {
            showError("Error", "Please log in first");
            return;
        }
        
        LocalDate today = LocalDate.now();
        String goodDeedsString = String.join(", ", goodDeeds);
        
        logger.info("Saving Ramadan entry - User: {}, Date: {}, Fasted: {}, Good Deeds: '{}', Quran Pages: {}", 
                   currentUser.getUsername(), today, fastingCheck.isSelected(), goodDeedsString, pagesReadSpinner.getValue());
        
        DatabaseHelper.saveRamadanEntry(
            currentUser.getId(),
            today,
            fastingCheck.isSelected(),
            fastingNotes.getText(),
            goodDeedsString,
            pagesReadSpinner.getValue()
        ).thenAccept(success -> {
            if (success) {
                Platform.runLater(() -> {
                    showSuccess("Success", "Ramadan entry saved successfully");
                    updateDayProgress();
                });
                logger.info("Ramadan entry saved successfully");
            } else {
                Platform.runLater(() -> showError("Error", "Failed to save Ramadan entry"));
                logger.error("Failed to save Ramadan entry");
            }
        }).exceptionally(throwable -> {
            logger.error("Error saving Ramadan entry", throwable);
            Platform.runLater(() -> showError("Error", "Failed to save Ramadan entry: " + throwable.getMessage()));
            return null;
        });
    }
    
    @FXML
    private void handleViewSummary() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ramadan_summary.fxml"));
            Parent summaryRoot = loader.load();
            
            RamadanSummaryController summaryController = loader.getController();
            summaryController.setCurrentUser(currentUser);
            
            Stage summaryStage = new Stage();
            summaryStage.setTitle("Ramadan Summary");
            summaryStage.setScene(new Scene(summaryRoot));
            summaryStage.setMaximized(true);
            summaryStage.show();
            
            logger.info("Ramadan summary opened for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error opening Ramadan summary: {}", e.getMessage(), e);
            showError("Error", "Failed to open Ramadan summary: " + e.getMessage());
        }
    }
    
    private void updateDayProgress() {
        LocalDate today = LocalDate.now();
        DayProgress progress = monthlyProgress.computeIfAbsent(today, k -> new DayProgress());
        
        progress.setFasted(fastingCheck.isSelected());
        progress.setQuranPages(pagesReadSpinner.getValue());
        progress.setGoodDeeds(new ArrayList<>(goodDeeds));
        
        // Update display
        setupMonthlyGrid();
    }
    
    private void loadProgress() {
        // TODO: Load progress from database
        // For now using dummy data
        monthlyProgress = new HashMap<>();
    }
    
    private static class DayProgress {
        private boolean fasted;
        private int quranPages;
        private List<String> goodDeeds;
        
        public boolean isCompleted() {
            return fasted && quranPages >= 20;
        }
        
        // Getters and setters
        public void setFasted(boolean fasted) { this.fasted = fasted; }
        public void setQuranPages(int pages) { this.quranPages = pages; }
        public void setGoodDeeds(List<String> deeds) { this.goodDeeds = deeds; }
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
    
    private void showSuccess(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.show();
        });
    }
    
    @FXML
    private void handleBackToDashboard() {
        try {
            javafx.stage.Stage currentStage = (javafx.stage.Stage) dateLabel.getScene().getWindow();
            currentStage.close();
            
            // Return to the original dashboard window
            javafx.stage.Stage mainDashboard = com.faithapp.controllers.DashboardController.getMainDashboardStage();
            if (mainDashboard != null) {
                mainDashboard.show();
                mainDashboard.toFront();
                logger.info("Returned to original dashboard from Ramadan Tracker");
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
} 