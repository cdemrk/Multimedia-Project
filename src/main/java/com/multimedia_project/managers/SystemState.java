package com.multimedia_project.managers;
import com.multimedia_project.data.DataManager;

public class SystemState {
    private UserManager userManager;
    private CategoryManager categoryManager;
    private DocumentManager documentManager;
    private FollowManager followManager;
    private DataManager dataManager;

    public SystemState() {
        // Αρχικοποίηση Managers
        this.followManager = new FollowManager();
        this.userManager = new UserManager(); // default admin
        this.categoryManager = new CategoryManager();
        this.documentManager = new DocumentManager(this.followManager);
        this.dataManager = new DataManager();
        
        // Φόρτωση Κατάστασης
        dataManager.loadState(this); 
    }

    // Getters για πρόσβαση στους managers από την GUI
    public UserManager getUserManager() { return userManager; }
    public CategoryManager getCategoryManager() { return categoryManager; }
    public DocumentManager getDocumentManager() { return documentManager; }
    public FollowManager getFollowManager() { return followManager; }

    // Μέθοδος που καλείται πριν τον τερματισμό από την GUI για μετατροπή των in-memory managers σε JSON αρχεία
    public void saveSystemState() {
        dataManager.saveState(this);
    }
}