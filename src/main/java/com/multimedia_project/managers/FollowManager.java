package com.multimedia_project.managers;

import com.multimedia_project.model.FollowEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// Αποθήκευση της λίστας παρακολουθήσεων ανά χρήστη
public class FollowManager {
    // Key: Username, Value: Λίστα FollowEntry (in-memory state)
    private Map<String, List<FollowEntry>> userFollows; 

    public FollowManager() {
        this.userFollows = new ConcurrentHashMap<>();
    }

    public void addFollow(String username, String documentId, int currentVersion) {
        // Δημιουργία λίστας αν δεν υπάρχει
        userFollows.putIfAbsent(username, new ArrayList<>());
        List<FollowEntry> follows = userFollows.get(username);
        
        // Έλεγχος αν το έγγραφο ήδη παρακολουθείται
        boolean found = false;
        for (FollowEntry entry : follows) {
            if (entry.getDocumentId().equals(documentId)) {
                // Ενημέρωση versionAtFollow αν χρειάζεται (π.χ., αν θέλουμε να το "μηδενίσουμε")
                entry.setVersionAtFollow(currentVersion);
                found = true;
                break;
            }
        }
        if (!found) {
            follows.add(new FollowEntry(documentId, currentVersion));
        }
    }

    public void removeFollow(String username, String documentId) {
        if (userFollows.containsKey(username)) {
            userFollows.get(username).removeIf(e -> e.getDocumentId().equals(documentId));
        }
    }


    // [ΝΕΑ ΜΕΘΟΔΟΣ] Επιστρέφει το Map για αποθήκευση στο JSON
    public Map<String, List<FollowEntry>> getAllFollowsMap() {
        return userFollows;
    }
    

    // [ΝΕΑ ΜΕΘΟΔΟΣ] Καθορίζει το Map μετά τη φόρτωση του JSON
    public void setUserFollows(Map<String, List<FollowEntry>> loadedFollows) {
        if (loadedFollows != null) {
            this.userFollows = loadedFollows;
        }
    }

    // Κατά τον τερματισμό, αυτή η μέθοδος θα χρησιμοποιηθεί για να μετατραπεί 
    // το Map σε μια λίστα για αποθήκευση στο follows.json (δεν υλοποιείται εδώ).

    // ... Άλλες μέθοδοι: getFollowedDocuments(username), removeFollowsForDeletedDocument(documentId) ...
    // Μέθοδοι: addFollow(), removeFollow(), removeFollowsForDeletedDocument() ...
}