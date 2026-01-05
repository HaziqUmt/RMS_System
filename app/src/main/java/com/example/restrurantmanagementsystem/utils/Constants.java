package com.example.restrurantmanagementsystem.utils;

public class Constants {

    // Collections jkjhjhj
    public static final String USERS_COLLECTION = "users";
    public static final String RESTAURANTS_PATH = "restaurants";
    public static final String CATEGORIES_PATH = "categories";
    public static final String MENU_ITEMS_PATH = "menu";

    // Storage
    public static final String MENU_IMAGES_PATH = "menu_images";

    // Other
    public static final String DEFAULT_RESTAURANT_ID = "main_restaurant"; // Example ID
    public static final int MIN_ITEM_NAME_LENGTH = 3;
    public static final int DEFAULT_PREP_TIME = 15;
    public static final int IMAGE_QUALITY = 85;

    // Intent Keys
    public static final String KEY_MENU_ITEM = "menu_item";
    public static final String KEY_IS_EDIT_MODE = "is_edit_mode";

    // Request Codes
    public static final int REQUEST_ADD_MENU_ITEM = 101;
    public static final int REQUEST_EDIT_MENU_ITEM = 102;
    public static final int REQUEST_MANAGE_CATEGORIES = 103;
    public static final int REQUEST_IMAGE_CAPTURE = 104;
    public static final int REQUEST_IMAGE_PICK = 105;
}
