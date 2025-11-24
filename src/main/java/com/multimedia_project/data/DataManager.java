package com.multimedia_project.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.medialab.documents.managers.SystemState;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// Helper classes for Gson deserialization (χρειάζονται για να διαβάσει σωστά τις λίστες)
// ... (πρέπει να ορίσετε TypeTokens για κάθε λίστα π.χ. List<User>) ...

public class DataManager {
    // Το όνομα του φακέλου (απαιτείται από την εκφώνηση)
    private static final String DATA_FOLDER = "medialab";
    
    // Τα ονόματα των αρχείων
    private static final String USERS_FILE = DATA_FOLDER + File.separator + "users.json";
    private static final String DOCUMENTS_FILE = DATA_FOLDER + File.separator + "documents.json";
    private static final String CATEGORIES_FILE = DATA_FOLDER + File.separator + "categories.json";
    private static final String FOLLOWS_FILE = DATA_FOLDER + File.separator + "follows.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public DataManager() {
        // Βεβαιωθείτε ότι ο φάκελος υπάρχει
        try {
            Files.createDirectories(Paths.get(DATA_FOLDER));
        } catch (IOException e) {
            System.err.println("Error creating data directory: " + e.getMessage());
        }
    }

    /**
     * Φορτώνει την κατάσταση του συστήματος από τα JSON αρχεία.
     * Καλή πρακτική: η μέθοδος αυτή θα καλείται κατά την αρχικοποίηση της εφαρμογής.
     */
    public void loadState(SystemState state) {
        // 1. Φόρτωση Χρηστών
        try (Reader reader = new FileReader(USERS_FILE)) {
            // Χρειάζεται Type Token για σωστή ανάγνωση λίστας
            // state.getUserManager().setUsers(gson.fromJson(reader, /* Type Token List<User> */));
            // Προσοχή: Εδώ χρειάζεται ειδική μεταχείριση για τον προεπιλεγμένο Admin.
            System.out.println("Users loaded."); 
        } catch (FileNotFoundException e) {
            System.out.println("Users file not found. Using default admin.");
        } catch (IOException e) {
            System.err.println("Error reading users: " + e.getMessage());
        }
        
        // ... Επανάληψη της λογικής για CATEGORIES, DOCUMENTS και FOLLOWS ...
        
    }

    /**
     * Αποθηκεύει την κατάσταση του συστήματος στα JSON αρχεία.
     * **ΣΗΜΑΝΤΙΚΟ:** Αυτή η μέθοδος καλείται ΑΠΟΚΛΕΙΣΤΙΚΑ πριν τον τερματισμό.
     */
    public void saveState(SystemState state) {
        // 1. Αποθήκευση Χρηστών
        try (Writer writer = new FileWriter(USERS_FILE)) {
            // Χρησιμοποιήστε τα δεδομένα από τον UserManager της SystemState
            // gson.toJson(state.getUserManager().getAllUsers(), writer);
            System.out.println("Users saved successfully.");
        } catch (IOException e) {
            System.err.println("Error writing users: " + e.getMessage());
        }

        // ... Επανάληψη της λογικής για CATEGORIES, DOCUMENTS και FOLLOWS ...
    }
}



















package com.medialab.project.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.medialab.project.managers.SystemState;
import com.medialab.project.model.Category;
import com.medialab.project.model.Document;
import com.medialab.project.model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DataManager {
    private static final String DATA_FOLDER = "medialab";
    private static final String USERS_FILE = DATA_FOLDER + File.separator + "users.json";
    private static final String DOCUMENTS_FILE = DATA_FOLDER + File.separator + "documents.json";
    private static final String CATEGORIES_FILE = DATA_FOLDER + File.separator + "categories.json";
    private static final String FOLLOWS_FILE = DATA_FOLDER + File.separator + "follows.json";

    // Χρησιμοποιούμε GsonBuilder για μορφοποιημένο JSON (pretty printing)
    private final Gson gson = new GsonBuilder()
                                .setPrettyPrinting()
                                .create();

    public DataManager() {
        // Δημιουργία φακέλου 'medialab' αν δεν υπάρχει
        try {
            Files.createDirectories(Paths.get(DATA_FOLDER));
        } catch (IOException e) {
            System.err.println("Fatal: Could not create data directory: " + DATA_FOLDER);
        }
    }

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