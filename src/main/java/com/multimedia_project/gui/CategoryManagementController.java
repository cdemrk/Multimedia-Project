package com.multimedia_project.gui;

import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.Category;
import com.multimedia_project.model.User;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Optional;

public class CategoryManagementController {
    
    @FXML private ListView<Category> categoryListView;
    @FXML private Button updateCategoryButton;
    @FXML private Button deleteCategoryButton;

    private SystemState systemState;
    private MainController mainController;

    public void initializeData(SystemState state, User user) {
        this.systemState = state;

        setupCategoryManagement();
        loadCategoryList();
    }
    
    private void setupCategoryManagement() {
        // Ακρόαση επιλογής: Εμφάνιση των κουμπιών Update/Delete ΜΟΝΟ όταν επιλεγεί κάτι
        categoryListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                boolean hasSelection = (newVal != null);
                updateCategoryButton.setVisible(hasSelection);
                deleteCategoryButton.setVisible(hasSelection);
            }
        );
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void loadCategoryList() {
        categoryListView.setItems(FXCollections.observableArrayList(
            systemState.getCategoryManager().getAllCategories()
        ));
    }
    
    @FXML
    private void handleShowAddCategoryDialog() {
        // Χρήση TextInputDialog για γρήγορη εισαγωγή ονόματος
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Category");
        dialog.setHeaderText("Create a new document category");
        dialog.setContentText("Please enter category name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            String name = newName.trim();
            if (name.isEmpty()) return;
            
            try {
                systemState.getCategoryManager().addCategory(name);
                
                if (mainController != null) {
                    mainController.updateSummaryLabels();
                }
                
                loadCategoryList();
                showAlert("Success", "Category '" + name + "' added.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleShowUpdateCategoryDialog() {
        Category selectedCategory = categoryListView.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) return;

        // TextInputDialog με προ-συμπληρωμένο το τρέχον όνομα
        TextInputDialog dialog = new TextInputDialog(selectedCategory.getName());
        dialog.setTitle("Update Category");
        dialog.setHeaderText("Rename category: " + selectedCategory.getName());
        dialog.setContentText("New name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            String name = newName.trim();
            if (name.isEmpty() || name.equals(selectedCategory.getName())) return;
            
            try {
                // Χρήση της μεθόδου σου που ελέγχει για διπλότυπα
                systemState.getCategoryManager().updateCategoryName(selectedCategory.getId(), name);
                loadCategoryList();
                showAlert("Success", "Category renamed to '" + name + "'.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleDeleteCategory() {
        Category selectedCategory = categoryListView.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "WARNING: Deleting this category will delete all associated documents, follows, and user permissions! Proceed?", 
            ButtonType.YES, ButtonType.NO);
        
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            int catId = selectedCategory.getId();

            // 1. Αφαίρεση της κατηγορίας από τη λίστα πρόσβασης ΟΛΩΝ των χρηστών
            systemState.getUserManager().getAllUsers().forEach(user -> {
                user.getAccessibleCategoryIds().removeIf(id -> id == catId);
            });

            // 2. Διαγραφή των Follows για ΟΛΑ τα έγγραφα αυτής της κατηγορίας
            systemState.getDocumentManager().getAllDocuments().stream()
                .filter(doc -> doc.getCategoryId() == catId)
                .forEach(doc -> {
                    systemState.getFollowManager().removeFollowsForDeletedDocument(doc.getDocumentId());
                });

            // 3. Διαγραφή της κατηγορίας (και των εγγράφων της) από τον Manager
            systemState.getCategoryManager().deleteCategory(catId);
            
            // 4. Ενημέρωση Summary (Main Dashboard)
            if (mainController != null) {
                mainController.updateSummaryLabels();
            }
            
            // 5. Ανανέωση Λίστας και κρύψιμο κουμπιών
            loadCategoryList(); 
            updateCategoryButton.setVisible(false);
            deleteCategoryButton.setVisible(false);
            
            showAlert("Success", "Category and all associated data deleted successfully.", Alert.AlertType.INFORMATION);
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