package com.multimedia_project.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Document {
    private String documentId;
    private String title;
    private String authorName;
    private int authorId;
    private int categoryId;
    private List<DocumentVersion> versions;

    public Document() {
        this.versions = new ArrayList<>();
    }

    public Document(String documentId, String title, int authorId, String authorName, int categoryId) {
        this.documentId = documentId;
        this.title = title;
        this.authorId = authorId;
        this.authorName = authorName;
        this.categoryId = categoryId;
        this.versions = new ArrayList<>();
    }

    public String getDocumentId() { return documentId; }
    public String getTitle() { return title; }
    public String getAuthorName() { return authorName; }
    public int getCategoryId() { return categoryId; }
    public int getAuthorId() { return authorId; }
    public List<DocumentVersion> getVersions() { return versions; }

    public DocumentVersion getLatestVersion() {
        if (versions.isEmpty()) return null;
        return versions.get(versions.size() - 1);
    }

    public void addVersion(String newContent) {
        int nextVersionNumber = getLatestVersion().getVersionNumber() + 1;
        this.versions.add(new DocumentVersion(nextVersionNumber, newContent));
    }

    public List<DocumentVersion> getVersions(User user) {
        if (user.getRole() == Role.SimpleUser) {
            return List.of(getLatestVersion());
        } else {
            int size = versions.size();
            int startIndex = Math.max(0, size - 3); 
            return versions.subList(startIndex, size);
        }
    }

    public void setAuthorId(int authorId) { this.authorId = authorId; }

    @Override
    public String toString() {
        return this.title;
    }
}