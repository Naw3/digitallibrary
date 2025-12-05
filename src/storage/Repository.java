package storage;

import models.Book;
import models.Reader;
import models.Loan;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Repository qui synchronise les données entre la mémoire (ObservableList) et la base de données MySQL.
 * Utilise Jackson pour l'export JSON et DOM pour l'import XML.
 */
public class Repository {

    private static Repository instance;
    
    private final ObservableList<Book> books = FXCollections.observableArrayList();
    private final ObservableList<Reader> readers = FXCollections.observableArrayList();
    private final ObservableList<Loan> loans = FXCollections.observableArrayList();
    
    private final DatabaseManager dbManager;
    private final ObjectMapper objectMapper;

    private Repository() {
        dbManager = DatabaseManager.getInstance();
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // Charger les données depuis la base de données au démarrage
        loadFromDatabase();
    }

    public static Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }
        return instance;
    }

    /**
     * Charge toutes les données depuis la base de données
     */
    public void loadFromDatabase() {
        books.clear();
        readers.clear();
        loans.clear();
        
        books.addAll(dbManager.getAllBooks());
        readers.addAll(dbManager.getAllReaders());
        loans.addAll(dbManager.getAllLoans());
    }

    /**
     * Recharge les données depuis la base
     */
    public void refresh() {
        loadFromDatabase();
    }

    public ObservableList<Book> getBooks() { return books; }
    public ObservableList<Reader> getReaders() { return readers; }
    public ObservableList<Loan> getLoans() { return loans; }

    // ========================================
    // CRUD LIVRES
    // ========================================
    
    public boolean addBook(Book book) {
        if (dbManager.addBook(book)) {
            books.add(book);
            return true;
        }
        return false;
    }
    
    public boolean updateBook(Book book) {
        if (dbManager.updateBook(book)) {
            // Mettre à jour dans la liste observable
            for (int i = 0; i < books.size(); i++) {
                if (books.get(i).getIsbn().equals(book.getIsbn())) {
                    books.set(i, book);
                    break;
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean removeBook(Book book) {
        if (dbManager.deleteBook(book.getIsbn())) {
            books.remove(book);
            return true;
        }
        return false;
    }

    // ========================================
    // CRUD LECTEURS
    // ========================================
    
    public boolean addReader(Reader r) {
        if (dbManager.addReader(r)) {
            readers.add(r);
            return true;
        }
        return false;
    }
    
    public boolean updateReader(Reader r) {
        if (dbManager.updateReader(r)) {
            for (int i = 0; i < readers.size(); i++) {
                if (readers.get(i).getSubscriberNumber().equals(r.getSubscriberNumber())) {
                    readers.set(i, r);
                    break;
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean removeReader(Reader r) {
        if (dbManager.deleteReader(r.getSubscriberNumber())) {
            readers.remove(r);
            return true;
        }
        return false;
    }

    // ========================================
    // EMPRUNTS
    // ========================================
    
    public Loan borrowBook(String isbn, String subscriberNumber) {
        Loan loan = dbManager.createLoan(isbn, subscriberNumber);
        if (loan != null) {
            loans.add(loan);
            // Mettre à jour le statut du livre dans la liste observable
            findBookByIsbn(isbn).ifPresent(b -> b.setStatus(Book.Status.BORROWED));
        }
        return loan;
    }
    
    public boolean returnBook(String loanId) {
        // Trouver l'emprunt pour récupérer l'ISBN
        Optional<Loan> loanOpt = loans.stream()
            .filter(l -> l.getId().equals(loanId))
            .findFirst();
        
        if (loanOpt.isPresent() && dbManager.returnBook(loanId)) {
            Loan loan = loanOpt.get();
            loan.setReturned(true);
            // Mettre à jour le statut du livre
            findBookByIsbn(loan.getBookIsbn()).ifPresent(b -> b.setStatus(Book.Status.AVAILABLE));
            return true;
        }
        return false;
    }
    
    public void addLoan(Loan l) { 
        loans.add(l); 
    }
    
    public void removeLoan(Loan l) { 
        loans.remove(l); 
    }

    // ========================================
    // EXPORT JSON AVEC JACKSON
    // ========================================
    
    /**
     * Exporte les livres au format JSON avec Jackson
     */
    public void exportBooksToJson(File file) throws IOException {
        List<Map<String, Object>> bookMaps = books.stream()
            .map(this::bookToMap)
            .collect(Collectors.toList());
        objectMapper.writeValue(file, bookMaps);
    }

    /**
     * Exporte les lecteurs au format JSON avec Jackson
     */
    public void exportReadersToJson(File file) throws IOException {
        List<Map<String, Object>> readerMaps = readers.stream()
            .map(this::readerToMap)
            .collect(Collectors.toList());
        objectMapper.writeValue(file, readerMaps);
    }

    private Map<String, Object> bookToMap(Book b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("isbn", b.getIsbn());
        m.put("titre", b.getTitle());
        m.put("auteur", b.getAuthor());
        m.put("annee", b.getYear());
        m.put("editeur", b.getPublisher());
        m.put("statut", b.getStatus() == Book.Status.BORROWED ? "emprunté" : "disponible");
        return m;
    }

    private Map<String, Object> readerToMap(Reader r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("numeroAbonne", r.getSubscriberNumber());
        m.put("prenom", r.getFirstName());
        m.put("nom", r.getLastName());
        m.put("email", r.getEmail());
        m.put("joursEmpruntMax", r.getMaxLoanDays());
        return m;
    }

    // ========================================
    // IMPORT XML LIVRES
    // ========================================
    
    /**
     * Importe des livres depuis un fichier XML
     * Format attendu:
     * <bibliotheque>
     *   <livre>
     *     <titre>...</titre>
     *     <auteur>...</auteur>
     *     <annee>...</annee>
     *     <isbn>...</isbn>
     *     <editeur>...</editeur>
     *     <statut>disponible|emprunté</statut>
     *   </livre>
     * </bibliotheque>
     */
    public List<Book> importBooksFromXml(File xmlFile) throws Exception {
        List<Book> imported = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        Element root = doc.getDocumentElement();
        NodeList livreNodes = root.getElementsByTagName("livre");
        
        for (int i = 0; i < livreNodes.getLength(); i++) {
            Node node = livreNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;
                String titre = getTextContent(e, "titre");
                String auteur = getTextContent(e, "auteur");
                String anneeStr = getTextContent(e, "annee");
                String isbn = getTextContent(e, "isbn");
                String editeur = getTextContent(e, "editeur");
                String statut = getTextContent(e, "statut");
                
                int annee = 0;
                try { 
                    annee = Integer.parseInt(anneeStr); 
                } catch (Exception ex) { }
                
                Book.Status status = "emprunté".equalsIgnoreCase(statut) ? 
                    Book.Status.BORROWED : Book.Status.AVAILABLE;
                    
                Book b = new Book(isbn, titre, auteur, annee, editeur, status);
                
                // Ajouter à la base de données
                if (addBook(b)) {
                    imported.add(b);
                }
            }
        }
        return imported;
    }

    // ========================================
    // IMPORT XML LECTEURS
    // ========================================
    
    /**
     * Importe des lecteurs depuis un fichier XML
     * Format attendu:
     * <bibliotheque>
     *   <lecteur>
     *     <numeroAbonne>...</numeroAbonne>
     *     <nom>...</nom>
     *     <prenom>...</prenom>
     *     <email>...</email>
     *     <joursEmpruntMax>...</joursEmpruntMax>
     *   </lecteur>
     * </bibliotheque>
     */
    public List<Reader> importReadersFromXml(File xmlFile) throws Exception {
        List<Reader> imported = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        Element root = doc.getDocumentElement();
        NodeList lecteurNodes = root.getElementsByTagName("lecteur");
        
        for (int i = 0; i < lecteurNodes.getLength(); i++) {
            Node node = lecteurNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;
                String numeroAbonne = getTextContent(e, "numeroAbonne");
                String nom = getTextContent(e, "nom");
                String prenom = getTextContent(e, "prenom");
                String email = getTextContent(e, "email");
                String joursStr = getTextContent(e, "joursEmpruntMax");
                
                int joursMax = 14; // valeur par défaut
                try { 
                    joursMax = Integer.parseInt(joursStr); 
                } catch (Exception ex) { }
                
                Reader r = new Reader(numeroAbonne, prenom, nom, email, joursMax);
                
                // Ajouter à la base de données
                if (addReader(r)) {
                    imported.add(r);
                }
            }
        }
        return imported;
    }

    private String getTextContent(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) return null;
        return nodes.item(0).getTextContent();
    }

    // ========================================
    // RECHERCHE
    // ========================================
    
    public Optional<Book> findBookByIsbn(String isbn) {
        return books.stream()
            .filter(b -> isbn != null && isbn.equals(b.getIsbn()))
            .findFirst();
    }

    public Optional<Reader> findReaderBySubscriber(String sub) {
        return readers.stream()
            .filter(r -> sub != null && sub.equals(r.getSubscriberNumber()))
            .findFirst();
    }

    // ========================================
    // EMPRUNTS - RÈGLES MÉTIER
    // ========================================
    
    /**
     * Vérifie si un livre peut être emprunté
     */
    public boolean canBorrowBook(String isbn) {
        Optional<Book> bopt = findBookByIsbn(isbn);
        return bopt.map(b -> b.getStatus() == Book.Status.AVAILABLE).orElse(false);
    }

    /**
     * Récupère les emprunts en retard pour un lecteur donné
     */
    public List<Loan> getOverdueLoansForReader(String subscriberNumber) {
        LocalDate today = LocalDate.now();
        return loans.stream()
            .filter(l -> !l.isReturned())
            .filter(l -> subscriberNumber.equals(l.getReaderSubscriberNumber()))
            .filter(l -> l.getDueDate().isBefore(today))
            .collect(Collectors.toList());
    }

    /**
     * Récupère tous les emprunts en retard
     */
    public List<Loan> getAllOverdueLoans() {
        LocalDate today = LocalDate.now();
        return loans.stream()
            .filter(l -> !l.isReturned())
            .filter(l -> l.getDueDate().isBefore(today))
            .collect(Collectors.toList());
    }

    /**
     * Récupère les emprunts actifs pour un lecteur
     */
    public List<Loan> getActiveLoansForReader(String subscriberNumber) {
        return loans.stream()
            .filter(l -> !l.isReturned())
            .filter(l -> subscriberNumber.equals(l.getReaderSubscriberNumber()))
            .collect(Collectors.toList());
    }

    // ========================================
    // STATISTIQUES
    // ========================================
    
    /**
     * Top N des livres les plus empruntés
     */
    public Map<String, Long> topBorrowedBooks(int limit) {
        Map<String, Long> counts = loans.stream()
            .collect(Collectors.groupingBy(Loan::getBookIsbn, Collectors.counting()));
        
        return counts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
            .limit(limit)
            .collect(Collectors.toMap(
                Map.Entry::getKey, 
                Map.Entry::getValue, 
                (a, b) -> a, 
                LinkedHashMap::new
            ));
    }

    /**
     * Nombre d'emprunts par lecteur
     */
    public Map<String, Long> loansCountByReader() {
        return loans.stream()
            .collect(Collectors.groupingBy(Loan::getReaderSubscriberNumber, Collectors.counting()));
    }

    /**
     * Récupère le titre d'un livre à partir de son ISBN
     */
    public String getBookTitle(String isbn) {
        return findBookByIsbn(isbn)
            .map(Book::getTitle)
            .orElse(isbn);
    }

    /**
     * Récupère le nom complet d'un lecteur à partir de son numéro d'abonné
     */
    public String getReaderName(String subscriberNumber) {
        return findReaderBySubscriber(subscriberNumber)
            .map(r -> r.getFirstName() + " " + r.getLastName())
            .orElse(subscriberNumber);
    }
}
