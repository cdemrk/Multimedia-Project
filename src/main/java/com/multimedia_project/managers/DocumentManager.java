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
    private int nextDocumentId = 1;
    private FollowManager followManager; // αναφορά στον FollowManager για διαχείριση διαγραφών


    public DocumentManager(FollowManager followManager) {
        this.documents = new ArrayList<>();
        this.followManager = followManager;
    }


    public Document createDocument(String title, int categoryId, int authorId, String authorName, String content) {
        String newId = "DOC_" + (documents.size() + 1);
        Document doc = new Document(newId, title, authorId, authorName, categoryId);
        
        DocumentVersion v1 = new DocumentVersion(1, content, LocalDateTime.now());
        doc.getVersions().add(v1);
        
        documents.add(doc);
        return doc;
    }

    public List<Document> searchDocuments(String title, String authorName, Integer categoryId) {
        return documents.stream()
            .filter(d -> title == null || title.isEmpty() || d.getTitle().toLowerCase().contains(title.toLowerCase()))
            // Εδώ χρησιμοποιούμε το d.getAuthorName() που κράτησες
            .filter(d -> authorName == null || authorName.isEmpty() || d.getAuthorName().toLowerCase().contains(authorName.toLowerCase()))
            .filter(d -> categoryId == null || d.getCategoryId() == categoryId)
            .collect(Collectors.toList());
    }

    // Τροποποίηση εγγράφου (ενσωματώνει Versioning)
    public boolean modifyDocument(String documentId, String newContent) {
        Optional<Document> docOpt = documents.stream()
                .filter(d -> d.getDocumentId().equals(documentId))
                .findFirst();
        
        if (docOpt.isPresent()) {
            Document doc = docOpt.get();
            doc.addVersion(newContent); // Αυτό αυξάνει αυτόματα τον αριθμό έκδοσης
            
            // **ΣΗΜΑΝΤΙΚΟ:** Κατά την τροποποίηση, ενημερώνουμε τη λογική παρακολούθησης
            // (αν και η πραγματική ενημέρωση του followEntry.versionAtFollow γίνεται στο login)
            // Εδώ απλώς έχει καταγραφεί η αλλαγή in-memory.
            return true;
        }
        return false;
    }

    // Διαγραφή εγγράφου (αφαιρεί όλες τις εκδόσεις)
    public boolean deleteDocument(String documentId) {
        boolean removed = documents.removeIf(d -> d.getDocumentId().equals(documentId));
        
        if (removed) {
            // Ενημέρωση του FollowManager για να αφαιρέσει όλες τις παρακολουθήσεις αυτού του εγγράφου
            followManager.removeFollowsForDeletedDocument(documentId);
        }
        return removed;
    }


    // Διαγραφή όλων των εγγράφων μιας κατηγορίας (καλείται από CategoryManager)
    public void deleteDocumentsByCategory(int categoryId) {
        List<String> idsToDelete = documents.stream()
            .filter(d -> d.getCategoryId() == categoryId)
            .map(Document::getDocumentId)
            .collect(Collectors.toList());
        
        for (String id : idsToDelete) {
            deleteDocument(id); // Χρησιμοποιούμε τη μέθοδο διαγραφής για να ενημερωθεί ο FollowManager
        }
    }
    
    // ... Άλλες μέθοδοι: getDocumentById(), getAllDocuments() ...


    // Καθορίζει τη λίστα εγγράφων μετά τη φόρτωση του JSON
    public void setDocuments(List<Document> loadedDocuments) {
        if (loadedDocuments != null) {
            this.documents = loadedDocuments;
            // Ενημέρωση του nextDocumentId αν χρειάζεται
            // (Λογική εύρεσης του μέγιστου ID, παρόμοια με CategoryManager)
        }
    }

    // Επιστρέφει όλα τα έγγραφα για αποθήκευση στο JSON
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