package com.faithapp.controllers;

import com.faithapp.database.DatabaseHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.regex.Pattern;

public class SignUpController {
    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Label messageLabel;
    
    // Gender selection
    @FXML
    private RadioButton maleRadioButton;
    
    @FXML
    private RadioButton femaleRadioButton;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    
    @FXML
    public void initialize() {
        // Set up radio button group for gender
        ToggleGroup genderGroup = new ToggleGroup();
        maleRadioButton.setToggleGroup(genderGroup);
        femaleRadioButton.setToggleGroup(genderGroup);
        maleRadioButton.setSelected(true); // Default to male
    }
    
    @FXML
    private void handleSignUp() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Get selected gender
        String gender = maleRadioButton.isSelected() ? "Male" : "Female";
        
        // Validation
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("Please fill in all fields");
            return;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            messageLabel.setText("Please enter a valid email address");
            return;
        }
        
        if (username.length() < 4) {
            messageLabel.setText("Username must be at least 4 characters long");
            return;
        }
        
        if (password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters long");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match");
            return;
        }
        
        // Check if user already exists
        DatabaseHelper.checkUserExists(username, email)
            .thenAcceptAsync(exists -> {
                if (exists) {
                    Platform.runLater(() -> messageLabel.setText("Username or email already exists"));
                } else {
                    // Register the user with gender parameter
                    DatabaseHelper.registerUser(fullName, email, username, password, gender)
                        .thenAcceptAsync(success -> {
                            if (success) {
                                Platform.runLater(() -> {
                                    messageLabel.setText("Registration successful! Please log in.");
                                    // Wait for 2 seconds and then redirect to login
                                    new Thread(() -> {
                                        try {
                                            Thread.sleep(2000);
                                            Platform.runLater(this::navigateToLogin);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }).start();
                                });
                            } else {
                                Platform.runLater(() -> messageLabel.setText("Registration failed. Please try again."));
                            }
                        });
                }
            });
    }
    
    @FXML
    private void handleBackToLogin() {
        navigateToLogin();
    }
    
    private void navigateToLogin() {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(login));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Error returning to login screen");
            e.printStackTrace();
        }
    }
} 