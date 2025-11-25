package com.multimedia_project.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Document {
    private String documentId; // Μοναδικό ID
    private String title;
    private String authorName;
    private int categoryId;
    private List<DocumentVersion> versions;

    // no-arg constructor για τον Gson
    public Document() {
        this.versions = new ArrayList<>();
    }

    public Document(String documentId, String title, String authorName, int categoryId, String initialContent) {
        this.documentId = documentId;
        this.title = title;
        this.authorName = authorName;
        this.categoryId = categoryId;
        this.versions = new ArrayList<>();
        this.versions.add(new DocumentVersion(1, initialContent)); // Προκαθορισμένος αριθμός έκδοσης V1
    }

    // --- Getters and Setters ---
    public String getDocumentId() { return documentId; }
    public String getTitle() { return title; }
    public String getAuthorName() { return authorName; }
    public int getCategoryId() { return categoryId; }
    public List<DocumentVersion> getVersions() { return versions; }

    // Επιστρέφει την τελευταία έκδοση
    public DocumentVersion getLatestVersion() {
        if (versions.isEmpty()) return null;
        return versions.get(versions.size() - 1);
    }

    // Versioning: Προσθήκη νέας έκδοσης
    public void addVersion(String newContent) {
        int nextVersionNumber = getLatestVersion().getVersionNumber() + 1;
        this.versions.add(new DocumentVersion(nextVersionNumber, newContent));
    }

    // Πρόσβαση σε προηγούμενες εκδόσεις (για Συγγραφείς/Διαχειριστές)
    public List<DocumentVersion> getVersions(User user) {
        // Χρειάζεστε πρόσβαση στην κλάση Role (π.χ. import com.multimedia_project.model.Role;)
        if (user.getRole() == Role.SimpleUser) {
            // Απλός χρήστης βλέπει μόνο την τελευταία
            return List.of(getLatestVersion());
        } else {
            // Συγγραφέας/Διαχειριστής βλέπει την τελευταία + 2 προηγούμενες
            int size = versions.size();
            // max(0, size - 3) εξασφαλίζει ότι δεν πάμε σε αρνητικό index
            int startIndex = Math.max(0, size - 3); 
            return versions.subList(startIndex, size);
        }
    }

    @Override
    public String toString() {
        return this.title;
    }
}