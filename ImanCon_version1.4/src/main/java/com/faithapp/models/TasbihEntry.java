package com.faithapp.models;

import java.time.LocalDate;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TasbihEntry {
    private final IntegerProperty id;
    private final IntegerProperty userId;
    private final ObjectProperty<LocalDate> date;
    private final StringProperty dhikrName;
    private final IntegerProperty count;
    private final IntegerProperty cycles;
    private final IntegerProperty totalCount;
    private final StringProperty notes;
    
    public TasbihEntry(int id, int userId, LocalDate date, String dhikrName, 
                      int count, int cycles, int totalCount, String notes) {
        this.id = new SimpleIntegerProperty(id);
        this.userId = new SimpleIntegerProperty(userId);
        this.date = new SimpleObjectProperty<>(date);
        this.dhikrName = new SimpleStringProperty(dhikrName);
        this.count = new SimpleIntegerProperty(count);
        this.cycles = new SimpleIntegerProperty(cycles);
        this.totalCount = new SimpleIntegerProperty(totalCount);
        this.notes = new SimpleStringProperty(notes);
    }
    
    // Getters
    public int getId() { return id.get(); }
    public int getUserId() { return userId.get(); }
    public LocalDate getDate() { return date.get(); }
    public String getDhikrName() { return dhikrName.get(); }
    public int getCount() { return count.get(); }
    public int getCycles() { return cycles.get(); }
    public int getTotalCount() { return totalCount.get(); }
    public String getNotes() { return notes.get(); }
    
    // Property getters
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty userIdProperty() { return userId; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public StringProperty dhikrNameProperty() { return dhikrName; }
    public IntegerProperty countProperty() { return count; }
    public IntegerProperty cyclesProperty() { return cycles; }
    public IntegerProperty totalCountProperty() { return totalCount; }
    public StringProperty notesProperty() { return notes; }
    
    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setUserId(int userId) { this.userId.set(userId); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public void setDhikrName(String dhikrName) { this.dhikrName.set(dhikrName); }
    public void setCount(int count) { this.count.set(count); }
    public void setCycles(int cycles) { this.cycles.set(cycles); }
    public void setTotalCount(int totalCount) { this.totalCount.set(totalCount); }
    public void setNotes(String notes) { this.notes.set(notes); }
    
    @Override
    public String toString() {
        return String.format("TasbihEntry{date=%s, dhikr='%s', count=%d, cycles=%d, total=%d}", 
                           date.get(), dhikrName.get(), count.get(), cycles.get(), totalCount.get());
    }
} 