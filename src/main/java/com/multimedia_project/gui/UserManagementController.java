package com.multimedia_project.gui;

import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.User;
import com.multimedia_project.model.Role;
import com.multimedia_project.model.Category;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserManagementController {
    
    // Μόνο τα πεδία που ανήκουν στη διαχείριση χρηστών
    @FXML private TextField userFirstNameField;
    @FXML private TextField userLastNameField;
    @FXML private TextField userUsernameField;
    @FXML private PasswordField userPasswordField;
    @FXML private ComboBox<Role> userRoleCombo;
    @FXML private ListView<Category> accessCategoriesListView;
    @FXML private Button deleteUserButton;
    @FXML private ListView<User> userListView;

    private SystemState systemState;
    private User loggedInUser;

    public void initializeData(SystemState state, User user) {
        this.systemState = state;
        this.loggedInUser = user;

        // Ρύθμιση και φόρτωση
        setupUserManagement();
        loadUserList();
    }
    
    private void setupUserManagement() {
        // 1. Ρύθμιση ComboBox Ρόλου
        userRoleCombo.setItems(FXCollections.observableArrayList(Arrays.asList(Role.SimpleUser, Role.Author, Role.Admin)));
        userRoleCombo.getSelectionModel().selectFirst();
        
        // 2. Ενεργοποίηση Πολλαπλής Επιλογής (ΑΠΑΡΑΙΤΗΤΟ)
        accessCategoriesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // 3. CUSTOM LOGIC: Ενεργοποίηση πολλαπλής επιλογής με απλό κλικ (Toggle)
        accessCategoriesListView.setCellFactory(lv -> {
            ListCell<Category> cell = new ListCell<Category>() {
                @Override
                protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            };

            // Χρησιμοποιούμε Event Filter στο MOUSE_PRESSED για να "κλέψουμε" το κλικ 
            // πριν η JavaFX εκτελέσει τη δική της λογική επιλογής.
            cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (!cell.isEmpty()) {
                    // Δίνουμε focus στη λίστα χειροκίνητα
                    accessCategoriesListView.requestFocus();
                    
                    int index = cell.getIndex();
                    SelectionModel<Category> model = accessCategoriesListView.getSelectionModel();
                    
                    if (model.isSelected(index)) {
                        model.clearSelection(index);
                    } else {
                        model.select(index);
                    }
                    
                    // Καταναλώνουμε το event για να μην τρέξει η default επιλογή της JavaFX
                    event.consume(); 
                }
            });
            return cell;
        });

        // 4. Ακρόαση επιλογής χρήστη από την πάνω λίστα (για να γεμίσουμε τη φόρμα)
        userListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> populateUserFields(newVal)
        );
    }

    
    public void loadUserList() {
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
            systemState.getUserManager().addUser(username, password, firstName, lastName, role, accessIds);
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
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}