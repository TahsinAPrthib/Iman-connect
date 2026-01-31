package com.faithapp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.User;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button scholarLoginButton;
    @FXML private Label messageLabel;
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Error", "Please enter both username and password");
            return;
        }
        
        // Disable login button while processing
        loginButton.setDisable(true);
        messageLabel.setText("Logging in...");
        
        DatabaseHelper.validateLogin(username, password)
            .thenAccept(valid -> {
                if (valid) {
                    DatabaseHelper.getUserByUsername(username)
                        .thenAccept(user -> {
                            if (user != null) {
                                Platform.runLater(() -> {
                                    try {
                                        logger.info("Loading dashboard for user: {}", username);
                                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                                        Parent dashboard = loader.load();
                                        
                                        DashboardController controller = loader.getController();
                                        if (controller == null) {
                                            throw new RuntimeException("Failed to get DashboardController");
                                        }
                                        
                                        controller.setCurrentUser(user);
                                        
                                        Stage stage = (Stage) loginButton.getScene().getWindow();
                                        Scene scene = new Scene(dashboard);
                                        
                                        // Apply elegant CSS styling
                                        scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
                                        
                                        // Set stage properties before showing
                                        stage.setMinWidth(800);
                                        stage.setMinHeight(600);
                                        stage.setMaximized(true);
                                        stage.setResizable(true);
                                        stage.setScene(scene);
                                        
                                        // Force maximized state
                                        Platform.runLater(() -> {
                                            stage.setMaximized(false); // Reset state
                                            stage.setMaximized(true);  // Force maximize
                                        });
                                        
                                        stage.show();
                                        
                                        logger.info("User logged in successfully: {}", username);
                                    } catch (Exception e) {
                                        logger.error("Error loading dashboard for user: {}", username, e);
                                        showError("Error", "Failed to load dashboard: " + e.getMessage());
                                        loginButton.setDisable(false);
                                        messageLabel.setText("");
                                    }
                                });
                            } else {
                                Platform.runLater(() -> {
                                    logger.error("Failed to get user data for user: {}", username);
                                    showError("Error", "Failed to get user data");
                                    loginButton.setDisable(false);
                                    messageLabel.setText("");
                                });
                            }
                        });
                } else {
                    Platform.runLater(() -> {
                        showError("Login Failed", "Invalid username or password");
                        loginButton.setDisable(false);
                        messageLabel.setText("");
                    });
                }
            })
            .exceptionally(e -> {
                logger.error("Login error for user: {}", username, e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to process login: " + e.getMessage());
                    loginButton.setDisable(false);
                    messageLabel.setText("");
                });
                return null;
            });
    }
    
    @FXML
    private void handleRegister() {
        try {
            Parent register = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(register));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (Exception e) {
            logger.error("Error loading register view", e);
            showError("Error", "Failed to load registration page");
        }
    }
    
    @FXML
    private void handleScholarLogin() {
        try {
            Parent scholarLogin = FXMLLoader.load(getClass().getResource("/fxml/scholar_login.fxml"));
            Stage stage = (Stage) scholarLoginButton.getScene().getWindow();
            stage.setScene(new Scene(scholarLogin));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (Exception e) {
            logger.error("Error loading scholar login view", e);
            showError("Error", "Failed to load scholar login page");
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