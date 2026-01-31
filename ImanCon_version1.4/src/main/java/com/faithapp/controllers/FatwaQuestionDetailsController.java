package com.faithapp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.FatwaAnswer;
import com.faithapp.models.FatwaQuestion;
import com.faithapp.models.Scholar;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FatwaQuestionDetailsController {
    private static final Logger logger = LoggerFactory.getLogger(FatwaQuestionDetailsController.class);
    
    @FXML private Label questionTitleLabel;
    @FXML private Label userNameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label priorityLabel;
    @FXML private Label statusLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea questionTextArea;
    @FXML private TextArea answerTextArea;
    @FXML private TextArea referencesTextArea;
    @FXML private CheckBox publicAnswerCheckBox;
    @FXML private Button submitAnswerButton;
    @FXML private Button closeButton;
    @FXML private VBox answerSection;
    @FXML private VBox existingAnswerSection;
    @FXML private Label existingAnswerLabel;
    @FXML private Label existingReferencesLabel;
    @FXML private Label answerDateLabel;
    
    private FatwaQuestion question;
    private Scholar scholar;
    
    public void setQuestion(FatwaQuestion question) {
        this.question = question;
        displayQuestion();
        loadExistingAnswer();
    }
    
    public void setScholar(Scholar scholar) {
        this.scholar = scholar;
    }
    
    private void displayQuestion() {
        if (question == null) return;
        
        questionTitleLabel.setText(question.getQuestionTitle());
        userNameLabel.setText("Asked by: " + question.getUserName());
        categoryLabel.setText("Category: " + question.getCategory());
        priorityLabel.setText("Priority: " + question.getPriority());
        statusLabel.setText("Status: " + question.getStatus());
        dateLabel.setText("Date: " + question.getCreatedAt());
        questionTextArea.setText(question.getQuestionText());
        
        // Set status color
        switch (question.getStatus()) {
            case "pending":
                statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                break;
            case "answered":
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                break;
            case "rejected":
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                break;
        }
        
        // Set priority color
        switch (question.getPriority()) {
            case "high":
                priorityLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                break;
            case "normal":
                priorityLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                break;
            case "low":
                priorityLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
                break;
        }
    }
    
    private void loadExistingAnswer() {
        if (question == null) return;
        
        DatabaseHelper.getFatwaAnswer(question.getId())
            .thenAccept(answer -> {
                Platform.runLater(() -> {
                    if (answer != null) {
                        // Show existing answer
                        existingAnswerSection.setVisible(true);
                        existingAnswerSection.setManaged(true);
                        answerSection.setVisible(false);
                        answerSection.setManaged(false);
                        
                        existingAnswerLabel.setText(answer.getAnswerText());
                        existingReferencesLabel.setText(answer.getReferencesText());
                        answerDateLabel.setText("Answered on: " + answer.getCreatedAt());
                        
                        submitAnswerButton.setDisable(true);
                        submitAnswerButton.setText("Already Answered");
                    } else {
                        // Show answer form
                        existingAnswerSection.setVisible(false);
                        existingAnswerSection.setManaged(false);
                        answerSection.setVisible(true);
                        answerSection.setManaged(true);
                        
                        submitAnswerButton.setDisable(false);
                        submitAnswerButton.setText("Submit Answer");
                    }
                });
            })
            .exceptionally(e -> {
                logger.error("Error loading existing answer", e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to load existing answer: " + e.getMessage());
                });
                return null;
            });
    }
    
    @FXML
    private void handleSubmitAnswer() {
        if (question == null || scholar == null) {
            showError("Error", "Question or scholar data not available");
            return;
        }
        
        String answerText = answerTextArea.getText().trim();
        String referencesText = referencesTextArea.getText().trim();
        boolean isPublic = publicAnswerCheckBox.isSelected();
        
        if (answerText.isEmpty()) {
            showError("Error", "Please provide an answer");
            return;
        }
        
        submitAnswerButton.setDisable(true);
        submitAnswerButton.setText("Submitting...");
        
        DatabaseHelper.submitFatwaAnswer(question.getId(), scholar.getId(), 
                                        answerText, referencesText, isPublic)
            .thenAccept(success -> {
                if (success) {
                    Platform.runLater(() -> {
                        showInfo("Success", "Answer submitted successfully!");
                        loadExistingAnswer(); // Refresh to show the answer
                    });
                } else {
                    Platform.runLater(() -> {
                        showError("Error", "Failed to submit answer. Please try again.");
                        submitAnswerButton.setDisable(false);
                        submitAnswerButton.setText("Submit Answer");
                    });
                }
            })
            .exceptionally(e -> {
                logger.error("Error submitting answer", e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to submit answer: " + e.getMessage());
                    submitAnswerButton.setDisable(false);
                    submitAnswerButton.setText("Submit Answer");
                });
                return null;
            });
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
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