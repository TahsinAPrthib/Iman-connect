package com.faithapp.models;

import java.time.LocalDateTime;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Scholar {
    private final IntegerProperty id;
    private final IntegerProperty userId;
    private final StringProperty fullName;
    private final StringProperty email;
    private final StringProperty username;
    private final StringProperty specialization;
    private final StringProperty qualifications;
    private final StringProperty bio;
    private final StringProperty gender;
    private final BooleanProperty isVerified;
    private final BooleanProperty isOnline;
    private final ObjectProperty<LocalDateTime> lastSeen;
    private final StringProperty profilePicturePath;
    private final StringProperty createdAt;

    public Scholar(int id, int userId, String fullName, String email, String username, 
                   String specialization, String qualifications, String bio, String gender,
                   boolean isVerified, boolean isOnline, LocalDateTime lastSeen, 
                   String profilePicturePath, String createdAt) {
        this.id = new SimpleIntegerProperty(id);
        this.userId = new SimpleIntegerProperty(userId);
        this.fullName = new SimpleStringProperty(fullName);
        this.email = new SimpleStringProperty(email);
        this.username = new SimpleStringProperty(username);
        this.specialization = new SimpleStringProperty(specialization);
        this.qualifications = new SimpleStringProperty(qualifications);
        this.bio = new SimpleStringProperty(bio);
        this.gender = new SimpleStringProperty(gender);
        this.isVerified = new SimpleBooleanProperty(isVerified);
        this.isOnline = new SimpleBooleanProperty(isOnline);
        this.lastSeen = new SimpleObjectProperty<>(lastSeen);
        this.profilePicturePath = new SimpleStringProperty(profilePicturePath);
        this.createdAt = new SimpleStringProperty(createdAt);
    }

    // Constructor without lastSeen
    public Scholar(int id, int userId, String fullName, String email, String username, 
                   String specialization, String qualifications, String bio, String gender,
                   boolean isVerified, boolean isOnline, String profilePicturePath, String createdAt) {
        this(id, userId, fullName, email, username, specialization, qualifications, bio, gender,
             isVerified, isOnline, null, profilePicturePath, createdAt);
    }

    // Getters for properties
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty userIdProperty() { return userId; }
    public StringProperty fullNameProperty() { return fullName; }
    public StringProperty emailProperty() { return email; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty specializationProperty() { return specialization; }
    public StringProperty qualificationsProperty() { return qualifications; }
    public StringProperty bioProperty() { return bio; }
    public StringProperty genderProperty() { return gender; }
    public BooleanProperty isVerifiedProperty() { return isVerified; }
    public BooleanProperty isOnlineProperty() { return isOnline; }
    public ObjectProperty<LocalDateTime> lastSeenProperty() { return lastSeen; }
    public StringProperty profilePicturePathProperty() { return profilePicturePath; }
    public StringProperty createdAtProperty() { return createdAt; }

    // Getters for values
    public int getId() { return id.get(); }
    public int getUserId() { return userId.get(); }
    public String getFullName() { return fullName.get(); }
    public String getEmail() { return email.get(); }
    public String getUsername() { return username.get(); }
    public String getSpecialization() { return specialization.get(); }
    public String getQualifications() { return qualifications.get(); }
    public String getBio() { return bio.get(); }
    public String getGender() { return gender.get(); }
    public boolean isVerified() { return isVerified.get(); }
    public boolean isOnline() { return isOnline.get(); }
    public LocalDateTime getLastSeen() { return lastSeen.get(); }
    public String getProfilePicturePath() { return profilePicturePath.get(); }
    public String getCreatedAt() { return createdAt.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public void setFullName(String fullName) { this.fullName.set(fullName); }
    public void setEmail(String email) { this.email.set(email); }
    public void setUsername(String username) { this.username.set(username); }
    public void setSpecialization(String specialization) { this.specialization.set(specialization); }
    public void setQualifications(String qualifications) { this.qualifications.set(qualifications); }
    public void setBio(String bio) { this.bio.set(bio); }
    public void setGender(String gender) { this.gender.set(gender); }
    public void setVerified(boolean isVerified) { this.isVerified.set(isVerified); }
    public void setOnline(boolean isOnline) { this.isOnline.set(isOnline); }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen.set(lastSeen); }
    public void setProfilePicturePath(String path) { this.profilePicturePath.set(path); }
    public void setCreatedAt(String createdAt) { this.createdAt.set(createdAt); }
} 