package com.faithapp.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.FatwaQuestion;
import com.faithapp.models.Scholar;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ScholarDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(ScholarDashboardController.class);
    
    @FXML private Label scholarNameLabel;
    @FXML private Label specializationLabel;
    @FXML private Label currentDateLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label pendingQuestionsLabel;
    @FXML private Label totalQuestionsLabel;
    @FXML private Button fatwaQuestionsButton;
    @FXML private Button logoutButton;
    @FXML private Button refreshButton;
    @FXML private Button viewDetailsButton;
    
    @FXML private TableView<FatwaQuestion> questionsTable;
    @FXML private TableColumn<FatwaQuestion, String> questionTitleColumn;
    @FXML private TableColumn<FatwaQuestion, String> questionPreviewColumn;
    @FXML private TableColumn<FatwaQuestion, String> userNameColumn;
    @FXML private TableColumn<FatwaQuestion, String> categoryColumn;
    @FXML private TableColumn<FatwaQuestion, String> priorityColumn;
    @FXML private TableColumn<FatwaQuestion, String> statusColumn;
    @FXML private TableColumn<FatwaQuestion, String> dateColumn;
    
    private Scholar currentScholar;
    private Timer dateTimeTimer;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private ObservableList<FatwaQuestion> questionsList = FXCollections.observableArrayList();
    
    public void initialize() {
        setupTable();
        initializeDateTimeDisplay();
        setupRefreshButton();
    }
    
    private void setupTable() {
        questionTitleColumn.setCellValueFactory(new PropertyValueFactory<>("questionTitle"));
        questionPreviewColumn.setCellValueFactory(new PropertyValueFactory<>("questionPreview"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        questionsTable.setItems(questionsList);
        
        // Double-click to view question details
        questionsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FatwaQuestion selectedQuestion = questionsTable.getSelectionModel().getSelectedItem();
                if (selectedQuestion != null) {
                    openQuestionDetails(selectedQuestion);
                }
            }
        });
    }
    
    private void initializeDateTimeDisplay() {
        updateDateTimeDisplay();
        
        dateTimeTimer = new Timer(true);
        dateTimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateDateTimeDisplay());
            }
        }, 1000, 1000);
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
        }
    }
    
    private void setupRefreshButton() {
        refreshButton.setOnAction(e -> loadQuestions());
    }
    
    public void setCurrentScholar(Scholar scholar) {
        this.currentScholar = scholar;
        initializeScholarData();
        loadQuestions();
    }
    
    private void initializeScholarData() {
        if (currentScholar != null) {
            scholarNameLabel.setText(currentScholar.getFullName());
            specializationLabel.setText(currentScholar.getSpecialization());
            logger.info("Scholar dashboard initialized for: {}", currentScholar.getUsername());
        }
    }
    
    private void loadQuestions() {
        if (currentScholar == null) return;
        
        DatabaseHelper.getFatwaQuestionsForScholar(currentScholar.getId())
            .thenAccept(questions -> {
                Platform.runLater(() -> {
                    questionsList.clear();
                    questionsList.addAll(questions);
                    
                    // Update statistics
                    long pendingCount = questions.stream().filter(q -> "pending".equals(q.getStatus())).count();
                    pendingQuestionsLabel.setText(String.valueOf(pendingCount));
                    totalQuestionsLabel.setText(String.valueOf(questions.size()));
                });
            })
            .exceptionally(e -> {
                logger.error("Error loading questions for scholar: {}", currentScholar.getId(), e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to load questions: " + e.getMessage());
                });
                return null;
            });
    }
    
    private void openQuestionDetails(FatwaQuestion question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fatwa_question_details.fxml"));
            Parent details = loader.load();
            
            FatwaQuestionDetailsController controller = loader.getController();
            controller.setQuestion(question);
            controller.setScholar(currentScholar);
            
            Stage stage = new Stage();
            stage.setTitle("Fatwa Question Details");
            stage.setScene(new Scene(details));
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.show();
            
        } catch (Exception e) {
            logger.error("Error opening question details", e);
            showError("Error", "Failed to open question details: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleFatwaQuestions() {
        // This could open a more detailed view of all questions
        loadQuestions();
    }
    
    @FXML
    private void handleRefresh() {
        loadQuestions();
    }
    
    @FXML
    private void handleViewDetails() {
        FatwaQuestion selectedQuestion = questionsTable.getSelectionModel().getSelectedItem();
        if (selectedQuestion != null) {
            openQuestionDetails(selectedQuestion);
        } else {
            showError("No Selection", "Please select a question to view details");
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            // Update scholar status to offline
            if (currentScholar != null) {
                // TODO: Update scholar online status in database
            }
            
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/scholar_login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(login));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
            
            logger.info("Scholar logged out: {}", currentScholar != null ? currentScholar.getUsername() : "unknown");
        } catch (Exception e) {
            logger.error("Error during logout", e);
            showError("Error", "Failed to logout: " + e.getMessage());
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
    
    public void cleanup() {
        if (dateTimeTimer != null) {
            dateTimeTimer.cancel();
        }
    }
} 