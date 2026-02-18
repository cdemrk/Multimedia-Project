package com.multimedia_project.gui;

import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.DocumentVersion;
import com.multimedia_project.model.Category;
import com.multimedia_project.model.Document;
import com.multimedia_project.model.User;
import com.multimedia_project.model.Role;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DocumentManagementController {

    @FXML private ComboBox<Category> categoryFilterCombo;
    @FXML private ListView<Document> documentListView;
    @FXML private TextArea contentDisplayArea;
    @FXML private Label versionInfoLabel;
    @FXML private ComboBox<Integer> versionSelectorCombo;
    
    @FXML private Button newDocumentButton;
    @FXML private Button editDocumentButton;
    @FXML private Button deleteDocumentButton;
    @FXML private Button saveChangesButton;
    
    private SystemState systemState;
    private MainController mainController;
    private User loggedInUser;
    private Document selectedDocument;
    private boolean isEditing = false;
    
    public void initializeData(SystemState state, User user) {
        this.systemState = state;
        this.loggedInUser = user;
        
        // 1. Φόρτωση κατηγοριών
        loadCategoryFilters();
        
        // 2. Ρύθμιση εμφάνισης λίστας (για να βλέπουμε τον τίτλο)
        documentListView.setCellFactory(lv -> new ListCell<Document>() {
            @Override
            protected void updateItem(Document item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTitle());
            }
        });

        // 3. Επιλογή πρώτης κατηγορίας και αρχικό φιλτράρισμα
        if (!categoryFilterCombo.getItems().isEmpty()) {
            categoryFilterCombo.getSelectionModel().selectFirst();
            refreshMyDocuments();
        }
        
        // 4. Listener για επιλογή εγγράφου
        documentListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> showDocumentDetails(newVal)
        );
        
        saveChangesButton.setVisible(false);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void loadCategoryFilters() {
        if (systemState == null || loggedInUser == null) return;
        
        List<Category> categories;
        if (loggedInUser.getRole() == Role.Admin) {
            // Ο Admin βλέπει όλες τις κατηγορίες για να μπορεί να φιλτράρει τα πάντα
            categories = systemState.getCategoryManager().getAllCategories();
        } else {
            // Οι υπόλοιποι βλέπουν μόνο όσες έχουν πρόσβαση
            categories = systemState.getCategoryManager().getAllCategories().stream()
                .filter(c -> loggedInUser.canAccessCategory(c.getId()))
                .collect(Collectors.toList());
        }
        
        categoryFilterCombo.setItems(FXCollections.observableArrayList(categories));
    }

    @FXML
    private void filterDocumentsByCategory() {
        refreshMyDocuments();
    }


    private void refreshMyDocuments() {
        Category selectedCat = categoryFilterCombo.getSelectionModel().getSelectedItem();
        if (selectedCat == null || loggedInUser == null) return;

        List<Document> filteredDocs = systemState.getDocumentManager().getAllDocuments().stream()
            .filter(doc -> doc.getCategoryId() == selectedCat.getId()) 
            .filter(doc -> {
                // 1. Admin: Βλέπει τα πάντα στην επιλεγμένη κατηγορία
                if (loggedInUser.getRole() == Role.Admin) return true;
                
                // 2. Author: Βλέπει τα πάντα στην κατηγορία ΑΝ έχει access σε αυτήν
                if (loggedInUser.getRole() == Role.Author) {
                    return loggedInUser.canAccessCategory(doc.getCategoryId());
                }
                
                // 3. Simple User: Μόνο τα δικά του
                return doc.getAuthorId() == loggedInUser.getId();
            })
            .collect(Collectors.toList());
                        
        documentListView.setItems(FXCollections.observableArrayList(filteredDocs));
    }

    
    private void showDocumentDetails(Document doc) {
        if (doc == null) {
            contentDisplayArea.setText("");
            versionInfoLabel.setText("");
            versionSelectorCombo.getItems().clear();
            selectedDocument = null;
            // Απενεργοποίηση κουμπιών αν δεν υπάρχει επιλεγμένο έγγραφο
            editDocumentButton.setDisable(true);
            deleteDocumentButton.setDisable(true);
            return;
        }
        
        this.selectedDocument = doc;
        isEditing = false;
        contentDisplayArea.setEditable(false);
        saveChangesButton.setVisible(false);

        // --- ΕΛΕΓΧΟΣ ΔΙΚΑΙΩΜΑΤΩΝ ---
        boolean canModify = false;

        if (loggedInUser.getRole() == Role.Admin) {
            // Ο Admin μπορεί να τροποποιήσει τα πάντα
            canModify = true;
        } else if (loggedInUser.getRole() == Role.Author) {
            // Ο Author μπορεί να τροποποιήσει ό,τι ανήκει στις κατηγορίες του
            canModify = loggedInUser.canAccessCategory(doc.getCategoryId());
        } else {
            // Ο Simple User (αν έχει πρόσβαση στο view) μόνο τα δικά του
            canModify = (doc.getAuthorId() == loggedInUser.getId());
        }

        // Εφαρμογή των δικαιωμάτων στα κουμπιά
        editDocumentButton.setDisable(!canModify);
        deleteDocumentButton.setDisable(!canModify);

        // --- ΥΠΟΛΟΙΠΗ ΛΕΙΤΟΥΡΓΙΚΟΤΗΤΑ ---
        // Γέμισμα εκδόσεων
        ObservableList<Integer> versions = doc.getVersions().stream()
            .map(DocumentVersion::getVersionNumber)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
                
        versionSelectorCombo.setItems(versions);
        versionSelectorCombo.getSelectionModel().selectLast();
        
        updateDisplay(doc.getLatestVersion());
    }

    @FXML
    private void handleVersionChange() {
        if (selectedDocument == null || versionSelectorCombo.getSelectionModel().isEmpty()) return;
        int vn = versionSelectorCombo.getSelectionModel().getSelectedItem();
        selectedDocument.getVersions().stream()
            .filter(v -> v.getVersionNumber() == vn)
            .findFirst()
            .ifPresent(this::updateDisplay);
    }

    private void updateDisplay(DocumentVersion v) {
        contentDisplayArea.setText(v.getContent());
        versionInfoLabel.setText("V" + v.getVersionNumber() + " | " + v.getCreationDate().toLocalDate());
    }


    @FXML
    private void handleNewDocument() {
        // 1. Δημιουργία ενός Custom Dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Document");
        dialog.setHeaderText("Enter details for the new document");

        // 2. Ορισμός των Buttons
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // 3. Δημιουργία του Layout του διαλόγου
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        
        ComboBox<Category> catCombo = new ComboBox<>(categoryFilterCombo.getItems());
        catCombo.getSelectionModel().selectFirst();

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter initial content here...");
        contentArea.setPrefRowCount(10);

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(catCombo, 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(contentArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // 4. Επεξεργασία του αποτελέσματος
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == createButtonType) {
            String title = titleField.getText().trim();
            Category selectedCat = catCombo.getValue();
            String content = contentArea.getText();

            if (title.isEmpty() || selectedCat == null) {
                new Alert(Alert.AlertType.ERROR, "Title and Category are required!").show();
                return;
            }

            // 5. Δημιουργία εγγράφου με το ΠΡΑΓΜΑΤΙΚΟ περιεχόμενο ως Version 1
            Document newDoc = systemState.getDocumentManager().createDocument(
                title, 
                selectedCat.getId(), 
                loggedInUser.getId(), 
                loggedInUser.getUsername(),
                content
            );

            if (newDoc != null) {
                refreshMyDocuments();
                documentListView.getSelectionModel().select(newDoc);
                showDocumentDetails(newDoc);
            }
        }
    }

    @FXML
    private void handleEditDocument() {
        if (selectedDocument == null) return;
        isEditing = true;
        contentDisplayArea.setEditable(true);
        saveChangesButton.setVisible(true);
        editDocumentButton.setDisable(true);
    }

    @FXML
    private void handleSaveChanges() {
        if (systemState.getDocumentManager().modifyDocument(selectedDocument.getDocumentId(), contentDisplayArea.getText())) {
            showDocumentDetails(selectedDocument);
            if (mainController != null) mainController.updateSummaryLabels(); // Ανανέωση summary
            new Alert(Alert.AlertType.INFORMATION, "Saved!").show();
        }
    }

    @FXML
    private void handleDeleteDocument() {
        if (selectedDocument == null) return;
        systemState.getDocumentManager().deleteDocument(selectedDocument.getDocumentId());
        refreshMyDocuments();
        showDocumentDetails(null);
        if (mainController != null) mainController.updateSummaryLabels(); // Ανανέωση summary
    }
}