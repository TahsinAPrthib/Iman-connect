package com.faithapp.controllers;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminViewController {
    private static final Logger logger = LoggerFactory.getLogger(AdminViewController.class);
    
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> createdAtColumn;
    
    @FXML
    public void initialize() {
        setupTable();
        loadUsers();
    }
    
    private void setupTable() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        fullNameColumn.setCellValueFactory(cellData -> cellData.getValue().fullNameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        createdAtColumn.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());
    }
    
    private void loadUsers() {
        DatabaseHelper.getAllUsers().thenAccept(users -> {
            Platform.runLater(() -> {
                userTable.getItems().clear();
                userTable.getItems().addAll(users);
            });
        }).exceptionally(e -> {
            logger.error("Error loading users", e);
            Platform.runLater(() -> showError("Error", "Failed to load users: " + e.getMessage()));
            return null;
        });
    }
    
    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.show();
        });
    }
} 