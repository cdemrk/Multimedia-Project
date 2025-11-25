package com.multimedia_project.gui;

import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.User;
import com.multimedia_project.model.Role;
import com.multimedia_project.model.Category;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminManagementController {

    private SystemState systemState;
    private User loggedInUser;
    
    // ΕΛΕΓΧΟΙ ΔΙΑΧΕΙΡΙΣΗΣ ΧΡΗΣΤΩΝ
    @FXML private TextField userFirstNameField;
    @FXML private TextField userLastNameField;
    @FXML private TextField userUsernameField;
    @FXML private PasswordField userPasswordField;
    @FXML private ComboBox<Role> userRoleCombo;
    @FXML private ListView<Category> accessCategoriesListView;
    @FXML private Button deleteUserButton;
    @FXML private ListView<User> userListView;
    
    // ΕΛΕΓΧΟΙ ΔΙΑΧΕΙΡΙΣΗΣ ΚΑΤΗΓΟΡΙΩΝ
    @FXML private TextField newCategoryNameField;
    @FXML private TextField currentCategoryNameField;
    @FXML private ListView<Category> categoryListView;
    @FXML private Button updateCategoryButton;
    @FXML private Button deleteCategoryButton;

    
    public void initializeData(SystemState state, User user) {
        this.systemState = state;
        this.loggedInUser = user;

        // Ρύθμιση για τη διαχείριση χρηστών
        setupUserManagement();
        // Ρύθμιση για τη διαχείριση κατηγοριών
        setupCategoryManagement();

        // Φόρτωση αρχικών λιστών
        loadUserList();
        loadCategoryList();
    }
    
    // ----------------------------------------------------------------------------------
    // ΛΟΓΙΚΗ ΔΙΑΧΕΙΡΙΣΗΣ ΧΡΗΣΤΩΝ (Α.1)
    // ----------------------------------------------------------------------------------

    private void setupUserManagement() {
        userRoleCombo.setItems(FXCollections.observableArrayList(Arrays.asList(Role.SimpleUser, Role.Author, Role.Admin)));
        userRoleCombo.getSelectionModel().selectFirst();
        
        // Επιτρέπει πολλαπλές επιλογές για τις κατηγορίες πρόσβασης
        accessCategoriesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Ορίζουμε πώς θα εμφανίζονται οι κατηγορίες
        accessCategoriesListView.setCellFactory(lv -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });

        // Ακρόαση επιλογής χρήστη
        userListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> populateUserFields(newVal)
        );
    }
    
    private void loadUserList() {
        // Εμφανίζουμε όλους τους χρήστες εκτός από τον τρέχοντα συνδεδεμένο Admin
        List<User> userList = systemState.getUserManager().getAllUsers().stream()
            .filter(u -> !u.getUsername().equals(loggedInUser.getUsername()))
            .collect(Collectors.toList());
            
        userListView.setItems(FXCollections.observableArrayList(userList));
        
        // Γέμισμα της λίστας κατηγοριών πρόσβασης
        accessCategoriesListView.setItems(FXCollections.observableArrayList(
            systemState.getCategoryManager().getAllCategories()
        ));
    }
    
    private void populateUserFields(User user) {
        if (user == null) {
            // ... καθαρισμός πεδίων ...
            return;
        }
        // Εμφάνιση των στοιχείων του επιλεγμένου χρήστη
        userFirstNameField.setText(user.getFirstName());
        userLastNameField.setText(user.getLastName());
        userUsernameField.setText(user.getUsername());
        userRoleCombo.getSelectionModel().select(user.getRole());
        
        // Επιλογή των κατηγοριών στις οποίες έχει πρόσβαση
        accessCategoriesListView.getSelectionModel().clearSelection();
        for (Category cat : accessCategoriesListView.getItems()) {
            if (user.canAccessCategory(cat.getId())) {
                accessCategoriesListView.getSelectionModel().select(cat);
            }
        }
    }

    @FXML
    private void handleAddUser() {
        try {
            // Συγκέντρωση δεδομένων
            String firstName = userFirstNameField.getText();
            String lastName = userLastNameField.getText();
            String username = userUsernameField.getText();
            String password = userPasswordField.getText();
            Role role = userRoleCombo.getSelectionModel().getSelectedItem();
            
            List<Integer> accessIds = accessCategoriesListView.getSelectionModel().getSelectedItems().stream()
                .map(Category::getId)
                .collect(Collectors.toList());
                
            // ΕΛΕΓΧΟΣ (βασικοί έλεγχοι: κενά πεδία, τουλάχιστον μια κατηγορία πρόσβασης)
            if (username.isEmpty() || password.isEmpty() || accessIds.isEmpty()) {
                showAlert("Error", "Username, Password, and at least one Access Category are required.", Alert.AlertType.ERROR);
                return;
            }
            
            // Κλήση Manager για προσθήκη χρήστη
            systemState.getUserManager().addUser(firstName, lastName, username, password, role, accessIds);
            showAlert("Success", "User " + username + " added successfully.", Alert.AlertType.INFORMATION);
            
            loadUserList(); // Ανανέωση λίστας
            
        } catch (Exception e) {
            showAlert("Error", "Failed to add user: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleDeleteUser() {
        User userToDelete = userListView.getSelectionModel().getSelectedItem();
        if (userToDelete == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Are you sure you want to delete user " + userToDelete.getUsername() + "?", 
            ButtonType.YES, ButtonType.NO);
        
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            // Κλήση Manager για διαγραφή
            systemState.getUserManager().deleteUser(userToDelete.getUsername()); 
            showAlert("Success", "User deleted successfully.", Alert.AlertType.INFORMATION);
            loadUserList(); // Ανανέωση λίστας
        }
    }


    // ----------------------------------------------------------------------------------
    // ΛΟΓΙΚΗ ΔΙΑΧΕΙΡΙΣΗΣ ΚΑΤΗΓΟΡΙΩΝ (Α.1)
    // ----------------------------------------------------------------------------------

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
    
    private void loadCategoryList() {
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
            loadUserList(); // Ενημέρωση της λίστας κατηγοριών και στο User Management
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
            loadUserList(); // Ενημέρωση λίστας κατηγοριών πρόσβασης χρηστών
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