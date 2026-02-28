package com.multimedia_project.managers;
import com.multimedia_project.data.DataManager;

public class SystemState {
    private UserManager userManager;
    private CategoryManager categoryManager;
    private DocumentManager documentManager;
    private FollowManager followManager;
    private DataManager dataManager;

    public SystemState() {
        this.followManager = new FollowManager();
        this.userManager = new UserManager();
        
        this.documentManager = new DocumentManager(this.followManager);
        this.categoryManager = new CategoryManager();
        this.categoryManager.setDocumentManager(this.documentManager);
        this.dataManager = new DataManager();

        dataManager.loadState(this); 
    }

    public UserManager getUserManager() { return userManager; }
    public CategoryManager getCategoryManager() { return categoryManager; }
    public DocumentManager getDocumentManager() { return documentManager; }
    public FollowManager getFollowManager() { return followManager; }

    public void saveSystemState() {
        dataManager.saveState(this);
    }
}