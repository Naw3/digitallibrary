# DigitalLibrary — Project skeleton copied from JavaFXGestionInscription

This project skeleton mirrors the structure of the JavaFXGestionInscription project but implements the library-specific features requested:

Features (expected):
1. Gestion des livres
   - Ajouter / modifier / supprimer un livre
   - Champs : ISBN, Titre, Auteur, Année, Éditeur, Statut (disponible / emprunté)
2. Gestion des lecteurs
   - Ajouter / modifier / supprimer un lecteur
   - Champs : Numéro d’abonné, Nom, Prénom, Email, Nombre de jours d'emprunt autorisé
3. Emprunts et retours
   - Enregistrer un emprunt (livre et lecteur)
   - Enregistrer un retour
   - Empêcher l’emprunt d’un livre déjà emprunté
   - Lister, par lecteur, les livres en retard
4. Interface JavaFX
   - FXML views are provided (to be opened and refined with Scene Builder)
   - Use alerts, confirm dialogs, and set fields as required via Scene Builder

Structure added:
- `src/models/` — model classes (`Book`, `Reader`, `Loan`)
- `src/controllers/` — controller skeletons: `BookController`, `ReaderController`, `LoanController`, `MainController` (load views into main area)
- `src/view/` — FXML skeletons for `MainView.fxml`, `BookView.fxml`, `ReaderView.fxml`, and `LoanView.fxml`

Notes:
- The provided controllers contain placeholder methods only. Implement controller logic (CRUD operations, validation, loans rules) as needed.
- FXML files are intentionally minimal and designed to be opened and edited with Scene Builder. No advanced layout logic has been added so you can design with drag-and-drop.
- The project currently uses the existing `controllers.DigitalLibrary` `Application` class for launching (Kept from NetBeans template). The `MainView.fxml` controller (`MainController`) loads the other subviews.

How to proceed:
- Open FXML files with Scene Builder and wire the table columns and events as desired.
- Implement the controller methods for the CRUD and borrowing rules.
- Optionally add a persistence layer (flat files, SQLite, or JDBC/MySQL) in a new `storage` package.

If you'd like, I can:
- Add a simple in-memory repository (for demo/testing)
- Convert the project to Maven and add OpenJFX dependencies (if you prefer to use JDK 11+)

Enjoy developing! ;)
