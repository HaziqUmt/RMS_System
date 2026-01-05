package com.example.restrurantmanagementsystem.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseHelper {

    private static FirebaseHelper instance;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseStorage mStorage;

    private FirebaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public CollectionReference getRestaurantCollection() {
        return mFirestore.collection(Constants.RESTAURANTS_PATH);
    }

    public CollectionReference getCategoriesCollection() {
        return getRestaurantCollection().document(Constants.DEFAULT_RESTAURANT_ID).collection(Constants.CATEGORIES_PATH);
    }

    public CollectionReference getMenuItemsCollection() {
        return getRestaurantCollection().document(Constants.DEFAULT_RESTAURANT_ID).collection(Constants.MENU_ITEMS_PATH);
    }

    public StorageReference getMenuImagesReference() {
        return mStorage.getReference().child(Constants.MENU_IMAGES_PATH);
    }

    public String generateUniqueId(CollectionReference collection) {
        return collection.document().getId();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
}