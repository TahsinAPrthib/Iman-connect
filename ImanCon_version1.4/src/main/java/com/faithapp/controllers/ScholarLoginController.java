package com.faithapp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.Scholar;

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

public class ScholarLoginController {
    private static final Logger logger = LoggerFactory.getLogger(ScholarLoginController.class);
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button backToUserLoginButton;
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
        
        DatabaseHelper.validateScholarLogin(username, password)
            .thenAccept(valid -> {
                if (valid) {
                    DatabaseHelper.getScholarByUsername(username)
                        .thenAccept(scholar -> {
                            if (scholar != null) {
                                Platform.runLater(() -> {
                                    try {
                                        logger.info("Loading scholar dashboard for: {}", username);
                                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/scholar_dashboard.fxml"));
                                        Parent dashboard = loader.load();
                                        
                                        ScholarDashboardController controller = loader.getController();
                                        if (controller == null) {
                                            throw new RuntimeException("Failed to get ScholarDashboardController");
                                        }
                                        
                                        controller.setCurrentScholar(scholar);
                                        
                                        Stage stage = (Stage) loginButton.getScene().getWindow();
                                        Scene scene = new Scene(dashboard);
                                        
                                        // Apply CSS styling
                                        scene.getStylesheets().add(getClass().getResource("/styles/scholar_dashboard.css").toExternalForm());
                                        
                                        // Set stage properties
                                        stage.setMinWidth(1000);
                                        stage.setMinHeight(700);
                                        stage.setMaximized(true);
                                        stage.setResizable(true);
                                        stage.setScene(scene);
                                        
                                        // Force maximized state
                                        Platform.runLater(() -> {
                                            stage.setMaximized(false);
                                            stage.setMaximized(true);
                                        });
                                        
                                        stage.show();
                                        
                                        logger.info("Scholar logged in successfully: {}", username);
                                    } catch (Exception e) {
                                        logger.error("Error loading scholar dashboard for: {}", username, e);
                                        showError("Error", "Failed to load dashboard: " + e.getMessage());
                                        loginButton.setDisable(false);
                                        messageLabel.setText("");
                                    }
                                });
                            } else {
                                Platform.runLater(() -> {
                                    logger.error("Failed to get scholar data for: {}", username);
                                    showError("Error", "Failed to get scholar data");
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
                logger.error("Scholar login error for: {}", username, e);
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
            Parent register = FXMLLoader.load(getClass().getResource("/fxml/scholar_register.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(register));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (Exception e) {
            logger.error("Error loading scholar register view", e);
            showError("Error", "Failed to load registration page");
        }
    }
    
    @FXML
    private void handleBackToUserLogin() {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) backToUserLoginButton.getScene().getWindow();
            stage.setScene(new Scene(login));
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (Exception e) {
            logger.error("Error loading user login view", e);
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
} 