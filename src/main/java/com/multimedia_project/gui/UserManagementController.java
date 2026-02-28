package com.multimedia_project.gui;

import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.User;
import com.multimedia_project.model.Role;
import com.multimedia_project.model.Category;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserManagementController {
    
    @FXML private Button deleteUserButton;
    @FXML private ListView<User> userListView;

    private SystemState systemState;
    private User loggedInUser;

    public void initializeData(SystemState state, User user) {
        this.systemState = state;
        this.loggedInUser = user;

        setupListView();
        loadUserList();
    }
    
    private void setupListView() {
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            deleteUserButton.setVisible(newVal != null);
        });
    }

    public void loadUserList() {
        List<User> userList = systemState.getUserManager().getAllUsers().stream()
            .filter(u -> !u.getUsername().equals(loggedInUser.getUsername()))
            .collect(Collectors.toList());
            
        userListView.setItems(FXCollections.observableArrayList(userList));
    }

    @FXML
    private void handleShowAddUserDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New User");
        dialog.setHeaderText("Fill in the details for the new user account.");

        ButtonType createButtonType = new ButtonType("Create User", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField fName = new TextField(); fName.setPromptText("First Name (Required)");
        TextField lName = new TextField(); lName.setPromptText("Last Name (Required)");
        TextField uName = new TextField(); uName.setPromptText("Username (Required)");
        PasswordField pWord = new PasswordField(); pWord.setPromptText("Password (Required)");
        
        ComboBox<Role> roleCombo = new ComboBox<>(FXCollections.observableArrayList(Arrays.asList(Role.SimpleUser, Role.Author, Role.Admin)));
        roleCombo.getSelectionModel().selectFirst();

        ListView<Category> catList = new ListView<>(FXCollections.observableArrayList(systemState.getCategoryManager().getAllCategories()));
        catList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        catList.setPrefHeight(150);
        
        // --- ΠΡΟΣΘΗΚΗ LISTENER ΓΙΑ ΤΟΝ ΡΟΛΟ ADMIN ---
        roleCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldRole, newRole) -> {
            if (newRole == Role.Admin) {
                catList.getSelectionModel().clearSelection(); // Καθαρισμός επιλογών
                catList.setDisable(true);                    // Απενεργοποίηση λίστας
                catList.setOpacity(0.5);                     // Οπτική ένδειξη "γκριζαρίσματος"
            } else {
                catList.setDisable(false);                   // Επαναφορά λίστας
                catList.setOpacity(1.0);
            }
        });

        catList.setCellFactory(lv -> {
            ListCell<Category> cell = new ListCell<Category>() {
                @Override protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getName());
                }
            };
            cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (!cell.isEmpty() && !catList.isDisable()) { // Έλεγχος αν η λίστα είναι ενεργή
                    catList.requestFocus();
                    int index = cell.getIndex();
                    if (catList.getSelectionModel().isSelected(index)) catList.getSelectionModel().clearSelection(index);
                    else catList.getSelectionModel().select(index);
                    event.consume();
                }
            });
            return cell;
        });

        grid.add(new Label("First Name:"), 0, 0); grid.add(fName, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);  grid.add(lName, 1, 1);
        grid.add(new Label("Username:"), 0, 2);   grid.add(uName, 1, 2);
        grid.add(new Label("Password:"), 0, 3);   grid.add(pWord, 1, 3);
        grid.add(new Label("User Role:"), 0, 4);  grid.add(roleCombo, 1, 4);
        grid.add(new Label("Access Categories:"), 0, 5); grid.add(catList, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == createButtonType) {
            try {
                String firstName = fName.getText().trim();
                String lastName = lName.getText().trim();
                String username = uName.getText().trim();
                String password = pWord.getText();
                Role selectedRole = roleCombo.getValue();
                
                List<Integer> accessIds = catList.getSelectionModel().getSelectedItems().stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());

                // --- ΤΡΟΠΟΠΟΙΗΜΕΝΟΣ ΕΛΕΓΧΟΣ VALIDATION ---
                // Αν είναι Admin, δεν απαιτούμε πλέον επιλεγμένη κατηγορία στο check
                boolean isCategoryRequired = (selectedRole != Role.Admin);
                if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty() || (isCategoryRequired && accessIds.isEmpty())) {
                    String errorMsg = isCategoryRequired ? 
                        "All fields and at least one category are required." : 
                        "All personal details (Name, Username, Password) are required.";
                    showAlert("Validation Error", errorMsg, Alert.AlertType.ERROR);
                    return;
                }

                systemState.getUserManager().addUser(username, password, firstName, lastName, selectedRole, accessIds);
                loadUserList();
                showAlert("Success", "User " + username + " created successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to add user: " + e.getMessage(), Alert.AlertType.ERROR);
            }
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
            systemState.getUserManager().deleteUser(userToDelete.getUsername()); 
            loadUserList();
            deleteUserButton.setVisible(false);
            showAlert("Success", "User deleted successfully.", Alert.AlertType.INFORMATION);
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