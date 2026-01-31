package com.faithapp.controllers;

import com.faithapp.database.DatabaseHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button backToLoginButton;
    @FXML private Label messageLabel;
    
    // Radio buttons for account type selection
    @FXML private RadioButton userRadioButton;
    @FXML private RadioButton scholarRadioButton;
    
    // Radio buttons for gender selection
    @FXML private RadioButton maleRadioButton;
    @FXML private RadioButton femaleRadioButton;
    
    // Scholar-specific fields
    @FXML private VBox scholarFields;
    @FXML private TextField qualificationField;
    @FXML private TextField specializationField;
    @FXML private TextArea bioField;
    
    @FXML
    public void initialize() {
        // Set up radio button group for account type
        ToggleGroup accountTypeGroup = new ToggleGroup();
        userRadioButton.setToggleGroup(accountTypeGroup);
        scholarRadioButton.setToggleGroup(accountTypeGroup);
        
        // Set up radio button group for gender
        ToggleGroup genderGroup = new ToggleGroup();
        maleRadioButton.setToggleGroup(genderGroup);
        femaleRadioButton.setToggleGroup(genderGroup);
        
        // Add listeners to show/hide scholar fields
        userRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                scholarFields.setVisible(false);
                scholarFields.setManaged(false);
            }
        });
        
        scholarRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                scholarFields.setVisible(true);
                scholarFields.setManaged(true);
            }
        });
    }
    
    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Get selected gender
        String gender = maleRadioButton.isSelected() ? "Male" : "Female";
        
        // Clear any previous error message
        messageLabel.setText("");
        
        // Validate common input
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showError("Error", "Please fill in all required fields");
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Error", "Please enter a valid email address");
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
        
        // Check if registering as scholar
        boolean isScholar = scholarRadioButton.isSelected();
        
        // Validate scholar-specific fields if registering as scholar
        if (isScholar) {
            String qualification = qualificationField.getText().trim();
            String specialization = specializationField.getText().trim();
            String bio = bioField.getText().trim();
            
            if (qualification.isEmpty() || specialization.isEmpty() || bio.isEmpty()) {
                showError("Error", "Please fill in all scholar information fields");
                return;
            }
            
            if (bio.length() < 50) {
                showError("Error", "Biography must be at least 50 characters long");
                return;
            }
        }
        
        // Disable register button while processing
        registerButton.setDisable(true);
        messageLabel.setText("Registering...");
        
        if (isScholar) {
            // Register as scholar
            registerScholar(fullName, email, username, password, gender);
        } else {
            // Register as regular user
            registerUser(fullName, email, username, password, gender);
        }
    }
    
    private void registerUser(String fullName, String email, String username, String password, String gender) {
        DatabaseHelper.checkUserExists(username, email)
            .thenAccept(exists -> {
                if (exists) {
                    Platform.runLater(() -> {
                        showError("Error", "Username or email already exists");
                        registerButton.setDisable(false);
                        messageLabel.setText("");
                    });
                    return;
                }
                
                DatabaseHelper.registerUser(fullName, email, username, password, gender)
                    .thenAccept(success -> {
                        if (success) {
                            Platform.runLater(() -> {
                                try {
                                    logger.info("User registered successfully, redirecting to login: {}", username);
                                    Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                                    Stage stage = (Stage) registerButton.getScene().getWindow();
                                    stage.setScene(new Scene(login));
                                    stage.setFullScreen(true);
                                    stage.setFullScreenExitHint("");
                                    stage.show();
                                } catch (Exception e) {
                                    logger.error("Error loading login view after registration", e);
                                    showError("Error", "Registration successful but failed to return to login page");
                                    registerButton.setDisable(false);
                                    messageLabel.setText("");
                                }
                            });
                        } else {
                            Platform.runLater(() -> {
                                showError("Error", "Failed to register user");
                                registerButton.setDisable(false);
                                messageLabel.setText("");
                            });
                        }
                    })
                    .exceptionally(e -> {
                        logger.error("Registration error for user: {}", username, e);
                        Platform.runLater(() -> {
                            showError("Error", "Failed to register user: " + e.getMessage());
                            registerButton.setDisable(false);
                            messageLabel.setText("");
                        });
                        return null;
                    });
            })
            .exceptionally(e -> {
                logger.error("Error checking user existence: {}", username, e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to check user existence: " + e.getMessage());
                    registerButton.setDisable(false);
                    messageLabel.setText("");
                });
                return null;
            });
    }
    
    private void registerScholar(String fullName, String email, String username, String password, String gender) {
        String qualification = qualificationField.getText().trim();
        String specialization = specializationField.getText().trim();
        String bio = bioField.getText().trim();
        
        DatabaseHelper.checkScholarExists(username, email)
            .thenAccept(exists -> {
                if (exists) {
                    Platform.runLater(() -> {
                        showError("Error", "Username or email already exists");
                        registerButton.setDisable(false);
                        messageLabel.setText("");
                    });
                    return;
                }
                
                DatabaseHelper.registerScholar(fullName, email, username, password, qualification, specialization, bio, gender)
                    .thenAccept(success -> {
                        if (success) {
                            Platform.runLater(() -> {
                                try {
                                    logger.info("Scholar registered successfully, redirecting to login: {}", username);
                                    Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                                    Stage stage = (Stage) registerButton.getScene().getWindow();
                                    stage.setScene(new Scene(login));
                                    stage.setFullScreen(true);
                                    stage.setFullScreenExitHint("");
                                    stage.show();
                                } catch (Exception e) {
                                    logger.error("Error loading login view after scholar registration", e);
                                    showError("Error", "Registration successful but failed to return to login page");
                                    registerButton.setDisable(false);
                                    messageLabel.setText("");
                                }
                            });
                        } else {
                            Platform.runLater(() -> {
                                showError("Error", "Failed to register scholar");
                                registerButton.setDisable(false);
                                messageLabel.setText("");
                            });
                        }
                    })
                    .exceptionally(e -> {
                        logger.error("Scholar registration error: {}", username, e);
                        Platform.runLater(() -> {
                            showError("Error", "Failed to register scholar: " + e.getMessage());
                            registerButton.setDisable(false);
                            messageLabel.setText("");
                        });
                        return null;
                    });
            })
            .exceptionally(e -> {
                logger.error("Error checking scholar existence: {}", username, e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to check scholar existence: " + e.getMessage());
                    registerButton.setDisable(false);
                    messageLabel.setText("");
                });
                return null;
            });
    }
    
    @FXML
    private void navigateToLogin() {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) backToLoginButton.getScene().getWindow();
            stage.setScene(new Scene(login));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (Exception e) {
            logger.error("Error loading login view", e);
            showError("Error", "Failed to return to login page");
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
} 