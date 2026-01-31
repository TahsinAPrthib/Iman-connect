package com.faithapp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.FatwaQuestion;
import com.faithapp.models.User;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class FatwaQuestionViewController {
    private static final Logger logger = LoggerFactory.getLogger(FatwaQuestionViewController.class);
    
    @FXML private Label questionTitleLabel;
    @FXML private Label scholarNameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label priorityLabel;
    @FXML private Label statusLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea questionTextArea;
    @FXML private Button closeButton;
    
    @FXML private VBox answerSection;
    @FXML private TextArea answerTextArea;
    @FXML private Label referencesLabel;
    @FXML private Label answerDateLabel;
    @FXML private Label noAnswerLabel;
    
    private FatwaQuestion question;
    private User user;
    
    public void setQuestion(FatwaQuestion question) {
        this.question = question;
        logger.info("Setting question for display: ID={}, Title={}, Status={}", 
                   question.getId(), question.getQuestionTitle(), question.getStatus());
        displayQuestion();
        loadAnswer();
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    private void displayQuestion() {
        if (question == null) return;
        
        questionTitleLabel.setText(question.getQuestionTitle());
        scholarNameLabel.setText("Scholar: " + question.getScholarName());
        categoryLabel.setText("Category: " + question.getCategory());
        priorityLabel.setText("Priority: " + question.getPriority());
        statusLabel.setText("Status: " + question.getStatus());
        dateLabel.setText("Asked on: " + question.getCreatedAt());
        questionTextArea.setText(question.getQuestionText());
        
        // Add basic styling
        questionTitleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10 0;");
        scholarNameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495e; -fx-padding: 5 0;");
        categoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555; -fx-padding: 2 0;");
        priorityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555; -fx-padding: 2 0;");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555; -fx-padding: 2 0;");
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555; -fx-padding: 2 0;");
        questionTextArea.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        
        // Set status color
        switch (question.getStatus()) {
            case "pending":
                statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 2 0;");
                break;
            case "answered":
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 2 0;");
                break;
            case "rejected":
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 2 0;");
                break;
        }
        
        // Set priority color
        switch (question.getPriority()) {
            case "high":
                priorityLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 2 0;");
                break;
            case "normal":
                priorityLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 2 0;");
                break;
            case "low":
                priorityLabel.setStyle("-fx-text-fill: gray; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 2 0;");
                break;
        }
        
        logger.info("Question displayed successfully. Status: {}", question.getStatus());
    }
    
    private void loadAnswer() {
        if (question == null) return;
        
        logger.info("Loading answer for question ID: {}", question.getId());
        
        DatabaseHelper.getFatwaAnswer(question.getId())
            .thenAccept(answer -> {
                Platform.runLater(() -> {
                    if (answer != null) {
                        logger.info("Answer found for question ID: {}. Answer text length: {}", 
                                  question.getId(), answer.getAnswerText().length());
                        
                        // Show answer
                        answerSection.setVisible(true);
                        answerSection.setManaged(true);
                        noAnswerLabel.setVisible(false);
                        noAnswerLabel.setManaged(false);
                        
                        // Add styling to make answer section more visible
                        answerSection.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #27ae60; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 20;");
                        answerTextArea.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                        referencesLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d; -fx-font-style: italic; -fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10;");
                        answerDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-padding: 5 0;");
                        
                        answerTextArea.setText(answer.getAnswerText());
                        referencesLabel.setText("References: " + (answer.getReferencesText() != null ? answer.getReferencesText() : "None provided"));
                        
                        // Format the date properly
                        LocalDateTime answerDate = answer.getCreatedAt();
                        String formattedDate = answerDate != null ? 
                            answerDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : 
                            "Date not available";
                        answerDateLabel.setText("Answered on: " + formattedDate);
                        
                        logger.info("Answer section displayed successfully");
                    } else {
                        logger.info("No answer found for question ID: {}", question.getId());
                        
                        // Show no answer message
                        answerSection.setVisible(false);
                        answerSection.setManaged(false);
                        noAnswerLabel.setVisible(true);
                        noAnswerLabel.setManaged(true);
                        noAnswerLabel.setText("No answer yet. The scholar will respond soon.");
                        noAnswerLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c; -fx-font-style: italic; -fx-padding: 20; -fx-background-color: #fdf2f2; -fx-border-color: #f5c6cb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-alignment: center;");
                    }
                });
            })
            .exceptionally(e -> {
                logger.error("Error loading answer for question ID: {}", question.getId(), e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to load answer: " + e.getMessage());
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
} 