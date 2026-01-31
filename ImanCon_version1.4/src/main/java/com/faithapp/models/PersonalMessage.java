package com.faithapp.models;

import javafx.beans.property.*;

public class PersonalMessage {
    private final IntegerProperty id;
    private final IntegerProperty senderId;
    private final IntegerProperty receiverId;
    private final StringProperty senderName;
    private final StringProperty receiverName;
    private final StringProperty messageText;
    private final StringProperty createdAt;
    private final BooleanProperty isRead;
    private final StringProperty senderProfilePicturePath;
    private final StringProperty receiverProfilePicturePath;

    public PersonalMessage(int id, int senderId, int receiverId, String senderName, String receiverName,
                          String messageText, String createdAt, boolean isRead, 
                          String senderProfilePicturePath, String receiverProfilePicturePath) {
        this.id = new SimpleIntegerProperty(id);
        this.senderId = new SimpleIntegerProperty(senderId);
        this.receiverId = new SimpleIntegerProperty(receiverId);
        this.senderName = new SimpleStringProperty(senderName);
        this.receiverName = new SimpleStringProperty(receiverName);
        this.messageText = new SimpleStringProperty(messageText);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.isRead = new SimpleBooleanProperty(isRead);
        this.senderProfilePicturePath = new SimpleStringProperty(senderProfilePicturePath);
        this.receiverProfilePicturePath = new SimpleStringProperty(receiverProfilePicturePath);
    }

    // Getters for properties
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty senderIdProperty() { return senderId; }
    public IntegerProperty receiverIdProperty() { return receiverId; }
    public StringProperty senderNameProperty() { return senderName; }
    public StringProperty receiverNameProperty() { return receiverName; }
    public StringProperty messageTextProperty() { return messageText; }
    public StringProperty createdAtProperty() { return createdAt; }
    public BooleanProperty isReadProperty() { return isRead; }
    public StringProperty senderProfilePicturePathProperty() { return senderProfilePicturePath; }
    public StringProperty receiverProfilePicturePathProperty() { return receiverProfilePicturePath; }

    // Getters for values
    public int getId() { return id.get(); }
    public int getSenderId() { return senderId.get(); }
    public int getReceiverId() { return receiverId.get(); }
    public String getSenderName() { return senderName.get(); }
    public String getReceiverName() { return receiverName.get(); }
    public String getMessageText() { return messageText.get(); }
    public String getCreatedAt() { return createdAt.get(); }
    public boolean isRead() { return isRead.get(); }
    public String getSenderProfilePicturePath() { return senderProfilePicturePath.get(); }
    public String getReceiverProfilePicturePath() { return receiverProfilePicturePath.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setSenderId(int senderId) { this.senderId.set(senderId); }
    public void setReceiverId(int receiverId) { this.receiverId.set(receiverId); }
    public void setSenderName(String senderName) { this.senderName.set(senderName); }
    public void setReceiverName(String receiverName) { this.receiverName.set(receiverName); }
    public void setMessageText(String messageText) { this.messageText.set(messageText); }
    public void setCreatedAt(String createdAt) { this.createdAt.set(createdAt); }
    public void setRead(boolean isRead) { this.isRead.set(isRead); }
    public void setSenderProfilePicturePath(String path) { this.senderProfilePicturePath.set(path); }
    public void setReceiverProfilePicturePath(String path) { this.receiverProfilePicturePath.set(path); }
} 