package com.faithapp.models;

import javafx.beans.property.*;

public class CommunityMessage {
    private final IntegerProperty id;
    private final IntegerProperty userId;
    private final StringProperty userFullName;
    private final StringProperty userGender;
    private final StringProperty messageText;
    private final StringProperty communityType; // "male" or "female"
    private final StringProperty createdAt;
    private final StringProperty userProfilePicturePath;

    public CommunityMessage(int id, int userId, String userFullName, String userGender, 
                          String messageText, String communityType, String createdAt, String userProfilePicturePath) {
        this.id = new SimpleIntegerProperty(id);
        this.userId = new SimpleIntegerProperty(userId);
        this.userFullName = new SimpleStringProperty(userFullName);
        this.userGender = new SimpleStringProperty(userGender);
        this.messageText = new SimpleStringProperty(messageText);
        this.communityType = new SimpleStringProperty(communityType);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.userProfilePicturePath = new SimpleStringProperty(userProfilePicturePath);
    }

    // Getters for properties
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty userIdProperty() { return userId; }
    public StringProperty userFullNameProperty() { return userFullName; }
    public StringProperty userGenderProperty() { return userGender; }
    public StringProperty messageTextProperty() { return messageText; }
    public StringProperty communityTypeProperty() { return communityType; }
    public StringProperty createdAtProperty() { return createdAt; }
    public StringProperty userProfilePicturePathProperty() { return userProfilePicturePath; }

    // Getters for values
    public int getId() { return id.get(); }
    public int getUserId() { return userId.get(); }
    public String getUserFullName() { return userFullName.get(); }
    public String getUserGender() { return userGender.get(); }
    public String getMessageText() { return messageText.get(); }
    public String getCommunityType() { return communityType.get(); }
    public String getCreatedAt() { return createdAt.get(); }
    public String getUserProfilePicturePath() { return userProfilePicturePath.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public void setUserFullName(String userFullName) { this.userFullName.set(userFullName); }
    public void setUserGender(String userGender) { this.userGender.set(userGender); }
    public void setMessageText(String messageText) { this.messageText.set(messageText); }
    public void setCommunityType(String communityType) { this.communityType.set(communityType); }
    public void setCreatedAt(String createdAt) { this.createdAt.set(createdAt); }
    public void setUserProfilePicturePath(String path) { this.userProfilePicturePath.set(path); }
} 