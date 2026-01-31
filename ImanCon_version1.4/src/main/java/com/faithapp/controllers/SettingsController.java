package com.faithapp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SettingsController {
    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);
    
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private CheckBox prayerNotificationsCheck;
    @FXML private CheckBox quranNotificationsCheck;
    @FXML private CheckBox zikrNotificationsCheck;
    @FXML private CheckBox darkModeCheck;
    @FXML private ComboBox<String> languageCombo;
    
    private String currentUsername;
    
    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }
    
    public void initialize() {
        // Initialize language options
        languageCombo.getItems().addAll("English", "Arabic", "Urdu");
        languageCombo.setValue("English");
        
        // Load user settings
        loadSettings();
    }
    
    private void loadSettings() {
        // TODO: Load actual settings from database
        // For now using dummy data
        fullNameField.setText("");
        emailField.setText("");
        prayerNotificationsCheck.setSelected(true);
        quranNotificationsCheck.setSelected(true);
        zikrNotificationsCheck.setSelected(true);
        darkModeCheck.setSelected(true);
    }
    
    @FXML
    private void handleChangePassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/change_password.fxml"));
            Parent changePassword = loader.load();
            
            ChangePasswordController controller = loader.getController();
            controller.setUsername(currentUsername);
            
            Stage stage = new Stage();
            stage.setTitle("Change Password");
            stage.setScene(new Scene(changePassword));
            stage.show();
            
            logger.info("Change password dialog opened for user: {}", currentUsername);
        } catch (Exception e) {
            logger.error("Error opening change password dialog", e);
            showError("Error", "Failed to open change password dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSave() {
        try {
            // TODO: Save settings to database
            showInfo("Success", "Settings saved successfully!");
            logger.info("Settings saved successfully");
            
            // Close the settings window
            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            logger.error("Error saving settings", e);
            showError("Error", "Failed to save settings. Please try again.");
        }
    }
    
    @FXML
    private void openAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_view.fxml"));
            Parent adminView = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Admin View - Registered Accounts");
            stage.setScene(new Scene(adminView));
            stage.setMaximized(true);
            stage.show();
            
            logger.info("Admin view opened successfully");
        } catch (Exception e) {
            logger.error("Error opening admin view", e);
            showError("Error", "Failed to open admin view: " + e.getMessage());
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
    
    @FXML
    private void handleBackToDashboard() {
        try {
            Stage currentStage = (Stage) fullNameField.getScene().getWindow();
            currentStage.close();
            
            // Return to the original dashboard window
            Stage mainDashboard = com.faithapp.controllers.DashboardController.getMainDashboardStage();
            if (mainDashboard != null) {
                mainDashboard.show();
                mainDashboard.toFront();
                logger.info("Returned to original dashboard from Settings");
            } else {
                logger.warn("Main dashboard stage not found, creating new dashboard");
                // Fallback: create new dashboard if main reference is lost
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent dashboard = loader.load();
                
                Stage dashboardStage = new Stage();
                dashboardStage.setTitle("ImanConnect - Dashboard");
                Scene scene = new Scene(dashboard);
                
                // Apply elegant CSS styling
                scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
                
                dashboardStage.setScene(scene);
                dashboardStage.setMaximized(true);
                dashboardStage.show();
            }
        } catch (Exception e) {
            logger.error("Error returning to dashboard: {}", e.getMessage(), e);
            showError("Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }
} 