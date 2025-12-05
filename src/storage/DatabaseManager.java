package storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import models.Book;
import models.Loan;
import models.Reader;

/**
 * Classe de gestion de la connexion et des opérations JDBC avec MySQL
 * Utilise le pattern Singleton pour la connexion
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    
    // Configuration de la connexion - À adapter selon votre configuration phpMyAdmin
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/digital_library?useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Mot de passe vide par défaut pour WAMP/XAMPP
    
    private Connection connection;

    private DatabaseManager() {
        connect();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Établit la connexion à la base de données
     */
    private void connect() {
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion à la base de données réussie !");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL non trouvé : " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Récupère la connexion, la rétablit si nécessaire
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }

    /**
     * Ferme la connexion
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }

    // ========================================
    // OPÉRATIONS CRUD POUR LES LIVRES
    // ========================================

    /**
     * Récupère tous les livres de la base de données
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT isbn, title, author, year, publisher, status FROM books";
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Book book = new Book(
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("year"),
                    rs.getString("publisher"),
                    Book.Status.valueOf(rs.getString("status"))
                );
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des livres : " + e.getMessage());
        }
        return books;
    }

    /**
     * Ajoute un nouveau livre
     */
    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, year, publisher, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setInt(4, book.getYear());
            pstmt.setString(5, book.getPublisher());
            pstmt.setString(6, book.getStatus().name());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du livre : " + e.getMessage());
            return false;
        }
    }

    /**
     * Met à jour un livre existant
     */
    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, year = ?, publisher = ?, status = ? WHERE isbn = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3, book.getYear());
            pstmt.setString(4, book.getPublisher());
            pstmt.setString(5, book.getStatus().name());
            pstmt.setString(6, book.getIsbn());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du livre : " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un livre par son ISBN
     */
    public boolean deleteBook(String isbn) {
        String sql = "DELETE FROM books WHERE isbn = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du livre : " + e.getMessage());
            return false;
        }
    }

    /**
     * Recherche un livre par ISBN
     */
    public Optional<Book> findBookByIsbn(String isbn) {
        String sql = "SELECT isbn, title, author, year, publisher, status FROM books WHERE isbn = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Book book = new Book(
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("year"),
                    rs.getString("publisher"),
                    Book.Status.valueOf(rs.getString("status"))
                );
                return Optional.of(book);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du livre : " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Met à jour le statut d'un livre
     */
    public boolean updateBookStatus(String isbn, Book.Status status) {
        String sql = "UPDATE books SET status = ? WHERE isbn = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, isbn);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du statut : " + e.getMessage());
            return false;
        }
    }

    // ========================================
    // OPÉRATIONS CRUD POUR LES LECTEURS
    // ========================================

    /**
     * Récupère tous les lecteurs
     */
    public List<Reader> getAllReaders() {
        List<Reader> readers = new ArrayList<>();
        String sql = "SELECT subscriber_number, first_name, last_name, email, max_loan_days FROM readers";
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Reader reader = new Reader(
                    rs.getString("subscriber_number"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getInt("max_loan_days")
                );
                readers.add(reader);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des lecteurs : " + e.getMessage());
        }
        return readers;
    }

    /**
     * Ajoute un nouveau lecteur
     */
    public boolean addReader(Reader reader) {
        String sql = "INSERT INTO readers (subscriber_number, first_name, last_name, email, max_loan_days) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, reader.getSubscriberNumber());
            pstmt.setString(2, reader.getFirstName());
            pstmt.setString(3, reader.getLastName());
            pstmt.setString(4, reader.getEmail());
            pstmt.setInt(5, reader.getMaxLoanDays());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du lecteur : " + e.getMessage());
            return false;
        }
    }

    /**
     * Met à jour un lecteur existant
     */
    public boolean updateReader(Reader reader) {
        String sql = "UPDATE readers SET first_name = ?, last_name = ?, email = ?, max_loan_days = ? WHERE subscriber_number = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, reader.getFirstName());
            pstmt.setString(2, reader.getLastName());
            pstmt.setString(3, reader.getEmail());
            pstmt.setInt(4, reader.getMaxLoanDays());
            pstmt.setString(5, reader.getSubscriberNumber());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du lecteur : " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprime un lecteur
     */
    public boolean deleteReader(String subscriberNumber) {
        String sql = "DELETE FROM readers WHERE subscriber_number = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, subscriberNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du lecteur : " + e.getMessage());
            return false;
        }
    }

    /**
     * Recherche un lecteur par numéro d'abonné
     */
    public Optional<Reader> findReaderBySubscriberNumber(String subscriberNumber) {
        String sql = "SELECT subscriber_number, first_name, last_name, email, max_loan_days FROM readers WHERE subscriber_number = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, subscriberNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Reader reader = new Reader(
                    rs.getString("subscriber_number"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getInt("max_loan_days")
                );
                return Optional.of(reader);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du lecteur : " + e.getMessage());
        }
        return Optional.empty();
    }

    // ========================================
    // OPÉRATIONS CRUD POUR LES EMPRUNTS
    // ========================================

    /**
     * Récupère tous les emprunts
     */
    public List<Loan> getAllLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT id, book_isbn, reader_subscriber_number, borrow_date, due_date, returned FROM loans";
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Loan loan = new Loan(
                    rs.getString("id"),
                    rs.getString("book_isbn"),
                    rs.getString("reader_subscriber_number"),
                    rs.getDate("borrow_date").toLocalDate(),
                    rs.getDate("due_date").toLocalDate(),
                    rs.getBoolean("returned")
                );
                loans.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des emprunts : " + e.getMessage());
        }
        return loans;
    }

    /**
     * Crée un nouvel emprunt
     */
    public Loan createLoan(String bookIsbn, String readerSubscriberNumber) {
        // Vérifier que le livre est disponible
        Optional<Book> bookOpt = findBookByIsbn(bookIsbn);
        if (!bookOpt.isPresent() || bookOpt.get().getStatus() == Book.Status.BORROWED) {
            return null;
        }

        // Récupérer le lecteur pour connaître le nombre de jours autorisés
        Optional<Reader> readerOpt = findReaderBySubscriberNumber(readerSubscriberNumber);
        if (!readerOpt.isPresent()) {
            return null;
        }

        Reader reader = readerOpt.get();
        String loanId = UUID.randomUUID().toString();
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(reader.getMaxLoanDays());

        String sql = "INSERT INTO loans (id, book_isbn, reader_subscriber_number, borrow_date, due_date, returned) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, loanId);
            pstmt.setString(2, bookIsbn);
            pstmt.setString(3, readerSubscriberNumber);
            pstmt.setDate(4, Date.valueOf(borrowDate));
            pstmt.setDate(5, Date.valueOf(dueDate));
            pstmt.setBoolean(6, false);
            
            if (pstmt.executeUpdate() > 0) {
                // Mettre à jour le statut du livre
                updateBookStatus(bookIsbn, Book.Status.BORROWED);
                return new Loan(loanId, bookIsbn, readerSubscriberNumber, borrowDate, dueDate, false);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de l'emprunt : " + e.getMessage());
        }
        return null;
    }

    /**
     * Enregistre le retour d'un livre
     */
    public boolean returnBook(String loanId) {
        // Récupérer l'emprunt pour avoir l'ISBN du livre
        String selectSql = "SELECT book_isbn FROM loans WHERE id = ? AND returned = FALSE";
        String bookIsbn = null;
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(selectSql)) {
            pstmt.setString(1, loanId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                bookIsbn = rs.getString("book_isbn");
            } else {
                return false; // Emprunt non trouvé ou déjà retourné
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'emprunt : " + e.getMessage());
            return false;
        }

        // Marquer l'emprunt comme retourné
        String updateSql = "UPDATE loans SET returned = TRUE, return_date = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(updateSql)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setString(2, loanId);
            
            if (pstmt.executeUpdate() > 0) {
                // Mettre à jour le statut du livre
                updateBookStatus(bookIsbn, Book.Status.AVAILABLE);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du retour du livre : " + e.getMessage());
        }
        return false;
    }

    /**
     * Récupère les emprunts en retard pour un lecteur
     */
    public List<Loan> getOverdueLoansForReader(String subscriberNumber) {
        List<Loan> overdueLoans = new ArrayList<>();
        String sql = "SELECT id, book_isbn, reader_subscriber_number, borrow_date, due_date, returned " +
                     "FROM loans WHERE reader_subscriber_number = ? AND returned = FALSE AND due_date < CURDATE()";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, subscriberNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Loan loan = new Loan(
                    rs.getString("id"),
                    rs.getString("book_isbn"),
                    rs.getString("reader_subscriber_number"),
                    rs.getDate("borrow_date").toLocalDate(),
                    rs.getDate("due_date").toLocalDate(),
                    rs.getBoolean("returned")
                );
                overdueLoans.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des emprunts en retard : " + e.getMessage());
        }
        return overdueLoans;
    }

    /**
     * Récupère tous les emprunts en retard
     */
    public List<Loan> getAllOverdueLoans() {
        List<Loan> overdueLoans = new ArrayList<>();
        String sql = "SELECT id, book_isbn, reader_subscriber_number, borrow_date, due_date, returned " +
                     "FROM loans WHERE returned = FALSE AND due_date < CURDATE()";
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Loan loan = new Loan(
                    rs.getString("id"),
                    rs.getString("book_isbn"),
                    rs.getString("reader_subscriber_number"),
                    rs.getDate("borrow_date").toLocalDate(),
                    rs.getDate("due_date").toLocalDate(),
                    rs.getBoolean("returned")
                );
                overdueLoans.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des emprunts en retard : " + e.getMessage());
        }
        return overdueLoans;
    }

    /**
     * Récupère les emprunts actifs (non retournés) pour un lecteur
     */
    public List<Loan> getActiveLoansForReader(String subscriberNumber) {
        List<Loan> activeLoans = new ArrayList<>();
        String sql = "SELECT id, book_isbn, reader_subscriber_number, borrow_date, due_date, returned " +
                     "FROM loans WHERE reader_subscriber_number = ? AND returned = FALSE";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, subscriberNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Loan loan = new Loan(
                    rs.getString("id"),
                    rs.getString("book_isbn"),
                    rs.getString("reader_subscriber_number"),
                    rs.getDate("borrow_date").toLocalDate(),
                    rs.getDate("due_date").toLocalDate(),
                    rs.getBoolean("returned")
                );
                activeLoans.add(loan);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des emprunts actifs : " + e.getMessage());
        }
        return activeLoans;
    }

    // ========================================
    // STATISTIQUES
    // ========================================

    /**
     * Compte le nombre total d'emprunts par livre (top N)
     */
    public List<Object[]> getTopBorrowedBooks(int limit) {
        List<Object[]> stats = new ArrayList<>();
        String sql = "SELECT b.title, COUNT(l.id) as loan_count " +
                     "FROM books b LEFT JOIN loans l ON b.isbn = l.book_isbn " +
                     "GROUP BY b.isbn, b.title " +
                     "ORDER BY loan_count DESC LIMIT ?";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                stats.add(new Object[]{rs.getString("title"), rs.getLong("loan_count")});
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des statistiques : " + e.getMessage());
        }
        return stats;
    }

    /**
     * Compte le nombre d'emprunts par lecteur
     */
    public List<Object[]> getLoansCountByReader() {
        List<Object[]> stats = new ArrayList<>();
        String sql = "SELECT CONCAT(r.first_name, ' ', r.last_name) as reader_name, COUNT(l.id) as loan_count " +
                     "FROM readers r LEFT JOIN loans l ON r.subscriber_number = l.reader_subscriber_number " +
                     "GROUP BY r.subscriber_number, r.first_name, r.last_name " +
                     "ORDER BY loan_count DESC";
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stats.add(new Object[]{rs.getString("reader_name"), rs.getLong("loan_count")});
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des statistiques : " + e.getMessage());
        }
        return stats;
    }

    /**
     * Importe une liste de livres dans la base de données
     */
    public int importBooks(List<Book> books) {
        int count = 0;
        for (Book book : books) {
            // Vérifier si le livre existe déjà
            if (!findBookByIsbn(book.getIsbn()).isPresent()) {
                if (addBook(book)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Importe une liste de lecteurs dans la base de données
     */
    public int importReaders(List<Reader> readers) {
        int count = 0;
        for (Reader reader : readers) {
            // Vérifier si le lecteur existe déjà
            if (!findReaderBySubscriberNumber(reader.getSubscriberNumber()).isPresent()) {
                if (addReader(reader)) {
                    count++;
                }
            }
        }
        return count;
    }
}
