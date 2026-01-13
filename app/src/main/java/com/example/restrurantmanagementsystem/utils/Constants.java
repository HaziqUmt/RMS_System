package com.example.restrurantmanagementsystem.utils;

public class Constants {

    // Collections
    public static final String USERS_COLLECTION = "users";
    public static final String RESTAURANTS_PATH = "restaurants";
    public static final String CATEGORIES_PATH = "categories";
    public static final String MENU_ITEMS_PATH = "menu";
    public static final String TABLES_PATH = "tables";
    public static final String ORDERS_PATH = "orders";

    // Storage
    public static final String MENU_IMAGES_PATH = "menu_images";

    // Other
    public static final String DEFAULT_RESTAURANT_ID = "main_restaurant"; // Example ID
    public static final int MIN_ITEM_NAME_LENGTH = 3;
    public static final int DEFAULT_PREP_TIME = 15;
    public static final int IMAGE_QUALITY = 85;

    // Intent and Shared Preferences Keys
    public static final String KEY_MENU_ITEM = "menu_item";
    public static final String KEY_IS_EDIT_MODE = "is_edit_mode";
    public static final String KEY_RESTAURANT_ID = "restaurant_id";
    public static final String KEY_TABLE_ID = "table_id";
    public static final String KEY_TABLE_NUMBER = "table_number";
    public static final String KEY_ORDER = "order";
    public static final String PREFS_NAME = "RestaurantPrefs";

    // Roles
    public static final String ROLE_MANAGER = "Manager";
    public static final String ROLE_WAITER = "Waiter";
    public static final String ROLE_CHEF = "Chef";

    // Table Status
    public static final String TABLE_STATUS_AVAILABLE = "available";
    public static final String TABLE_STATUS_OCCUPIED = "occupied";
    public static final String TABLE_STATUS_RESERVED = "reserved";

    // Order Status
    public static final String ORDER_STATUS_PENDING = "pending";
    public static final String ORDER_STATUS_IN_PROGRESS = "in_progress";
    public static final String ORDER_STATUS_READY = "ready";
    public static final String ORDER_STATUS_SERVED = "served";
    public static final String ORDER_STATUS_CANCELLED = "cancelled";

    // Request Codes
    public static final int REQUEST_ADD_MENU_ITEM = 101;
    public static final int REQUEST_EDIT_MENU_ITEM = 102;
    public static final int REQUEST_MANAGE_CATEGORIES = 103;
    public static final int REQUEST_IMAGE_CAPTURE = 104;
    public static final int REQUEST_IMAGE_PICK = 105;
    public static final int REQUEST_ADD_STAFF = 106;
}
