package com.multimedia_project.gui;

import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UnifiedDocumentController {

    // --- SEARCH SECTION ---
    @FXML private ComboBox<Category> categorySearchCombo;
    @FXML private TextField titleSearchField;
    @FXML private ListView<Document> documentListView;

    // --- DISPLAY SECTION ---
    @FXML private Label detailTitleLabel;
    @FXML private Label detailAuthorLabel;
    @FXML private Label versionInfoLabel;
    @FXML private ComboBox<Integer> versionSelectorCombo;
    @FXML private TextArea contentDisplayArea;

    // --- ACTION BUTTONS ---
    @FXML private Button followButton;
    @FXML private Button newDocumentButton;
    @FXML private Button editDocumentButton;
    @FXML private Button deleteDocumentButton;
    @FXML private Button saveChangesButton;

    private SystemState systemState;
    private User loggedInUser;
    private MainController mainController;
    private Document selectedDocument;
    private boolean isEditing = false;

    public void initializeData(SystemState state, User user) {
        this.systemState = state;
        this.loggedInUser = user;

        setupUIByRole();
        loadCategories();
        setupListView();
        
        // Αρχική αναζήτηση για να γεμίσει η λίστα
        handleSearch();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setupUIByRole() {
        // Οι απλοί χρήστες δεν βλέπουν καθόλου τα κουμπιά διαχείρισης
        boolean isStaff = loggedInUser.getRole() == Role.Admin || loggedInUser.getRole() == Role.Author;
        newDocumentButton.setVisible(isStaff);
        editDocumentButton.setVisible(isStaff);
        deleteDocumentButton.setVisible(isStaff);
        saveChangesButton.setVisible(false);
    }

    private void loadCategories() {
        List<Category> categories;
        
        if (loggedInUser.getRole() == Role.Admin) {
            // Ο Admin βλέπει ΤΑ ΠΑΝΤΑ
            categories = systemState.getCategoryManager().getAllCategories();
        } else {
            // Οι υπόλοιποι βλέπουν μόνο ό,τι τους επιτρέπεται
            categories = systemState.getCategoryManager().getAllCategories().stream()
                    .filter(c -> loggedInUser.canAccessCategory(c.getId()))
                    .collect(Collectors.toList());
        }

        Category all = new Category(0, "All Categories");
        ObservableList<Category> comboItems = FXCollections.observableArrayList(all);
        comboItems.addAll(categories);

        categorySearchCombo.setItems(comboItems);
        categorySearchCombo.getSelectionModel().selectFirst();
    }

    private void setupListView() {
        documentListView.setCellFactory(lv -> new ListCell<Document>() {
            @Override
            protected void updateItem(Document item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getTitle() + " (" + item.getAuthorName() + ")");
            }
        });

        documentListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> showDocumentDetails(newVal)
        );
    }

    @FXML
    private void handleSearch() {
        Category selectedCat = categorySearchCombo.getValue();
        String titleQuery = titleSearchField.getText().trim().toLowerCase();

        List<Document> filtered = systemState.getDocumentManager().getAllDocuments().stream()
                .filter(doc -> {
                    // Αν είναι Admin, περνάει πάντα το φίλτρο πρόσβασης
                    if (loggedInUser.getRole() == Role.Admin) return true;
                    // Αλλιώς, έλεγχος δικαιωμάτων
                    return loggedInUser.canAccessCategory(doc.getCategoryId());
                })
                .filter(doc -> selectedCat == null || selectedCat.getId() == 0 || doc.getCategoryId() == selectedCat.getId())
                .filter(doc -> titleQuery.isEmpty() || doc.getTitle().toLowerCase().contains(titleQuery))
                .collect(Collectors.toList());

        documentListView.setItems(FXCollections.observableArrayList(filtered));
    }

    private void showDocumentDetails(Document doc) {
        if (doc == null) {
            clearDetails();
            return;
        }
        this.selectedDocument = doc;
        this.isEditing = false;
        contentDisplayArea.setEditable(false);
        saveChangesButton.setVisible(false);

        // 1. Βασικές Πληροφορίες
        detailTitleLabel.setText(doc.getTitle());
        detailAuthorLabel.setText("Author: " + doc.getAuthorName());

        // 2. Έλεγχος Follow
        updateFollowButtonState();

        // 3. Περιορισμός Versions βάσει Ρόλου
        setupVersionControl(doc);

        // 4. Έλεγχος Δικαιωμάτων Edit/Delete
        boolean canModify = (loggedInUser.getRole() == Role.Admin) || 
                            (loggedInUser.getRole() == Role.Author && loggedInUser.canAccessCategory(doc.getCategoryId()));
        
        editDocumentButton.setDisable(!canModify);
        deleteDocumentButton.setDisable(!canModify);

        updateDisplay(doc.getLatestVersion());
    }

    private void setupVersionControl(Document doc) {
        List<DocumentVersion> allVers = doc.getVersions();
        List<Integer> allowedNums;

        if (loggedInUser.getRole() == Role.Admin) {
            // Admin: Όλες οι εκδόσεις
            allowedNums = allVers.stream().map(DocumentVersion::getVersionNumber).collect(Collectors.toList());
        } else if (loggedInUser.getRole() == Role.Author) {
            // Author: Τελευταίες 5
            allowedNums = allVers.stream()
                    .map(DocumentVersion::getVersionNumber)
                    .sorted(Comparator.reverseOrder())
                    .limit(5)
                    .sorted()
                    .collect(Collectors.toList());
        } else {
            // User: Μόνο η τελευταία
            allowedNums = List.of(doc.getLatestVersion().getVersionNumber());
        }

        versionSelectorCombo.setItems(FXCollections.observableArrayList(allowedNums));
        versionSelectorCombo.getSelectionModel().selectLast();
        versionSelectorCombo.setDisable(allowedNums.size() <= 1);
    }

    private void updateFollowButtonState() {
        boolean isFollowing = systemState.getFollowManager().isFollowing(loggedInUser.getUsername(), selectedDocument.getDocumentId());
        followButton.setText(isFollowing ? "Already Following" : "Follow Document");
        followButton.setDisable(isFollowing);
    }

    @FXML
    private void handleFollow() {
        if (selectedDocument == null) return;
        systemState.getFollowManager().addFollow(
                loggedInUser.getUsername(),
                selectedDocument.getDocumentId(),
                selectedDocument.getLatestVersion().getVersionNumber()
        );
        updateFollowButtonState();
        if (mainController != null) mainController.updateSummaryLabels();
        new Alert(Alert.AlertType.INFORMATION, "Following " + selectedDocument.getTitle()).show();
    }

    @FXML
    private void handleVersionChange() {
        if (selectedDocument == null || versionSelectorCombo.getValue() == null) return;
        int vn = versionSelectorCombo.getValue();
        selectedDocument.getVersions().stream()
                .filter(v -> v.getVersionNumber() == vn)
                .findFirst().ifPresent(this::updateDisplay);
    }

    private void updateDisplay(DocumentVersion v) {
        contentDisplayArea.setText(v.getContent());
        versionInfoLabel.setText("V" + v.getVersionNumber() + " | " + v.getCreationDate().toLocalDate());
    }

    // --- MANAGEMENT OPERATIONS ---

    @FXML
    private void handleNewDocument() {
        // 1. Δημιουργία του Custom Dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Document");
        dialog.setHeaderText("Enter details for the new document");

        // 2. Ορισμός των Buttons (Create και Cancel)
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // 3. Δημιουργία του Layout του διαλόγου (GridPane)
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Document Title");
        
        // ΛΟΓΙΚΗ ADMIN: Αν είναι Admin δείξε όλες τις κατηγορίες, αλλιώς μόνο τις επιτρεπόμενες
        List<Category> availableCategories;
        if (loggedInUser.getRole() == Role.Admin) {
            availableCategories = systemState.getCategoryManager().getAllCategories();
        } else {
            availableCategories = systemState.getCategoryManager().getAllCategories().stream()
                    .filter(c -> loggedInUser.canAccessCategory(c.getId()))
                    .collect(Collectors.toList());
        }
        
        ComboBox<Category> catCombo = new ComboBox<>(FXCollections.observableArrayList(availableCategories));
        catCombo.setPromptText("Select Category");
        if (!availableCategories.isEmpty()) {
            catCombo.getSelectionModel().selectFirst();
        }

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter document content here...");
        contentArea.setPrefRowCount(10);

        // Τοποθέτηση στοιχείων στο Grid
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(catCombo, 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(contentArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // 4. Επεξεργασία του αποτελέσματος όταν πατηθεί το "Create"
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == createButtonType) {
            String title = titleField.getText().trim();
            Category selectedCat = catCombo.getValue();
            String content = contentArea.getText();

            // Έλεγχος εγκυρότητας
            if (title.isEmpty() || selectedCat == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Title and Category are required!");
                alert.showAndWait();
                return;
            }

            // 5. Κλήση του DocumentManager για τη δημιουργία του εγγράφου
            Document newDoc = systemState.getDocumentManager().createDocument(
                title, 
                selectedCat.getId(), 
                loggedInUser.getId(), 
                loggedInUser.getUsername(),
                content
            );

            if (newDoc != null) {
                // Ανανέωση της λίστας εγγράφων στο UI
                handleSearch(); 
                
                // Επιλογή του νέου εγγράφου στη λίστα και προβολή των λεπτομερειών του
                documentListView.getSelectionModel().select(newDoc);
                showDocumentDetails(newDoc);
                
                // Ενημέρωση των labels στο Main Dashboard
                if (mainController != null) {
                    mainController.updateSummaryLabels();
                }
                
                Alert success = new Alert(Alert.AlertType.INFORMATION, "Document created successfully!");
                success.showAndWait();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR, "Failed to create document.");
                error.showAndWait();
            }
        }
    }

    @FXML
    private void handleEdit() {
        isEditing = true;
        contentDisplayArea.setEditable(true);
        saveChangesButton.setVisible(true);
        editDocumentButton.setDisable(true);
    }

    @FXML
    private void handleSave() {
        if (systemState.getDocumentManager().modifyDocument(selectedDocument.getDocumentId(), contentDisplayArea.getText())) {
            showDocumentDetails(selectedDocument);
            new Alert(Alert.AlertType.INFORMATION, "New version created!").show();
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this document?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            systemState.getDocumentManager().deleteDocument(selectedDocument.getDocumentId());
            handleSearch();
            clearDetails();
            if (mainController != null) mainController.updateSummaryLabels();
        }
    }

    private void clearDetails() {
        selectedDocument = null;
        detailTitleLabel.setText("");
        detailAuthorLabel.setText("");
        contentDisplayArea.setText("");
        versionSelectorCombo.getItems().clear();
        editDocumentButton.setDisable(true);
        deleteDocumentButton.setDisable(true);
    }
}