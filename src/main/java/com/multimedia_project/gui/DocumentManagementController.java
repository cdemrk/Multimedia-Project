package com.multimedia_project.gui;

import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.User;
import com.multimedia_project.model.Role;
import com.multimedia_project.model.Category;
import com.multimedia_project.model.Document;
import com.multimedia_project.model.DocumentVersion;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DocumentManagementController {

    // Views
    @FXML private ComboBox<Category> categoryFilterCombo;
    @FXML private ListView<Document> documentListView;
    @FXML private TextArea contentDisplayArea;
    @FXML private Label versionInfoLabel;
    @FXML private ComboBox<Integer> versionSelectorCombo;
    
    // Management Controls (για Συγγραφείς/Διαχειριστές)
    @FXML private Button newDocumentButton;
    @FXML private Button editDocumentButton;
    @FXML private Button deleteDocumentButton;
    @FXML private Button saveChangesButton;
    
    private SystemState systemState;
    private User loggedInUser;
    private Document selectedDocument;
    private boolean isEditing = false;
    
    public void initializeData(SystemState state, User user) {
        this.systemState = state;
        this.loggedInUser = user;
        
        loadCategoryFilters();
        setupPermissions();
        
        // Φόρτωση εγγράφων για την αρχική επιλεγμένη κατηγορία
        categoryFilterCombo.getSelectionModel().selectFirst();
        filterDocumentsByCategory();
        
        // Ακρόαση αλλαγών στη λίστα εγγράφων
        documentListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> showDocumentDetails(newVal)
        );
    }
    
    private void setupPermissions() {
        boolean canManage = loggedInUser.getRole() == Role.Author || loggedInUser.getRole() == Role.Admin;
        
        // Κρύβουμε τα κουμπιά διαχείρισης για τον απλό χρήστη
        newDocumentButton.setVisible(canManage);
        editDocumentButton.setVisible(canManage);
        deleteDocumentButton.setVisible(canManage);
        saveChangesButton.setVisible(false); // Εμφανίζεται μόνο κατά την επεξεργασία
        versionSelectorCombo.setVisible(canManage); // Ο απλός χρήστης βλέπει μόνο την τελευταία
    }

    private void loadCategoryFilters() {
        List<Category> accessibleCategories = systemState.getCategoryManager().getAllCategories().stream()
            .filter(c -> loggedInUser.canAccessCategory(c.getId()))
            .collect(Collectors.toList());
        
        categoryFilterCombo.setItems(FXCollections.observableArrayList(accessibleCategories));
    }
    
    @FXML
    private void filterDocumentsByCategory() {
        Category selectedCategory = categoryFilterCombo.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) return;
        
        // Φιλτράρουμε όλα τα έγγραφα του συστήματος
        List<Document> filteredDocs = systemState.getDocumentManager().getAllDocuments().stream()
            .filter(doc -> doc.getCategoryId() == selectedCategory.getId())
            .collect(Collectors.toList());
            
        documentListView.setItems(FXCollections.observableArrayList(filteredDocs));
    }

    private void showDocumentDetails(Document doc) {
        if (doc == null) {
            contentDisplayArea.setText("");
            versionInfoLabel.setText("");
            versionSelectorCombo.getItems().clear();
            selectedDocument = null;
            return;
        }
        
        this.selectedDocument = doc;
        
        // Γεμίζουμε το ComboBox με τις διαθέσιμες εκδόσεις
        List<DocumentVersion> availableVersions = doc.getVersions(loggedInUser);
        ObservableList<Integer> versions = availableVersions.stream()
            .map(DocumentVersion::getVersionNumber)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
            
        versionSelectorCombo.setItems(versions);
        
        // Επιλέγουμε πάντα την τελευταία έκδοση
        versionSelectorCombo.getSelectionModel().select(doc.getLatestVersion().getVersionNumber());
    }

    @FXML
    private void handleVersionChange() {
        if (selectedDocument == null || versionSelectorCombo.getSelectionModel().isEmpty()) return;

        int selectedVersionNumber = versionSelectorCombo.getSelectionModel().getSelectedItem();
        
        // Βρίσκουμε το DocumentVersion με τον επιλεγμένο αριθμό
        Optional<DocumentVersion> versionOpt = selectedDocument.getVersions().stream()
            .filter(v -> v.getVersionNumber() == selectedVersionNumber)
            .findFirst();

        if (versionOpt.isPresent()) {
            DocumentVersion version = versionOpt.get();
            contentDisplayArea.setText(version.getContent());
            versionInfoLabel.setText("Viewing Version: " + version.getVersionNumber() + 
                                     " | Created: " + version.getCreationDate().toLocalDate());
        }
    }
    
    @FXML
    private void handleNewDocument() {
        // [ΛΟΓΙΚΗ ΔΗΜΙΟΥΡΓΙΑΣ] Εμφάνιση Modal/Φόρμας για προσθήκη τίτλου/κατηγορίας/περιεχομένου
        // ... (κατόπιν καλείται ο systemState.getDocumentManager().createDocument(...))
        // ...
        // Ανανέωση λίστας: filterDocumentsByCategory();
    }
    
    @FXML
    private void handleEditDocument() {
        if (selectedDocument == null) return;
        
        // Επιτρέπουμε μόνο την επεξεργασία του κειμένου
        contentDisplayArea.setEditable(true);
        saveChangesButton.setVisible(true);
        editDocumentButton.setDisable(true);
        isEditing = true;
    }

    @FXML
    private void handleSaveChanges() {
        if (!isEditing || selectedDocument == null) return;
        
        String newContent = contentDisplayArea.getText();
        
        // [ΛΟΓΙΚΗ VERSIONING] Καλείται η μέθοδος τροποποίησης του Manager
        boolean success = systemState.getDocumentManager().modifyDocument(
            selectedDocument.getDocumentId(), 
            newContent
        );
        
        if (success) {
            // Επαναφορά στην κατάσταση προβολής
            contentDisplayArea.setEditable(false);
            editDocumentButton.setDisable(false);
            saveChangesButton.setVisible(false);
            isEditing = false;
            
            // Ανανέωση του view και εμφάνιση της νέας έκδοσης
            showDocumentDetails(selectedDocument); 
            
            showAlert("Success", "Document saved and new version created (V" + selectedDocument.getLatestVersion().getVersionNumber() + ").", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "Could not save changes.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteDocument() {
        if (selectedDocument == null) return;
        
        // Επιβεβαίωση διαγραφής
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete the document: " + selectedDocument.getTitle() + "?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.YES) {
            // [ΛΟΓΙΚΗ ΔΙΑΓΡΑΦΗΣ] Καλείται η μέθοδος διαγραφής του Manager (που ενημερώνει και τους Followers)
            systemState.getDocumentManager().deleteDocument(selectedDocument.getDocumentId());
            
            // Ανανέωση λίστας
            filterDocumentsByCategory();
            showDocumentDetails(null);
            showAlert("Success", "Document deleted successfully.", Alert.AlertType.INFORMATION);
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
