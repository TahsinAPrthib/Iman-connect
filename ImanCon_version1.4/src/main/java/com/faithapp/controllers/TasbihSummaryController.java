package com.faithapp.controllers;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.database.DatabaseHelper;
import com.faithapp.models.TasbihEntry;
import com.faithapp.models.User;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class TasbihSummaryController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(TasbihSummaryController.class);
    
    @FXML private TableView<TasbihSummaryRow> summaryTable;
    @FXML private TableColumn<TasbihSummaryRow, String> dateColumn;
    @FXML private TableColumn<TasbihSummaryRow, String> dhikrColumn;
    @FXML private TableColumn<TasbihSummaryRow, String> countColumn;
    @FXML private TableColumn<TasbihSummaryRow, String> cyclesColumn;
    @FXML private TableColumn<TasbihSummaryRow, String> totalColumn;
    @FXML private TableColumn<TasbihSummaryRow, String> notesColumn;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private TableColumn<TasbihSummaryRow, Void> editColumn;
    
    private List<TasbihSummaryRow> allRows = new ArrayList<>();
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dhikrColumn.setCellValueFactory(new PropertyValueFactory<>("dhikr"));
        countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        cyclesColumn.setCellValueFactory(new PropertyValueFactory<>("cycles"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        
        addEditButtonToTable();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadTasbihData();
    }
    
    private void loadTasbihData() {
        if (currentUser != null) {
            DatabaseHelper.getTasbihEntries(currentUser.getId())
                .thenAccept(entries -> {
                    Platform.runLater(() -> {
                        allRows.clear();
                        for (TasbihEntry entry : entries) {
                            allRows.add(new TasbihSummaryRow(
                                entry.getDate().toString(),
                                entry.getDhikrName(),
                                String.valueOf(entry.getCount()),
                                String.valueOf(entry.getCycles()),
                                String.valueOf(entry.getTotalCount()),
                                entry.getNotes() != null ? entry.getNotes() : ""
                            ));
                        }
                        summaryTable.getItems().setAll(allRows);
                        logger.info("Loaded {} tasbih entries for summary", entries.size());
                    });
                });
        }
    }

    @FXML
    private void handleFilter() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        if (from == null && to == null) {
            summaryTable.getItems().setAll(allRows);
            return;
        }
        List<TasbihSummaryRow> filtered = new ArrayList<>();
        for (TasbihSummaryRow row : allRows) {
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
        fileChooser.setInitialFileName("tasbih_summary.csv");
        File file = fileChooser.showSaveDialog(summaryTable.getScene().getWindow());
        if (file != null) {
            try (PrintWriter pw = new PrintWriter(file)) {
                pw.println("Date,Dhikr,Count,Cycles,Total,Notes");
                for (TasbihSummaryRow row : summaryTable.getItems()) {
                    pw.printf("%s,%s,%s,%s,%s,%s\n", 
                             row.getDate(), row.getDhikr(), row.getCount(), 
                             row.getCycles(), row.getTotal(), row.getNotes());
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to export: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void addEditButtonToTable() {
        editColumn.setCellFactory(col -> new TableCell<TasbihSummaryRow, Void>() {
            private final Button btn = new Button("Edit");
            {
                btn.setOnAction(e -> {
                    TasbihSummaryRow row = getTableView().getItems().get(getIndex());
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

    private void showEditDialog(TasbihSummaryRow row) {
        Dialog<TasbihSummaryRow> dialog = new Dialog<>();
        dialog.setTitle("Edit Tasbih Entry");
        dialog.setHeaderText("Edit entry for " + row.getDate());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        TextField countField = new TextField(row.getCount());
        TextField cyclesField = new TextField(row.getCycles());
        TextField totalField = new TextField(row.getTotal());
        TextArea notesArea = new TextArea(row.getNotes());
        notesArea.setPrefRowCount(3);

        content.getChildren().addAll(
            new Label("Count:"), countField,
            new Label("Cycles:"), cyclesField,
            new Label("Total:"), totalField,
            new Label("Notes:"), notesArea
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    row.setCount(countField.getText());
                    row.setCycles(cyclesField.getText());
                    row.setTotal(totalField.getText());
                    row.setNotes(notesArea.getText());
                    updateTxtFile(row);
                    return row;
                } catch (NumberFormatException e) {
                    showError("Invalid Input", "Please enter valid numbers for count, cycles, and total.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void updateTxtFile(TasbihSummaryRow updatedRow) {
        // This method is now deprecated since we're using database
        // For now, just reload the data from database
        loadTasbihData();
        handleFilter();
    }

    @FXML
    private void handleShowAll() {
        summaryTable.getItems().setAll(allRows);
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            Stage currentStage = (Stage) summaryTable.getScene().getWindow();
            currentStage.close();
            
            // Return to the original dashboard window
            Stage mainDashboard = com.faithapp.controllers.DashboardController.getMainDashboardStage();
            if (mainDashboard != null) {
                mainDashboard.show();
                mainDashboard.toFront();
                logger.info("Returned to original dashboard from Tasbih Summary");
            } else {
                logger.warn("Main dashboard stage not found, creating new dashboard");
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
            logger.error("Error returning to dashboard: {}", e.getMessage(), e);
            showError("Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class TasbihSummaryRow {
        private final SimpleStringProperty date, dhikr, count, cycles, total, notes;
        
        public TasbihSummaryRow(String date, String dhikr, String count, String cycles, String total, String notes) {
            this.date = new SimpleStringProperty(date);
            this.dhikr = new SimpleStringProperty(dhikr);
            this.count = new SimpleStringProperty(count);
            this.cycles = new SimpleStringProperty(cycles);
            this.total = new SimpleStringProperty(total);
            this.notes = new SimpleStringProperty(notes);
        }
        
        public String getDate() { return date.get(); }
        public String getDhikr() { return dhikr.get(); }
        public String getCount() { return count.get(); }
        public String getCycles() { return cycles.get(); }
        public String getTotal() { return total.get(); }
        public String getNotes() { return notes.get(); }
        
        public void setCount(String count) { this.count.set(count); }
        public void setCycles(String cycles) { this.cycles.set(cycles); }
        public void setTotal(String total) { this.total.set(total); }
        public void setNotes(String notes) { this.notes.set(notes); }
    }
} 