package com.multimedia_project.gui;

import com.multimedia_project.MainApp;
import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.User;
import com.multimedia_project.model.Role;
import com.multimedia_project.model.Document; // Υποθέτουμε ότι το Document είναι στο model
import com.multimedia_project.model.FollowEntry; // Υποθέτουμε ότι το FollowEntry είναι στο model

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class MainController {
    
    @FXML private Label totalCategoriesLabel;
    @FXML private Label totalDocumentsLabel;
    @FXML private Label userFollowsLabel;
    @FXML private Label userRoleLabel;
    
    @FXML private VBox searchTabContent;
    @FXML private VBox documentTabContent;
    @FXML private VBox followsTabContent;
    
    @FXML private VBox userTabContent;
    @FXML private VBox categoryTabContent;
    @FXML private Tab userManagementTab;
    @FXML private Tab categoryManagementTab;

    private SystemState systemState;
    private User loggedInUser;

    // Καθορίζει την κατάσταση και τον συνδεδεμένο χρήστη
    public void initializeData(SystemState state, User user) {
        this.systemState = state;
        this.loggedInUser = user;

        userRoleLabel.setText("Role: " + user.getRole().toString());

        // Φόρτωση όλων των κοινών Views
        loadSearchForm(); 
        loadDocumentManagementView();
        loadFollowsView();

        // φόρτωση views ανάλογα με ρόλο του χρήστη
        checkRolePermissions(user.getRole());

        // Ενημέρωση και Ειδοποιήσεις
        updateSummaryLabels();
        checkForNewVersions(); 
    }


    private void checkRolePermissions(Role role) {
        if (role == Role.Admin) {
            // Εάν είναι Admin, φορτώνουμε το Admin View
            loadUserManagementView();
            loadCategoryManagementView();

        } else {
            // Εάν ΔΕΝ είναι Admin, αφαιρούμε τις καρτέλες διαχείρισης
            TabPane tabPane = userManagementTab.getTabPane(); 
            if (tabPane != null) {
                tabPane.getTabs().remove(userManagementTab);
                tabPane.getTabs().remove(categoryManagementTab);
            }
        }
    }


    private void loadSearchForm() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/multimedia_project/fxml/search_form.fxml")
            );
            
            Node searchFormNode = loader.load();
            DocumentSearchController searchController = loader.getController();
            searchController.initializeData(systemState, loggedInUser);
            searchTabContent.getChildren().setAll(searchFormNode);
            
        } catch (IOException e) {
            System.err.println("Failed to load search form FXML: " + e.getMessage());
            e.printStackTrace();
            searchTabContent.getChildren().setAll(new Label("Error loading search form."));
        }
    }


    private void loadDocumentManagementView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/multimedia_project/fxml/document_management_view.fxml")
            );
            
            Node documentViewNode = loader.load();
            DocumentManagementController docController = loader.getController();
            docController.initializeData(systemState, loggedInUser);
            documentTabContent.getChildren().setAll(documentViewNode);
            
        } catch (IOException e) {
            System.err.println("Failed to load document management FXML: " + e.getMessage());
            documentTabContent.getChildren().setAll(new Label("Error loading document view."));
        }
    }

    private void loadFollowsView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/multimedia_project/fxml/follows_view.fxml")
            );

            Node followsViewNode = loader.load();
            FollowsController followsController = loader.getController();
            followsController.initializeData(systemState, loggedInUser);
            followsTabContent.getChildren().setAll(followsViewNode);

        } catch (IOException e) {
            System.err.println("Failed to load follows management FXML: " + e.getMessage());
            followsTabContent.getChildren().setAll(new Label("Error loading follows view."));
        }
    }


    private void loadUserManagementView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/multimedia_project/fxml/user_management_view.fxml")
            );
            Node userViewNode = loader.load();
            
            // 1. Αρχικοποίηση Controller (ΝΕΟΣ ΤΥΠΟΣ)
            UserManagementController userController = loader.getController();
            userController.initializeData(systemState, loggedInUser); 
            
            userTabContent.getChildren().setAll(userViewNode); 
            
        } catch (IOException e) { /* ... */ }
    }

    private void loadCategoryManagementView() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/multimedia_project/fxml/category_management_view.fxml")
            );
            Node categoryViewNode = loader.load();
            
            // 1. Αρχικοποίηση Controller (ΝΕΟΣ ΤΥΠΟΣ)
            CategoryManagementController categoryController = loader.getController();
            categoryController.initializeData(systemState, loggedInUser); 
            
            categoryTabContent.getChildren().setAll(categoryViewNode); 
            
        } catch (IOException e) { /* ... */ }
    }
 

    // Έλεγχος για νέα έκδοση κατά το Login
    private void checkForNewVersions() {
        if (loggedInUser == null) return;
        
        List<FollowEntry> userFollows = systemState.getFollowManager().getFollowsForUser(loggedInUser.getUsername());
        if (userFollows.isEmpty()) return;

        StringBuilder notificationMessage = new StringBuilder();
        
        // Λίστα για τα FollowEntry που θα ενημερωθούν IN-MEMORY, δηλαδή αυτά για τα οποία ο χρήστης ειδοποιήθηκε
        List<FollowEntry> updatedFollows = new java.util.ArrayList<>(); 

        for (FollowEntry entry : userFollows) {
            Document doc = systemState.getDocumentManager().getDocumentById(entry.getDocumentId());
            
            if (doc != null) {
                int latestVersion = doc.getLatestVersion().getVersionNumber();
                
                if (entry.hasNewVersion(latestVersion)) {
                    // Υπάρχει νέα έκδοση!
                    notificationMessage.append("- Document: ").append(doc.getTitle())
                                       .append(" has new version ").append(latestVersion)
                                       .append("\n");
                    
                    // Σημαδεύουμε για ενημέρωση (in-memory)
                    updatedFollows.add(entry);
                }
            }
        }
        
        if (notificationMessage.length() > 0) {
            showAlert("Document Updates", 
                      "The following documents you are tracking have new versions:\n\n" + notificationMessage.toString(), 
                      Alert.AlertType.INFORMATION);
            
            // Καταγράφουμε ότι ο χρήστης είδε την τελευταία έκδοση in memory
            updateFollowsInMemory(updatedFollows);
        }
    }
    
    // Ενημερώνει το versionAtFollow για να μην ξαναβγεί το popup. Αυτή η αλλαγή θα αποθηκευτεί στο JSON ΜΟΝΟ κατά τον ΤΕΡΜΑΤΙΣΜΟ.
    private void updateFollowsInMemory(List<FollowEntry> followsToUpdate) {
        for (FollowEntry entry : followsToUpdate) {
            Document doc = systemState.getDocumentManager().getDocumentById(entry.getDocumentId());
            if (doc != null) {
                entry.setVersionAtFollow(doc.getLatestVersion().getVersionNumber());
            }
        }
    }

    // Ενημέρωση των Συγκεντρωτικών Πληροφοριών
    private void updateSummaryLabels() {
        int totalCategories = systemState.getCategoryManager().getAllCategories().size();
        int totalDocuments = systemState.getDocumentManager().getAllDocuments().size();
        int userFollowsCount = systemState.getFollowManager().getFollowsForUser(loggedInUser.getUsername()).size();
        
        totalCategoriesLabel.setText(String.valueOf(totalCategories));
        totalDocumentsLabel.setText(String.valueOf(totalDocuments));
        userFollowsLabel.setText(String.valueOf(userFollowsCount));
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText("New Document Versions Available!");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        try {
            // Επαναφορά στην οθόνη σύνδεσης
            MainApp.showLoginView();
        } catch (IOException e) {
            showAlert("Error", "Could not return to login view.", Alert.AlertType.ERROR);
        }
    }
}