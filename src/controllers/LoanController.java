package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import models.Loan;

/**
 * Controller for Emprunts / Retours
 * Placeholder methods only
 */
public class LoanController {

    @FXML
    private TableView<Loan> loanTable;

    @FXML
    private ComboBox<String> bookComboBox; // should be a list of ISBNs or Book objects

    @FXML
    private ComboBox<String> readerComboBox; // should be a list of reader IDs

    @FXML
    private DatePicker borrowDatePicker, dueDatePicker;

    @FXML
    private Button borrowButton, returnButton;

    public void initialize() {
        // setup loan table and controls
    }

    @FXML
    public void onBorrow() {
        // placeholder
    }

    @FXML
    public void onReturn() {
        // placeholder
    }
}
