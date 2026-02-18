package com.multimedia_project.managers;
import com.multimedia_project.data.DataManager;

public class SystemState {
    private UserManager userManager;
    private CategoryManager categoryManager;
    private DocumentManager documentManager;
    private FollowManager followManager;
    private DataManager dataManager;

    public SystemState() {
        // 1. Αρχικοποίηση Managers (Προσοχή στη σειρά)
        this.followManager = new FollowManager();
        this.userManager = new UserManager();
        
        // Δημιουργούμε πρώτα τον DocumentManager γιατί τον χρειάζεται ο CategoryManager
        this.documentManager = new DocumentManager(this.followManager);
        this.categoryManager = new CategoryManager();
        
        // Η ΚΡΙΣΙΜΗ ΠΡΟΣΘΗΚΗ: Σύνδεση των δύο managers
        this.categoryManager.setDocumentManager(this.documentManager);

        this.dataManager = new DataManager();
        
        // 2. Φόρτωση Κατάστασης (πρέπει να γίνει αφού έχουν δημιουργηθεί όλοι οι managers)
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