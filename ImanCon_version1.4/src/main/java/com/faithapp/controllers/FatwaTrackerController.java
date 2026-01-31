package com.faithapp.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.FatwaAnswer;
import com.faithapp.models.FatwaQuestion;
import com.faithapp.models.Scholar;
import com.faithapp.models.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableCell;
import javafx.stage.Stage;

public class FatwaTrackerController {
    private static final Logger logger = LoggerFactory.getLogger(FatwaTrackerController.class);
    
    @FXML private Label userNameLabel;
    @FXML private Button askQuestionButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    
    // Ask Question Section
    @FXML private VBox askQuestionForm;
    @FXML private TextField questionTitleField;
    @FXML private TextArea questionTextArea;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private ComboBox<Scholar> scholarComboBox;
    @FXML private Button submitQuestionButton;
    @FXML private Button cancelQuestionButton;
    
    // Questions Table
    @FXML private TableView<FatwaQuestion> questionsTable;
    @FXML private TableColumn<FatwaQuestion, String> questionTitleColumn;
    @FXML private TableColumn<FatwaQuestion, String> scholarNameColumn;
    @FXML private TableColumn<FatwaQuestion, String> categoryColumn;
    @FXML private TableColumn<FatwaQuestion, String> priorityColumn;
    @FXML private TableColumn<FatwaQuestion, String> statusColumn;
    @FXML private TableColumn<FatwaQuestion, String> dateColumn;
    @FXML private TableColumn<FatwaQuestion, Void> actionColumn;
    
    private User currentUser;
    private ObservableList<FatwaQuestion> questionsList = FXCollections.observableArrayList();
    private ObservableList<Scholar> scholarsList = FXCollections.observableArrayList();
    
    public void initialize() {
        setupTable();
        setupComboBoxes();
        loadScholars();
    }
    
    // FXML Event Handlers
    @FXML
    private void handleAskQuestion() {
        showAskQuestionForm();
    }
    
    @FXML
    private void handleRefresh() {
        loadQuestions();
        loadScholars();
    }
    
    @FXML
    private void handleCancelQuestion() {
        hideAskQuestionForm();
    }
    
    @FXML
    private void handleSubmitQuestion() {
        submitQuestion();
    }
    
    @FXML
    private void handleBack() {
        navigateBack();
    }
    
    private void setupTable() {
        questionTitleColumn.setCellValueFactory(new PropertyValueFactory<>("questionTitle"));
        scholarNameColumn.setCellValueFactory(new PropertyValueFactory<>("scholarName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        // Setup action column for View Answer button
        actionColumn.setCellFactory(param -> new TableCell<FatwaQuestion, Void>() {
            private final Button viewAnswerButton = new Button("View Answer");
            private final Button viewQuestionButton = new Button("View Question");
            
            {
                viewAnswerButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
                viewAnswerButton.setOnAction(event -> {
                    FatwaQuestion question = getTableView().getItems().get(getIndex());
                    if (question != null) {
                        openQuestionDetails(question);
                    }
                });
                
                viewQuestionButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
                viewQuestionButton.setOnAction(event -> {
                    FatwaQuestion question = getTableView().getItems().get(getIndex());
                    if (question != null) {
                        openQuestionDetails(question);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    FatwaQuestion question = getTableView().getItems().get(getIndex());
                    if (question != null && "answered".equals(question.getStatus())) {
                        setGraphic(viewAnswerButton);
                    } else {
                        setGraphic(viewQuestionButton);
                    }
                }
            }
        });
        
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
    
    private void setupComboBoxes() {
        // Categories
        categoryComboBox.getItems().addAll(
            "Aqeedah (Beliefs)",
            "Fiqh (Islamic Law)",
            "Hadith",
            "Quran",
            "Family & Marriage",
            "Business & Finance",
            "Health & Medicine",
            "Education",
            "Social Issues",
            "Other"
        );
        
        // Priorities
        priorityComboBox.getItems().addAll("low", "normal", "high");
        priorityComboBox.setValue("normal");
        
        // Scholars
        scholarComboBox.setItems(scholarsList);
    }
    
    private void loadScholars() {
        DatabaseHelper.getAllScholars()
            .thenAccept(scholars -> {
                Platform.runLater(() -> {
                    scholarsList.clear();
                    scholarsList.addAll(scholars);
                });
            })
            .exceptionally(e -> {
                logger.error("Error loading scholars", e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to load scholars: " + e.getMessage());
                });
                return null;
            });
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            userNameLabel.setText("Welcome, " + user.getFullName());
            loadQuestions();
        }
    }
    
    private void loadQuestions() {
        if (currentUser == null) return;
        
        DatabaseHelper.getFatwaQuestionsForUser(currentUser.getId())
            .thenAccept(questions -> {
                Platform.runLater(() -> {
                    questionsList.clear();
                    questionsList.addAll(questions);
                });
            })
            .exceptionally(e -> {
                logger.error("Error loading questions for user: {}", currentUser.getId(), e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to load questions: " + e.getMessage());
                });
                return null;
            });
    }
    
    private void showAskQuestionForm() {
        // Clear form
        questionTitleField.clear();
        questionTextArea.clear();
        categoryComboBox.setValue(null);
        priorityComboBox.setValue("normal");
        scholarComboBox.setValue(null);
        
        // Show form
        askQuestionForm.setVisible(true);
        askQuestionForm.setManaged(true);
    }
    
    private void hideAskQuestionForm() {
        // Hide form
        askQuestionForm.setVisible(false);
        askQuestionForm.setManaged(false);
    }
    
    private void submitQuestion() {
        if (currentUser == null) {
            showError("Error", "User not logged in");
            return;
        }
        
        String questionTitle = questionTitleField.getText().trim();
        String questionText = questionTextArea.getText().trim();
        String category = categoryComboBox.getValue();
        String priority = priorityComboBox.getValue();
        Scholar selectedScholar = scholarComboBox.getValue();
        
        // Validation
        if (questionTitle.isEmpty() || questionText.isEmpty() || 
            category == null || selectedScholar == null) {
            showError("Error", "Please fill in all required fields");
            return;
        }
        
        submitQuestionButton.setDisable(true);
        submitQuestionButton.setText("Submitting...");
        
        DatabaseHelper.submitFatwaQuestion(currentUser.getId(), selectedScholar.getId(),
                                          questionTitle, questionText, category, priority)
            .thenAccept(success -> {
                if (success) {
                    Platform.runLater(() -> {
                        showInfo("Success", "Question submitted successfully!");
                        hideAskQuestionForm();
                        loadQuestions(); // Refresh the table
                    });
                } else {
                    Platform.runLater(() -> {
                        showError("Error", "Failed to submit question. Please try again.");
                    });
                }
                Platform.runLater(() -> {
                    submitQuestionButton.setDisable(false);
                    submitQuestionButton.setText("Submit Question");
                });
            })
            .exceptionally(e -> {
                logger.error("Error submitting question", e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to submit question: " + e.getMessage());
                    submitQuestionButton.setDisable(false);
                    submitQuestionButton.setText("Submit Question");
                });
                return null;
            });
    }
    
    private void openQuestionDetails(FatwaQuestion question) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fatwa_question_view.fxml"));
            Parent details = loader.load();
            
            FatwaQuestionViewController controller = loader.getController();
            controller.setQuestion(question);
            controller.setUser(currentUser);
            
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
    
    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent dashboard = loader.load();
            
            DashboardController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(dashboard));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            logger.error("Error returning to dashboard", e);
            showError("Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
} 