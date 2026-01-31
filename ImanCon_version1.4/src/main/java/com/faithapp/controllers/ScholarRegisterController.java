package com.faithapp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class ScholarRegisterController {
    private static final Logger logger = LoggerFactory.getLogger(ScholarRegisterController.class);
    
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField specializationField;
    @FXML private TextArea qualificationsArea;
    @FXML private TextArea bioArea;
    @FXML private Button registerButton;
    @FXML private Button backToLoginButton;
    @FXML private Label messageLabel;
    
    // Gender selection
    @FXML private RadioButton maleRadioButton;
    @FXML private RadioButton femaleRadioButton;
    
    @FXML
    public void initialize() {
        // Set up radio button group for gender
        ToggleGroup genderGroup = new ToggleGroup();
        maleRadioButton.setToggleGroup(genderGroup);
        femaleRadioButton.setToggleGroup(genderGroup);
        maleRadioButton.setSelected(true); // Default to male
    }
    
    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String specialization = specializationField.getText().trim();
        String qualifications = qualificationsArea.getText().trim();
        String bio = bioArea.getText().trim();
        
        // Get selected gender
        String gender = maleRadioButton.isSelected() ? "Male" : "Female";
        
        // Validation
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || 
            password.isEmpty() || specialization.isEmpty() || qualifications.isEmpty()) {
            showError("Error", "Please fill in all required fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Error", "Passwords do not match");
            return;
        }
        
        if (password.length() < 6) {
            showError("Error", "Password must be at least 6 characters long");
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Error", "Please enter a valid email address");
            return;
        }
        
        // Disable register button while processing
        registerButton.setDisable(true);
        messageLabel.setText("Checking availability...");
        
        // Check if scholar already exists
        DatabaseHelper.checkScholarExists(username, email)
            .thenAccept(exists -> {
                if (exists) {
                    Platform.runLater(() -> {
                        showError("Registration Failed", "Username or email already exists");
                        registerButton.setDisable(false);
                        messageLabel.setText("");
                    });
                } else {
                    // Proceed with registration
                    messageLabel.setText("Registering scholar...");
                    
                    DatabaseHelper.registerScholar(fullName, email, username, password, 
                                                  specialization, qualifications, bio, gender)
                        .thenAccept(success -> {
                            if (success) {
                                Platform.runLater(() -> {
                                    showInfo("Success", "Scholar registered successfully! You can now login.");
                                    handleBackToLogin();
                                });
                            } else {
                                Platform.runLater(() -> {
                                    showError("Registration Failed", "Failed to register scholar. Please try again.");
                                    registerButton.setDisable(false);
                                    messageLabel.setText("");
                                });
                            }
                        })
                        .exceptionally(e -> {
                            logger.error("Error during scholar registration", e);
                            Platform.runLater(() -> {
                                showError("Error", "Registration failed: " + e.getMessage());
                                registerButton.setDisable(false);
                                messageLabel.setText("");
                            });
                            return null;
                        });
                }
            })
            .exceptionally(e -> {
                logger.error("Error checking scholar existence", e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to check availability: " + e.getMessage());
                    registerButton.setDisable(false);
                    messageLabel.setText("");
                });
                return null;
            });
    }
    
    @FXML
    private void handleBackToLogin() {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) backToLoginButton.getScene().getWindow();
            stage.setScene(new Scene(login));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (Exception e) {
            logger.error("Error loading login view", e);
            showError("Error", "Failed to load login page");
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