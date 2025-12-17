package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Book;
import models.Loan;
import models.Reader;
import storage.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class LoanController {

    @FXML
    private TableView<Loan> loanTable;

    @FXML
    private TableColumn<Loan, String> loanIdColumn;
    @FXML
    private TableColumn<Loan, String> bookTitleColumn;
    @FXML
    private TableColumn<Loan, String> readerNameColumn;
    @FXML
    private TableColumn<Loan, String> borrowDateColumn;
    @FXML
    private TableColumn<Loan, String> dueDateColumn;
    @FXML
    private TableColumn<Loan, String> statusColumn;

    @FXML
    private ComboBox<Book> bookComboBox;

    @FXML
    private ComboBox<Reader> readerComboBox;

    @FXML
    private Button borrowButton, returnButton;

    @FXML
    private Label overdueCountLabel;

    @FXML
    private TableView<Loan> overdueTable;

    @FXML
    private TableColumn<Loan, String> overdueBookColumn;
    @FXML
    private TableColumn<Loan, String> overdueReaderColumn;
    @FXML
    private TableColumn<Loan, String> overdueDateColumn;
    @FXML
    private TableColumn<Loan, String> overdueDaysColumn;

    private Repository repository;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        repository = Repository.getInstance();

        loanIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        bookTitleColumn.setCellValueFactory(cellData -> {
            String isbn = cellData.getValue().getBookIsbn();
            String title = repository.getBookTitle(isbn);
            return new SimpleStringProperty(title);
        });

        readerNameColumn.setCellValueFactory(cellData -> {
            String subscriberNumber = cellData.getValue().getReaderSubscriberNumber();
            String name = repository.getReaderName(subscriberNumber);
            return new SimpleStringProperty(name);
        });

        borrowDateColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getBorrowDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });

        dueDateColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDueDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });

        statusColumn.setCellValueFactory(cellData -> {
            Loan loan = cellData.getValue();
            if (loan.isReturned()) {
                return new SimpleStringProperty("Retourné");
            } else if (loan.getDueDate().isBefore(LocalDate.now())) {
                return new SimpleStringProperty("EN RETARD");
            } else {
                return new SimpleStringProperty("En cours");
            }
        });

        if (overdueTable != null) {
            setupOverdueTable();
        }

        refreshLoanTable();

        setupComboBoxes();

        updateOverdueCount();
    }

    private void setupComboBoxes() {
        ObservableList<Book> availableBooks = FXCollections.observableArrayList();
        for (Book book : repository.getBooks()) {
            if (book.getStatus() == Book.Status.AVAILABLE) {
                availableBooks.add(book);
            }
        }
        bookComboBox.setItems(availableBooks);

        readerComboBox.setItems(repository.getReaders());
    }

    private void setupOverdueTable() {
        overdueBookColumn.setCellValueFactory(cellData -> {
            String isbn = cellData.getValue().getBookIsbn();
            String title = repository.getBookTitle(isbn);
            return new SimpleStringProperty(title);
        });

        overdueReaderColumn.setCellValueFactory(cellData -> {
            String subscriberNumber = cellData.getValue().getReaderSubscriberNumber();
            String name = repository.getReaderName(subscriberNumber);
            return new SimpleStringProperty(name);
        });

        overdueDateColumn.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getDueDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });

        overdueDaysColumn.setCellValueFactory(cellData -> {
            LocalDate dueDate = cellData.getValue().getDueDate();
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
            return new SimpleStringProperty(daysOverdue + " jour(s)");
        });

        refreshOverdueTable();
    }

    private void refreshLoanTable() {
        loanTable.setItems(repository.getLoans());
    }

    private void refreshOverdueTable() {
        if (overdueTable != null) {
            List<Loan> overdueLoans = repository.getAllOverdueLoans();
            overdueTable.setItems(FXCollections.observableArrayList(overdueLoans));
        }
    }

    private void updateOverdueCount() {
        if (overdueCountLabel != null) {
            int count = repository.getAllOverdueLoans().size();
            overdueCountLabel.setText("Emprunts en retard : " + count);
            if (count > 0) {
                overdueCountLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else {
                overdueCountLabel.setStyle("-fx-text-fill: green;");
            }
        }
    }

    @FXML
    public void onBorrow() {
        Book selectedBook = bookComboBox.getValue();
        Reader selectedReader = readerComboBox.getValue();

        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", null, "Veuillez sélectionner un livre.");
            return;
        }
        if (selectedReader == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", null, "Veuillez sélectionner un lecteur.");
            return;
        }
        int loansThisMonth = repository.getLoansCountThisMonth(selectedReader.getSubscriberNumber());
        if (loansThisMonth >= 2) {
            showAlert(Alert.AlertType.WARNING, "Attention", null, "Limite atteinte : ce lecteur a déjà " + loansThisMonth + " emprunt(s) ce mois-ci.");
            return;
        }

        if (!repository.canBorrowBook(selectedBook.getIsbn())) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Emprunt impossible",
                    "Ce livre est déjà emprunté et ne peut pas être emprunté à nouveau.");
            return;
        }

        List<Loan> overdueLoans = repository.getOverdueLoansForReader(selectedReader.getSubscriberNumber());
        if (!overdueLoans.isEmpty()) {
            Optional<ButtonType> result = showConfirmation("Attention - Retards",
                    "Ce lecteur a " + overdueLoans.size() + " livre(s) en retard !",
                    "Voulez-vous quand même continuer l'emprunt ?");
            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return;
            }
        }

        Loan loan = repository.borrowBook(selectedBook.getIsbn(), selectedReader.getSubscriberNumber());

        if (loan != null) {
            showAlert(Alert.AlertType.INFORMATION, "Emprunt enregistré", null,
                    "Le livre \"" + selectedBook.getTitle() + "\" a été emprunté par " +
                            selectedReader.getFirstName() + " " + selectedReader.getLastName() + ".\n\n" +
                            "Date de retour prévue : " + loan.getDueDate().format(dateFormatter));

            setupComboBoxes();
            refreshLoanTable();
            updateOverdueCount();
            bookComboBox.setValue(null);
            readerComboBox.setValue(null);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors de l'enregistrement de l'emprunt.");
        }
    }

    @FXML
    public void onReturn() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();

        if (selectedLoan == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", null,
                    "Veuillez sélectionner un emprunt dans la table.");
            return;
        }

        if (selectedLoan.isReturned()) {
            showAlert(Alert.AlertType.WARNING, "Attention", null,
                    "Cet emprunt a déjà été retourné.");
            return;
        }

        String bookTitle = repository.getBookTitle(selectedLoan.getBookIsbn());
        String readerName = repository.getReaderName(selectedLoan.getReaderSubscriberNumber());

        String message = "Confirmer le retour du livre \"" + bookTitle + "\" emprunté par " + readerName + " ?";

        if (selectedLoan.getDueDate().isBefore(LocalDate.now())) {
            long daysLate = ChronoUnit.DAYS.between(selectedLoan.getDueDate(), LocalDate.now());
            message += "\n\n⚠️ ATTENTION : Ce livre est en retard de " + daysLate + " jour(s) !";
        }

        Optional<ButtonType> result = showConfirmation("Confirmation de retour", "Retour de livre", message);

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (repository.returnBook(selectedLoan.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Retour enregistré", null,
                        "Le livre \"" + bookTitle + "\" a été retourné avec succès.");

                setupComboBoxes();
                refreshLoanTable();
                refreshOverdueTable();
                updateOverdueCount();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Erreur lors de l'enregistrement du retour.");
            }
        }
    }

    @FXML
    public void onShowOverdueByReader() {
        Reader selectedReader = readerComboBox.getValue();

        if (selectedReader == null) {
            showAllOverdueLoans();
            return;
        }

        List<Loan> overdueLoans = repository.getOverdueLoansForReader(selectedReader.getSubscriberNumber());

        if (overdueLoans.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Aucun retard", null,
                    "Le lecteur " + selectedReader.getFirstName() + " " + selectedReader.getLastName() +
                            " n'a aucun livre en retard.");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Livres en retard pour ").append(selectedReader.getFirstName())
                    .append(" ").append(selectedReader.getLastName()).append(" :\n\n");

            for (Loan loan : overdueLoans) {
                String bookTitle = repository.getBookTitle(loan.getBookIsbn());
                long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
                sb.append("• ").append(bookTitle)
                        .append(" (retard de ").append(daysLate).append(" jour(s))\n")
                        .append("  Date prévue : ").append(loan.getDueDate().format(dateFormatter)).append("\n\n");
            }

            showAlert(Alert.AlertType.WARNING, "Livres en retard",
                    overdueLoans.size() + " livre(s) en retard", sb.toString());
        }
    }

    private void showAllOverdueLoans() {
        List<Loan> overdueLoans = repository.getAllOverdueLoans();

        if (overdueLoans.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Aucun retard", null,
                    "Aucun livre n'est actuellement en retard.");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Tous les livres en retard :\n\n");

            for (Loan loan : overdueLoans) {
                String bookTitle = repository.getBookTitle(loan.getBookIsbn());
                String readerName = repository.getReaderName(loan.getReaderSubscriberNumber());
                long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
                sb.append("• ").append(bookTitle)
                        .append("\n  Emprunteur : ").append(readerName)
                        .append("\n  Retard : ").append(daysLate).append(" jour(s)")
                        .append("\n  Date prévue : ").append(loan.getDueDate().format(dateFormatter)).append("\n\n");
            }

            showAlert(Alert.AlertType.WARNING, "Livres en retard",
                    overdueLoans.size() + " livre(s) en retard au total", sb.toString());
        }
    }

    @FXML
    public void onRefresh() {
        repository.refresh();
        setupComboBoxes();
        refreshLoanTable();
        refreshOverdueTable();
        updateOverdueCount();
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
