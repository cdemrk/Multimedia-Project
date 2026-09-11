# Multimedia Project - NTUA (2025-2026)
# MediaLab Documents

Despoina Christina Markatou (el21433)


## 1. Development Environment and Technical Specifications
The application was designed and implemented in the **Visual Studio Code (VS Code)** environment, using the **Java 21** programming language. The Graphical User Interface (GUI) was developed utilizing the **JavaFX** library. Project dependencies, such as the JavaFX libraries for the visual components and **Gson** for processing **JSON** data, are managed via **Maven**.

## 2. System Architecture
The application follows the **MVC (Model-View-Controller)** architectural pattern, cleanly separating the business logic from the presentation layer through specialized management classes coordinated by the **SystemState**.

### Management Classes (Managers)
* **UserManager:** Manages system users.
* **CategoryManager:** Manages document categories.
* **DocumentManager:** Manages documents and their complete version history.
* **FollowManager:** Handles document tracking/following by the users.
* **SystemState:** The central class that unifies all managers, ensuring a shared data state across the application.

## 3. Data Structure (JSON)
* **users.json:** Contains ID, username, password, full name, role, and accessCategoryIds.
* **categories.json:** Contains the unique id and name of each category.
* **documents.json:** Contains ID, title, category, creator, and a versions array (version number, date, content).
* **follows.json:** A Map structure where the key is the username and the values are document IDs and the versionAtFollow.

## 4. Code Documentation (Javadoc)
To ensure code quality, comprehensive documentation was implemented using the **Javadoc** tool for the core `DocumentManager` class. Every public method includes a detailed description of its functionality, parameters (`@param`), return values (`@return`), and exceptions (`@throws`).

## 5. Use Case Scenario & Workflow
The application includes pre-loaded data for an immediate demonstration of its features. The primary scenario focuses on the **Default Admin** (username: `medialab`, password: `medialab_2025`):

1. **Login:** Accessing the system with full administrative privileges.
2. **Search:** Locating documents based on title, creator, or category.
3. **Document Management:** Viewing details, enabling/disabling document tracking (follow), and adding a new content version.
4. **User Management:** Creating/modifying users and assigning category access rights.
5. **Category Organization:** Managing thematic categories.

## 6. Execution Instructions
The application can be executed via the terminal from the root folder of the project using the following command:

```bash
mvnd javafx:run
```

------------------------------------------------------------------------------------------

## 1. Περιβάλλον Ανάπτυξης και Τεχνικές Προδιαγραφές
Η εφαρμογή σχεδιάστηκε και υλοποιήθηκε στο περιβάλλον εργασίας **Visual Studio Code (VS Code)**, χρησιμοποιώντας τη γλώσσα προγραμματισμού **Java 21**. Για τη δημιουργία του Γραφικού Περιβάλλοντος Χρήστη (GUI) αξιοποιήθηκε η βιβλιοθήκη **JavaFX**. Η διαχείριση των εξαρτήσεων του έργου, όπως οι βιβλιοθήκες JavaFX για τα οπτικά μέρη και η **Gson** για την επεξεργασία δεδομένων σε μορφή **JSON**, πραγματοποιείται μέσω του εργαλείου **Maven**.

## 2. Αρχιτεκτονική Συστήματος
Η εφαρμογή ακολουθεί το αρχιτεκτονικό πρότυπο **MVC (Model-View-Controller)**, διαχωρίζοντας την επιχειρησιακή λογική από το επίπεδο παρουσίασης μέσω εξειδικευμένων κλάσεων διαχείρισης υπό τον συντονισμό του **SystemState**.

### Κλάσεις Διαχείρισης (Managers)
* **UserManager:** Διαχειρίζεται τους χρήστες.
* **CategoryManager:** Διαχειρίζεται τις κατηγορίες.
* **DocumentManager:** Διαχειρίζεται τα έγγραφα και το πλήρες ιστορικό εκδόσεων.
* **FollowManager:** Χειρίζεται τις παρακολουθήσεις εγγράφων από τους χρήστες.
* **SystemState:** Η κεντρική κλάση που ενοποιεί όλους τους managers, διασφαλίζοντας κοινή κατάσταση δεδομένων.

## 3. Δομή Δεδομένων (JSON)
* **users.json:** Περιλαμβάνει ID, username, password, ονοματεπώνυμο, ρόλο και accessCategoryIds.
* **categories.json:** Περιλαμβάνει το μοναδικό id και το name κάθε κατηγορίας.
* **documents.json:** Περιλαμβάνει ID, τίτλο, κατηγορία, δημιουργό και έναν πίνακα versions (αριθμός έκδοσης, ημερομηνία, περιεχόμενο).
* **follows.json:** Δομή Map με κλειδί το username και τιμές τα ID εγγράφων και το versionAtFollow.

## 4. Τεκμηρίωση Κώδικα (Javadoc)
Για τη διασφάλιση της ποιότητας του κώδικα, εφαρμόστηκε πλήρης τεκμηρίωση μέσω του εργαλείου **Javadoc** στην κεντρική κλάση `DocumentManager`. Κάθε δημόσια μέθοδος περιλαμβάνει αναλυτική περιγραφή λειτουργικότητας, παραμέτρων (`@param`), επιστρεφόμενων τιμών (`@return`) και εξαιρέσεων (`@throws`).

## 6. Σενάριο Χρήσης & Ροή Εργασίας
Η εφαρμογή περιλαμβάνει προεγκατεστημένα δεδομένα για την άμεση επίδειξη των λειτουργιών. Το σενάριο εστιάζει στον **Default Admin** (username: medialab, password: medialab_2025):

1.  **Είσοδος:** Σύνδεση στο σύστημα με πλήρη δικαιώματα.
2.  **Αναζήτηση:** Εντοπισμός εγγράφων με βάση τίτλο, δημιουργό ή κατηγορία.
3.  **Διαχείριση Εγγράφου:** Προβολή λεπτομερειών, ενεργοποίηση/απενεργοποίηση παρακολούθησης και προσθήκη νέας έκδοσης περιεχομένου.
4.  **Διαχείριση Χρηστών:** Δημιουργία/τροποποίηση χρηστών και απόδοση δικαιωμάτων πρόσβασης σε κατηγορίες.
5.  **Οργάνωση Κατηγοριών:** Διαχείριση θεματικών ενοτήτων.

## 7. Οδηγίες Εκτέλεσης
Η εκτέλεση πραγματοποιείται μέσω τερματικού από τον ριζικό φάκελο του project με την εντολή:
`mvnd javafx:run`
