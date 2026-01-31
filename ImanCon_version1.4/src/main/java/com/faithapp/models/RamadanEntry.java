package com.faithapp.models;

import java.time.LocalDate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RamadanEntry {
    private final IntegerProperty id;
    private final IntegerProperty userId;
    private final ObjectProperty<LocalDate> date;
    private final BooleanProperty fasted;
    private final StringProperty fastingNotes;
    private final StringProperty goodDeeds;
    private final IntegerProperty quranPages;
    
    public RamadanEntry(int id, int userId, LocalDate date, boolean fasted, 
                       String fastingNotes, String goodDeeds, int quranPages) {
        this.id = new SimpleIntegerProperty(id);
        this.userId = new SimpleIntegerProperty(userId);
        this.date = new SimpleObjectProperty<>(date);
        this.fasted = new SimpleBooleanProperty(fasted);
        this.fastingNotes = new SimpleStringProperty(fastingNotes);
        this.goodDeeds = new SimpleStringProperty(goodDeeds);
        this.quranPages = new SimpleIntegerProperty(quranPages);
    }
    
    // Getters
    public int getId() { return id.get(); }
    public int getUserId() { return userId.get(); }
    public LocalDate getDate() { return date.get(); }
    public boolean isFasted() { return fasted.get(); }
    public String getFastingNotes() { return fastingNotes.get(); }
    public String getGoodDeeds() { return goodDeeds.get(); }
    public int getQuranPages() { return quranPages.get(); }
    
    // Property getters
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty userIdProperty() { return userId; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public BooleanProperty fastedProperty() { return fasted; }
    public StringProperty fastingNotesProperty() { return fastingNotes; }
    public StringProperty goodDeedsProperty() { return goodDeeds; }
    public IntegerProperty quranPagesProperty() { return quranPages; }
    
    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public void setFasted(boolean fasted) { this.fasted.set(fasted); }
    public void setFastingNotes(String fastingNotes) { this.fastingNotes.set(fastingNotes); }
    public void setGoodDeeds(String goodDeeds) { this.goodDeeds.set(goodDeeds); }
    public void setQuranPages(int quranPages) { this.quranPages.set(quranPages); }
    
    @Override
    public String toString() {
        return String.format("RamadanEntry{date=%s, fasted=%s, quranPages=%d}", 
                           date.get(), fasted.get(), quranPages.get());
    }
} 