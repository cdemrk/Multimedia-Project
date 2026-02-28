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

    /**
     * Προσθέτει μια νέα κατηγορία.
     * Ελέγχει αν το όνομα υπάρχει ήδη (αγνοώντας κεφαλαία/μικρά) και πετάει Exception αν ναι.
     */
    public Category addCategory(String name) throws Exception {
        String cleanName = name.trim();
        
        // 1. Έλεγχος για διπλότυπο όνομα
        boolean exists = categories.stream().anyMatch(c -> c.getName().equalsIgnoreCase(cleanName));
        if (exists) {
            throw new Exception("The category '" + cleanName + "' already exists!");
        }

        // 2. Υπολογισμός του επόμενου διαθέσιμου ID
        int nextId = categories.stream()
                .mapToInt(Category::getId)
                .max()
                .orElse(0) + 1;

        // 3. Δημιουργία και προσθήκη
        Category newCategory = new Category(nextId, cleanName);
        categories.add(newCategory);
        
        return newCategory;
    }

    /**
     * Τροποποίηση ονόματος υπάρχουσας κατηγορίας.
     */
    public boolean updateCategoryName(int id, String newName) throws Exception {
        String cleanName = newName.trim();
        
        Optional<Category> categoryOpt = categories.stream()
                .filter(c -> c.getId() == id)
                .findFirst();
        
        if (categoryOpt.isPresent()) {
            // Έλεγχος αν το νέο όνομα υπάρχει ήδη σε ΑΛΛΗ κατηγορία
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

    /**
     * Διαγραφή κατηγορίας και εκτέλεση Cascade Delete στα έγγραφα.
     */
    public boolean deleteCategory(int id) {
        // 1. Διαγραφή όλων των εγγράφων που ανήκουν σε αυτή την κατηγορία
        if (documentManager != null) {
            documentManager.deleteDocumentsByCategory(id);
        }
        
        // 2. Διαγραφή της ίδιας της κατηγορίας από τη λίστα
        return categories.removeIf(c -> c.getId() == id);
    }

    /**
     * Καθορίζει τη λίστα κατηγοριών μετά τη φόρτωση από το JSON.
     */
    public void setCategories(List<Category> loadedCategories) {
        if (loadedCategories != null) {
            // Φροντίζουμε η λίστα να είναι πάντα επεξεργάσιμη (mutable)
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
}