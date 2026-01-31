package com.faithapp.models;

import java.time.LocalDateTime;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FatwaQuestion {
    private final IntegerProperty id;
    private final IntegerProperty userId;
    private final IntegerProperty scholarId;
    private final StringProperty questionTitle;
    private final StringProperty questionText;
    private final StringProperty category;
    private final StringProperty priority;
    private final StringProperty status;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final ObjectProperty<LocalDateTime> updatedAt;
    private final StringProperty userName; // For display purposes
    private final StringProperty scholarName; // For display purposes
    private final StringProperty questionPreview; // For display purposes

    public FatwaQuestion(int id, int userId, int scholarId, String questionTitle, 
                        String questionText, String category, String priority, 
                        String status, LocalDateTime createdAt, LocalDateTime updatedAt,
                        String userName, String scholarName) {
        this.id = new SimpleIntegerProperty(id);
        this.userId = new SimpleIntegerProperty(userId);
        this.scholarId = new SimpleIntegerProperty(scholarId);
        this.questionTitle = new SimpleStringProperty(questionTitle);
        this.questionText = new SimpleStringProperty(questionText);
        this.category = new SimpleStringProperty(category);
        this.priority = new SimpleStringProperty(priority);
        this.status = new SimpleStringProperty(status);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
        this.updatedAt = new SimpleObjectProperty<>(updatedAt);
        this.userName = new SimpleStringProperty(userName);
        this.scholarName = new SimpleStringProperty(scholarName);
        this.questionPreview = new SimpleStringProperty(questionText.length() > 50 ? questionText.substring(0, 50) + "..." : questionText);
    }

    // Constructor without timestamps
    public FatwaQuestion(int id, int userId, int scholarId, String questionTitle, 
                        String questionText, String category, String priority, 
                        String status, String userName, String scholarName) {
        this(id, userId, scholarId, questionTitle, questionText, category, priority, 
             status, null, null, userName, scholarName);
    }

    // Getters for properties
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty userIdProperty() { return userId; }
    public IntegerProperty scholarIdProperty() { return scholarId; }
    public StringProperty questionTitleProperty() { return questionTitle; }
    public StringProperty questionTextProperty() { return questionText; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty priorityProperty() { return priority; }
    public StringProperty statusProperty() { return status; }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
    public StringProperty userNameProperty() { return userName; }
    public StringProperty scholarNameProperty() { return scholarName; }
    public StringProperty questionPreviewProperty() { return questionPreview; }

    // Getters for values
    public int getId() { return id.get(); }
    public int getUserId() { return userId.get(); }
    public int getScholarId() { return scholarId.get(); }
    public String getQuestionTitle() { return questionTitle.get(); }
    public String getQuestionText() { return questionText.get(); }
    public String getCategory() { return category.get(); }
    public String getPriority() { return priority.get(); }
    public String getStatus() { return status.get(); }
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public String getUserName() { return userName.get(); }
    public String getScholarName() { return scholarName.get(); }
    public String getQuestionPreview() { return questionPreview.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public void setScholarId(int scholarId) { this.scholarId.set(scholarId); }
    public void setQuestionTitle(String questionTitle) { this.questionTitle.set(questionTitle); }
    public void setQuestionText(String questionText) { this.questionText.set(questionText); }
    public void setCategory(String category) { this.category.set(category); }
    public void setPriority(String priority) { this.priority.set(priority); }
    public void setStatus(String status) { this.status.set(status); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }
    public void setUserName(String userName) { this.userName.set(userName); }
    public void setScholarName(String scholarName) { this.scholarName.set(scholarName); }
    public void setQuestionPreview(String questionPreview) { this.questionPreview.set(questionPreview); }
} 