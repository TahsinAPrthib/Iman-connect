package com.faithapp.models;

import javafx.beans.property.*;

public class User {
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty fullName;
    private final StringProperty email;
    private final StringProperty gender;
    private final StringProperty createdAt;
    private final StringProperty profilePicturePath;

    public User(int id, String username, String fullName, String email, String gender, String createdAt, String profilePicturePath) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.fullName = new SimpleStringProperty(fullName);
        this.email = new SimpleStringProperty(email);
        this.gender = new SimpleStringProperty(gender);
        this.createdAt = new SimpleStringProperty(createdAt);
        this.profilePicturePath = new SimpleStringProperty(profilePicturePath);
    }

    // Additional constructor without profilePicturePath
    public User(int id, String username, String fullName, String email, String gender, String createdAt) {
        this(id, username, fullName, email, gender, createdAt, null);
    }

    // Getters for properties
    public IntegerProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty fullNameProperty() { return fullName; }
    public StringProperty emailProperty() { return email; }
    public StringProperty genderProperty() { return gender; }
    public StringProperty createdAtProperty() { return createdAt; }
    public StringProperty profilePicturePathProperty() { return profilePicturePath; }

    // Getters for values
    public int getId() { return id.get(); }
    public String getUsername() { return username.get(); }
    public String getFullName() { return fullName.get(); }
    public String getEmail() { return email.get(); }
    public String getGender() { return gender.get(); }
    public String getCreatedAt() { return createdAt.get(); }
    public String getProfilePicturePath() { return profilePicturePath.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setUsername(String username) { this.username.set(username); }
    public void setFullName(String fullName) { this.fullName.set(fullName); }
    public void setEmail(String email) { this.email.set(email); }
    public void setGender(String gender) { this.gender.set(gender); }
    public void setCreatedAt(String createdAt) { this.createdAt.set(createdAt); }
    public void setProfilePicturePath(String path) { this.profilePicturePath.set(path); }
} 