package com.multimedia_project.managers;

import com.multimedia_project.model.User;
import com.multimedia_project.model.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserManager {
    private List<User> users;
    // Χρησιμοποιούμε μια σημαία για να ξέρουμε αν έχει τρέξει ποτέ το σύστημα
    private boolean initialRun = true; 

    public UserManager() {
        this.users = new ArrayList<>();
        this.users.add(new User(
            "medialab",
            "medialab_2025",
            "Default",
            "Admin",
            Role.Admin,
            List.of(1, 2) // Παράδειγμα πρόσβασης
        ));
    }

    public User authenticate(String username, String password) {
        return users.stream()
            .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
            .findFirst()
            .orElse(null); // Επιστρέφουμε null αν δεν βρεθεί
    }

    public void addUser(String username, String password, String firstName, String lastName, Role role, List<Integer> accessIds) {
        // Προαιρετικός έλεγχος μοναδικότητας
        boolean exists = users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        if (exists) {
            System.err.println("User already exists!");
            return;
        }
        this.users.add(new User(username, password, firstName, lastName, role, accessIds));
    }


    // [ΝΕΑ ΜΕΘΟΔΟΣ] Καθορίζει αν πρέπει να κρατήσουμε τον default admin ή να φορτώσουμε
    public boolean isInitialRun() {
        return initialRun;
    }
    
    // [ΝΕΑ ΜΕΘΟΔΟΣ] Καθορίζει τη λίστα χρηστών μετά τη φόρτωση του JSON
    public void setUsers(List<User> loadedUsers) {
        if (loadedUsers != null && !loadedUsers.isEmpty()) {
            this.users = loadedUsers;
            this.initialRun = false; // Έχουμε δεδομένα, άρα δεν είναι η πρώτη εκτέλεση
        }
    }

    // [ΝΕΑ ΜΕΘΟΔΟΣ] Επιστρέφει όλους τους χρήστες για αποθήκευση στο JSON
    public List<User> getAllUsers() {
        return users;
    }


    /**
     * Διαγράφει έναν χρήστη από το σύστημα βάσει του username.
     */
    public boolean deleteUser(String username) {
        if (username.equals("medialab")) {
            // Αποτροπή διαγραφής του προεπιλεγμένου Admin
            System.err.println("Cannot delete default system administrator.");
            return false;
        }

        // Η removeIf αφαιρεί τον χρήστη από τη λίστα αν το predicate είναι true
        boolean wasDeleted = users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));

        // ΣΗΜΑΝΤΙΚΟ:
        // Αν ο διαγραφόμενος χρήστης ήταν Συγγραφέας, ίσως χρειαστείτε λογική
        // για την ανάθεση των εγγράφων του σε άλλο χρήστη (αν και δεν ζητείται ρητά).
        // Για την απλότητα της εκφώνησης, απλά διαγράφουμε τον χρήστη.

        return wasDeleted;
    }

    public void removeCategoryIdFromAllUsers(int categoryId) {
        for (User user : users) {
            // Καλούμε τη μέθοδο που φτιάξαμε στην User.java
            List<Integer> categories = user.getAccessibleCategoryIds();
            
            if (categories != null) {
                categories.removeIf(id -> id == categoryId);
            }
        }
    }
}