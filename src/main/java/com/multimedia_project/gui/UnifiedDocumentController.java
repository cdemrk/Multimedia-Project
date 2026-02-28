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
    @FXML private VBox placeholderBox;      // Το κεντραρισμένο μήνυμα
    @FXML private VBox documentDetailsBox;  // Το κουτί με τα πραγματικά δεδομένα
    
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
        
        handleSearch();
        clearDetails(); // Εμφανίζει το ωραίο Empty State στην αρχή!
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void setupUIByRole() {
        boolean isStaff = loggedInUser.getRole() == Role.Admin || loggedInUser.getRole() == Role.Author;
        newDocumentButton.setVisible(isStaff);
        if (editDocumentButton != null) editDocumentButton.setVisible(isStaff);
        if (deleteDocumentButton != null) deleteDocumentButton.setVisible(isStaff);
        if (saveChangesButton != null) saveChangesButton.setVisible(false);
    }

    private void loadCategories() {
        List<Category> categories;
        
        if (loggedInUser.getRole() == Role.Admin) {
            categories = systemState.getCategoryManager().getAllCategories();
        } else {
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
    public void handleSearch() {
        Category selectedCat = categorySearchCombo.getValue();
        String titleQuery = titleSearchField.getText().trim().toLowerCase();

        List<Document> filtered = systemState.getDocumentManager().getAllDocuments().stream()
                .filter(doc -> {
                    if (loggedInUser.getRole() == Role.Admin) return true;
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
        
        // 1. ΕΜΦΑΝΙΣΗ ΤΟΥ ΕΓΓΡΑΦΟΥ & ΑΠΟΚΡΥΨΗ ΤΟΥ PLACEHOLDER
        if (placeholderBox != null) placeholderBox.setVisible(false);
        if (documentDetailsBox != null) documentDetailsBox.setVisible(true);

        this.selectedDocument = doc;
        this.isEditing = false;
        contentDisplayArea.setEditable(false);
        saveChangesButton.setVisible(false);

        // 2. Βασικές Πληροφορίες
        detailTitleLabel.setText(doc.getTitle());
        detailAuthorLabel.setText("Author: " + doc.getAuthorName());

        updateFollowButtonState();
        setupVersionControl(doc);

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
            allowedNums = allVers.stream().map(DocumentVersion::getVersionNumber).collect(Collectors.toList());
        } else if (loggedInUser.getRole() == Role.Author) {
            allowedNums = allVers.stream()
                    .map(DocumentVersion::getVersionNumber)
                    .sorted(Comparator.reverseOrder())
                    .limit(5)
                    .sorted()
                    .collect(Collectors.toList());
        } else {
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
    public void handleFollow() {
        if (selectedDocument == null) return;
        
        systemState.getFollowManager().addFollow(
                loggedInUser.getUsername(),
                selectedDocument.getDocumentId(),
                selectedDocument.getLatestVersion().getVersionNumber()
        );
        
        updateFollowButtonState();

        if (mainController != null) {
            mainController.updateSummaryLabels();
        }
        
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

    @FXML
    private void handleNewDocument() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Document");
        dialog.setHeaderText("Enter details for the new document");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Document Title");
        
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

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(catCombo, 1, 1);
        grid.add(new Label("Content:"), 0, 2);
        grid.add(contentArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == createButtonType) {
            String title = titleField.getText().trim();
            Category selectedCat = catCombo.getValue();
            String content = contentArea.getText();

            if (title.isEmpty() || selectedCat == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Title and Category are required!");
                alert.showAndWait();
                return;
            }

            try {
                String fullName = loggedInUser.getFirstName() + " " + loggedInUser.getLastName();

                Document newDoc = systemState.getDocumentManager().createDocument(
                    title, 
                    selectedCat.getId(), 
                    loggedInUser.getId(), 
                    fullName,
                    content
                );

                handleSearch(); 
                documentListView.getSelectionModel().select(newDoc);
                showDocumentDetails(newDoc);
                
                if (mainController != null) {
                    mainController.updateSummaryLabels();
                }
                
                Alert success = new Alert(Alert.AlertType.INFORMATION, "Document created successfully!");
                success.showAndWait();

            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR, e.getMessage());
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
            int newVersion = selectedDocument.getLatestVersion().getVersionNumber();
            if (systemState.getFollowManager().isFollowing(loggedInUser.getUsername(), selectedDocument.getDocumentId())) {
                systemState.getFollowManager().updateFollowVersion(loggedInUser.getUsername(), selectedDocument.getDocumentId(), newVersion);
            }

            showDocumentDetails(selectedDocument);
            new Alert(Alert.AlertType.INFORMATION, "New version created!").show();
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedDocument == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this document and all its follows?", ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            String docId = selectedDocument.getDocumentId();
            systemState.getFollowManager().removeFollowsByDocumentId(docId);
            systemState.getDocumentManager().deleteDocument(docId);
            handleSearch();
            clearDetails();
            if (mainController != null) {mainController.updateSummaryLabels();}
        }
    }
    
    private void clearDetails() {
        selectedDocument = null;
        
        // 1. ΕΜΦΑΝΙΣΗ ΤΟΥ PLACEHOLDER & ΑΠΟΚΡΥΨΗ ΤΟΥ ΕΓΓΡΑΦΟΥ
        if (placeholderBox != null) placeholderBox.setVisible(true);
        if (documentDetailsBox != null) documentDetailsBox.setVisible(false);
        
        if (detailTitleLabel != null) detailTitleLabel.setText("");
        if (detailAuthorLabel != null) detailAuthorLabel.setText("");
        if (contentDisplayArea != null) contentDisplayArea.setText("");
        if (versionSelectorCombo != null) versionSelectorCombo.getItems().clear();
        if (editDocumentButton != null) editDocumentButton.setDisable(true);
        if (deleteDocumentButton != null) deleteDocumentButton.setDisable(true);
    }
}