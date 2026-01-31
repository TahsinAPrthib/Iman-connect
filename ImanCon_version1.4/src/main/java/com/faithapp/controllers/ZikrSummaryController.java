package com.faithapp.controllers;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.User;
import com.faithapp.models.ZikrEntry;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ZikrSummaryController {
    private static final Logger logger = LoggerFactory.getLogger(ZikrSummaryController.class);

    @FXML private Label titleLabel;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private TableView<ZikrEntry> summaryTable;
    @FXML private TableColumn<ZikrEntry, LocalDate> dateColumn;
    @FXML private TableColumn<ZikrEntry, String> periodColumn;
    @FXML private TableColumn<ZikrEntry, Boolean> completedColumn;
    @FXML private TableColumn<ZikrEntry, String> notesColumn;
    @FXML private Label totalSessionsLabel;
    @FXML private Label morningCompletedLabel;
    @FXML private Label eveningCompletedLabel;
    @FXML private Button backButton;

    private User currentUser;
    private ObservableList<ZikrEntry> entries;

    @FXML
    public void initialize() {
        setupTable();
        setupYearComboBox();
        entries = FXCollections.observableArrayList();
        summaryTable.setItems(entries);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        titleLabel.setText("Zikr Summary - " + user.getFullName());
        loadData();
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("period"));
        completedColumn.setCellValueFactory(new PropertyValueFactory<>("completed"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
    }

    private void setupYearComboBox() {
        int currentYear = LocalDate.now().getYear();
        yearComboBox.getItems().addAll(currentYear - 1, currentYear, currentYear + 1);
        yearComboBox.setValue(currentYear);
        yearComboBox.setOnAction(e -> loadData());
    }

    private void loadData() {
        if (currentUser == null) return;
        int selectedYear = yearComboBox.getValue();
        DatabaseHelper.getZikrEntries(currentUser.getId(), selectedYear)
            .thenAccept(loaded -> Platform.runLater(() -> {
                entries.clear();
                entries.addAll(loaded);
                updateSummary(loaded);
            }))
            .exceptionally(throwable -> {
                logger.error("Error loading Zikr entries", throwable);
                Platform.runLater(() -> showError("Error", "Failed to load Zikr data: " + throwable.getMessage()));
                return null;
            });
    }

    private void updateSummary(List<ZikrEntry> entries) {
        int total = entries.size();
        int morning = (int) entries.stream().filter(e -> "morning".equalsIgnoreCase(e.getPeriod()) && e.isCompleted()).count();
        int evening = (int) entries.stream().filter(e -> "evening".equalsIgnoreCase(e.getPeriod()) && e.isCompleted()).count();
        totalSessionsLabel.setText("Total Sessions: " + total);
        morningCompletedLabel.setText("Morning Completed: " + morning);
        eveningCompletedLabel.setText("Evening Completed: " + evening);
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            javafx.stage.Stage currentStage = (javafx.stage.Stage) titleLabel.getScene().getWindow();
            currentStage.close();
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