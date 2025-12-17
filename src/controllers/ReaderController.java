package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import storage.Repository;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import models.Reader;

public class ReaderController {

    @FXML
    private TableView<Reader> readerTable;

    @FXML
    private TableColumn<Reader, String> subscriberNumberColumn;
    @FXML
    private TableColumn<Reader, String> firstNameColumn;
    @FXML
    private TableColumn<Reader, String> lastNameColumn;
    @FXML
    private TableColumn<Reader, String> emailColumn;
    @FXML
    private TableColumn<Reader, Integer> maxLoanDaysColumn;

    @FXML
    private TextField subscriberNumberField, firstNameField, lastNameField, emailField, maxLoanDaysField;

    @FXML
    private Button addReaderButton, editReaderButton, deleteReaderButton;

    private Repository repository;

    @FXML
    public void initialize() {
        repository = Repository.getInstance();

        subscriberNumberColumn.setCellValueFactory(new PropertyValueFactory<>("subscriberNumber"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        maxLoanDaysColumn.setCellValueFactory(new PropertyValueFactory<>("maxLoanDays"));

        readerTable.setItems(repository.getReaders());

        readerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFieldsWithReader(newSelection);
            }
        });
    }

    private void fillFieldsWithReader(Reader reader) {
        subscriberNumberField.setText(reader.getSubscriberNumber());
        firstNameField.setText(reader.getFirstName());
        lastNameField.setText(reader.getLastName());
        emailField.setText(reader.getEmail());
        maxLoanDaysField.setText(String.valueOf(reader.getMaxLoanDays()));
    }

    private void clearFields() {
        subscriberNumberField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        maxLoanDaysField.clear();
        readerTable.getSelectionModel().clearSelection();
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (subscriberNumberField.getText() == null || subscriberNumberField.getText().trim().isEmpty()) {
            errors.append("- Le numéro d'abonné est obligatoire\n");
        }
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            errors.append("- Le prénom est obligatoire\n");
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
        }
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            errors.append("- L'email est obligatoire\n");
        } else if (!isValidEmail(emailField.getText().trim())) {
            errors.append("- L'email n'est pas valide\n");
        }
        if (maxLoanDaysField.getText() == null || maxLoanDaysField.getText().trim().isEmpty()) {
            errors.append("- Le nombre de jours d'emprunt est obligatoire\n");
        } else {
            try {
                int days = Integer.parseInt(maxLoanDaysField.getText().trim());
                if (days <= 0) {
                    errors.append("- Le nombre de jours doit être positif\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Le nombre de jours doit être un nombre valide\n");
            }
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez corriger les erreurs suivantes :",
                    errors.toString());
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private Reader createReaderFromFields() {
        return new Reader(
                subscriberNumberField.getText().trim(),
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                emailField.getText().trim(),
                Integer.parseInt(maxLoanDaysField.getText().trim()));
    }

    @FXML
    public void onAddReader() {
        if (!validateFields())
            return;

        if (repository.findReaderBySubscriber(subscriberNumberField.getText().trim()).isPresent()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Numéro d'abonné existant",
                    "Un lecteur avec ce numéro d'abonné existe déjà.");
            return;
        }

        Reader reader = createReaderFromFields();

        if (repository.addReader(reader)) {
            showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Lecteur ajouté avec succès !");
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors de l'ajout du lecteur.");
        }
    }

    @FXML
    public void onEditReader() {
        Reader selectedReader = readerTable.getSelectionModel().getSelectedItem();
        if (selectedReader == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", null, "Veuillez sélectionner un lecteur à modifier.");
            return;
        }

        if (!validateFields())
            return;

        Optional<ButtonType> result = showConfirmation("Confirmation",
                "Modifier le lecteur ?",
                "Voulez-vous vraiment modifier ce lecteur ?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Reader updatedReader = createReaderFromFields();

            if (repository.updateReader(updatedReader)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Lecteur modifié avec succès !");
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors de la modification du lecteur.");
            }
        }
    }

    @FXML
    public void onDeleteReader() {
        Reader selectedReader = readerTable.getSelectionModel().getSelectedItem();
        if (selectedReader == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", null, "Veuillez sélectionner un lecteur à supprimer.");
            return;
        }

        if (!repository.getActiveLoansForReader(selectedReader.getSubscriberNumber()).isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Suppression impossible",
                    "Ce lecteur a des emprunts en cours. Veuillez attendre le retour de tous les livres.");
            return;
        }

        Optional<ButtonType> result = showConfirmation("Confirmation de suppression",
                "Supprimer le lecteur ?",
                "Voulez-vous vraiment supprimer le lecteur " + selectedReader.getFirstName() + " "
                        + selectedReader.getLastName() + " ?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (repository.removeReader(selectedReader)) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Lecteur supprimé avec succès !");
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors de la suppression du lecteur.");
            }
        }
    }

    @FXML
    public void onClearFields() {
        clearFields();
    }

    @FXML
    public void onExportReaders() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter les lecteurs en JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers JSON", "*.json"));
        chooser.setInitialFileName("lecteurs.json");
        File file = chooser.showSaveDialog(readerTable.getScene().getWindow());

        if (file == null)
            return;

        try {
            repository.exportReadersToJson(file);
            showAlert(Alert.AlertType.INFORMATION, "Export réussi", null,
                    "Les lecteurs ont été exportés vers :\n" + file.getAbsolutePath());
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'export", null,
                    "Erreur lors de l'export : " + ex.getMessage());
        }
    }

    @FXML
    public void onImportReadersXml() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importer des lecteurs depuis XML");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers XML", "*.xml"));
        File file = chooser.showOpenDialog(readerTable.getScene().getWindow());

        if (file == null)
            return;

        try {
            List<Reader> imported = repository.importReadersFromXml(file);
            showAlert(Alert.AlertType.INFORMATION, "Import réussi", null,
                    imported.size() + " lecteur(s) importé(s) depuis :\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'import", null,
                    "Erreur lors de l'import : " + ex.getMessage());
        }
    }

    @FXML
    public void onImportReadersJson() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Importer des lecteurs depuis JSON");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers JSON", "*.json"));
        File file = chooser.showOpenDialog(readerTable.getScene().getWindow());

        if (file == null)
            return;

        try {
            int count = repository.importReadersFromJson(file);
            showAlert(Alert.AlertType.INFORMATION, "Import réussi", null,
                    count + " lecteur(s) importé(s) depuis :\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'import", null,
                    "Erreur lors de l'import : " + ex.getMessage());
        }
    }

    @FXML
    public void onExportReadersXml() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exporter les lecteurs en XML");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers XML", "*.xml"));
        chooser.setInitialFileName("lecteurs.xml");
        File file = chooser.showSaveDialog(readerTable.getScene().getWindow());

        if (file == null)
            return;

        try {
            repository.exportReadersToXml(file);
            showAlert(Alert.AlertType.INFORMATION, "Export réussi", null,
                    "Les lecteurs ont été exportés vers :\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'export", null,
                    "Erreur lors de l'export : " + ex.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}
