package com.faithapp.controllers;

import com.faithapp.database.DatabaseHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangePasswordController {
    private static final Logger logger = LoggerFactory.getLogger(ChangePasswordController.class);
    
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    
    private String username;
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    @FXML
    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Clear previous error message
        messageLabel.setText("");
        
        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("Please fill in all fields");
            return;
        }
        
        if (newPassword.length() < 6) {
            messageLabel.setText("New password must be at least 6 characters long");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            messageLabel.setText("New passwords do not match");
            return;
        }
        
        // Verify current password and update to new password
        DatabaseHelper.validateLogin(username, currentPassword)
            .thenAccept(valid -> {
                if (valid) {
                    DatabaseHelper.updatePassword(username, newPassword)
                        .thenAccept(success -> {
                            if (success) {
                                Platform.runLater(() -> {
                                    showInfo("Success", "Password changed successfully");
                                    closeDialog();
                                });
                            } else {
                                Platform.runLater(() -> 
                                    messageLabel.setText("Failed to update password. Please try again."));
                            }
                        })
                        .exceptionally(e -> {
                            Platform.runLater(() -> 
                                messageLabel.setText("Error: " + e.getMessage()));
                            return null;
                        });
                } else {
                    Platform.runLater(() -> 
                        messageLabel.setText("Current password is incorrect"));
                }
            })
            .exceptionally(e -> {
                Platform.runLater(() -> 
                    messageLabel.setText("Error: " + e.getMessage()));
                return null;
            });
    }
    
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    private void closeDialog() {
        Stage stage = (Stage) currentPasswordField.getScene().getWindow();
        stage.close();
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
} 