package com.faithapp.controllers;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.User;
import com.faithapp.models.SalahEntry;
import com.faithapp.models.QuranEntry;
import com.faithapp.models.ZikrEntry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class IslamicTrackerController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(IslamicTrackerController.class);
    private User currentUser;

    // Ramadan Tracking
    @FXML private ComboBox<Integer> ramadanYearCombo;
    @FXML private ComboBox<Integer> ramadanDayCombo;
    @FXML private GridPane ramadanGrid;
    @FXML private TextArea ramadanNotes;

    // Salah Tracking
    @FXML private DatePicker salahDate;
    @FXML private CheckBox fajrCheck;
    @FXML private CheckBox dhuhrCheck;
    @FXML private CheckBox asrCheck;
    @FXML private CheckBox maghribCheck;
    @FXML private CheckBox ishaCheck;
    @FXML private TextArea salahNotes;
    @FXML private TableView<SalahEntry> salahHistoryTable;
    @FXML private TableColumn<SalahEntry, LocalDate> salahDateColumn;
    @FXML private TableColumn<SalahEntry, Boolean> fajrColumn;
    @FXML private TableColumn<SalahEntry, Boolean> dhuhrColumn;
    @FXML private TableColumn<SalahEntry, Boolean> asrColumn;
    @FXML private TableColumn<SalahEntry, Boolean> maghribColumn;
    @FXML private TableColumn<SalahEntry, Boolean> ishaColumn;

    // Quran Tracking
    @FXML private DatePicker quranDate;
    @FXML private ComboBox<Integer> surahCombo;
    @FXML private TextField ayahFromField;
    @FXML private TextField ayahToField;
    @FXML private TextField durationField;
    @FXML private TextArea quranNotes;
    @FXML private TableView<QuranEntry> quranHistoryTable;
    @FXML private TableColumn<QuranEntry, LocalDate> quranDateColumn;
    @FXML private TableColumn<QuranEntry, Integer> surahColumn;
    @FXML private TableColumn<QuranEntry, Integer> ayahFromColumn;
    @FXML private TableColumn<QuranEntry, Integer> ayahToColumn;
    @FXML private TableColumn<QuranEntry, Integer> durationColumn;

    // Zikr Tracking
    @FXML private DatePicker zikrDate;
    @FXML private RadioButton morningRadio;
    @FXML private RadioButton eveningRadio;
    @FXML private CheckBox zikrCompleted;
    @FXML private TextArea zikrNotes;
    @FXML private TableView<ZikrEntry> zikrHistoryTable;
    @FXML private TableColumn<ZikrEntry, LocalDate> zikrDateColumn;
    @FXML private TableColumn<ZikrEntry, String> periodColumn;
    @FXML private TableColumn<ZikrEntry, Boolean> completedColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeRamadanTracker();
        initializeSalahTracker();
        initializeQuranTracker();
        initializeZikrTracker();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    private void initializeRamadanTracker() {
        // Initialize year combo box with current and next year
        int currentYear = LocalDate.now().getYear();
        ramadanYearCombo.getItems().addAll(currentYear, currentYear + 1);
        ramadanYearCombo.setValue(currentYear);

        // Initialize day combo box with 1-30
        for (int i = 1; i <= 30; i++) {
            ramadanDayCombo.getItems().add(i);
        }
        ramadanDayCombo.setValue(1);
    }

    private void initializeSalahTracker() {
        salahDate.setValue(LocalDate.now());
        
        // Initialize table columns
        salahDateColumn.setCellValueFactory(data -> data.getValue().dateProperty());
        fajrColumn.setCellValueFactory(data -> data.getValue().fajrProperty());
        dhuhrColumn.setCellValueFactory(data -> data.getValue().dhuhrProperty());
        asrColumn.setCellValueFactory(data -> data.getValue().asrProperty());
        maghribColumn.setCellValueFactory(data -> data.getValue().maghribProperty());
        ishaColumn.setCellValueFactory(data -> data.getValue().ishaProperty());
    }

    private void initializeQuranTracker() {
        quranDate.setValue(LocalDate.now());
        
        // Initialize Surah combo box (1-114)
        for (int i = 1; i <= 114; i++) {
            surahCombo.getItems().add(i);
        }

        // Initialize table columns
        quranDateColumn.setCellValueFactory(data -> data.getValue().dateProperty());
        surahColumn.setCellValueFactory(data -> data.getValue().surahProperty().asObject());
        ayahFromColumn.setCellValueFactory(data -> data.getValue().ayahFromProperty().asObject());
        ayahToColumn.setCellValueFactory(data -> data.getValue().ayahToProperty().asObject());
        durationColumn.setCellValueFactory(data -> data.getValue().durationProperty().asObject());
    }

    private void initializeZikrTracker() {
        zikrDate.setValue(LocalDate.now());
        
        // Initialize table columns
        zikrDateColumn.setCellValueFactory(data -> data.getValue().dateProperty());
        periodColumn.setCellValueFactory(data -> data.getValue().periodProperty());
        completedColumn.setCellValueFactory(data -> data.getValue().completedProperty());
    }

    @FXML
    private void saveRamadanEntry() {
        if (currentUser == null) {
            showError("Error", "Please log in first");
            return;
        }

        int year = ramadanYearCombo.getValue();
        int day = ramadanDayCombo.getValue();
        String notes = ramadanNotes.getText();

        DatabaseHelper.trackRamadanFast(currentUser.getId(), year, day, true, notes)
            .thenAccept(success -> {
                if (success) {
                    showSuccess("Success", "Ramadan fast recorded successfully");
                    loadUserData();
                } else {
                    showError("Error", "Failed to record Ramadan fast");
                }
            });
    }

    @FXML
    private void saveSalahEntry() {
        if (currentUser == null) {
            showError("Error", "Please log in first");
            return;
        }

        Date prayerDate = Date.valueOf(salahDate.getValue());
        String notes = salahNotes.getText();

        DatabaseHelper.trackSalah(currentUser.getId(), prayerDate,
                fajrCheck.isSelected(), dhuhrCheck.isSelected(),
                asrCheck.isSelected(), maghribCheck.isSelected(),
                ishaCheck.isSelected(), notes)
            .thenAccept(success -> {
                if (success) {
                    showSuccess("Success", "Prayers recorded successfully");
                    loadUserData();
                } else {
                    showError("Error", "Failed to record prayers");
                }
            });
    }

    @FXML
    private void saveQuranEntry() {
        if (currentUser == null) {
            showError("Error", "Please log in first");
            return;
        }

        try {
            Date readingDate = Date.valueOf(quranDate.getValue());
            int surah = surahCombo.getValue();
            int ayahFrom = Integer.parseInt(ayahFromField.getText());
            int ayahTo = Integer.parseInt(ayahToField.getText());
            int duration = Integer.parseInt(durationField.getText());
            String notes = quranNotes.getText();

            DatabaseHelper.trackQuranReading(currentUser.getId(), readingDate,
                    surah, ayahFrom, ayahTo, duration, notes)
                .thenAccept(success -> {
                    if (success) {
                        showSuccess("Success", "Quran reading recorded successfully");
                        loadUserData();
                    } else {
                        showError("Error", "Failed to record Quran reading");
                    }
                });
        } catch (NumberFormatException e) {
            showError("Error", "Please enter valid numbers for ayah range and duration");
        }
    }

    @FXML
    private void saveZikrEntry() {
        if (currentUser == null) {
            showError("Error", "Please log in first");
            return;
        }

        Date zikrDateValue = Date.valueOf(zikrDate.getValue());
        String period = morningRadio.isSelected() ? "morning" : "evening";
        boolean completed = zikrCompleted.isSelected();
        String notes = zikrNotes.getText();

        DatabaseHelper.trackZikr(currentUser.getId(), zikrDateValue, period, completed, notes)
            .thenAccept(success -> {
                if (success) {
                    showSuccess("Success", "Zikr recorded successfully");
                    loadUserData();
                } else {
                    showError("Error", "Failed to record Zikr");
                }
            });
    }

    private void loadUserData() {
        // Load user's tracking data from database and populate the tables
        // This will be implemented in the next step
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 