package com.multimedia_project.gui;

import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.User;
import com.multimedia_project.model.Document;
import com.multimedia_project.model.FollowEntry;
import com.multimedia_project.model.Role;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FollowsController {

    @FXML private ListView<Document> availableDocumentsListView;
    @FXML private ListView<FollowEntry> followedDocumentsListView;
    @FXML private Button addFollowButton;
    @FXML private Button removeFollowButton;

    private SystemState systemState;
    private User loggedInUser;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
    
    public void initializeData(SystemState state, User user) {
        this.systemState = state;
        this.loggedInUser = user;

        // Ενεργοποίηση πολλαπλής επιλογής και για τις δύο λίστες
        availableDocumentsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        followedDocumentsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        loadLists();

        // 1. Toggle Logic για τα Διαθέσιμα Έγγραφα (Available Documents)
        availableDocumentsListView.setCellFactory(lv -> {
            ListCell<Document> cell = new ListCell<Document>() {
                @Override
                protected void updateItem(Document item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getTitle());
                }
            };
            
            cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (!cell.isEmpty()) {
                    availableDocumentsListView.requestFocus();
                    int index = cell.getIndex();
                    if (availableDocumentsListView.getSelectionModel().isSelected(index)) {
                        availableDocumentsListView.getSelectionModel().clearSelection(index);
                    } else {
                        availableDocumentsListView.getSelectionModel().select(index);
                    }
                    event.consume();
                }
            });
            return cell;
        });

        // 2. Toggle Logic ΚΑΙ Cell Factory για τα Παρακολουθούμενα (Followed Documents)
        followedDocumentsListView.setCellFactory(lv -> {
            ListCell<FollowEntry> cell = new ListCell<FollowEntry>() {
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
            };

            cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (!cell.isEmpty()) {
                    followedDocumentsListView.requestFocus();
                    int index = cell.getIndex();
                    if (followedDocumentsListView.getSelectionModel().isSelected(index)) {
                        followedDocumentsListView.getSelectionModel().clearSelection(index);
                    } else {
                        followedDocumentsListView.getSelectionModel().select(index);
                    }
                    event.consume();
                }
            });
            return cell;
        });
    }

    public void loadLists() {
        // 1. Φόρτωση παρακολουθούμενων εγγράφων (αυτά που ήδη ακολουθεί)
        List<FollowEntry> userFollows = systemState.getFollowManager().getFollowsForUser(loggedInUser.getUsername());
        followedDocumentsListView.setItems(FXCollections.observableArrayList(userFollows));
        
        // Λίστα με τα IDs των εγγράφων που ήδη παρακολουθούνται
        List<String> followedIds = userFollows.stream()
            .map(FollowEntry::getDocumentId)
            .collect(Collectors.toList());

        // 2. Φόρτωση διαθέσιμων εγγράφων για παρακολούθηση
        List<Document> availableDocs = systemState.getDocumentManager().getAllDocuments().stream()
            .filter(doc -> {
                // Αν είναι Admin, έχει πρόσβαση στα ΠΑΝΤΑ
                if (loggedInUser.getRole() == Role.Admin) {
                    return true;
                }
                // Αν δεν είναι Admin, ελέγχουμε τις κατηγορίες του
                return loggedInUser.canAccessCategory(doc.getCategoryId());
            })
            .filter(doc -> !followedIds.contains(doc.getDocumentId())) // Να μην το ακολουθεί ήδη
            .collect(Collectors.toList());

        availableDocumentsListView.setItems(FXCollections.observableArrayList(availableDocs));
    }

    @FXML
    private void handleAddFollow() {
        // Παίρνουμε όλα τα επιλεγμένα έγγραφα
        ObservableList<Document> selectedDocs = availableDocumentsListView.getSelectionModel().getSelectedItems();
        
        if (selectedDocs == null || selectedDocs.isEmpty()) {
            showAlert("Warning", "Please select at least one document.", Alert.AlertType.WARNING);
            return;
        }

        // Διατρέχουμε τη λίστα και προσθέτουμε follow για το καθένα
        for (Document docToFollow : selectedDocs) {
            int currentVersion = docToFollow.getLatestVersion().getVersionNumber();
            systemState.getFollowManager().addFollow(
                loggedInUser.getUsername(),
                docToFollow.getDocumentId(),
                currentVersion
            );
        }
        
        // Ενημέρωση του Summary στο Dashboard (MainController)
        if (mainController != null) {
            mainController.updateSummaryLabels();
        }
        
        showAlert("Success", "Tracking started for " + selectedDocs.size() + " documents.", Alert.AlertType.INFORMATION);
        
        loadLists(); // Ανανέωση των λιστών στο UI
    }

    @FXML
    private void handleRemoveFollow() {
        ObservableList<FollowEntry> selectedEntries = followedDocumentsListView.getSelectionModel().getSelectedItems();
        
        if (selectedEntries == null || selectedEntries.isEmpty()) {
            showAlert("Warning", "Please select at least one tracked document to remove.", Alert.AlertType.WARNING);
            return;
        }

        // Κρατάμε το πλήθος για το μήνυμα επιτυχίας
        int count = selectedEntries.size();

        // Φτιάχνουμε αντίγραφο για ασφαλή διαγραφή
        List<FollowEntry> toRemove = new ArrayList<>(selectedEntries);

        for (FollowEntry entry : toRemove) {
            systemState.getFollowManager().removeFollow(
                loggedInUser.getUsername(),
                entry.getDocumentId()
            );
        }

        // Ενημέρωση του Summary στο αριστερό panel
        if (mainController != null) {
            mainController.updateSummaryLabels();
        }
        
        // ΠΡΟΣΘΗΚΗ: Το μήνυμα επιβεβαίωσης
        showAlert("Success", "Stopped tracking for " + count + " documents.", Alert.AlertType.INFORMATION);
        
        loadLists(); // Ανανέωση των λιστών στο UI
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}