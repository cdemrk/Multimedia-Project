package com.multimedia_project.managers;

import com.multimedia_project.model.Category;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class CategoryManager {
    private List<Category> categories;
    private DocumentManager documentManager;
    private int nextCategoryId = 1;


    public CategoryManager() {
        this.categories = new ArrayList<>();
        // In-memory initialization logic for testing
        // You'll load this from JSON in the final app
    }

    // Προσθήκη νέας κατηγορίας
    public Category addCategory(String name) {
        // Έλεγχος για ύπαρξη ονόματος
        if (categories.stream().anyMatch(c -> c.getName().equalsIgnoreCase(name))) {
            return null; // Ή πέταγμα εξαίρεσης
        }
        Category newCategory = new Category(nextCategoryId++, name);
        categories.add(newCategory);
        return newCategory;
    }

    // Τροποποίηση ονόματος κατηγορίας
    public boolean updateCategoryName(int id, String newName) {
        Optional<Category> categoryOpt = categories.stream()
                .filter(c -> c.getId() == id)
                .findFirst();
        
        if (categoryOpt.isPresent()) {
            // Έλεγχος για διπλό όνομα (αν το νέο όνομα υπάρχει ήδη)
            if (categories.stream().anyMatch(c -> c.getName().equalsIgnoreCase(newName) && c.getId() != id)) {
                return false; // Το όνομα υπάρχει
            }
            categoryOpt.get().setName(newName);
            return true;
        }
        return false;
    }

    public boolean deleteCategory(int id) {
        // καλο΄ύμε τον DocumentManager για να διαγράψει όλα τα έγγραφα που ανήκουν σε αυτή την κατηγορία, και στη συνέχεια να γίνει η ενημέρωση στον FollowManager.
        documentManager.deleteDocumentsByCategory(id);
        
        // 2. Διαγραφή της κατηγορίας
        return categories.removeIf(c -> c.getId() == id);
    }


    // Επιστρέφει όλες τις κατηγορίες για αποθήκευση στο JSON
    public List<Category> getAllCategories() {
        return categories;
    }

    // Καθορίζει τη λίστα κατηγοριών μετά τη φόρτωση του JSON
    public void setCategories(List<Category> loadedCategories) {
        if (loadedCategories != null) {
            this.categories = loadedCategories;
            // Ενημέρωση του nextCategoryId αν χρειάζεται
            if (!categories.isEmpty()) {
                // Εύρεση του μέγιστου ID για να συνεχίσουμε σωστά την αρίθμηση
                this.nextCategoryId = categories.stream()
                    .mapToInt(Category::getId)
                    .max().orElse(0) + 1;
            }
        }
    }


    public String getCategoryNameById(int id) {
        return categories.stream()
                .filter(c -> c.getId() == id)
                .map(Category::getName)
                .findFirst()
                .orElse("N/A");
    }
}