package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import models.Book;
import storage.Repository;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Book management (Ajouter/Modifier/Supprimer)
 * Gère également l'import XML et l'export JSON des livres
 */
public class BookController {

    @FXML
    private TableView<Book> bookTable;
    
    @FXML
    private TableColumn<Book, String> isbnColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, Integer> yearColumn;
    @FXML
    private TableColumn<Book, String> publisherColumn;
    @FXML
    private TableColumn<Book, Book.Status> statusColumn;

    @FXML
    private TextField isbnField, titleField, authorField, yearField, publisherField;
    
    @FXML
    private ComboBox<Book.Status> statusComboBox;

    @FXML
    private Button addButton, editButton, deleteButton;
    
    private Repository repository;

    @FXML
    public void initialize() {
        repository = Repository.getInstance();
        
        // Configurer les colonnes de la table
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Lier la table aux données du repository
        bookTable.setItems(repository.getBooks());
        
        // Configurer le ComboBox pour le statut
        statusComboBox.getItems().addAll(Book.Status.AVAILABLE, Book.Status.BORROWED);
        statusComboBox.setValue(Book.Status.AVAILABLE);
        
        // Listener pour remplir les champs quand on sélectionne un livre
        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFieldsWithBook(newSelection);
            }
        });
    }
    
    /**
     * Remplit les champs du formulaire avec les données d'un livre
     */
    private void fillFieldsWithBook(Book book) {
        isbnField.setText(book.getIsbn());
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        yearField.setText(String.valueOf(book.getYear()));
        publisherField.setText(book.getPublisher());
        statusComboBox.setValue(book.getStatus());
    }
    
    /**
     * Efface tous les champs du formulaire
     */
    private void clearFields() {
        isbnField.clear();
        titleField.clear();
        authorField.clear();
        yearField.clear();
        publisherField.clear();
        statusComboBox.setValue(Book.Status.AVAILABLE);
        bookTable.getSelectionModel().clearSelection();
    }
    
    /**
     * Valide les champs obligatoires
     */
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        
        if (isbnField.getText() == null || isbnField.getText().trim().isEmpty()) {
            errors.append("- L'ISBN est obligatoire\n");
        }
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errors.append("- Le titre est obligatoire\n");
        }
        if (authorField.getText() == null || authorField.getText().trim().isEmpty()) {
            errors.append("- L'auteur est obligatoire\n");
        }
        if (yearField.getText() == null || yearField.getText().trim().isEmpty()) {
            errors.append("- L'année est obligatoire\n");
        } else {
            try {
                Integer.parseInt(yearField.getText().trim());
            } catch (NumberFormatException e) {
                errors.append("- L'année doit être un nombre valide\n");
            }
        }
        if (publisherField.getText() == null || publisherField.getText().trim().isEmpty()) {
            errors.append("- L'éditeur est obligatoire\n");
        }
        
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez corriger les erreurs suivantes :", errors.toString());
            return false;
        }
        return true;
    }
    
    /**
     * Crée un objet Book à partir des champs du formulaire
     */
    private Book createBookFromFields() {
        return new Book(
            isbnField.getText().trim(),
            titleField.getText().trim(),
            authorField.getText().trim(),
            Integer.parseInt(yearField.getText().trim()),
            publisherField.getText().trim(),
            statusComboBox.getValue()
        );
    }

    @FXML
    public void onAddBook() {
        if (!validateFields()) return;
        
        // Vérifier si l'ISBN existe déjà
        if (repository.findBookByIsbn(isbnField.getText().trim()).isPresent()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "ISBN existant", 
                "Un livre avec cet ISBN existe déjà dans la base de données.");
            return;
        }
        
        Book book = createBookFromFields();
        
        if (repository.addBook(book)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Livre ajouté avec succès !");
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors de l'ajout du livre.");
        }
    }

    @FXML
    public void onEditBook() {
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", null, "Veuillez sélectionner un livre à modifier.");
            return;
        }
        
        if (!validateFields()) return;
        
        // Dialogue de confirmation
        Optional<ButtonType> result = showConfirmation("Confirmation", 
            "Modifier le livre ?", 
            "Voulez-vous vraiment modifier ce livre ?");
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Book updatedBook = createBookFromFields();
            
            if (repository.updateBook(updatedBook)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Livre modifié avec succès !");
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors de la modification du livre.");
            }
        }
    }

    @FXML
    public void onDeleteBook() {
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", null, "Veuillez sélectionner un livre à supprimer.");
            return;
        }
        
        // Vérifier si le livre est emprunté
        if (selectedBook.getStatus() == Book.Status.BORROWED) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Suppression impossible", 
                "Ce livre est actuellement emprunté. Veuillez attendre son retour avant de le supprimer.");
            return;
        }
        
        // Dialogue de confirmation
        Optional<ButtonType> result = showConfirmation("Confirmation de suppression", 
            "Supprimer le livre ?", 
            "Voulez-vous vraiment supprimer le livre \"" + selectedBook.getTitle() + "\" ?");
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (repository.removeBook(selectedBook)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Livre supprimé avec succès !");
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors de la suppression du livre.");
            }
        }
    }
    
    @FXML
    public void onClearFields() {
        clearFields();
    }

    @FXML
    public void onExportBooks() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter les livres en JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers JSON", "*.json"));
        chooser.setInitialFileName("livres.json");
        File file = chooser.showSaveDialog(bookTable.getScene().getWindow());
        
        if (file == null) return;
        
        try {
            repository.exportBooksToJson(file);
            showAlert(Alert.AlertType.INFORMATION, "Export réussi", null, 
                "Les livres ont été exportés vers :\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'export", null, 
                "Erreur lors de l'export : " + ex.getMessage());
        }
    }

    @FXML
    public void onImportBooksXml() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importer des livres depuis XML");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers XML", "*.xml"));
        File file = chooser.showOpenDialog(bookTable.getScene().getWindow());
        
        if (file == null) return;
        
        try {
            List<Book> imported = repository.importBooksFromXml(file);
            showAlert(Alert.AlertType.INFORMATION, "Import réussi", null, 
                imported.size() + " livre(s) importé(s) depuis :\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'import", null, 
                "Erreur lors de l'import : " + ex.getMessage());
        }
    }
    
    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Affiche un dialogue de confirmation
     */
    private Optional<ButtonType> showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}
