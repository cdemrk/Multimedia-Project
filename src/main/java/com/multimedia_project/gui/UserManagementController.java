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
        // Ακρόαση για εμφάνιση/απόκρυψη του Delete
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
        // 1. Δημιουργία του Dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New User");
        dialog.setHeaderText("Fill in the details for the new user account.");

        // 2. Ορισμός Buttons
        ButtonType createButtonType = new ButtonType("Create User", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // 3. Κατασκευή του Layout του παραθύρου
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField fName = new TextField(); fName.setPromptText("First Name");
        TextField lName = new TextField(); lName.setPromptText("Last Name");
        TextField uName = new TextField(); uName.setPromptText("Username (Required)");
        PasswordField pWord = new PasswordField(); pWord.setPromptText("Password (Required)");
        
        ComboBox<Role> roleCombo = new ComboBox<>(FXCollections.observableArrayList(Arrays.asList(Role.SimpleUser, Role.Author, Role.Admin)));
        roleCombo.getSelectionModel().selectFirst();

        // Λίστα κατηγοριών με Toggle Logic
        ListView<Category> catList = new ListView<>(FXCollections.observableArrayList(systemState.getCategoryManager().getAllCategories()));
        catList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        catList.setPrefHeight(150);
        
        // Custom cell factory για toggle επιλογή κατηγοριών χωρίς Ctrl
        catList.setCellFactory(lv -> {
            ListCell<Category> cell = new ListCell<Category>() {
                @Override protected void updateItem(Category item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getName());
                }
            };
            cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (!cell.isEmpty()) {
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

        // 4. Επεξεργασία αποτελέσματος
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == createButtonType) {
            try {
                String username = uName.getText().trim();
                String password = pWord.getText();
                
                List<Integer> accessIds = catList.getSelectionModel().getSelectedItems().stream()
                    .map(Category::getId)
                    .collect(Collectors.toList());

                if (username.isEmpty() || password.isEmpty() || accessIds.isEmpty()) {
                    showAlert("Validation Error", "Username, Password and at least one category are required.", Alert.AlertType.ERROR);
                    return;
                }

                systemState.getUserManager().addUser(username, password, fName.getText(), lName.getText(), roleCombo.getValue(), accessIds);
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