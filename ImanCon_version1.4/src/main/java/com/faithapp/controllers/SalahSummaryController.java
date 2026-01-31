package com.faithapp.controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

public class SalahSummaryController implements Initializable {
    @FXML private TableView<SalahSummaryRow> summaryTable;
    @FXML private TableColumn<SalahSummaryRow, String> dateColumn;
    @FXML private TableColumn<SalahSummaryRow, String> fajrColumn;
    @FXML private TableColumn<SalahSummaryRow, String> dhuhrColumn;
    @FXML private TableColumn<SalahSummaryRow, String> asrColumn;
    @FXML private TableColumn<SalahSummaryRow, String> maghribColumn;
    @FXML private TableColumn<SalahSummaryRow, String> ishaColumn;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TableColumn<SalahSummaryRow, Void> editColumn;
    
    private static final String SALAH_DATA_FILE = "salah_data.txt";

    private List<SalahSummaryRow> allRows = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        fajrColumn.setCellValueFactory(new PropertyValueFactory<>("fajr"));
        dhuhrColumn.setCellValueFactory(new PropertyValueFactory<>("dhuhr"));
        asrColumn.setCellValueFactory(new PropertyValueFactory<>("asr"));
        maghribColumn.setCellValueFactory(new PropertyValueFactory<>("maghrib"));
        ishaColumn.setCellValueFactory(new PropertyValueFactory<>("isha"));
        addEditButtonToTable();
        allRows = loadRows();
        summaryTable.getItems().setAll(allRows);
    }

    private List<SalahSummaryRow> loadRows() {
        List<SalahSummaryRow> rows = new ArrayList<>();
        File file = new File(SALAH_DATA_FILE);
        if (!file.exists()) return rows;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    rows.add(new SalahSummaryRow(
                        parts[0],
                        parts[1].split(":")[1],
                        parts[2].split(":")[1],
                        parts[3].split(":")[1],
                        parts[4].split(":")[1],
                        parts[5].split(":")[1]
                    ));
                }
            }
        } catch (IOException e) { /* ignore */ }
        return rows;
    }

    @FXML
    private void handleFilter() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        if (from == null && to == null) {
            summaryTable.getItems().setAll(allRows);
            return;
        }
        List<SalahSummaryRow> filtered = new ArrayList<>();
        for (SalahSummaryRow row : allRows) {
            LocalDate date = LocalDate.parse(row.getDate());
            if ((from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to))) {
                filtered.add(row);
            }
        }
        summaryTable.getItems().setAll(filtered);
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Summary as CSV");
        fileChooser.setInitialFileName("salah_summary.csv");
        File file = fileChooser.showSaveDialog(summaryTable.getScene().getWindow());
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("Date,Fajr,Dhuhr,Asr,Maghrib,Isha");
                for (SalahSummaryRow row : summaryTable.getItems()) {
                    pw.printf("%s,%s,%s,%s,%s,%s\n", row.getDate(), row.getFajr(), row.getDhuhr(), row.getAsr(), row.getMaghrib(), row.getIsha());
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to export: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void addEditButtonToTable() {
        editColumn.setCellFactory(col -> new TableCell<SalahSummaryRow, Void>() {
            private final Button btn = new Button("Edit");
            {
                btn.setOnAction(e -> {
                    SalahSummaryRow row = getTableView().getItems().get(getIndex());
                    showEditDialog(row);
                });
                btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12; -fx-background-radius: 5; -fx-padding: 2 10;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void showEditDialog(SalahSummaryRow row) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Salah Status");
        dialog.setHeaderText("Edit status for " + row.getDate());
        ComboBox<String> fajrBox = new ComboBox<>();
        ComboBox<String> dhuhrBox = new ComboBox<>();
        ComboBox<String> asrBox = new ComboBox<>();
        ComboBox<String> maghribBox = new ComboBox<>();
        ComboBox<String> ishaBox = new ComboBox<>();
        List<String> options = Arrays.asList("ON_TIME", "LATE", "MISSED");
        fajrBox.getItems().addAll(options); fajrBox.setValue(row.getFajr());
        dhuhrBox.getItems().addAll(options); dhuhrBox.setValue(row.getDhuhr());
        asrBox.getItems().addAll(options); asrBox.setValue(row.getAsr());
        maghribBox.getItems().addAll(options); maghribBox.setValue(row.getMaghrib());
        ishaBox.getItems().addAll(options); ishaBox.setValue(row.getIsha());
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Fajr:"), fajrBox);
        grid.addRow(1, new Label("Dhuhr:"), dhuhrBox);
        grid.addRow(2, new Label("Asr:"), asrBox);
        grid.addRow(3, new Label("Maghrib:"), maghribBox);
        grid.addRow(4, new Label("Isha:"), ishaBox);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                row.setFajr(fajrBox.getValue());
                row.setDhuhr(dhuhrBox.getValue());
                row.setAsr(asrBox.getValue());
                row.setMaghrib(maghribBox.getValue());
                row.setIsha(ishaBox.getValue());
                updateTxtFile(row);
                summaryTable.refresh();
            }
        });
    }

    private void updateTxtFile(SalahSummaryRow updatedRow) {
        File file = new File(SALAH_DATA_FILE);
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(updatedRow.getDate() + ",")) {
                    lines.add(String.format("%s,Fajr:%s,Dhuhr:%s,Asr:%s,Maghrib:%s,Isha:%s",
                        updatedRow.getDate(), updatedRow.getFajr(), updatedRow.getDhuhr(), updatedRow.getAsr(), updatedRow.getMaghrib(), updatedRow.getIsha()));
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) { /* ignore */ }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String l : lines) bw.write(l + "\n");
        } catch (IOException e) { /* ignore */ }
        // Reload allRows and re-filter
        allRows = loadRows();
        handleFilter();
    }

    @FXML
    private void handleShowAll() {
        summaryTable.getItems().setAll(allRows);
    }
    
    @FXML
    private void handleBackToDashboard() {
        try {
            javafx.stage.Stage currentStage = (javafx.stage.Stage) summaryTable.getScene().getWindow();
            currentStage.close();
            
            // Return to the original dashboard window
            javafx.stage.Stage mainDashboard = com.faithapp.controllers.DashboardController.getMainDashboardStage();
            if (mainDashboard != null) {
                mainDashboard.show();
                mainDashboard.toFront();
            } else {
                // Fallback: create new dashboard if main reference is lost
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                javafx.scene.Parent dashboard = loader.load();
                
                javafx.stage.Stage dashboardStage = new javafx.stage.Stage();
                dashboardStage.setTitle("ImanConnect - Dashboard");
                dashboardStage.setScene(new javafx.scene.Scene(dashboard));
                dashboardStage.setMaximized(true);
                dashboardStage.show();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to return to dashboard: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static class SalahSummaryRow {
        private final SimpleStringProperty date, fajr, dhuhr, asr, maghrib, isha;
        public SalahSummaryRow(String date, String fajr, String dhuhr, String asr, String maghrib, String isha) {
            this.date = new SimpleStringProperty(date);
            this.fajr = new SimpleStringProperty(fajr);
            this.dhuhr = new SimpleStringProperty(dhuhr);
            this.asr = new SimpleStringProperty(asr);
            this.maghrib = new SimpleStringProperty(maghrib);
            this.isha = new SimpleStringProperty(isha);
        }
        public String getDate() { return date.get(); }
        public String getFajr() { return fajr.get(); }
        public String getDhuhr() { return dhuhr.get(); }
        public String getAsr() { return asr.get(); }
        public String getMaghrib() { return maghrib.get(); }
        public String getIsha() { return isha.get(); }
        public void setFajr(String fajr) { this.fajr.set(fajr); }
        public void setDhuhr(String dhuhr) { this.dhuhr.set(dhuhr); }
        public void setAsr(String asr) { this.asr.set(asr); }
        public void setMaghrib(String maghrib) { this.maghrib.set(maghrib); }
        public void setIsha(String isha) { this.isha.set(isha); }
    }
} 