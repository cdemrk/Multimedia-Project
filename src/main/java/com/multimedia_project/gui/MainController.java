package com.multimedia_project.gui;

import com.multimedia_project.MainApp;
import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.List;

public class MainController {
    
    @FXML private Label totalCategoriesLabel;
    @FXML private Label totalDocumentsLabel;
    @FXML private Label userFollowsLabel;

    @FXML private Label usernameLabel;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label userRoleLabel;
    
    @FXML private VBox documentTabContent;
    @FXML private VBox followsTabContent;
    @FXML private VBox userTabContent;
    @FXML private VBox categoryTabContent;

    @FXML private Tab userManagementTab;
    @FXML private Tab categoryManagementTab;
    @FXML private TabPane mainTabPane;

    private SystemState systemState;
    private User loggedInUser;
    private UnifiedDocumentController unifiedDocumentController;
    private FollowsController followsController;

    public void initializeData(SystemState state, User user) {
        this.systemState = state;
        this.loggedInUser = user;

        usernameLabel.setText(user.getUsername());
        firstNameLabel.setText(user.getFirstName());
        lastNameLabel.setText(user.getLastName());
        userRoleLabel.setText(user.getRole().toString());

        if (user.getRole() != Role.Admin) {
            mainTabPane.getTabs().remove(userManagementTab);
            mainTabPane.getTabs().remove(categoryManagementTab);
        }

        loadUnifiedDocumentView();
        loadFollowsView();
        
        if (user.getRole() == Role.Admin) {
            loadUserManagementView();
            loadCategoryManagementView();
        }

        updateSummaryLabels();

        checkForNewVersions();

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                refreshCurrentTab(newTab.getText());
            }
        });
    }

    private void refreshCurrentTab(String tabTitle) {
        if (tabTitle.equals("Documents Explorer") && unifiedDocumentController != null) {
            unifiedDocumentController.handleSearch();
        } else if (tabTitle.equals("My Follows") && followsController != null) {
            followsController.loadLists();
        }
        updateSummaryLabels();
    }

    private void loadUnifiedDocumentView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/multimedia_project/fxml/unified_document_view.fxml"));
            Node node = loader.load();
            unifiedDocumentController = loader.getController();
            unifiedDocumentController.initializeData(systemState, loggedInUser);
            unifiedDocumentController.setMainController(this);
            documentTabContent.getChildren().setAll(node);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadFollowsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/multimedia_project/fxml/follows_view.fxml"));
            Node node = loader.load();
            followsController = loader.getController();
            followsController.initializeData(systemState, loggedInUser);
            followsController.setMainController(this);
            followsTabContent.getChildren().setAll(node);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadUserManagementView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/multimedia_project/fxml/user_management_view.fxml"));
            Node node = loader.load();
            UserManagementController controller = loader.getController();
            controller.initializeData(systemState, loggedInUser);
            userTabContent.getChildren().setAll(node);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadCategoryManagementView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/multimedia_project/fxml/category_management_view.fxml"));
            Node node = loader.load();
            CategoryManagementController controller = loader.getController();
            controller.initializeData(systemState, loggedInUser);
            controller.setMainController(this); 
            categoryTabContent.getChildren().setAll(node);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void checkForNewVersions() {
        if (loggedInUser == null) return;
        
        List<FollowEntry> userFollows = systemState.getFollowManager().getFollowsForUser(loggedInUser.getUsername());
        StringBuilder message = new StringBuilder();
        boolean found = false;

        for (FollowEntry entry : userFollows) {
            Document doc = systemState.getDocumentManager().getDocumentById(entry.getDocumentId());
            if (doc != null && doc.getLatestVersion().getVersionNumber() > entry.getVersionAtFollow()) {
                message.append("- ").append(doc.getTitle())
                       .append(" (New: V").append(doc.getLatestVersion().getVersionNumber()).append(")\n");

                entry.setVersionAtFollow(doc.getLatestVersion().getVersionNumber());
                found = true;
            }
        }
        
        if (found) {
            final String finalMessage = message.toString();
            Platform.runLater(() -> {
                showAlert("Updates Found", "New versions were found for your followed documents:\n\n" + finalMessage, Alert.AlertType.INFORMATION);
            });
        }
    }

    public void updateSummaryLabels() {
        int totalCategories = systemState.getCategoryManager().getAllCategories().size();
        int totalDocuments = systemState.getDocumentManager().getAllDocuments().size();
        int userFollowsCount = systemState.getFollowManager().getFollowsForUser(loggedInUser.getUsername()).size();
        
        totalCategoriesLabel.setText(String.valueOf(totalCategories));
        totalDocumentsLabel.setText(String.valueOf(totalDocuments));
        userFollowsLabel.setText(String.valueOf(userFollowsCount));
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);

        if (mainTabPane != null && mainTabPane.getScene() != null) {
            alert.initOwner(mainTabPane.getScene().getWindow());
        }
        
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        try {
            javafx.stage.Stage currentStage = (javafx.stage.Stage) mainTabPane.getScene().getWindow();
            currentStage.close();
            
            MainApp.showLoginView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}