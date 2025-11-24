package com.multimedia_project.managers;

public class SystemState {
    private UserManager userManager;
    private CategoryManager categoryManager;
    private DocumentManager documentManager;
    private FollowManager followManager;
    // Πιθανώς και μια αναφορά στον DataManager για φόρτωση/αποθήκευση

    public SystemState() {
        // Η σειρά αρχικοποίησης έχει σημασία (π.χ., DocumentManager χρειάζεται FollowManager)
        this.followManager = new FollowManager();
        this.userManager = new UserManager();
        this.categoryManager = new CategoryManager();
        this.documentManager = new DocumentManager(this.followManager);
        
        // **ΣΗΜΑΝΤΙΚΟ:** Εδώ πρέπει να καλέσετε τη φόρτωση δεδομένων
        // this.dataManager.loadState(this); 
    }

    // Getters για πρόσβαση στους managers από την GUI
    public UserManager getUserManager() { return userManager; }
    public CategoryManager getCategoryManager() { return categoryManager; }
    public DocumentManager getDocumentManager() { return documentManager; }
    public FollowManager getFollowManager() { return followManager; }

    // Μέθοδος που καλείται πριν τον τερματισμό από την GUI
    public void saveSystemState() {
        // 
        // dataManager.saveState(this); // Υλοποίηση της μετατροπής των in-memory managers σε JSON αρχεία
    }
}





























package com.medialab.project.managers;

import com.medialab.project.data.DataManager;

public class SystemState {
    private UserManager userManager;
    private CategoryManager categoryManager;
    private DocumentManager documentManager;
    private FollowManager followManager;
    private DataManager dataManager; // Αναφορά στον DataManager

    public SystemState() {
        // 1. Αρχικοποίηση Managers
        this.followManager = new FollowManager();
        this.userManager = new UserManager(); // Δημιουργεί τον default admin
        this.categoryManager = new CategoryManager();
        this.documentManager = new DocumentManager(this.followManager);
        this.dataManager = new DataManager();
        
        // 2. Φόρτωση Κατάστασης (Απαιτούμενο)
        dataManager.loadState(this); 
    }

    // ... Getters ...

    // [ΝΕΑ ΜΕΘΟΔΟΣ] Καθαρή κλήση για αποθήκευση πριν τον τερματισμό
    public void saveSystemState() {
        dataManager.saveState(this); 
    }
}