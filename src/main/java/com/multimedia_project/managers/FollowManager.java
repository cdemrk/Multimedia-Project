package com.multimedia_project.managers;

import com.multimedia_project.model.FollowEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FollowManager {
    private Map<String, List<FollowEntry>> userFollows; 

    public FollowManager() {
        this.userFollows = new ConcurrentHashMap<>();
    }

    public void addFollow(String username, String documentId, int currentVersion) {
        userFollows.putIfAbsent(username, new ArrayList<>());
        List<FollowEntry> follows = userFollows.get(username);
        
        boolean found = false;
        for (FollowEntry entry : follows) {
            if (entry.getDocumentId().equals(documentId)) {
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


    public Map<String, List<FollowEntry>> getAllFollowsMap() {
        return userFollows;
    }

    public void setUserFollows(Map<String, List<FollowEntry>> loadedFollows) {
        if (loadedFollows != null) {
            this.userFollows = loadedFollows;
        }
    }

    public List<FollowEntry> getFollowsForUser(String username) {
        return userFollows.getOrDefault(username, Collections.emptyList());
    }

    public boolean isFollowing(String username, String documentId) {
        List<FollowEntry> follows = userFollows.get(username);
        if (follows == null) {
            return false;
        }
        return follows.stream()
                    .anyMatch(entry -> entry.getDocumentId().equals(documentId));
    }

    public void removeFollowsForDeletedDocument(String documentId) {
        userFollows.forEach((username, followsList) -> {
            followsList.removeIf(entry -> entry.getDocumentId().equals(documentId));
        });
        
        userFollows.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        
        System.out.println("Follows removed for deleted document: " + documentId);
    }

    public void removeFollowsByDocumentId(String documentId) {
        removeFollowsForDeletedDocument(documentId);
    }

    public void updateFollowVersion(String username, String documentId, int newVersion) {
        List<FollowEntry> userFollows = getFollowsForUser(username);
        if (userFollows != null) {
            for (FollowEntry entry : userFollows) {
                if (entry.getDocumentId().equals(documentId)) {
                    entry.setVersionAtFollow(newVersion);
                    break;
                }
            }
        }
    }
}