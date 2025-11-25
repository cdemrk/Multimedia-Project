package com.multimedia_project.gui;

import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.DocumentVersion;
import com.multimedia_project.model.Category;
import com.multimedia_project.model.Document;
import com.multimedia_project.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentSearchController {

    @FXML private ComboBox<Category> categoryCombo;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TableView<DocumentResult> resultsTable;
    @FXML private TableColumn<DocumentResult, String> titleColumn;
    @FXML private TableColumn<DocumentResult, String> authorColumn;
    @FXML private TableColumn<DocumentResult, String> categoryColumn;
    @FXML private TableColumn<DocumentResult, String> dateColumn;
    @FXML private TableColumn<DocumentResult, Integer> versionColumn;

    private SystemState systemState;
    private User loggedInUser;

    // Καθορίζει την κατάσταση του συστήματος
    public void initializeData(SystemState state, User user) {
        this.systemState = state;
        this.loggedInUser = user;
        
        // Φόρτωση των κατηγοριών στο ComboBox
        loadCategories();
        
        // Ρύθμιση του TableView
        setupResultsTable();
    }

    private void loadCategories() {
        // Παίρνουμε όλες τις κατηγορίες στις οποίες έχει πρόσβαση ο χρήστης
        List<Category> allCategories = systemState.getCategoryManager().getAllCategories();
        
        List<Category> accessibleCategories = allCategories.stream()
            .filter(c -> loggedInUser.canAccessCategory(c.getId()))
            .collect(Collectors.toList());
            
        // Προσθέτουμε μια επιλογή "Όλες οι Κατηγορίες"
        Category all = new Category(0, "All Categories");
        accessibleCategories.add(0, all); 

        categoryCombo.setItems(FXCollections.observableArrayList(accessibleCategories));
        categoryCombo.getSelectionModel().select(0);
    }
    
    private void setupResultsTable() {
        // Ορίζουμε ποιες ιδιότητες (properties) της κλάσης DocumentResult θα εμφανίζονται σε κάθε στήλη
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        versionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
        
        // Μηδενική αρχική αναζήτηση
        handleSearch();
    }

    @FXML
    private void handleSearch() {
        // 1. Συγκέντρωση κριτηρίων
        Integer categoryId = categoryCombo.getSelectionModel().getSelectedItem().getId();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        
        // Αν το ID είναι 0, σημαίνει "Όλες οι Κατηγορίες"
        Integer finalCategoryId = (categoryId == 0) ? null : categoryId;
        
        // 2. Εκτέλεση Αναζήτησης μέσω του Manager
        List<Document> documentsFound = systemState.getDocumentManager().searchDocuments(
            title.isEmpty() ? null : title,
            author.isEmpty() ? null : author,
            finalCategoryId
        );
        
        // 3. Φιλτράρισμα βάσει Δικαιωμάτων Πρόσβασης
        List<Document> finalResults = documentsFound.stream()
            .filter(doc -> loggedInUser.canAccessCategory(doc.getCategoryId()))
            .collect(Collectors.toList());

        // 4. Μετατροπή των Document σε DocumentResult για την εμφάνιση
        ObservableList<DocumentResult> results = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Document doc : finalResults) {
            DocumentVersion latest = doc.getLatestVersion();
            results.add(new DocumentResult(
                doc.getTitle(),
                doc.getAuthorName(),
                systemState.getCategoryManager().getCategoryNameById(doc.getCategoryId()),
                latest.getCreationDate().format(formatter),
                latest.getVersionNumber()
            ));
        }

        resultsTable.setItems(results);
    }
    
    // Κλάση Βοήθειας για την εμφάνιση των αποτελεσμάτων στον TableView
    public static class DocumentResult {
        private final String title;
        private final String author;
        private final String category;
        private final String date;
        private final Integer version;

        public DocumentResult(String title, String author, String category, String date, Integer version) {
            this.title = title;
            this.author = author;
            this.category = category;
            this.date = date;
            this.version = version;
        }

        // --- Getters (Απαραίτητα για την TableView) ---
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public String getCategory() { return category; }
        public String getDate() { return date; }
        public Integer getVersion() { return version; }
    }
}
