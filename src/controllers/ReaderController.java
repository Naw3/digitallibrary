package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import storage.Repository;
import java.io.File;
import java.io.IOException;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import models.Reader;

/**
 * Controller for Reader management (AJouter/Modifier/Supprimer)
 * Placeholder methods only
 */
public class ReaderController {

    @FXML
    private TableView<Reader> readerTable;

    @FXML
    private TextField subscriberNumberField, firstNameField, lastNameField, emailField, maxLoanDaysField;

    @FXML
    private Button addReaderButton, editReaderButton, deleteReaderButton;

    public void initialize() {
        // initialize table, columns etc.
    }

    @FXML
    public void onAddReader() {
        // placeholder
    }

    @FXML
    public void onEditReader() {
        // placeholder
    }

    @FXML
    public void onDeleteReader() {
        // placeholder
    }

    @FXML
    public void onExportReaders() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Readers to JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = chooser.showSaveDialog(subscriberNumberField.getScene().getWindow());
        if (file == null) return;
        try {
            Repository.getInstance().exportReadersToJson(file);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Export successful");
            a.setHeaderText(null);
            a.setContentText("Readers exported to " + file.getAbsolutePath());
            a.showAndWait();
        } catch (IOException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Failed to export: " + ex.getMessage());
            a.showAndWait();
        }
    }
}
