package com.multimedia_project.data;

import com.google.gson.reflect.TypeToken;
import com.medialab.project.model.Category;
import com.medialab.project.model.Document;
import com.medialab.project.model.User;
import com.medialab.project.managers.FollowEntry; // Ή όπου αλλού έχετε βάλει το FollowEntry

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class TypeTokens {
    public static final Type USER_LIST_TYPE = new TypeToken<List<User>>() {}.getType();
    public static final Type CATEGORY_LIST_TYPE = new TypeToken<List<Category>>() {}.getType();
    public static final Type DOCUMENT_LIST_TYPE = new TypeToken<List<Document>>() {}.getType();
    
    // Επειδή το FollowManager είναι Map<String, List<FollowEntry>> in-memory, 
    // μπορείτε να το αποθηκεύσετε ως λίστα ή ως Map στο JSON. Ας υποθέσουμε Map για ευκολία ανάκτησης.
    public static final Type FOLLOWS_MAP_TYPE = new TypeToken<Map<String, List<FollowEntry>>>() {}.getType();
}