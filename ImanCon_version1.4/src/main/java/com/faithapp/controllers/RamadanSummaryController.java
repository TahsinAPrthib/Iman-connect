package com.faithapp.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.RamadanEntry;
import com.faithapp.models.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class RamadanSummaryController {
    private static final Logger logger = LoggerFactory.getLogger(RamadanSummaryController.class);
    
    @FXML private Label titleLabel;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private TableView<RamadanEntry> summaryTable;
    @FXML private TableColumn<RamadanEntry, LocalDate> dateColumn;
    @FXML private TableColumn<RamadanEntry, Boolean> fastedColumn;
    @FXML private TableColumn<RamadanEntry, String> notesColumn;
    @FXML private TableColumn<RamadanEntry, String> goodDeedsColumn;
    @FXML private TableColumn<RamadanEntry, Integer> quranPagesColumn;
    @FXML private Label totalFastsLabel;
    @FXML private Label totalGoodDeedsLabel;
    @FXML private Label totalQuranPagesLabel;
    @FXML private Button backButton;
    
    private User currentUser;
    private ObservableList<RamadanEntry> entries;
    
    @FXML
    public void initialize() {
        try {
            setupTable();
            setupYearComboBox();
            entries = FXCollections.observableArrayList();
            summaryTable.setItems(entries);
            logger.info("RamadanSummaryController initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing RamadanSummaryController", e);
            throw new RuntimeException("Failed to initialize RamadanSummaryController", e);
        }
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        titleLabel.setText("Ramadan Summary - " + user.getFullName());
        loadData();
    }
    
    private void setupTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(column -> new TableCell<RamadanEntry, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                }
            }
        });
        
        fastedColumn.setCellValueFactory(new PropertyValueFactory<>("fasted"));
        fastedColumn.setCellFactory(column -> new TableCell<RamadanEntry, Boolean>() {
            @Override
            protected void updateItem(Boolean fasted, boolean empty) {
                super.updateItem(fasted, empty);
                if (empty || fasted == null) {
                    setText(null);
                } else {
                    setText(fasted ? "✅" : "❌");
                }
            }
        });
        
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("fastingNotes"));
        goodDeedsColumn.setCellValueFactory(new PropertyValueFactory<>("goodDeeds"));
        quranPagesColumn.setCellValueFactory(new PropertyValueFactory<>("quranPages"));
        
        // Set column widths
        dateColumn.setPrefWidth(120);
        fastedColumn.setPrefWidth(80);
        notesColumn.setPrefWidth(200);
        goodDeedsColumn.setPrefWidth(250);
        quranPagesColumn.setPrefWidth(100);
    }
    
    private void setupYearComboBox() {
        int currentYear = LocalDate.now().getYear();
        yearComboBox.getItems().addAll(currentYear - 1, currentYear, currentYear + 1);
        yearComboBox.setValue(currentYear);
        
        yearComboBox.setOnAction(e -> loadData());
    }
    
    private void loadData() {
        if (currentUser == null) {
            logger.warn("No current user set");
            return;
        }
        
        int selectedYear = yearComboBox.getValue();
        DatabaseHelper.getRamadanEntries(currentUser.getId(), selectedYear)
            .thenAccept(ramadanEntries -> {
                Platform.runLater(() -> {
                    entries.clear();
                    entries.addAll(ramadanEntries);
                    updateSummary(ramadanEntries);
                });
            })
            .exceptionally(throwable -> {
                logger.error("Error loading Ramadan entries", throwable);
                Platform.runLater(() -> showError("Error", "Failed to load Ramadan data: " + throwable.getMessage()));
                return null;
            });
    }
    
    private void updateSummary(List<RamadanEntry> entries) {
        int totalFasts = (int) entries.stream().filter(RamadanEntry::isFasted).count();
        int totalQuranPages = entries.stream().mapToInt(RamadanEntry::getQuranPages).sum();
        
        // Count good deeds (assuming they are comma-separated)
        int totalGoodDeeds = entries.stream()
            .mapToInt(entry -> {
                String deeds = entry.getGoodDeeds();
                if (deeds == null || deeds.trim().isEmpty()) {
                    return 0;
                }
                return deeds.split(",").length;
            })
            .sum();
        
        totalFastsLabel.setText("Total Fasts: " + totalFasts);
        totalGoodDeedsLabel.setText("Total Good Deeds: " + totalGoodDeeds);
        totalQuranPagesLabel.setText("Total Quran Pages: " + totalQuranPages);
    }
    
    @FXML
    private void handleBackToDashboard() {
        try {
            javafx.stage.Stage currentStage = (javafx.stage.Stage) titleLabel.getScene().getWindow();
            currentStage.close();
            
            // Return to the original dashboard window
            javafx.stage.Stage mainDashboard = com.faithapp.controllers.DashboardController.getMainDashboardStage();
            if (mainDashboard != null) {
                mainDashboard.show();
                mainDashboard.toFront();
                logger.info("Returned to original dashboard from Ramadan Summary");
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
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.show();
        });
    }
} 