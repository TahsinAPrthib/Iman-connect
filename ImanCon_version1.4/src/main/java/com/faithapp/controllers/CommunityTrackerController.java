package com.faithapp.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.CommunityMessage;
import com.faithapp.models.PersonalMessage;
import com.faithapp.models.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class CommunityTrackerController {
    private static final Logger logger = LoggerFactory.getLogger(CommunityTrackerController.class);
    
    private User currentUser;
    private String currentCommunityType;
    private ObservableList<CommunityMessage> communityMessages;
    private ObservableList<User> availableUsers;
    private User selectedUserForChat;
    
    @FXML private Label communityTypeLabel;
    @FXML private ListView<CommunityMessage> communityMessagesList;
    @FXML private TextArea messageTextArea;
    @FXML private Button postMessageButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button personalChatButton;
    @FXML private Button initializeCommunitiesButton;
    @FXML private VBox mainContent;
    @FXML private ListView<User> usersList;
    @FXML private ListView<PersonalMessage> personalMessagesList;
    @FXML private TextArea personalMessageTextArea;
    @FXML private Button sendPersonalMessageButton;
    @FXML private Button backToCommunityButton;
    @FXML private Label chatWithLabel;
    
    // New FXML fields for community members
    @FXML private ListView<User> communityMembersList;
    @FXML private Label memberCountLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label totalMessagesLabel;
    @FXML private Label communityCreatedLabel;
    @FXML private VBox personalChatArea;
    
    private ObservableList<User> communityMembers;
    
    @FXML
    public void initialize() {
        communityMessages = FXCollections.observableArrayList();
        availableUsers = FXCollections.observableArrayList();
        communityMembers = FXCollections.observableArrayList();
        
        setupCommunityMessagesList();
        setupUsersList();
        setupPersonalMessagesList();
        setupCommunityMembersList();
        
        // Initially hide personal chat section
        if (personalChatArea != null) {
            personalChatArea.setVisible(false);
            personalChatArea.setManaged(false);
        }
        
        // Ensure personal message textarea is enabled but with appropriate prompt
        if (personalMessageTextArea != null) {
            personalMessageTextArea.setDisable(false);
            personalMessageTextArea.setPromptText("Click 'Personal Chat' to start chatting...");
        }
        
        // Ensure send button is enabled
        if (sendPersonalMessageButton != null) {
            sendPersonalMessageButton.setDisable(false);
        }
        
        logger.info("CommunityTrackerController initialized");
    }
    
    private void setupCommunityMessagesList() {
        communityMessagesList.setItems(communityMessages);
        communityMessagesList.setCellFactory(param -> new ListCell<CommunityMessage>() {
            @Override
            protected void updateItem(CommunityMessage message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setGraphic(null);
                } else {
                    VBox messageBox = new VBox(5);
                    messageBox.setPadding(new Insets(10));
                    messageBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10;");
                    
                    HBox headerBox = new HBox(10);
                    headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    // Profile picture
                    StackPane profilePane = new StackPane();
                    Circle profileCircle = new Circle(20);
                    profileCircle.setStyle("-fx-fill: #2196F3; -fx-stroke: #ffffff; -fx-stroke-width: 2;");
                    
                    ImageView profileImage = new ImageView();
                    profileImage.setFitWidth(40);
                    profileImage.setFitHeight(40);
                    profileImage.setPreserveRatio(true);
                    profileImage.setClip(new Circle(20, 20, 20));
                    
                    if (message.getUserProfilePicturePath() != null && !message.getUserProfilePicturePath().isEmpty()) {
                        try {
                            Image image = new Image("file:" + message.getUserProfilePicturePath());
                            profileImage.setImage(image);
                        } catch (Exception e) {
                            // Use default image if profile picture fails to load
                            profileImage.setImage(new Image(getClass().getResourceAsStream("/images/default-profile.png")));
                        }
                    } else {
                        profileImage.setImage(new Image(getClass().getResourceAsStream("/images/default-profile.png")));
                    }
                    
                    profilePane.getChildren().addAll(profileCircle, profileImage);
                    
                    VBox userInfo = new VBox(2);
                    Label nameLabel = new Label(message.getUserFullName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                    Label timeLabel = new Label(message.getCreatedAt());
                    timeLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666666;");
                    userInfo.getChildren().addAll(nameLabel, timeLabel);
                    
                    headerBox.getChildren().addAll(profilePane, userInfo);
                    
                    Label messageLabel = new Label(message.getMessageText());
                    messageLabel.setWrapText(true);
                    messageLabel.setStyle("-fx-font-size: 14; -fx-padding: 5 0;");
                    
                    messageBox.getChildren().addAll(headerBox, messageLabel);
                    setGraphic(messageBox);
                }
            }
        });
    }
    
    private void setupUsersList() {
        usersList.setItems(availableUsers);
        usersList.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    HBox userBox = new HBox(10);
                    userBox.setPadding(new Insets(10));
                    userBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5; -fx-cursor: hand;");
                    
                    // Profile picture
                    StackPane profilePane = new StackPane();
                    Circle profileCircle = new Circle(15);
                    profileCircle.setStyle("-fx-fill: #2196F3; -fx-stroke: #ffffff; -fx-stroke-width: 1;");
                    
                    ImageView profileImage = new ImageView();
                    profileImage.setFitWidth(30);
                    profileImage.setFitHeight(30);
                    profileImage.setPreserveRatio(true);
                    profileImage.setClip(new Circle(15, 15, 15));
                    
                    if (user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
                        try {
                            Image image = new Image("file:" + user.getProfilePicturePath());
                            profileImage.setImage(image);
                        } catch (Exception e) {
                            profileImage.setImage(new Image(getClass().getResourceAsStream("/images/default-profile.png")));
                        }
                    } else {
                        profileImage.setImage(new Image(getClass().getResourceAsStream("/images/default-profile.png")));
                    }
                    
                    profilePane.getChildren().addAll(profileCircle, profileImage);
                    
                    VBox userInfo = new VBox(2);
                    Label nameLabel = new Label(user.getFullName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
                    Label statusLabel = new Label("Online");
                    statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #28a745;");
                    userInfo.getChildren().addAll(nameLabel, statusLabel);
                    
                    userBox.getChildren().addAll(profilePane, userInfo);
                    setGraphic(userBox);
                }
            }
        });
        
        usersList.setOnMouseClicked(event -> {
            User selectedUser = usersList.getSelectionModel().getSelectedItem();
            if (selectedUser != null && !selectedUser.getUsername().equals(currentUser.getUsername())) {
                startPersonalChat(selectedUser);
            }
        });
    }
    
    private void setupCommunityMembersList() {
        communityMembersList.setItems(communityMembers);
        communityMembersList.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                } else {
                    HBox memberBox = new HBox(10);
                    memberBox.setPadding(new Insets(8));
                    memberBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
                    
                    // Profile picture
                    StackPane profilePane = new StackPane();
                    Circle profileCircle = new Circle(12);
                    profileCircle.setStyle("-fx-fill: #2196F3; -fx-stroke: #ffffff; -fx-stroke-width: 1;");
                    
                    ImageView profileImage = new ImageView();
                    profileImage.setFitWidth(24);
                    profileImage.setFitHeight(24);
                    profileImage.setPreserveRatio(true);
                    profileImage.setClip(new Circle(12, 12, 12));
                    
                    if (user.getProfilePicturePath() != null && !user.getProfilePicturePath().isEmpty()) {
                        try {
                            Image image = new Image("file:" + user.getProfilePicturePath());
                            profileImage.setImage(image);
                        } catch (Exception e) {
                            profileImage.setImage(new Image(getClass().getResourceAsStream("/images/default-profile.png")));
                        }
                    } else {
                        profileImage.setImage(new Image(getClass().getResourceAsStream("/images/default-profile.png")));
                    }
                    
                    profilePane.getChildren().addAll(profileCircle, profileImage);
                    
                    VBox memberInfo = new VBox(2);
                    Label nameLabel = new Label(user.getFullName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
                    
                    String status = user.getUsername().equals(currentUser.getUsername()) ? "You" : "Member";
                    Label statusLabel = new Label(status);
                    statusLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6c757d;");
                    
                    memberInfo.getChildren().addAll(nameLabel, statusLabel);
                    
                    memberBox.getChildren().addAll(profilePane, memberInfo);
                    setGraphic(memberBox);
                }
            }
        });
    }
    
    private void setupPersonalMessagesList() {
        personalMessagesList.setCellFactory(param -> new ListCell<PersonalMessage>() {
            @Override
            protected void updateItem(PersonalMessage message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setGraphic(null);
                } else {
                    VBox messageBox = new VBox(5);
                    messageBox.setPadding(new Insets(10));
                    
                    boolean isFromCurrentUser = message.getSenderId() == currentUser.getId();
                    String backgroundColor = isFromCurrentUser ? "#E3F2FD" : "#F5F5F5";
                    String alignment = isFromCurrentUser ? "RIGHT" : "LEFT";
                    
                    messageBox.setStyle("-fx-background-color: " + backgroundColor + "; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10;");
                    messageBox.setAlignment(isFromCurrentUser ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
                    
                    HBox headerBox = new HBox(10);
                    headerBox.setAlignment(isFromCurrentUser ? javafx.geometry.Pos.CENTER_RIGHT : javafx.geometry.Pos.CENTER_LEFT);
                    
                    if (!isFromCurrentUser) {
                        // Profile picture for received messages
                        StackPane profilePane = new StackPane();
                        Circle profileCircle = new Circle(15);
                        profileCircle.setStyle("-fx-fill: #2196F3; -fx-stroke: #ffffff; -fx-stroke-width: 1;");
                        
                        ImageView profileImage = new ImageView();
                        profileImage.setFitWidth(30);
                        profileImage.setFitHeight(30);
                        profileImage.setPreserveRatio(true);
                        profileImage.setClip(new Circle(15, 15, 15));
                        
                        if (message.getSenderProfilePicturePath() != null && !message.getSenderProfilePicturePath().isEmpty()) {
                            try {
                                Image image = new Image("file:" + message.getSenderProfilePicturePath());
                                profileImage.setImage(image);
                            } catch (Exception e) {
                                profileImage.setImage(new Image(getClass().getResourceAsStream("/images/default-profile.png")));
                            }
                        } else {
                            profileImage.setImage(new Image(getClass().getResourceAsStream("/images/default-profile.png")));
                        }
                        
                        profilePane.getChildren().addAll(profileCircle, profileImage);
                        headerBox.getChildren().add(profilePane);
                    }
                    
                    VBox messageInfo = new VBox(2);
                    Label nameLabel = new Label(message.getSenderName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
                    Label timeLabel = new Label(message.getCreatedAt());
                    timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #666666;");
                    messageInfo.getChildren().addAll(nameLabel, timeLabel);
                    
                    headerBox.getChildren().add(messageInfo);
                    
                    Label messageLabel = new Label(message.getMessageText());
                    messageLabel.setWrapText(true);
                    messageLabel.setStyle("-fx-font-size: 14; -fx-padding: 5 0;");
                    
                    messageBox.getChildren().addAll(headerBox, messageLabel);
                    setGraphic(messageBox);
                }
            }
        });
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            // Debug logging
            logger.info("Setting current user: {} with gender: {}", user.getFullName(), user.getGender());
            
            // Set the appropriate community based on user's gender
            if ("Male".equalsIgnoreCase(user.getGender())) {
                logger.info("User is male, selecting male community");
                currentCommunityType = "male";
                communityTypeLabel.setText("Male Community");
            } else if ("Female".equalsIgnoreCase(user.getGender())) {
                logger.info("User is female, selecting female community");
                currentCommunityType = "female";
                communityTypeLabel.setText("Female Community");
            } else {
                // Default to male community if gender is not set
                logger.warn("User gender not set or invalid: '{}', defaulting to male community", user.getGender());
                currentCommunityType = "male";
                communityTypeLabel.setText("Male Community");
            }
            
            // Initialize communities with welcome messages if they're empty
            initializeCommunitiesIfNeeded();
            
            loadCommunityMessages();
            loadCommunityMembers();
            loadAvailableUsers();
        } else {
            logger.error("setCurrentUser called with null user");
        }
    }
    
    private void initializeCommunitiesIfNeeded() {
        DatabaseHelper.initializeCommunities()
            .thenAccept(success -> {
                if (success) {
                    logger.info("Communities initialized successfully");
                    // Reload messages after initialization
                    Platform.runLater(() -> {
                        loadCommunityMessages();
                    });
                } else {
                    logger.error("Failed to initialize communities");
                }
            })
            .exceptionally(e -> {
                logger.error("Error initializing communities", e);
                return null;
            });
    }
    
    private void loadCommunityMessages() {
        if (currentCommunityType == null) {
            logger.warn("No community type selected");
            return;
        }
        
        logger.info("Loading community messages for community: {}", currentCommunityType);
        
        DatabaseHelper.getCommunityMessages(currentCommunityType)
            .thenAccept(messages -> {
                logger.info("Received {} community messages", messages.size());
                Platform.runLater(() -> {
                    communityMessages.clear();
                    communityMessages.addAll(messages);
                    logger.info("Updated community messages list with {} messages", communityMessages.size());
                });
            })
            .exceptionally(e -> {
                logger.error("Error loading community messages", e);
                return null;
            });
    }
    
    private void loadCommunityMembers() {
        if (currentCommunityType == null) {
            logger.warn("No community type selected");
            return;
        }
        
        logger.info("Loading community members for community: {}", currentCommunityType);
        
        DatabaseHelper.getUsersByGender(currentCommunityType.equals("male") ? "Male" : "Female")
            .thenAccept(members -> {
                logger.info("Received {} community members", members.size());
                Platform.runLater(() -> {
                    communityMembers.clear();
                    communityMembers.addAll(members);
                    updateCommunityStatistics();
                    logger.info("Updated community members list with {} members", communityMembers.size());
                });
            })
            .exceptionally(e -> {
                logger.error("Error loading community members", e);
                return null;
            });
    }
    
    private void updateCommunityStatistics() {
        if (currentCommunityType == null) return;
        
        // Update member count
        int memberCount = communityMembers.size();
        memberCountLabel.setText(memberCount + " member" + (memberCount != 1 ? "s" : ""));
        
        // Update active users (for now, just show member count)
        activeUsersLabel.setText("Active Users: " + memberCount);
        
        // Update total messages
        totalMessagesLabel.setText("Total Messages: " + communityMessages.size());
        
        // Update community created date
        communityCreatedLabel.setText("Created: Today");
    }
    
    private void loadAvailableUsers() {
        if (currentUser == null) return;
        
        DatabaseHelper.getUsersForMessaging(currentUser.getId(), currentUser.getGender())
            .thenAccept(users -> {
                Platform.runLater(() -> {
                    availableUsers.clear();
                    availableUsers.addAll(users);
                });
            })
            .exceptionally(e -> {
                logger.error("Error loading available users", e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to load available users: " + e.getMessage());
                });
                return null;
            });
    }
    
    @FXML
    private void handlePostMessage() {
        if (currentUser == null) {
            showError("Error", "User not logged in");
            return;
        }
        
        String messageText = messageTextArea.getText().trim();
        if (messageText.isEmpty()) {
            showError("Error", "Please enter a message");
            return;
        }
        
        if (currentCommunityType == null) {
            showError("Error", "Please select a community");
            return;
        }
        
        postMessageButton.setDisable(true);
        postMessageButton.setText("Posting...");
        
        DatabaseHelper.postCommunityMessage(currentUser.getId(), messageText, currentCommunityType)
            .thenAccept(success -> {
                if (success) {
                    Platform.runLater(() -> {
                        messageTextArea.clear();
                        loadCommunityMessages();
                        showInfo("Success", "Message posted successfully!");
                    });
                } else {
                    Platform.runLater(() -> {
                        showError("Error", "Failed to post message. Please try again.");
                    });
                }
                Platform.runLater(() -> {
                    postMessageButton.setDisable(false);
                    postMessageButton.setText("Post Message");
                });
            })
            .exceptionally(e -> {
                logger.error("Error posting message", e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to post message: " + e.getMessage());
                    postMessageButton.setDisable(false);
                    postMessageButton.setText("Post Message");
                });
                return null;
            });
    }
    
    @FXML
    private void handleRefresh() {
        loadCommunityMessages();
        loadCommunityMembers();
        loadAvailableUsers();
    }
    
    @FXML
    private void handleInitializeCommunities() {
        initializeCommunitiesButton.setDisable(true);
        initializeCommunitiesButton.setText("Initializing...");
        
        DatabaseHelper.initializeCommunities()
            .thenAccept(success -> {
                if (success) {
                    Platform.runLater(() -> {
                        loadCommunityMessages();
                        showInfo("Success", "Communities initialized with welcome messages!");
                    });
                } else {
                    Platform.runLater(() -> {
                        showError("Error", "Failed to initialize communities. Please try again.");
                    });
                }
                Platform.runLater(() -> {
                    initializeCommunitiesButton.setDisable(false);
                    initializeCommunitiesButton.setText("🏗️ Initialize Communities");
                });
            })
            .exceptionally(e -> {
                logger.error("Error initializing communities", e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to initialize communities: " + e.getMessage());
                    initializeCommunitiesButton.setDisable(false);
                    initializeCommunitiesButton.setText("🏗️ Initialize Communities");
                });
                return null;
            });
    }
    
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/dashboard.fxml"));
            Parent dashboard = loader.load();
            
            DashboardController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(dashboard);
            currentStage.setScene(scene);
            currentStage.setMaximized(true);
            
            logger.info("Returned to dashboard from community tracker");
        } catch (Exception e) {
            logger.error("Error returning to dashboard: {}", e.getMessage(), e);
            showError("Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }
    
    @FXML
    private void handlePersonalChat() {
        logger.info("Personal Chat button clicked");
        
        // Show personal chat area and users list
        personalChatArea.setVisible(true);
        personalChatArea.setManaged(true);
        logger.info("Personal chat area visibility: {}, managed: {}", personalChatArea.isVisible(), personalChatArea.isManaged());
        
        // Ensure users list is visible and loaded
        usersList.setVisible(true);
        usersList.setManaged(true);
        logger.info("Users list visibility: {}, managed: {}", usersList.isVisible(), usersList.isManaged());
        
        // Load available users if not already loaded
        if (availableUsers.isEmpty()) {
            logger.info("Loading available users...");
            loadAvailableUsers();
        } else {
            logger.info("Available users already loaded: {}", availableUsers.size());
        }
        
        // Clear any previous chat selection
        selectedUserForChat = null;
        chatWithLabel.setText("Chat with: ");
        personalMessagesList.getItems().clear();
        personalMessageTextArea.clear();
        
        // Enable the personal message input area
        personalMessageTextArea.setDisable(false);
        personalMessageTextArea.setPromptText("Select a user to start chatting...");
        sendPersonalMessageButton.setDisable(false);
        logger.info("Personal message textarea enabled: {}, send button enabled: {}", 
                   !personalMessageTextArea.isDisabled(), !sendPersonalMessageButton.isDisabled());
        
        // Ensure the right panel is properly sized
        if (personalChatArea.getParent() instanceof VBox) {
            VBox parent = (VBox) personalChatArea.getParent();
            parent.setVisible(true);
            parent.setManaged(true);
            logger.info("Parent VBox visibility: {}, managed: {}", parent.isVisible(), parent.isManaged());
        }
    }
    
    @FXML
    private void handleBackToCommunity() {
        // Hide personal chat area
        personalChatArea.setVisible(false);
        personalChatArea.setManaged(false);
        selectedUserForChat = null;
        chatWithLabel.setText("Chat with: ");
    }
    
    private void startPersonalChat(User user) {
        if (user == null || user.getUsername().equals(currentUser.getUsername())) {
            logger.warn("Invalid user selected for personal chat: {}", user != null ? user.getUsername() : "null");
            return;
        }
        
        logger.info("Starting personal chat with user: {}", user.getFullName());
        
        selectedUserForChat = user;
        chatWithLabel.setText("Chat with: " + user.getFullName());
        
        // Ensure personal chat area is visible
        personalChatArea.setVisible(true);
        personalChatArea.setManaged(true);
        logger.info("Personal chat area visibility: {}, managed: {}", personalChatArea.isVisible(), personalChatArea.isManaged());
        
        // Enable the personal message input area
        personalMessageTextArea.setDisable(false);
        personalMessageTextArea.setPromptText("Type your message to " + user.getFullName() + "...");
        sendPersonalMessageButton.setDisable(false);
        logger.info("Personal message textarea enabled: {}, send button enabled: {}", 
                   !personalMessageTextArea.isDisabled(), !sendPersonalMessageButton.isDisabled());
        
        // Load personal messages
        DatabaseHelper.getPersonalMessages(currentUser.getId(), user.getId())
            .thenAccept(messages -> {
                Platform.runLater(() -> {
                    ObservableList<PersonalMessage> personalMessages = FXCollections.observableArrayList();
                    personalMessages.addAll(messages);
                    personalMessagesList.setItems(personalMessages);
                    logger.info("Loaded {} personal messages", messages.size());
                    
                    // Scroll to the bottom to show latest messages
                    if (!personalMessages.isEmpty()) {
                        personalMessagesList.scrollTo(personalMessages.size() - 1);
                    }
                });
            })
            .exceptionally(e -> {
                logger.error("Error loading personal messages", e);
                return null;
            });
    }
    
    @FXML
    private void handleSendPersonalMessage() {
        if (selectedUserForChat == null) {
            showError("Error", "Please select a user to chat with");
            return;
        }
        
        String messageText = personalMessageTextArea.getText().trim();
        if (messageText.isEmpty()) {
            showError("Error", "Please enter a message");
            return;
        }
        
        sendPersonalMessageButton.setDisable(true);
        sendPersonalMessageButton.setText("Sending...");
        
        DatabaseHelper.sendPersonalMessage(currentUser.getId(), selectedUserForChat.getId(), messageText)
            .thenAccept(success -> {
                if (success) {
                    Platform.runLater(() -> {
                        personalMessageTextArea.clear();
                        startPersonalChat(selectedUserForChat); // Reload messages
                    });
                } else {
                    Platform.runLater(() -> {
                        showError("Error", "Failed to send message. Please try again.");
                    });
                }
                Platform.runLater(() -> {
                    sendPersonalMessageButton.setDisable(false);
                    sendPersonalMessageButton.setText("📤 Send");
                });
            })
            .exceptionally(e -> {
                logger.error("Error sending personal message", e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to send message: " + e.getMessage());
                    sendPersonalMessageButton.setDisable(false);
                    sendPersonalMessageButton.setText("📤 Send");
                });
                return null;
            });
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 