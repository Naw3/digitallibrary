package storage;

import models.Book;
import models.Reader;
import models.Loan;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

/**
 * Simple in-memory repository for Books, Readers, and Loans
 * Provides import/export utilities (JSON export, XML import) and statistics helpers
 */
public class Repository {

    private static Repository instance;

    private final ObservableList<Book> books = FXCollections.observableArrayList();
    private final ObservableList<Reader> readers = FXCollections.observableArrayList();
    private final ObservableList<Loan> loans = FXCollections.observableArrayList();

    private Repository() {}

    public static Repository getInstance() {
        if (instance == null) instance = new Repository();
        return instance;
    }

    public ObservableList<Book> getBooks() { return books; }
    public ObservableList<Reader> getReaders() { return readers; }
    public ObservableList<Loan> getLoans() { return loans; }

    // CRUD helpers
    public void addBook(Book book) { books.add(book); }
    public void removeBook(Book book) { books.remove(book); }

    public void addReader(Reader r) { readers.add(r); }
    public void removeReader(Reader r) { readers.remove(r); }

    public void addLoan(Loan l) { loans.add(l); }
    public void removeLoan(Loan l) { loans.remove(l); }

    // Export books or readers to JSON
    public void exportBooksToJson(File file) throws IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(toJsonArray(books.stream().map(this::bookToMap).collect(Collectors.toList())));
        }
    }

    public void exportReadersToJson(File file) throws IOException {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(toJsonArray(readers.stream().map(this::readerToMap).collect(Collectors.toList())));
        }
    }

    private Map<String, Object> bookToMap(Book b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("isbn", b.getIsbn());
        m.put("title", b.getTitle());
        m.put("author", b.getAuthor());
        m.put("year", b.getYear());
        m.put("publisher", b.getPublisher());
        m.put("status", b.getStatus() != null ? b.getStatus().name().toLowerCase() : null);
        return m;
    }

    private Map<String, Object> readerToMap(Reader r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("subscriberNumber", r.getSubscriberNumber());
        m.put("firstName", r.getFirstName());
        m.put("lastName", r.getLastName());
        m.put("email", r.getEmail());
        m.put("maxLoanDays", r.getMaxLoanDays());
        return m;
    }

    private String toJsonArray(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Map<String, Object> m : list) {
            if (!first) sb.append(",\n");
            sb.append("  {");
            boolean innerFirst = true;
            for (Map.Entry<String, Object> e : m.entrySet()) {
                if (!innerFirst) sb.append(", ");
                sb.append('\"').append(escapeJson(e.getKey())).append('\"').append(": ");
                Object v = e.getValue();
                if (v == null) sb.append("null");
                else if (v instanceof Number || v instanceof Boolean) sb.append(v.toString());
                else sb.append('"').append(escapeJson(String.valueOf(v))).append('"');
                innerFirst = false;
            }
            sb.append("}");
            first = false;
        }
        sb.append("\n]");
        return sb.toString();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    // Import books from XML format provided in the prompt
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
                try { annee = Integer.parseInt(anneeStr); } catch (Exception ex) { }
                Book b = new Book(isbn, titre, auteur, annee, editeur, "emprunté".equalsIgnoreCase(statut) ? Book.Status.BORROWED : Book.Status.AVAILABLE);
                imported.add(b);
            }
        }
        books.addAll(imported);
        return imported;
    }

    private String getTextContent(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) return null;
        return nodes.item(0).getTextContent();
    }

    // Example statistics helper: top borrowed books by counting loans
    public Map<String, Long> topBorrowedBooks(int limit) {
        Map<String, Long> counts = loans.stream()
                .collect(Collectors.groupingBy(Loan::getBookIsbn, Collectors.counting()));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b)->a, LinkedHashMap::new));
    }

    // Number of loans per reader
    public Map<String, Long> loansCountByReader() {
        return loans.stream()
                .collect(Collectors.groupingBy(Loan::getReaderSubscriberNumber, Collectors.counting()));
    }

    // A convenience method to find a Book by ISBN
    public Optional<Book> findBookByIsbn(String isbn) {
        return books.stream().filter(b -> isbn != null && isbn.equals(b.getIsbn())).findFirst();
    }

    // and for readers
    public Optional<Reader> findReaderBySubscriber(String sub) {
        return readers.stream().filter(r -> sub != null && sub.equals(r.getSubscriberNumber())).findFirst();
    }

    // Basic borrow rule: disallow borrow if the book status is BORROWED
    public boolean canBorrowBook(String isbn) {
        Optional<Book> bopt = findBookByIsbn(isbn);
        return bopt.map(b -> b.getStatus() == Book.Status.AVAILABLE).orElse(false);
    }

    public Loan borrowBook(String isbn, String subscriberNumber) {
        Optional<Book> bopt = findBookByIsbn(isbn);
        Optional<Reader> ropt = findReaderBySubscriber(subscriberNumber);
        if (!bopt.isPresent() || !ropt.isPresent()) return null;
        Book b = bopt.get();
        Reader r = ropt.get();
        if (b.getStatus() == Book.Status.BORROWED) return null;
        b.setStatus(Book.Status.BORROWED);
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(r.getMaxLoanDays() > 0 ? r.getMaxLoanDays() : 14);
        Loan l = new Loan(UUID.randomUUID().toString(), isbn, subscriberNumber, borrowDate, dueDate, false);
        loans.add(l);
        return l;
    }

    public boolean returnBook(String isbn, String subscriberNumber) {
        Optional<Loan> loanOpt = loans.stream()
                .filter(l -> !l.isReturned() && isbn.equals(l.getBookIsbn()) && subscriberNumber.equals(l.getReaderSubscriberNumber()))
                .findFirst();
        if (!loanOpt.isPresent()) return false;
        Loan l = loanOpt.get();
        l.setReturned(true);
        findBookByIsbn(isbn).ifPresent(b -> b.setStatus(Book.Status.AVAILABLE));
        return true;
    }
}
