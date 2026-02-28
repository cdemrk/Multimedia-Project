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

        // Επιλογή πολλαπλών στοιχείων
        availableDocumentsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        followedDocumentsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Listeners για την εμφάνιση των κουμπιών
        availableDocumentsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            addFollowButton.setVisible(newVal != null);
        });

        followedDocumentsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            removeFollowButton.setVisible(newVal != null);
        });

        setupCellFactories();
        loadLists();
    }

    private void setupCellFactories() {
        // CellFactory για τα διαθέσιμα έγγραφα
        availableDocumentsListView.setCellFactory(lv -> {
            ListCell<Document> cell = new ListCell<Document>() {
                @Override
                protected void updateItem(Document item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTitle());
                }
            };
            setupClickFiltering(cell, availableDocumentsListView);
            return cell;
        });

        // CellFactory για τα έγγραφα που ακολουθεί ο χρήστης
        followedDocumentsListView.setCellFactory(lv -> {
            ListCell<FollowEntry> cell = new ListCell<FollowEntry>() {
                @Override
                protected void updateItem(FollowEntry item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        Document doc = systemState.getDocumentManager().getDocumentById(item.getDocumentId());
                        // Εμφάνιση μόνο του τίτλου, χωρίς versions ή "DELETED"
                        setText(doc != null ? doc.getTitle() : null);
                    }
                }
            };
            setupClickFiltering(cell, followedDocumentsListView);
            return cell;
        });
    }

    // Βοηθητική μέθοδος για το "toggle" selection με το ποντίκι
    private <T> void setupClickFiltering(ListCell<T> cell, ListView<T> listView) {
        cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
            if (!cell.isEmpty()) {
                listView.requestFocus();
                int index = cell.getIndex();
                if (listView.getSelectionModel().isSelected(index)) {
                    listView.getSelectionModel().clearSelection(index);
                } else {
                    listView.getSelectionModel().select(index);
                }
                event.consume();
            }
        });
    }

    public void loadLists() {
        // 1. Φορτώνουμε τα follows και φιλτράρουμε ώστε να μείνουν ΜΟΝΟ τα υπαρκτά έγγραφα
        List<FollowEntry> validFollows = systemState.getFollowManager()
            .getFollowsForUser(loggedInUser.getUsername())
            .stream()
            .filter(entry -> systemState.getDocumentManager().getDocumentById(entry.getDocumentId()) != null)
            .collect(Collectors.toList());

        followedDocumentsListView.setItems(FXCollections.observableArrayList(validFollows));
        
        // 2. IDs των εγγράφων που ήδη ακολουθούνται
        List<String> followedIds = validFollows.stream()
            .map(FollowEntry::getDocumentId)
            .collect(Collectors.toList());

        // 3. Διαθέσιμα έγγραφα (όχι ακολουθούμενα και με δικαίωμα πρόσβασης)
        List<Document> availableDocs = systemState.getDocumentManager().getAllDocuments().stream()
            .filter(doc -> {
                if (loggedInUser.getRole() == Role.Admin) return true;
                return loggedInUser.canAccessCategory(doc.getCategoryId());
            })
            .filter(doc -> !followedIds.contains(doc.getDocumentId()))
            .collect(Collectors.toList());

        availableDocumentsListView.setItems(FXCollections.observableArrayList(availableDocs));
    }

    @FXML
    private void handleAddFollow() {
        ObservableList<Document> selectedDocs = availableDocumentsListView.getSelectionModel().getSelectedItems();
        if (selectedDocs == null || selectedDocs.isEmpty()) return;

        for (Document docToFollow : new ArrayList<>(selectedDocs)) {
            int currentVersion = docToFollow.getLatestVersion().getVersionNumber();
            systemState.getFollowManager().addFollow(
                loggedInUser.getUsername(),
                docToFollow.getDocumentId(),
                currentVersion
            );
        }
        
        refreshUI("Started following the selected documents.");
    }

    @FXML
    private void handleRemoveFollow() {
        ObservableList<FollowEntry> selectedEntries = followedDocumentsListView.getSelectionModel().getSelectedItems();
        if (selectedEntries == null || selectedEntries.isEmpty()) return;

        for (FollowEntry entry : new ArrayList<>(selectedEntries)) {
            systemState.getFollowManager().removeFollow(
                loggedInUser.getUsername(),
                entry.getDocumentId()
            );
        }

        refreshUI("Stopped following the selected documents.");
    }

    private void refreshUI(String successMessage) {
        if (mainController != null) {
            mainController.updateSummaryLabels();
        }
        loadLists();
        addFollowButton.setVisible(false);
        removeFollowButton.setVisible(false);
        showAlert("Success", successMessage, Alert.AlertType.INFORMATION);
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (availableDocumentsListView.getScene() != null) {
            alert.initOwner(availableDocumentsListView.getScene().getWindow());
        }
        alert.showAndWait();
    }
}