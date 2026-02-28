package com.multimedia_project.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int userid;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Role role;
    private List<Integer> accessCategoryIds;
    
    public User(int userid, String username, String password, String firstName, String lastName, Role role, List<Integer> accessCategoryIds) {
        this.userid = userid;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.accessCategoryIds = (accessCategoryIds != null) ? new ArrayList<>(accessCategoryIds) : new ArrayList<>();
    }

    public int getId() { return userid; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Role getRole() { return role; }

    public boolean canAccessCategory(int categoryId) {
        // 
        if (this.role == Role.Admin) {
            return true;
        }
        return accessCategoryIds != null && accessCategoryIds.contains(categoryId);
    }

    public List<Integer> getAccessibleCategoryIds() {
        if (this.accessCategoryIds == null) {
            this.accessCategoryIds = new ArrayList<>();
        }
        return this.accessCategoryIds;
    }

    public void setId(int userid) {
        this.userid = userid;
    }

    @Override
    public String toString() {
        if (firstName != null && lastName != null && !firstName.isEmpty()) {
            return firstName + " " + lastName + " (" + username + ") - " + role;
        }
        return username + " (" + role + ")"; 
    }
}