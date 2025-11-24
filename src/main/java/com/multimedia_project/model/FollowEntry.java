package com.multimedia_project.model;

public class FollowEntry {
    private String documentId;
    private int versionAtFollow; // Έκδοση κατά την τελευταία ενημέρωση/login

    public FollowEntry(String documentId, int versionAtFollow) {
        this.documentId = documentId;
        this.versionAtFollow = versionAtFollow;
    }

    public String getDocumentId() { return documentId; }
    public int getVersionAtFollow() { return versionAtFollow; }
    public void setVersionAtFollow(int versionAtFollow) { this.versionAtFollow = versionAtFollow; }

    // Έλεγχος αν υπάρχει νέα έκδοση
    public boolean hasNewVersion(int latestVersion) {
        return latestVersion > versionAtFollow;
    }
}