package com.multimedia_project.gui;

import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.User;
import com.multimedia_project.model.Document;
import com.multimedia_project.model.FollowEntry;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import java.util.stream.Collectors;

public class FollowsController {

    @FXML private ListView<Document> availableDocumentsListView;
    @FXML private ListView<FollowEntry> followedDocumentsListView;
    @FXML private Button addFollowButton;
    @FXML private Button removeFollowButton;

    private SystemState systemState;
    private User loggedInUser;
    
    public void initializeData(SystemState state, User user) {
        this.systemState = state;
        this.loggedInUser = user;
        
        loadLists();
        
        // Ορίζουμε πώς θα εμφανίζονται τα έγγραφα στη λίστα παρακολούθησης
        followedDocumentsListView.setCellFactory(lv -> new ListCell<FollowEntry>() {
            @Override
            protected void updateItem(FollowEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Document doc = systemState.getDocumentManager().getDocumentById(item.getDocumentId());
                    if (doc != null) {
                        setText(doc.getTitle() + " (Last seen V" + item.getVersionAtFollow() + ")");
                    } else {
                        setText("Document ID: " + item.getDocumentId() + " (DELETED)");
                    }
                }
            }
        });
    }

    private void loadLists() {
        // 1. Φόρτωση παρακολουθούμενων εγγράφων
        List<FollowEntry> userFollows = systemState.getFollowManager().getFollowsForUser(loggedInUser.getUsername());
        followedDocumentsListView.setItems(FXCollections.observableArrayList(userFollows));
        
        // 2. Φόρτωση διαθέσιμων εγγράφων για παρακολούθηση
        // Δείχνουμε όλα τα έγγραφα στα οποία ο χρήστης έχει πρόσβαση και ΔΕΝ τα παρακολουθεί ήδη
        List<String> followedIds = userFollows.stream()
            .map(FollowEntry::getDocumentId)
            .collect(Collectors.toList());

        List<Document> availableDocs = systemState.getDocumentManager().getAllDocuments().stream()
            .filter(doc -> loggedInUser.canAccessCategory(doc.getCategoryId())) // Πρόσβαση
            .filter(doc -> !followedIds.contains(doc.getDocumentId())) // Δεν παρακολουθείται ήδη
            .collect(Collectors.toList());

        availableDocumentsListView.setItems(FXCollections.observableArrayList(availableDocs));
    }

    @FXML
    private void handleAddFollow() {
        Document docToFollow = availableDocumentsListView.getSelectionModel().getSelectedItem();
        if (docToFollow == null) return;
        
        // Προσθήκη παρακολούθησης με την τρέχουσα τελευταία έκδοση
        int currentVersion = docToFollow.getLatestVersion().getVersionNumber();
        systemState.getFollowManager().addFollow(
            loggedInUser.getUsername(),
            docToFollow.getDocumentId(),
            currentVersion
        );
        
        showAlert("Success", "Tracking started for: " + docToFollow.getTitle(), Alert.AlertType.INFORMATION);
        loadLists(); // Ανανέωση των δύο λιστών
    }

    @FXML
    private void handleRemoveFollow() {
        FollowEntry entryToRemove = followedDocumentsListView.getSelectionModel().getSelectedItem();
        if (entryToRemove == null) return;
        
        // Αφαίρεση παρακολούθησης
        systemState.getFollowManager().removeFollow(
            loggedInUser.getUsername(),
            entryToRemove.getDocumentId()
        );
        
        showAlert("Success", "Tracking removed for document ID: " + entryToRemove.getDocumentId(), Alert.AlertType.INFORMATION);
        loadLists(); // Ανανέωση των δύο λιστών
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}