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

    // Μέθοδος για το login
    public Optional<User> authenticate(String username, String password) {
        return users.stream()
            .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
            .findFirst();
    }

    public void addUser(String username, String password, String firstName, String lastName, Role role, List<Integer> accessIds) {
        // Εδώ θα έμπαινε έλεγχος για μοναδικό username
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

    // ... Άλλες μέθοδοι: deleteUser(), findUserByUsername(), getAllUsers() ...
    
    // ... Μέθοδοι: authenticate(), addUser(), deleteUser() ...
}