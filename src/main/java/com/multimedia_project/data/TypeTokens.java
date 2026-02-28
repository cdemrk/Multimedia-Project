package com.multimedia_project.data;

import com.google.gson.reflect.TypeToken;
import com.multimedia_project.model.Category;
import com.multimedia_project.model.Document;
import com.multimedia_project.model.User;
import com.multimedia_project.model.FollowEntry;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class TypeTokens {
    public static final Type USER_LIST_TYPE = new TypeToken<List<User>>() {}.getType();
    public static final Type CATEGORY_LIST_TYPE = new TypeToken<List<Category>>() {}.getType();
    public static final Type DOCUMENT_LIST_TYPE = new TypeToken<List<Document>>() {}.getType();
    
    public static final Type FOLLOWS_MAP_TYPE = new TypeToken<Map<String, List<FollowEntry>>>() {}.getType();
}