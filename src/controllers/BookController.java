package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import models.Book;
import storage.Repository;
import java.io.File;
import java.io.IOException;

/**
 * Controller for Book management (AJouter/Modifier/Supprimer)
 * Note: Methods are placeholders; implement logic as needed.
 */
public class BookController {

    @FXML
    private TableView<Book> bookTable;

    @FXML
    private TextField isbnField, titleField, authorField, yearField, publisherField;

    @FXML
    private Button addButton, editButton, deleteButton;

    // TODO: Add FXML bindings and implementations
    public void initialize() {
        // initialize table, columns etc.
    }

    @FXML
    public void onAddBook() {
        // placeholder
    }

    @FXML
    public void onEditBook() {
        // placeholder
    }

    @FXML
    public void onDeleteBook() {
        // placeholder
    }

    @FXML
    public void onExportBooks() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Books to JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = chooser.showSaveDialog(isbnField.getScene().getWindow());
        if (file == null) return;
        try {
            Repository.getInstance().exportBooksToJson(file);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Export successful");
            a.setHeaderText(null);
            a.setContentText("Books exported to " + file.getAbsolutePath());
            a.showAndWait();
        } catch (IOException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Failed to export: " + ex.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    public void onImportBooksXml() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Books from XML");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File file = chooser.showOpenDialog(isbnField.getScene().getWindow());
        if (file == null) return;
        try {
            Repository.getInstance().importBooksFromXml(file);
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Books imported from " + file.getAbsolutePath());
            a.setHeaderText(null);
            a.showAndWait();
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Failed to import: " + ex.getMessage());
            a.showAndWait();
        }
    }
}
