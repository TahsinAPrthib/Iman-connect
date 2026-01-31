package com.faithapp.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class SalahEntry {
    private final IntegerProperty id;
    private final ObjectProperty<LocalDate> date;
    private final BooleanProperty fajr;
    private final BooleanProperty dhuhr;
    private final BooleanProperty asr;
    private final BooleanProperty maghrib;
    private final BooleanProperty isha;
    private final StringProperty notes;

    public SalahEntry(int id, LocalDate date, boolean fajr, boolean dhuhr, boolean asr, 
            boolean maghrib, boolean isha, String notes) {
        this.id = new SimpleIntegerProperty(id);
        this.date = new SimpleObjectProperty<>(date);
        this.fajr = new SimpleBooleanProperty(fajr);
        this.dhuhr = new SimpleBooleanProperty(dhuhr);
        this.asr = new SimpleBooleanProperty(asr);
        this.maghrib = new SimpleBooleanProperty(maghrib);
        this.isha = new SimpleBooleanProperty(isha);
        this.notes = new SimpleStringProperty(notes);
    }

    // Getters for properties
    public IntegerProperty idProperty() { return id; }
    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public BooleanProperty fajrProperty() { return fajr; }
    public BooleanProperty dhuhrProperty() { return dhuhr; }
    public BooleanProperty asrProperty() { return asr; }
    public BooleanProperty maghribProperty() { return maghrib; }
    public BooleanProperty ishaProperty() { return isha; }
    public StringProperty notesProperty() { return notes; }

    // Getters for values
    public int getId() { return id.get(); }
    public LocalDate getDate() { return date.get(); }
    public boolean isFajr() { return fajr.get(); }
    public boolean isDhuhr() { return dhuhr.get(); }
    public boolean isAsr() { return asr.get(); }
    public boolean isMaghrib() { return maghrib.get(); }
    public boolean isIsha() { return isha.get(); }
    public String getNotes() { return notes.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setDate(LocalDate date) { this.date.set(date); }
    public void setFajr(boolean fajr) { this.fajr.set(fajr); }
    public void setDhuhr(boolean dhuhr) { this.dhuhr.set(dhuhr); }
    public void setAsr(boolean asr) { this.asr.set(asr); }
    public void setMaghrib(boolean maghrib) { this.maghrib.set(maghrib); }
    public void setIsha(boolean isha) { this.isha.set(isha); }
    public void setNotes(String notes) { this.notes.set(notes); }
} 