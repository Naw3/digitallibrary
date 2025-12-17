package models;

public class Book {
    private String isbn;
    private String title;
    private String author;
    private int year;
    private String publisher;
    private Status status;

    public enum Status {
        AVAILABLE,
        BORROWED
    }

    public Book() {
    }

    public Book(String isbn, String title, String author, int year, String publisher, Status status) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.publisher = publisher;
        this.status = status;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return title + " (" + isbn + ")";
    }
}
