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

public class FatwaAnswer {
    private final IntegerProperty id;
    private final IntegerProperty questionId;
    private final IntegerProperty scholarId;
    private final StringProperty answerText;
    private final StringProperty referencesText;
    private final BooleanProperty isPublic;
    private final ObjectProperty<LocalDateTime> createdAt;
    private final StringProperty scholarName; // For display purposes

    public FatwaAnswer(int id, int questionId, int scholarId, String answerText, 
                      String referencesText, boolean isPublic, LocalDateTime createdAt,
                      String scholarName) {
        this.id = new SimpleIntegerProperty(id);
        this.questionId = new SimpleIntegerProperty(questionId);
        this.scholarId = new SimpleIntegerProperty(scholarId);
        this.answerText = new SimpleStringProperty(answerText);
        this.referencesText = new SimpleStringProperty(referencesText);
        this.isPublic = new SimpleBooleanProperty(isPublic);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
        this.scholarName = new SimpleStringProperty(scholarName);
    }

    // Constructor without timestamp
    public FatwaAnswer(int id, int questionId, int scholarId, String answerText, 
                      String referencesText, boolean isPublic, String scholarName) {
        this(id, questionId, scholarId, answerText, referencesText, isPublic, null, scholarName);
    }

    // Getters for properties
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty questionIdProperty() { return questionId; }
    public IntegerProperty scholarIdProperty() { return scholarId; }
    public StringProperty answerTextProperty() { return answerText; }
    public StringProperty referencesTextProperty() { return referencesText; }
    public BooleanProperty isPublicProperty() { return isPublic; }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public StringProperty scholarNameProperty() { return scholarName; }

    // Getters for values
    public int getId() { return id.get(); }
    public int getQuestionId() { return questionId.get(); }
    public int getScholarId() { return scholarId.get(); }
    public String getAnswerText() { return answerText.get(); }
    public String getReferencesText() { return referencesText.get(); }
    public boolean isPublic() { return isPublic.get(); }
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public String getScholarName() { return scholarName.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setQuestionId(int questionId) { this.questionId.set(questionId); }
    public void setScholarId(int scholarId) { this.scholarId.set(scholarId); }
    public void setAnswerText(String answerText) { this.answerText.set(answerText); }
    public void setReferencesText(String referencesText) { this.referencesText.set(referencesText); }
    public void setPublic(boolean isPublic) { this.isPublic.set(isPublic); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }
    public void setScholarName(String scholarName) { this.scholarName.set(scholarName); }
} 