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

        loadFromDatabase();
    }

    public static Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }
        return instance;
    }

    public void loadFromDatabase() {
        books.clear();
        readers.clear();
        loans.clear();

        books.addAll(dbManager.getAllBooks());
        readers.addAll(dbManager.getAllReaders());
        loans.addAll(dbManager.getAllLoans());
    }

    public void refresh() {
        loadFromDatabase();
    }

    public ObservableList<Book> getBooks() {
        return books;
    }

    public ObservableList<Reader> getReaders() {
        return readers;
    }

    public ObservableList<Loan> getLoans() {
        return loans;
    }

    public boolean addBook(Book book) {
        if (dbManager.addBook(book)) {
            books.add(book);
            return true;
        }
        return false;
    }

    public boolean updateBook(Book book) {
        if (dbManager.updateBook(book)) {
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

    public Loan borrowBook(String isbn, String subscriberNumber) {
        Loan loan = dbManager.createLoan(isbn, subscriberNumber);
        if (loan != null) {
            loans.add(loan);
            findBookByIsbn(isbn).ifPresent(b -> b.setStatus(Book.Status.BORROWED));
        }
        return loan;
    }

    public boolean returnBook(String loanId) {
        Optional<Loan> loanOpt = loans.stream()
                .filter(l -> l.getId().equals(loanId))
                .findFirst();

        if (loanOpt.isPresent() && dbManager.returnBook(loanId)) {
            Loan loan = loanOpt.get();
            loan.setReturned(true);
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

    public void exportBooksToJson(File file) throws IOException {
        List<Map<String, Object>> bookMaps = books.stream()
                .map(this::bookToMap)
                .collect(Collectors.toList());
        objectMapper.writeValue(file, bookMaps);
    }

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
                } catch (Exception ex) {
                }

                Book.Status status = "emprunté".equalsIgnoreCase(statut) ? Book.Status.BORROWED : Book.Status.AVAILABLE;

                Book b = new Book(isbn, titre, auteur, annee, editeur, status);

                if (addBook(b)) {
                    imported.add(b);
                }
            }
        }
        return imported;
    }

    public void exportBooksToXml(File file) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("bibliotheque");
        doc.appendChild(rootElement);

        for (Book book : books) {
            Element livre = doc.createElement("livre");
            rootElement.appendChild(livre);

            createElement(doc, livre, "isbn", book.getIsbn());
            createElement(doc, livre, "titre", book.getTitle());
            createElement(doc, livre, "auteur", book.getAuthor());
            createElement(doc, livre, "annee", String.valueOf(book.getYear()));
            createElement(doc, livre, "editeur", book.getPublisher());
            createElement(doc, livre, "statut", book.getStatus() == Book.Status.BORROWED ? "emprunté" : "disponible");
        }

        javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory
                .newInstance();
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(doc);
        javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(file);
        transformer.transform(source, result);
    }

    private void createElement(Document doc, Element parent, String tagName, String text) {
        Element elem = doc.createElement(tagName);
        elem.setTextContent(text);
        parent.appendChild(elem);
    }

    public int importBooksFromJson(File jsonFile) throws IOException {
        int count = 0;
        List<Map<String, Object>> bookMaps = objectMapper.readValue(jsonFile,
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                });

        for (Map<String, Object> map : bookMaps) {
            String isbn = (String) map.get("isbn");
            String title = (String) map.get("titre");
            String author = (String) map.get("auteur");
            int year = 0;
            try {
                year = Integer.parseInt(map.get("annee").toString());
            } catch (Exception e) {
            }
            String publisher = (String) map.get("editeur");
            String statusStr = (String) map.get("statut");

            Book.Status status = "emprunté".equalsIgnoreCase(statusStr) ? Book.Status.BORROWED : Book.Status.AVAILABLE;

            Book book = new Book(isbn, title, author, year, publisher, status);
            if (addBook(book)) {
                count++;
            }
        }
        return count;
    }

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

                int joursMax = 14;
                try {
                    joursMax = Integer.parseInt(joursStr);
                } catch (Exception ex) {
                }

                Reader r = new Reader(numeroAbonne, prenom, nom, email, joursMax);

                if (addReader(r)) {
                    imported.add(r);
                }
            }
        }
        return imported;
    }

    public void exportReadersToXml(File file) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("bibliotheque");
        doc.appendChild(rootElement);

        for (Reader reader : readers) {
            Element lecteur = doc.createElement("lecteur");
            rootElement.appendChild(lecteur);

            createElement(doc, lecteur, "numeroAbonne", reader.getSubscriberNumber());
            createElement(doc, lecteur, "prenom", reader.getFirstName());
            createElement(doc, lecteur, "nom", reader.getLastName());
            createElement(doc, lecteur, "email", reader.getEmail());
            createElement(doc, lecteur, "joursEmpruntMax", String.valueOf(reader.getMaxLoanDays()));
        }

        javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory
                .newInstance();
        javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(doc);
        javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(file);
        transformer.transform(source, result);
    }

    public int importReadersFromJson(File jsonFile) throws IOException {
        int count = 0;
        List<Map<String, Object>> readerMaps = objectMapper.readValue(jsonFile,
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
                });

        for (Map<String, Object> map : readerMaps) {
            String subscriberNumber = (String) map.get("numeroAbonne");
            String firstName = (String) map.get("prenom");
            String lastName = (String) map.get("nom");
            String email = (String) map.get("email");
            int maxLoanDays = 14;
            try {
                maxLoanDays = Integer.parseInt(map.get("joursEmpruntMax").toString());
            } catch (Exception e) {
            }

            Reader reader = new Reader(subscriberNumber, firstName, lastName, email, maxLoanDays);
            if (addReader(reader)) {
                count++;
            }
        }
        return count;
    }

    private String getTextContent(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0)
            return null;
        return nodes.item(0).getTextContent();
    }

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

    public boolean canBorrowBook(String isbn) {
        Optional<Book> bopt = findBookByIsbn(isbn);
        return bopt.map(b -> b.getStatus() == Book.Status.AVAILABLE).orElse(false);
    }

    public List<Loan> getOverdueLoansForReader(String subscriberNumber) {
        LocalDate today = LocalDate.now();
        return loans.stream()
                .filter(l -> !l.isReturned())
                .filter(l -> subscriberNumber.equals(l.getReaderSubscriberNumber()))
                .filter(l -> l.getDueDate().isBefore(today))
                .collect(Collectors.toList());
    }

    public List<Loan> getAllOverdueLoans() {
        LocalDate today = LocalDate.now();
        return loans.stream()
                .filter(l -> !l.isReturned())
                .filter(l -> l.getDueDate().isBefore(today))
                .collect(Collectors.toList());
    }

    public List<Loan> getActiveLoansForReader(String subscriberNumber) {
        return loans.stream()
                .filter(l -> !l.isReturned())
                .filter(l -> subscriberNumber.equals(l.getReaderSubscriberNumber()))
                .collect(Collectors.toList());
    }

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
                        LinkedHashMap::new));
    }

    public Map<String, Long> loansCountByReader() {
        return loans.stream()
                .collect(Collectors.groupingBy(Loan::getReaderSubscriberNumber, Collectors.counting()));
    }

    public String getBookTitle(String isbn) {
        return findBookByIsbn(isbn)
                .map(Book::getTitle)
                .orElse(isbn);
    }

    public String getReaderName(String subscriberNumber) {
        return findReaderBySubscriber(subscriberNumber)
                .map(r -> r.getFirstName() + " " + r.getLastName())
                .orElse(subscriberNumber);
    }
}
