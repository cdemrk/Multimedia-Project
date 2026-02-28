package com.multimedia_project.managers;

import com.multimedia_project.model.Document;
import com.multimedia_project.model.DocumentVersion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DocumentManager {
    private List<Document> documents;
    private FollowManager followManager;

    public DocumentManager(FollowManager followManager) {
        this.documents = new ArrayList<>();
        this.followManager = followManager;
    }

    public Document createDocument(String title, int categoryId, int authorId, String authorName, String content) throws Exception {
        
        String cleanTitle = title.trim();
        boolean titleExists = documents.stream().anyMatch(d -> d.getTitle().equalsIgnoreCase(cleanTitle));
        
        if (titleExists) {
            throw new Exception("A document with name '" + cleanTitle + "' already exists!");
        }

        int maxId = 0;
        for (Document d : documents) {
            try {
                int currentId = Integer.parseInt(d.getDocumentId());
                if (currentId > maxId) maxId = currentId;
            } catch (NumberFormatException e) {
                String cleanId = d.getDocumentId().replace("DOC_", "");
                try {
                    int currentId = Integer.parseInt(cleanId);
                    if (currentId > maxId) maxId = currentId;
                } catch (Exception ex) { /* Ignore invalid formats */ }
            }
        }

        String newId = String.valueOf(maxId + 1);
        
        Document doc = new Document(newId, cleanTitle, authorId, authorName, categoryId);
        
        DocumentVersion v1 = new DocumentVersion(1, content, LocalDateTime.now());
        doc.getVersions().add(v1);
        
        try {
            documents.add(doc);
        } catch (UnsupportedOperationException e) {
            documents = new ArrayList<>(documents);
            documents.add(doc);
        }
        
        return doc;
    }

    public List<Document> searchDocuments(String title, String authorName, Integer categoryId) {
        return documents.stream()
            .filter(d -> title == null || title.isEmpty() || d.getTitle().toLowerCase().contains(title.toLowerCase()))
            .filter(d -> authorName == null || authorName.isEmpty() || d.getAuthorName().toLowerCase().contains(authorName.toLowerCase()))
            .filter(d -> categoryId == null || d.getCategoryId() == categoryId)
            .collect(Collectors.toList());
    }

    public boolean modifyDocument(String documentId, String newContent) {
        Optional<Document> docOpt = documents.stream()
                .filter(d -> d.getDocumentId().equals(documentId))
                .findFirst();
        
        if (docOpt.isPresent()) {
            Document doc = docOpt.get();
            doc.addVersion(newContent); 
            return true;
        }
        return false;
    }

    public boolean deleteDocument(String documentId) {
        boolean removed = documents.removeIf(d -> d.getDocumentId().equals(documentId));
        if (removed) {
            followManager.removeFollowsForDeletedDocument(documentId);
        }
        return removed;
    }

    public void deleteDocumentsByCategory(int categoryId) {
        List<String> idsToDelete = documents.stream()
            .filter(d -> d.getCategoryId() == categoryId)
            .map(Document::getDocumentId)
            .collect(Collectors.toList());
        
        for (String id : idsToDelete) {
            deleteDocument(id);
        }
    }
    
    public void setDocuments(List<Document> loadedDocuments) {
        if (loadedDocuments != null) {
            this.documents = new ArrayList<>(loadedDocuments);
        }
    }

    public List<Document> getAllDocuments() {
        return documents;
    }

    public Document getDocumentById(String documentId) {
        return documents.stream()
                .filter(d -> d.getDocumentId().equals(documentId))
                .findFirst()
                .orElse(null);
    }
}