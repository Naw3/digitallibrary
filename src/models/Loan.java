package models;

import java.time.LocalDate;

/**
 * Model representing a Loan/Emprunt
 */
public class Loan {
    private String id; // internal id
    private String bookIsbn;
    private String readerSubscriberNumber;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned;

    public Loan() { }

    public Loan(String id, String bookIsbn, String readerSubscriberNumber, LocalDate borrowDate, LocalDate dueDate, boolean returned) {
        this.id = id;
        this.bookIsbn = bookIsbn;
        this.readerSubscriberNumber = readerSubscriberNumber;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returned = returned;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookIsbn() { return bookIsbn; }
    public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }

    public String getReaderSubscriberNumber() { return readerSubscriberNumber; }
    public void setReaderSubscriberNumber(String readerSubscriberNumber) { this.readerSubscriberNumber = readerSubscriberNumber; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) { this.borrowDate = borrowDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public boolean isReturned() { return returned; }
    public void setReturned(boolean returned) { this.returned = returned; }
}
