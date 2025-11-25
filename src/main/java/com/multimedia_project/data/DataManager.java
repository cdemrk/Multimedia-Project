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
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;


public class DataManager {
    private static final String DATA_FOLDER = "medialab"; // όνομα του φακέλου
       
    // ονόματα των αρχείων
    private static final String USERS_FILE = DATA_FOLDER + File.separator + "users.json";
    private static final String DOCUMENTS_FILE = DATA_FOLDER + File.separator + "documents.json";
    private static final String CATEGORIES_FILE = DATA_FOLDER + File.separator + "categories.json";
    private static final String FOLLOWS_FILE = DATA_FOLDER + File.separator + "follows.json";

    private final Gson gson = new GsonBuilder()
                                .setPrettyPrinting()
                                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                                .create();

    public DataManager() {
        // Δημιουργία φακέλου 'medialab' αν δεν υπάρχει
        try {
            Files.createDirectories(Paths.get(DATA_FOLDER));
        } catch (IOException e) {
            System.err.println("Error creating data directory: " + e.getMessage());
        }
    }

    /**
     * Φορτώνει την κατάσταση του συστήματος από τα JSON αρχεία.
     * Η μέθοδος αυτή θα καλείται κατά την αρχικοποίηση της εφαρμογής.
     */
    public void loadState(SystemState state) {
        // 1. Φόρτωση Χρηστών
        List<User> loadedUsers = loadFile(USERS_FILE, TypeTokens.USER_LIST_TYPE);
        if (loadedUsers.isEmpty() && state.getUserManager().isInitialRun()) {
            // Αν το αρχείο είναι κενό και είναι η πρώτη εκτέλεση, χρησιμοποιούμε τον προεπιλεγμένο διαχειριστή.
            // Ο προεπιλεγμένος Admin έχει ήδη προστεθεί στον UserManager κατά την αρχικοποίηση.
            System.out.println("Users file empty or not found. Using default Admin.");
        } else {
            // Ανάκτηση των φορτωμένων χρηστών
            state.getUserManager().setUsers(loadedUsers);
        }

        // 2. Φόρτωση Κατηγοριών
        List<Category> loadedCategories = loadFile(CATEGORIES_FILE, TypeTokens.CATEGORY_LIST_TYPE);
        state.getCategoryManager().setCategories(loadedCategories);

        // 3. Φόρτωση Εγγράφων
        List<Document> loadedDocuments = loadFile(DOCUMENTS_FILE, TypeTokens.DOCUMENT_LIST_TYPE);
        state.getDocumentManager().setDocuments(loadedDocuments);

        // 4. Φόρτωση Παρακολούθησης
        Map<String, List<FollowEntry>> loadedFollows = loadFile(FOLLOWS_FILE, TypeTokens.FOLLOWS_MAP_TYPE);
        state.getFollowManager().setUserFollows(loadedFollows); 
    }

    /**
     * Αποθηκεύει την κατάσταση του συστήματος στα JSON αρχεία.
     * Αυτή η μέθοδος καλείται ΑΠΟΚΛΕΙΣΤΙΚΑ πριν τον τερματισμό.
     */
    public void saveState(SystemState state) {
        // Αποθήκευση της τρέχουσας κατάστασης (in-memory) πριν τον τερματισμό
        saveFile(USERS_FILE, state.getUserManager().getAllUsers());
        saveFile(CATEGORIES_FILE, state.getCategoryManager().getAllCategories());
        saveFile(DOCUMENTS_FILE, state.getDocumentManager().getAllDocuments());
        saveFile(FOLLOWS_FILE, state.getFollowManager().getAllFollowsMap());
        System.out.println("System state saved successfully to 'medialab' folder.");
    }

    // -- Private Helper Methods --

    private <T> T loadFile(String filePath, Type typeOfT) {
        try (Reader reader = new FileReader(filePath)) {
            T data = gson.fromJson(reader, typeOfT);
            return (data != null) ? data : (T) Collections.emptyList();
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
            return (T) Collections.emptyList(); // Επιστροφή κενής λίστας αν δεν υπάρχει αρχείο
        } catch (IOException e) {
            System.err.println("Error reading file " + filePath + ": " + e.getMessage());
            return (T) Collections.emptyList();
        }
    }

    private void saveFile(String filePath, Object data) {
        try (Writer writer = new FileWriter(filePath)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Error writing file " + filePath + ": " + e.getMessage());
        }
    }
}