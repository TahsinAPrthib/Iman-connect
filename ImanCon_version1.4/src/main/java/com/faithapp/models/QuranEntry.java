package com.faithapp.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class QuranEntry {
    private final IntegerProperty id;
    private final ObjectProperty<LocalDate> date;
    private final IntegerProperty surah;
    private final IntegerProperty ayahFrom;
    private final IntegerProperty ayahTo;
    private final IntegerProperty duration;
    private final StringProperty notes;

    public QuranEntry(int id, LocalDate date, int surah, int ayahFrom, int ayahTo, 
            int duration, String notes) {
        this.id = new SimpleIntegerProperty(id);
        this.date = new SimpleObjectProperty<>(date);
        this.surah = new SimpleIntegerProperty(surah);
        this.ayahFrom = new SimpleIntegerProperty(ayahFrom);
        this.ayahTo = new SimpleIntegerProperty(ayahTo);
        this.duration = new SimpleIntegerProperty(duration);
        this.notes = new SimpleStringProperty(notes);
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public IntegerProperty surahProperty() { return surah; }
    public IntegerProperty ayahFromProperty() { return ayahFrom; }
    public IntegerProperty ayahToProperty() { return ayahTo; }
    public IntegerProperty durationProperty() { return duration; }
    public StringProperty notesProperty() { return notes; }

    // Value getters
    public int getId() { return id.get(); }
    public LocalDate getDate() { return date.get(); }
    public int getSurah() { return surah.get(); }
    public int getAyahFrom() { return ayahFrom.get(); }
    public int getAyahTo() { return ayahTo.get(); }
    public int getDuration() { return duration.get(); }
    public String getNotes() { return notes.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public void setSurah(int surah) { this.surah.set(surah); }
    public void setAyahFrom(int ayahFrom) { this.ayahFrom.set(ayahFrom); }
    public void setAyahTo(int ayahTo) { this.ayahTo.set(ayahTo); }
    public void setDuration(int duration) { this.duration.set(duration); }
    public void setNotes(String notes) { this.notes.set(notes); }
} 