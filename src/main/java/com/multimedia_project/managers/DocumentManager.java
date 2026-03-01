package com.multimedia_project.managers;

import com.multimedia_project.model.Document;
import com.multimedia_project.model.DocumentVersion;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Διαχειριστής εγγράφων του συστήματος.
 * Υπεύθυνος για τη δημιουργία, τροποποίηση, διαγραφή και αναζήτηση εγγράφων,
 * καθώς και για τη διαχείριση των εκδόσεών τους.
 */
public class DocumentManager {
    private List<Document> documents;
    private FollowManager followManager;

    /**
     * Κατασκευαστής του DocumentManager.
     * @param followManager Ο διαχειριστής ακολουθήσεων για τον καθαρισμό δεδομένων κατά τη διαγραφή.
     */
    public DocumentManager(FollowManager followManager) {
        this.documents = new ArrayList<>();
        this.followManager = followManager;
    }

    /**
     * Δημιουργεί ένα νέο έγγραφο στο σύστημα και αρχικοποιεί την πρώτη του έκδοση (V1).
     * @param title      Ο τίτλος του εγγράφου.
     * @param categoryId Το ID της κατηγορίας στην οποία ανήκει.
     * @param authorId   Το ID του συγγραφέα.
     * @param authorName Το πλήρες όνομα του συγγραφέα.
     * @param content    Το αρχικό κείμενο του εγγράφου.
     * @return Το αντικείμενο {@link Document} που δημιουργήθηκε.
     * @throws Exception Εάν υπάρχει ήδη έγγραφο με τον ίδιο τίτλο.
     */
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

    /**
     * Αναζητά έγγραφα με βάση πολλαπλά κριτήρια φιλτραρίσματος.
     * @param title      Τμήμα ή ολόκληρος ο τίτλος προς αναζήτηση.
     * @param authorName Το όνομα του συγγραφέα.
     * @param categoryId Το ID της κατηγορίας.
     * @return Μια λίστα με τα έγγραφα που πληρούν τα κριτήρια.
     */
    public List<Document> searchDocuments(String title, String authorName, Integer categoryId) {
        return documents.stream()
            .filter(d -> title == null || title.isEmpty() || d.getTitle().toLowerCase().contains(title.toLowerCase()))
            .filter(d -> authorName == null || authorName.isEmpty() || d.getAuthorName().toLowerCase().contains(authorName.toLowerCase()))
            .filter(d -> categoryId == null || d.getCategoryId() == categoryId)
            .collect(Collectors.toList());
    }

    /**
     * Τροποποιεί ένα υπάρχον έγγραφο προσθέτοντας μια νέα έκδοση με ενημερωμένο περιεχόμενο.
     * @param documentId Το ID του εγγράφου προς τροποποίηση.
     * @param newContent Το νέο περιεχόμενο του εγγράφου.
     * @return true αν η τροποποίηση πέτυχε, false αν το έγγραφο δεν βρέθηκε.
     */
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

    /**
     * Διαγράφει ένα έγγραφο από το σύστημα και αφαιρεί όλες τις σχετικές ακολουθήσεις (follows).
     * @param documentId Το μοναδικό ID του εγγράφου προς διαγραφή.
     * @return true αν η διαγραφή ολοκληρώθηκε, false αν το έγγραφο δεν υπήρχε.
     */
    public boolean deleteDocument(String documentId) {
        boolean removed = documents.removeIf(d -> d.getDocumentId().equals(documentId));
        if (removed) {
            followManager.removeFollowsForDeletedDocument(documentId);
        }
        return removed;
    }

    /**
     * Διαγράφει μαζικά όλα τα έγγραφα που ανήκουν σε μια συγκεκριμένη κατηγορία.
     * @param categoryId Το ID της κατηγορίας προς εκκαθάριση.
     */
    public void deleteDocumentsByCategory(int categoryId) {
        List<String> idsToDelete = documents.stream()
            .filter(d -> d.getCategoryId() == categoryId)
            .map(Document::getDocumentId)
            .collect(Collectors.toList());
        
        for (String id : idsToDelete) {
            deleteDocument(id);
        }
    }
    
    /**
     * Ενημερώνει τη λίστα των εγγράφων με δεδομένα από εξωτερική πηγή (π.χ. φόρτωση από JSON).
     * @param loadedDocuments Η νέα λίστα εγγράφων.
     */
    public void setDocuments(List<Document> loadedDocuments) {
        if (loadedDocuments != null) {
            this.documents = new ArrayList<>(loadedDocuments);
        }
    }

    /**
     * Επιστρέφει το σύνολο των εγγράφων που υπάρχουν στο σύστημα.
     * @return Λίστα με όλα τα αντικείμενα {@link Document}.
     */
    public List<Document> getAllDocuments() {
        return documents;
    }

    /**
     * Αναζητά ένα συγκεκριμένο έγγραφο με βάση το ID του.
     * @param documentId Το ID του εγγράφου.
     * @return Το αντικείμενο {@link Document} αν βρεθεί, αλλιώς null.
     */
    public Document getDocumentById(String documentId) {
        return documents.stream()
                .filter(d -> d.getDocumentId().equals(documentId))
                .findFirst()
                .orElse(null);
    }
}