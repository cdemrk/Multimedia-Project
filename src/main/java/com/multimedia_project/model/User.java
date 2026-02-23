package com.multimedia_project.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int userid;
    private String username;
    private String password; // Θα έπρεπε να είναι hashed στην πραγματικότητα
    private String firstName;
    private String lastName;
    private Role role;
    private List<Integer> accessCategoryIds;
    
    // Constructor, Getters και Setters
    public User(String username, String password, String firstName, String lastName, Role role, List<Integer> accessCategoryIds) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.accessCategoryIds = accessCategoryIds;
    }

    public int getId() { return userid; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Role getRole() { return role; }
    public List<Integer> getAccessCategoryIds() { return accessCategoryIds; }

    // Μέθοδος για έλεγχο δικαιωμάτων
    public boolean canAccessCategory(int categoryId) {
        return accessCategoryIds.contains(categoryId);
    }

    @Override
    public String toString() {
        // Επιστρέφει το όνομα και το επώνυμο (π.χ. "Chris Papadopoulos")
        if (firstName != null && lastName != null && !firstName.isEmpty()) {
            return firstName + " " + lastName + " (" + username + ")";
        }
        return username; 
    }

    public List<Integer> getAccessibleCategoryIds() {
        if (this.accessCategoryIds == null) {
            this.accessCategoryIds = new ArrayList<>();
        }
        return this.accessCategoryIds;
    }
}