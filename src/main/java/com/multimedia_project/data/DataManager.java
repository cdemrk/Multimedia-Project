package com.multimedia_project.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.multimedia_project.managers.SystemState;
import com.multimedia_project.model.Category;
import com.multimedia_project.model.Document;
import com.multimedia_project.model.User;
import com.multimedia_project.model.FollowEntry;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;

public class DataManager {
    private static final String DATA_FOLDER = "medialab";
        
    private static final String USERS_FILE = DATA_FOLDER + File.separator + "users.json";
    private static final String DOCUMENTS_FILE = DATA_FOLDER + File.separator + "documents.json";
    private static final String CATEGORIES_FILE = DATA_FOLDER + File.separator + "categories.json";
    private static final String FOLLOWS_FILE = DATA_FOLDER + File.separator + "follows.json";

    private final Gson gson = new GsonBuilder()
                                .setPrettyPrinting()
                                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                                .create();

    public DataManager() {
        try {
            Files.createDirectories(Paths.get(DATA_FOLDER));
        } catch (IOException e) {
            System.err.println("Error creating data directory: " + e.getMessage());
        }
    }

    public void loadState(SystemState state) {
        // Φόρτωση Χρηστών
        List<User> loadedUsers = loadFile(USERS_FILE, TypeTokens.USER_LIST_TYPE);
        if (loadedUsers.isEmpty()) {
            System.out.println("Users file empty or not found. Using default Admin logic.");
        } else {
            state.getUserManager().setUsers(loadedUsers);
        }

        // Φόρτωση Κατηγοριών
        List<Category> loadedCategories = loadFile(CATEGORIES_FILE, TypeTokens.CATEGORY_LIST_TYPE);
        state.getCategoryManager().setCategories(loadedCategories);

        // Φόρτωση Εγγράφων
        List<Document> loadedDocuments = loadFile(DOCUMENTS_FILE, TypeTokens.DOCUMENT_LIST_TYPE);
        state.getDocumentManager().setDocuments(loadedDocuments);

        // Φόρτωση Follows (Εδώ γινόταν το σφάλμα)
        Map<String, List<FollowEntry>> loadedFollows = loadFile(FOLLOWS_FILE, TypeTokens.FOLLOWS_MAP_TYPE);
        state.getFollowManager().setUserFollows(loadedFollows); 
    }

    public void saveState(SystemState state) {
        saveFile(USERS_FILE, state.getUserManager().getAllUsers());
        saveFile(CATEGORIES_FILE, state.getCategoryManager().getAllCategories());
        saveFile(DOCUMENTS_FILE, state.getDocumentManager().getAllDocuments());
        saveFile(FOLLOWS_FILE, state.getFollowManager().getAllFollowsMap());
        System.out.println("System state saved successfully to 'medialab' folder.");
    }

    @SuppressWarnings("unchecked")
    private <T> T loadFile(String filePath, Type typeOfT) {
        try (Reader reader = new FileReader(filePath)) {
            T data = gson.fromJson(reader, typeOfT);
            if (data != null) return data;
            return createEmptyInstance(typeOfT);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath + ". Creating empty structure.");
            return createEmptyInstance(typeOfT);
        } catch (IOException e) {
            System.err.println("Error reading file " + filePath + ": " + e.getMessage());
            return createEmptyInstance(typeOfT);
        }
    }

    /**
     * Επιστρέφει μια άδεια λίστα ή ένα άδειο Map ανάλογα με τον τύπο,
     * ώστε να αποφεύγονται τα ClassCastExceptions.
     */
    @SuppressWarnings("unchecked")
    private <T> T createEmptyInstance(Type typeOfT) {
        String typeName = typeOfT.getTypeName();
        if (typeName.contains("Map")) {
            return (T) new HashMap<String, List<FollowEntry>>();
        }
        return (T) Collections.emptyList();
    }

    private void saveFile(String filePath, Object data) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Error writing file " + filePath + ": " + e.getMessage());
        }
    }
}