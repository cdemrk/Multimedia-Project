package com.multimedia_project.gui;

import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.User;
import com.multimedia_project.model.Category;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Optional;

// ΣΗΜ: Χρειάζεστε αναφορά στον UserManagementController για να καλέσετε loadUserList()
// Ή, εναλλακτικά, η MainController αναλαμβάνει να ενημερώσει όλα τα views μετά την αλλαγή.
// Για την απλότητα, θα κάνουμε τη φόρτωση από τον MainController.

public class CategoryManagementController {
    
    // Μόνο τα πεδία που ανήκουν στη διαχείριση κατηγοριών
    @FXML private TextField newCategoryNameField;
    @FXML private TextField currentCategoryNameField;
    @FXML private ListView<Category> categoryListView;
    @FXML private Button updateCategoryButton;
    @FXML private Button deleteCategoryButton;

    private SystemState systemState;
    private User loggedInUser;
    
    public void initializeData(SystemState state, User user) {
        this.systemState = state;
        this.loggedInUser = user;

        setupCategoryManagement();
        loadCategoryList();
    }
    
    private void setupCategoryManagement() {
        // Ακρόαση επιλογής κατηγορίας
        categoryListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    currentCategoryNameField.setText(newVal.getName());
                    updateCategoryButton.setDisable(false);
                    deleteCategoryButton.setDisable(false);
                } else {
                    currentCategoryNameField.setText("");
                    updateCategoryButton.setDisable(true);
                    deleteCategoryButton.setDisable(true);
                }
            }
        );
    }
    
    public void loadCategoryList() {
        categoryListView.setItems(FXCollections.observableArrayList(
            systemState.getCategoryManager().getAllCategories()
        ));
    }
    
    @FXML
    private void handleAddCategory() {
        String newName = newCategoryNameField.getText().trim();
        if (newName.isEmpty()) return;
        
        Category newCat = systemState.getCategoryManager().addCategory(newName);
        
        if (newCat != null) {
            showAlert("Success", "Category '" + newName + "' added.", Alert.AlertType.INFORMATION);
            newCategoryNameField.clear();
            loadCategoryList();
        } else {
            showAlert("Error", "Category name already exists.", Alert.AlertType.ERROR);
        }
    }


    @FXML
    private void handleUpdateCategoryName() {
        Category selectedCategory = categoryListView.getSelectionModel().getSelectedItem();
        String newName = currentCategoryNameField.getText().trim();
        
        if (selectedCategory == null || newName.isEmpty()) return;
        
        boolean success = systemState.getCategoryManager().updateCategoryName(selectedCategory.getId(), newName);
        
        if (success) {
            showAlert("Success", "Category name updated to '" + newName + "'.", Alert.AlertType.INFORMATION);
            loadCategoryList();
            // Λόγω του update: loadUserList() αν κριθεί απαραίτητο, αν και τα accessCategoryIds δεν αλλάζουν.
        } else {
            showAlert("Error", "Category name is already in use.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Category selectedCategory = categoryListView.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "WARNING: Deleting this category will DELETE ALL documents belonging to it! Proceed?", 
            ButtonType.YES, ButtonType.NO);
        
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            // Καλείται ο CategoryManager, ο οποίος θα καλέσει τον DocumentManager για διαγραφή εγγράφων
            systemState.getCategoryManager().deleteCategory(selectedCategory.getId());
            
            showAlert("Success", "Category and all associated documents deleted.", Alert.AlertType.INFORMATION);
            loadCategoryList();
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