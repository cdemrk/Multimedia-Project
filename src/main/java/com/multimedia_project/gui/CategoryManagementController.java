package com.multimedia_project.gui;

import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.Category;
import com.multimedia_project.model.User;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
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
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Category");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter category name:");
        
        if (categoryListView.getScene() != null) {
            dialog.initOwner(categoryListView.getScene().getWindow());
        }

        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String name = result.get().trim();
            
            if (name.isEmpty()) {
                showAlert("Error", "Category name cannot be empty!", Alert.AlertType.ERROR);
                return;
            }
            
            try {
                systemState.getCategoryManager().addCategory(name);
                if (mainController != null) mainController.updateSummaryLabels();
                loadCategoryList();
                showAlert("Success", "Category '" + name + "' added.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleShowUpdateCategoryDialog() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Update Category");
        dialog.setHeaderText(null);
        dialog.setContentText("New name for '" + selected.getName() + "':");

        if (categoryListView.getScene() != null) {
            dialog.initOwner(categoryListView.getScene().getWindow());
        }

        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String name = result.get().trim();
            
            if (name.isEmpty()) {
                showAlert("Error", "Category name cannot be empty!", Alert.AlertType.ERROR);
                return;
            }
            
            if (name.equals(selected.getName())) return; // Δεν άλλαξε κάτι, απλό κλείσιμο
            
            try {
                systemState.getCategoryManager().updateCategoryName(selected.getId(), name);
                loadCategoryList();
                showAlert("Success", "Category renamed to '" + name + "'.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleDeleteCategory() {
        Category selectedCategory = categoryListView.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Category Confirmation");
        confirm.setHeaderText(null);
        
        if (categoryListView.getScene() != null) {
            confirm.initOwner(categoryListView.getScene().getWindow());
        }

        String msg = "WARNING: Deleting category '" + selectedCategory.getName() + "' will delete ALL associated documents and follows! Are you sure you want to proceed?";
        confirm.setContentText(msg);
        
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                int catId = selectedCategory.getId();

                systemState.getUserManager().getAllUsers().forEach(u -> {
                    u.getAccessibleCategoryIds().removeIf(id -> id == catId);
                });

                systemState.getDocumentManager().getAllDocuments().stream()
                    .filter(doc -> doc.getCategoryId() == catId)
                    .forEach(doc -> systemState.getFollowManager().removeFollowsForDeletedDocument(doc.getDocumentId()));

                systemState.getCategoryManager().deleteCategory(catId);
                
                if (mainController != null) mainController.updateSummaryLabels();
                loadCategoryList(); 
                
                categoryListView.getSelectionModel().clearSelection();
                updateCategoryButton.setVisible(false);
                deleteCategoryButton.setVisible(false);
                
                showAlert("Success", "Category and all associated data deleted successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Delete failed: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        if (categoryListView.getScene() != null) {
            alert.initOwner(categoryListView.getScene().getWindow());
        }
        alert.showAndWait();
    }
}