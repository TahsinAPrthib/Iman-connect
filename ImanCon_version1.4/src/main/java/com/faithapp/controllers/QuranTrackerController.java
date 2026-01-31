package com.faithapp.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.models.Surah;
import com.faithapp.services.QuranService;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class QuranTrackerController {
    private static final Logger logger = LoggerFactory.getLogger(QuranTrackerController.class);
    private static final String QURAN_DATA_FILE = "quran_data.txt";
    
    @FXML private Label dateLabel;
    @FXML private Label completedSurahLabel;
    @FXML private Label pagesReadLabel;
    @FXML private Label estimatedTimeLabel;
    @FXML private Label todayPagesLabel;
    @FXML private Label dailyGoalLabel;
    @FXML private Label streakLabel;
    @FXML private ProgressBar overallProgress;
    @FXML private GridPane surahGrid;
    @FXML private ComboBox<String> dailyGoalCombo;
    @FXML private Spinner<Integer> pagesReadSpinner;
    @FXML private ProgressBar dailyProgress;
    @FXML private DatePicker lastReadDate;
    
    private List<SurahInfo> surahList;
    private Map<Integer, VBox> surahCards;
    private IntegerProperty totalPagesRead = new SimpleIntegerProperty(0);
    private IntegerProperty completedSurah = new SimpleIntegerProperty(0);
    private int dailyGoal = 10; // Default daily goal
    private QuranService quranService;
    
    @FXML
    public void initialize() {
        try {
            quranService = new QuranService();
            setupDate();
            initializeSurahData();
            setupSurahGrid();
            setupControls();
            setupBindings();
            loadUserProgress();
            logger.info("QuranTracker initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing QuranTracker", e);
            throw new RuntimeException("Failed to initialize QuranTracker", e);
        }
    }
    
    @FXML
    private void handleOpenTextViewer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quran_text_viewer.fxml"));
            Scene scene = new Scene(loader.load());
            Stage textViewerStage = new Stage();
            textViewerStage.setTitle("Quran Text Viewer");
            textViewerStage.setScene(scene);
            
            QuranTextViewerController controller = loader.getController();
            controller.setStage(textViewerStage);
            
            textViewerStage.show();
        } catch (IOException e) {
            logger.error("Error opening text viewer", e);
        }
    }
    
    private void setupDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        dateLabel.setText(today.format(formatter));
    }
    
    private void initializeSurahData() {
        surahList = new ArrayList<>();
        List<Surah> quranSurahs = quranService.getSurahs();
        
        // Calculate approximate page numbers for each surah
        // This is a rough estimation - actual page numbers may vary
        int currentPage = 1;
        for (Surah surah : quranSurahs) {
            // Rough estimation: each ayah takes about 0.5-1 page depending on length
            int estimatedPages = Math.max(1, surah.getNumberOfAyahs() / 10);
            int endPage = currentPage + estimatedPages - 1;
            
            surahList.add(new SurahInfo(
                surah.getNumber(),
                surah.getName(),
                surah.getEnglishName(),
                surah.getEnglishTranslation(),
                surah.getNumberOfAyahs(),
                surah.getRevelationType(),
                currentPage,
                endPage
            ));
            
            currentPage = endPage + 1;
        }
    }
    
    private void setupSurahGrid() {
        surahCards = new HashMap<>();
        
        // Clear existing content
        surahGrid.getChildren().clear();
        surahGrid.getColumnConstraints().clear();
        surahGrid.getRowConstraints().clear();
        
        // Create responsive column constraints - use 3 columns for better layout
        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHgrow(Priority.SOMETIMES);
            surahGrid.getColumnConstraints().add(col);
        }
        
        int row = 0;
        int col = 0;
        
        for (SurahInfo surah : surahList) {
            VBox surahCard = createSurahCard(surah);
            surahCards.put(surah.getNumber(), surahCard);
            surahGrid.add(surahCard, col, row);
            
            // Move to next column, wrap to next row if needed
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
        
        // Add some row constraints for better spacing
        for (int i = 0; i <= row; i++) {
            RowConstraints rowConstraint = new RowConstraints();
            rowConstraint.setVgrow(Priority.NEVER);
            rowConstraint.setMinHeight(120);
            surahGrid.getRowConstraints().add(rowConstraint);
        }
    }
    
    private VBox createSurahCard(SurahInfo surah) {
        VBox card = new VBox(10);
        card.getStyleClass().add("surah-card");
        card.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("Surah " + surah.getNumber() + ": " + surah.getEnglishName());
        titleLabel.getStyleClass().add("surah-title");
        
        Label arabicLabel = new Label(surah.getArabicName());
        arabicLabel.getStyleClass().add("surah-arabic");
        
        Label detailsLabel = new Label("Pages: " + surah.getStartPage() + "-" + surah.getEndPage() + 
                                     " | Verses: " + surah.getNumberOfAyahs() + 
                                     " | " + surah.getRevelationType());
        detailsLabel.getStyleClass().add("surah-details");
        
        ProgressBar progress = new ProgressBar(0);
        progress.getStyleClass().add("progress-bar");
        progress.setPrefWidth(180);
        
        Button markCompleteBtn = new Button("Mark Complete");
        markCompleteBtn.getStyleClass().add("action-button");
        markCompleteBtn.setOnAction(e -> markSurahComplete(surah.getNumber()));
        
        card.getChildren().addAll(titleLabel, arabicLabel, detailsLabel, progress, markCompleteBtn);
        
        // Make the whole card clickable to open the Surah reader
        card.setOnMouseClicked(e -> openSurahReader(surah));
        return card;
    }
    
    private void openSurahReader(SurahInfo surah) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quran_text_viewer.fxml"));
            Scene scene = new Scene(loader.load());
            Stage surahReaderStage = new Stage();
            surahReaderStage.setTitle("Quran - " + surah.getEnglishName());
            surahReaderStage.setScene(scene);
            
            QuranTextViewerController controller = loader.getController();
            controller.setStage(surahReaderStage);
            // Select the surah in the ListView which will trigger the loadSurah method
            controller.selectSurah(surah.getNumber() - 1); // Adjust for 0-based index
            
            surahReaderStage.show();
        } catch (Exception ex) {
            logger.error("Error opening Surah reader", ex);
        }
    }
    
    private void setupControls() {
        // Setup daily goal combo
        dailyGoalCombo.setItems(FXCollections.observableArrayList(
            "1 page", "2 pages", "4 pages", "5 pages", "10 pages", "20 pages"
        ));
        dailyGoalCombo.setValue("10 pages"); // Default goal
        
        // Setup pages read spinner
        SpinnerValueFactory<Integer> pagesValueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 604, 0);
        pagesReadSpinner.setValueFactory(pagesValueFactory);
        
        // Add listener to update progress when pages change
        pagesReadSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateDailyProgress();
        });
        
        // Set today's date as default
        lastReadDate.setValue(LocalDate.now());
        
        // Load today's data
        loadTodaysPagesRead();
    }
    
    private void setupBindings() {
        // Add null checks before using properties
        if (totalPagesRead != null) {
            totalPagesRead.addListener((obs, oldVal, newVal) -> {
                if (overallProgress != null) {
                    overallProgress.setProgress(newVal.doubleValue() / 604.0);
                }
                if (pagesReadLabel != null) {
                    pagesReadLabel.setText(String.valueOf(newVal));
                }
                updateEstimatedTime();
            });
        }
        
        if (completedSurah != null) {
            completedSurah.addListener((obs, oldVal, newVal) -> {
                if (completedSurahLabel != null) {
                    completedSurahLabel.setText(String.valueOf(newVal));
                }
            });
        }
        
        // Update daily progress labels
        if (pagesReadSpinner != null) {
            pagesReadSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (todayPagesLabel != null) {
                    todayPagesLabel.setText(String.valueOf(newVal));
                }
                if (dailyProgress != null) {
                    dailyProgress.setProgress(newVal / (double) dailyGoal);
                }
                updateDailyProgress();
            });
        }
        
        if (dailyGoalCombo != null) {
            dailyGoalCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    dailyGoal = Integer.parseInt(newVal.split(" ")[0]);
                    if (dailyGoalLabel != null) {
                        dailyGoalLabel.setText(String.valueOf(dailyGoal));
                    }
                    updateDailyProgress();
                }
            });
        }
        
        // Initialize labels with null checks
        if (todayPagesLabel != null) todayPagesLabel.setText("0");
        if (dailyGoalLabel != null) dailyGoalLabel.setText("10");
        if (streakLabel != null) streakLabel.setText("0");
    }
    
    private void loadUserProgress() {
        // TODO: Load actual progress from database
        // For now using dummy data
        totalPagesRead.set(0);
        completedSurah.set(0);
        updateEstimatedTime();
    }
    
    private void loadTodaysPagesRead() {
        try {
            File quranDataFile = new File(QURAN_DATA_FILE);
            if (!quranDataFile.exists()) {
                pagesReadSpinner.getValueFactory().setValue(0);
                updateDailyProgress();
                return;
            }
            
            LocalDate today = LocalDate.now();
            
            try (BufferedReader br = new BufferedReader(new FileReader(quranDataFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(today.toString() + ",")) {
                        // Found today's data
                        String[] parts = line.split(",");
                        if (parts.length >= 3) {
                            int pagesRead = Integer.parseInt(parts[1]);
                            int goal = Integer.parseInt(parts[2]);
                            
                            pagesReadSpinner.getValueFactory().setValue(pagesRead);
                            dailyGoal = goal;
                            dailyGoalCombo.setValue(goal + " pages");
                            updateDailyProgress();
                        }
                        break;
                    }
                }
            }
            
            logger.info("Loaded today's Quran reading data");
            
        } catch (Exception e) {
            logger.error("Error loading Quran reading data", e);
            pagesReadSpinner.getValueFactory().setValue(0);
            updateDailyProgress();
        }
    }
    
    private void savePagesReadToFile() {
        try {
            LocalDate today = LocalDate.now();
            int pagesRead = pagesReadSpinner.getValue();
            
            // Parse daily goal from combo box
            String goalText = dailyGoalCombo.getValue();
            if (goalText != null) {
                dailyGoal = Integer.parseInt(goalText.split(" ")[0]);
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(today).append(",");
            sb.append(pagesRead).append(",");
            sb.append(dailyGoal);
            
            List<String> lines = new ArrayList<>();
            boolean replaced = false;
            
            // Read existing lines, replace today's if exists
            File file = new File(QURAN_DATA_FILE);
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
                    logger.error("Error reading Quran data file", e);
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
            
            updateDailyProgress();
            logger.info("Saved Quran reading data: {} pages read, goal: {} pages", pagesRead, dailyGoal);
            
            // Refresh dashboard Quran stats
            com.faithapp.controllers.DashboardController.refreshQuranStats();
            
        } catch (Exception e) {
            logger.error("Error saving Quran reading data", e);
            showError("Error", "Failed to save Quran reading data: " + e.getMessage());
        }
    }
    
    private void updateDailyProgress() {
        int pagesRead = pagesReadSpinner.getValue();
        double progress = dailyGoal > 0 ? (double) pagesRead / dailyGoal : 0.0;
        dailyProgress.setProgress(Math.min(progress, 1.0));
    }
    
    private void updateEstimatedTime() {
        int remainingPages = 604 - totalPagesRead.get();
        // Assuming average reading speed of 2 pages per minute
        int estimatedMinutes = remainingPages / 2;
        int hours = estimatedMinutes / 60;
        int minutes = estimatedMinutes % 60;
        
        estimatedTimeLabel.setText(String.format("Estimated Time: %d hours %d minutes", hours, minutes));
    }
    
    @FXML
    private void handleSetGoal() {
        String selected = dailyGoalCombo.getValue();
        if (selected != null) {
            dailyGoal = Integer.parseInt(selected.split(" ")[0]);
            updateDailyProgress();
            savePagesReadToFile();
            logger.info("Daily goal set to {} pages", dailyGoal);
        }
    }
    
    @FXML
    private void handleUpdatePages() {
        int pages = pagesReadSpinner.getValue();
        totalPagesRead.set(pages);
        updateEstimatedTime();
        updateDailyProgress();
        savePagesReadToFile();
        logger.info("Updated pages read to {}", pages);
    }
    
    @FXML
    private void handleSavePagesRead() {
        savePagesReadToFile();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Saved");
        alert.setHeaderText(null);
        alert.setContentText("Today's Quran reading has been saved successfully.");
        alert.showAndWait();
    }
    
    private void markSurahComplete(int surahNumber) {
        VBox surahCard = surahCards.get(surahNumber);
        if (surahCard != null) {
            surahCard.getStyleClass().add("completed");
            completedSurah.set(completedSurah.get() + 1);
            // TODO: Save to database
            logger.info("Marked Surah {} as complete", surahNumber);
        }
    }
    
    private static class SurahInfo {
        private final int number;
        private final String name;
        private final String englishName;
        private final String englishTranslation;
        private final int numberOfAyahs;
        private final String revelationType;
        private final int startPage;
        private final int endPage;
        
        public SurahInfo(int number, String name, String englishName, String englishTranslation, 
                         int numberOfAyahs, String revelationType, int startPage, int endPage) {
            this.number = number;
            this.name = name;
            this.englishName = englishName;
            this.englishTranslation = englishTranslation;
            this.numberOfAyahs = numberOfAyahs;
            this.revelationType = revelationType;
            this.startPage = startPage;
            this.endPage = endPage;
        }
        
        public int getNumber() { return number; }
        public String getName() { return name; }
        public String getArabicName() { return name; }
        public String getEnglishName() { return englishName; }
        public String getEnglishTranslation() { return englishTranslation; }
        public int getNumberOfAyahs() { return numberOfAyahs; }
        public String getRevelationType() { return revelationType; }
        public int getStartPage() { return startPage; }
        public int getEndPage() { return endPage; }
    }
    
    @FXML
    private void handleViewStats() {
        try {
            // Create a simple statistics dialog
            Alert statsAlert = new Alert(Alert.AlertType.INFORMATION);
            statsAlert.setTitle("Quran Reading Statistics");
            statsAlert.setHeaderText("Your Quran Reading Progress");
            
            StringBuilder content = new StringBuilder();
            content.append("📊 Overall Statistics:\n");
            content.append("• Completed Surahs: ").append(completedSurah.get()).append("/114\n");
            content.append("• Pages Read: ").append(totalPagesRead.get()).append("/604\n");
            content.append("• Daily Goal: ").append(dailyGoal).append(" pages\n");
            content.append("• Current Streak: ").append(getCurrentStreak()).append(" days\n");
            content.append("• Today's Progress: ").append(pagesReadSpinner.getValue()).append("/").append(dailyGoal).append(" pages\n");
            
            content.append("\n📈 Progress Summary:\n");
            content.append("• Overall Progress: ").append(String.format("%.1f", (totalPagesRead.get() / 604.0) * 100)).append("%\n");
            content.append("• Daily Progress: ").append(String.format("%.1f", (pagesReadSpinner.getValue() / (double) dailyGoal) * 100)).append("%\n");
            
            statsAlert.setContentText(content.toString());
            statsAlert.showAndWait();
        } catch (Exception e) {
            logger.error("Error showing statistics", e);
            showError("Error", "Failed to load statistics");
        }
    }
    
    private int getCurrentStreak() {
        // Simple implementation - in a real app, you'd track this in the database
        return 5; // Placeholder
    }
    
    @FXML
    private void handleBackToDashboard() {
        try {
            Stage currentStage = (Stage) dateLabel.getScene().getWindow();
            currentStage.close();
            
            // Return to the original dashboard window
            Stage mainDashboard = com.faithapp.controllers.DashboardController.getMainDashboardStage();
            if (mainDashboard != null) {
                mainDashboard.show();
                mainDashboard.toFront();
                logger.info("Returned to original dashboard from Quran Tracker");
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
} 