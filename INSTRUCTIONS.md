# DigitalLibrary — Bibliothèque Numérique

Application JavaFX de gestion de bibliothèque avec base de données MySQL.

## 📋 Fonctionnalités implémentées

### 1. Gestion des livres ✅
- Ajouter / modifier / supprimer un livre
- Champs : ISBN, Titre, Auteur, Année, Éditeur, Statut (disponible / emprunté)
- Export JSON (avec Jackson)
- Import XML

### 2. Gestion des lecteurs ✅
- Ajouter / modifier / supprimer un lecteur
- Champs : Numéro d'abonné, Nom, Prénom, Email, Nombre de jours d'emprunt autorisé
- Export JSON (avec Jackson)
- Import XML

### 3. Emprunts et retours ✅
- Enregistrer un emprunt (livre et lecteur)
- Enregistrer un retour
- Empêcher l'emprunt d'un livre déjà emprunté
- **Lister, par lecteur, les livres en retard**
- Affichage des emprunts en retard

### 4. Interface JavaFX ✅
- Interfaces claires et organisées
- Fichiers FXML éditables avec Scene Builder
- Alertes et dialogues de confirmation
- Validation des champs obligatoires

### 5. Import / Export ✅
- Export JSON des livres et lecteurs (via Jackson)
- Import XML des livres et lecteurs (via DOM)

### 6. Statistiques (bonus) ✅
- Graphique des livres les plus empruntés
- Nombre total d'emprunts par lecteur

---

## 🛠️ Technologies utilisées

- **Java 8**
- **JavaFX + Scene Builder**
- **JDBC avec MySQL** (mysql-connector-java-5.1.23-bin.jar)
- **Jackson** pour JSON (jackson-core, jackson-databind, jackson-annotations)
- **XML via DOM** pour l'import

---

## 📦 Installation

### 1. Configuration de la base de données

1. Ouvrir phpMyAdmin
2. Exécuter le script SQL situé dans `sql/create_database.sql`
3. Ce script crée :
   - La base de données `digital_library`
   - Les tables `books`, `readers`, `loans`
   - Des données de test
   - Des vues utiles pour les statistiques

### 2. Télécharger les JARs Jackson

Télécharger les 3 fichiers JAR Jackson et les placer dans le dossier `lib/` :

- [jackson-core-2.15.2.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar)
- [jackson-databind-2.15.2.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar)
- [jackson-annotations-2.15.2.jar](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar)

### 3. Configuration de la connexion MySQL

Modifier les paramètres dans `src/storage/DatabaseManager.java` si nécessaire :

```java
private static final String URL = "jdbc:mysql://localhost:3306/digital_library?useUnicode=true&characterEncoding=UTF-8";
private static final String USER = "root";
private static final String PASSWORD = ""; // Mot de passe vide par défaut
```

### 4. Ouvrir le projet dans NetBeans

1. Ouvrir NetBeans 24
2. File > Open Project
3. Sélectionner le dossier `DigitalLibrary`
4. Faire un clic droit sur le projet > Properties > Libraries
5. Vérifier que les JARs sont bien référencés

---

## 📁 Structure du projet

```
DigitalLibrary/
├── src/
│   ├── controllers/          # Contrôleurs JavaFX
│   │   ├── BookController.java
│   │   ├── ReaderController.java
│   │   ├── LoanController.java
│   │   ├── StatisticsController.java
│   │   ├── MainController.java
│   │   └── MainApp.java
│   ├── models/               # Modèles de données
│   │   ├── Book.java
│   │   ├── Reader.java
│   │   └── Loan.java
│   ├── storage/              # Couche de persistance
│   │   ├── DatabaseManager.java  # Connexion JDBC MySQL
│   │   └── Repository.java       # Repository avec cache
│   └── view/                 # Fichiers FXML
│       ├── MainView.fxml
│       ├── BookView.fxml
│       ├── ReaderView.fxml
│       ├── LoanView.fxml
│       └── StatisticsView.fxml
├── sql/
│   └── create_database.sql   # Script de création de la BDD
├── data/
│   ├── livres_exemple.xml    # Fichier XML exemple pour import
│   └── lecteurs_exemple.xml  # Fichier XML exemple pour import
├── lib/                      # JARs Jackson (à télécharger)
└── JavaFXGestionInscription/
    └── mysql-connector-java-5.1.23-bin.jar
```

---

## 🚀 Lancement

1. S'assurer que MySQL est démarré (WAMP/XAMPP)
2. Vérifier que la base `digital_library` existe
3. Lancer le projet depuis NetBeans (F6)

---

## 📝 Formats XML pour l'import

### Format XML pour les livres

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bibliotheque>
    <livre>
        <titre>1984</titre>
        <auteur>George Orwell</auteur>
        <annee>1949</annee>
        <isbn>9780451524935</isbn>
        <editeur>Plon</editeur>
        <statut>disponible</statut>
    </livre>
</bibliotheque>
```

### Format XML pour les lecteurs

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bibliotheque>
    <lecteur>
        <numeroAbonne>LEC001</numeroAbonne>
        <nom>Dupont</nom>
        <prenom>Jean</prenom>
        <email>jean.dupont@email.com</email>
        <joursEmpruntMax>21</joursEmpruntMax>
    </lecteur>
</bibliotheque>
```

---

## ✅ Checklist des consignes respectées

| Consigne | Statut |
|----------|--------|
| JDBC avec MySQL | ✅ |
| JavaFX + Scene Builder | ✅ |
| JSON via Jackson | ✅ |
| XML via DOM | ✅ |
| Gestion des livres (CRUD) | ✅ |
| Gestion des lecteurs (CRUD) | ✅ |
| Emprunts et retours | ✅ |
| Empêcher emprunt si déjà emprunté | ✅ |
| Liste livres en retard par lecteur | ✅ |
| Interface claire | ✅ |
| Alertes et confirmations | ✅ |
| Validation champs obligatoires | ✅ |
| Export JSON livres/lecteurs | ✅ |
| Import XML livres/lecteurs | ✅ |
| Statistiques (bonus) | ✅ |
