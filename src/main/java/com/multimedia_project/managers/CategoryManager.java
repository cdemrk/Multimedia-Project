package com.multimedia_project.managers;

import com.multimedia_project.model.Category;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryManager {
    private List<Category> categories;
    private DocumentManager documentManager;

    public CategoryManager() {
        this.categories = new ArrayList<>();
    }

    public void setDocumentManager(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    public Category addCategory(String name) throws Exception {
        String cleanName = name.trim();
        
        boolean exists = categories.stream().anyMatch(c -> c.getName().equalsIgnoreCase(cleanName));
        if (exists) {
            throw new Exception("The category '" + cleanName + "' already exists!");
        }

        int nextId = categories.stream()
                .mapToInt(Category::getId)
                .max()
                .orElse(0) + 1;

        Category newCategory = new Category(nextId, cleanName);
        categories.add(newCategory);
        
        return newCategory;
    }


    public boolean updateCategoryName(int id, String newName) throws Exception {
        String cleanName = newName.trim();
        
        Optional<Category> categoryOpt = categories.stream()
                .filter(c -> c.getId() == id)
                .findFirst();
        
        if (categoryOpt.isPresent()) {
            boolean nameExists = categories.stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(cleanName) && c.getId() != id);
            
            if (nameExists) {
                throw new Exception("The category '" + cleanName + "' already exists!");
            }

            categoryOpt.get().setName(cleanName);
            return true;
        }
        return false;
    }

    public boolean deleteCategory(int id) {
        if (documentManager != null) {
            documentManager.deleteDocumentsByCategory(id);
        }
        
        return categories.removeIf(c -> c.getId() == id);
    }


    public void setCategories(List<Category> loadedCategories) {
        if (loadedCategories != null) {
            this.categories = new ArrayList<>(loadedCategories);
        } else {
            this.categories = new ArrayList<>();
        }
    }

    public List<Category> getAllCategories() {
        return categories;
    }

    public String getCategoryNameById(int id) {
        return categories.stream()
                .filter(c -> c.getId() == id)
                .map(Category::getName)
                .findFirst()
                .orElse("N/A");
    }

    public Category getCategoryById(int id) {
        return categories.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }
}