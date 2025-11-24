package com.multimedia_project.model;

import java.time.LocalDateTime;

public class DocumentVersion {
    private int versionNumber;
    private LocalDateTime creationDate;
    private String content;

    public DocumentVersion(int versionNumber, String content) {
        this.versionNumber = versionNumber;
        this.creationDate = LocalDateTime.now();
        this.content = content;
    }

    public int getVersionNumber() { return versionNumber; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public String getContent() { return content; }
}