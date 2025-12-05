-- ============================================
-- Script de création de la base de données
-- Digital Library - Bibliothèque Numérique
-- ============================================

-- Création de la base de données
CREATE DATABASE IF NOT EXISTS digital_library
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE digital_library;

-- ============================================
-- Table des livres (books)
-- ============================================
DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS readers;

CREATE TABLE books (
    isbn VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    year INT NOT NULL,
    publisher VARCHAR(255) NOT NULL,
    status ENUM('AVAILABLE', 'BORROWED') NOT NULL DEFAULT 'AVAILABLE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Table des lecteurs (readers)
-- ============================================
CREATE TABLE readers (
    subscriber_number VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    max_loan_days INT NOT NULL DEFAULT 14
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Table des emprunts (loans)
-- ============================================
CREATE TABLE loans (
    id VARCHAR(50) PRIMARY KEY,
    book_isbn VARCHAR(20) NOT NULL,
    reader_subscriber_number VARCHAR(50) NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    returned BOOLEAN NOT NULL DEFAULT FALSE,
    return_date DATE NULL,
    FOREIGN KEY (book_isbn) REFERENCES books(isbn) ON DELETE CASCADE,
    FOREIGN KEY (reader_subscriber_number) REFERENCES readers(subscriber_number) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Index pour optimisation des requêtes
-- ============================================
CREATE INDEX idx_loans_reader ON loans(reader_subscriber_number);
CREATE INDEX idx_loans_book ON loans(book_isbn);
CREATE INDEX idx_loans_returned ON loans(returned);
CREATE INDEX idx_loans_due_date ON loans(due_date);

-- ============================================
-- Données de test
-- ============================================

-- Insertion de quelques livres
INSERT INTO books (isbn, title, author, year, publisher, status) VALUES
('9780451524935', '1984', 'George Orwell', 1949, 'Plon', 'AVAILABLE'),
('9782070368228', 'Le Meilleur des mondes', 'Aldous Huxley', 1932, 'Gallimard', 'AVAILABLE'),
('9782070409341', 'Le Petit Prince', 'Antoine de Saint-Exupéry', 1943, 'Gallimard', 'AVAILABLE'),
('9782070360024', 'L''Étranger', 'Albert Camus', 1942, 'Gallimard', 'AVAILABLE'),
('9782253004226', 'Les Misérables', 'Victor Hugo', 1862, 'Le Livre de Poche', 'AVAILABLE');

-- Insertion de quelques lecteurs
INSERT INTO readers (subscriber_number, first_name, last_name, email, max_loan_days) VALUES
('LEC001', 'Jean', 'Dupont', 'jean.dupont@email.com', 21),
('LEC002', 'Marie', 'Martin', 'marie.martin@email.com', 14),
('LEC003', 'Pierre', 'Bernard', 'pierre.bernard@email.com', 28);

-- ============================================
-- Vues utiles
-- ============================================

-- Vue des emprunts en cours avec détails
CREATE OR REPLACE VIEW v_current_loans AS
SELECT 
    l.id,
    b.isbn,
    b.title AS book_title,
    b.author,
    r.subscriber_number,
    CONCAT(r.first_name, ' ', r.last_name) AS reader_name,
    r.email,
    l.borrow_date,
    l.due_date,
    CASE 
        WHEN l.due_date < CURDATE() AND l.returned = FALSE THEN 'EN RETARD'
        WHEN l.returned = TRUE THEN 'RETOURNÉ'
        ELSE 'EN COURS'
    END AS loan_status
FROM loans l
JOIN books b ON l.book_isbn = b.isbn
JOIN readers r ON l.reader_subscriber_number = r.subscriber_number;

-- Vue des livres en retard par lecteur
CREATE OR REPLACE VIEW v_overdue_loans AS
SELECT 
    r.subscriber_number,
    CONCAT(r.first_name, ' ', r.last_name) AS reader_name,
    r.email,
    b.isbn,
    b.title AS book_title,
    l.borrow_date,
    l.due_date,
    DATEDIFF(CURDATE(), l.due_date) AS days_overdue
FROM loans l
JOIN books b ON l.book_isbn = b.isbn
JOIN readers r ON l.reader_subscriber_number = r.subscriber_number
WHERE l.returned = FALSE AND l.due_date < CURDATE()
ORDER BY r.subscriber_number, l.due_date;

-- Vue des statistiques d'emprunts par livre
CREATE OR REPLACE VIEW v_book_stats AS
SELECT 
    b.isbn,
    b.title,
    b.author,
    COUNT(l.id) AS total_loans
FROM books b
LEFT JOIN loans l ON b.isbn = l.book_isbn
GROUP BY b.isbn, b.title, b.author
ORDER BY total_loans DESC;

-- Vue des statistiques d'emprunts par lecteur
CREATE OR REPLACE VIEW v_reader_stats AS
SELECT 
    r.subscriber_number,
    CONCAT(r.first_name, ' ', r.last_name) AS reader_name,
    COUNT(l.id) AS total_loans,
    SUM(CASE WHEN l.returned = FALSE THEN 1 ELSE 0 END) AS current_loans
FROM readers r
LEFT JOIN loans l ON r.subscriber_number = l.reader_subscriber_number
GROUP BY r.subscriber_number, r.first_name, r.last_name
ORDER BY total_loans DESC;
