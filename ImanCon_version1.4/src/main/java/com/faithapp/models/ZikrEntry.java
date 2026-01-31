package com.faithapp.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class ZikrEntry {
    private final IntegerProperty id;
    private final ObjectProperty<LocalDate> date;
    private final StringProperty period;
    private final BooleanProperty completed;
    private final StringProperty notes;

    public ZikrEntry(int id, LocalDate date, String period, boolean completed, String notes) {
        this.id = new SimpleIntegerProperty(id);
        this.date = new SimpleObjectProperty<>(date);
        this.period = new SimpleStringProperty(period);
        this.completed = new SimpleBooleanProperty(completed);
        this.notes = new SimpleStringProperty(notes);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public StringProperty periodProperty() { return period; }
    public BooleanProperty completedProperty() { return completed; }
    public StringProperty notesProperty() { return notes; }

    // Value getters
    public int getId() { return id.get(); }
    public LocalDate getDate() { return date.get(); }
    public String getPeriod() { return period.get(); }
    public boolean isCompleted() { return completed.get(); }
    public String getNotes() { return notes.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public void setPeriod(String period) { this.period.set(period); }
    public void setCompleted(boolean completed) { this.completed.set(completed); }
    public void setNotes(String notes) { this.notes.set(notes); }
} 