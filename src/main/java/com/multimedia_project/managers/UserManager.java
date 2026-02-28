package com.multimedia_project.managers;

import com.multimedia_project.model.User;
import com.multimedia_project.model.Role;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private List<User> users;
    private boolean initialRun = true; 

    public UserManager() {
        this.users = new ArrayList<>();
        this.users.add(new User(
            1, 
            "medialab",
            "medialab_2025",
            "Default",
            "Admin",
            Role.Admin,
            new ArrayList<>()
        ));
    }

    public User authenticate(String username, String password) {
        return users.stream()
            .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
            .findFirst()
            .orElse(null);
    }

    public void addUser(String username, String password, String firstName, String lastName, Role role, List<Integer> accessIds) throws Exception {

        boolean exists = users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
        if (exists) {
            throw new Exception("The username '" + username + "' already exists!");
        }

        int nextId = users.stream()
                        .mapToInt(User::getId)
                        .max()
                        .orElse(0) + 1;

        List<Integer> finalAccessIds = (role == Role.Admin) ? new ArrayList<>() : new ArrayList<>(accessIds);

        this.users.add(new User(nextId, username, password, firstName, lastName, role, finalAccessIds));
    }

    public boolean isInitialRun() {
        return initialRun;
    }
    
    public void setUsers(List<User> loadedUsers) {
        if (loadedUsers != null && !loadedUsers.isEmpty()) {
            this.users = loadedUsers;
            this.initialRun = false;
        }
    }

    public List<User> getAllUsers() {
        return users;
    }

    public boolean deleteUser(String username) {
        if (username.equals("medialab")) {
            System.err.println("Cannot delete default system administrator.");
            return false;
        }
        return users.removeIf(u -> u.getUsername().equalsIgnoreCase(username));
    }

    public void removeCategoryIdFromAllUsers(int categoryId) {
        for (User user : users) {
            List<Integer> categories = user.getAccessibleCategoryIds();
            if (categories != null) {
                categories.removeIf(id -> id == categoryId);
            }
        }
    }
}