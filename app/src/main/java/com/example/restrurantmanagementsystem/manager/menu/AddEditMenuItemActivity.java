package com.example.restrurantmanagementsystem.manager.menu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.bumptech.glide.Glide;
import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Category;
import com.example.restrurantmanagementsystem.models.MenuItem;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddEditMenuItemActivity extends AppCompatActivity {

    private static final String TAG = "AddEditMenuItemActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView ivMenuItemImage;
    private LinearLayout llUploadOptions;
    private Button btnCamera, btnGallery, btnSave;
    private EditText etItemName, etPrice, etDescription, etPrepTime, etAllergens;
    private Spinner spinnerCategory;
    private SwitchCompat switchAvailable;
    private ProgressBar progressBar;

    // Data
    private FirebaseHelper firebaseHelper;
    private List<Category> categoryList;
    private ArrayAdapter<String> categoryAdapter;
    private MenuItem currentMenuItem;
    private boolean isEditMode = false;
    private Uri selectedImageUri;
    private Bitmap selectedImageBitmap;
    private String uploadedImageUrl;
    private String restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_menu_item);

        // Get restaurantId from intent
        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);

        // Initialize Firebase
        firebaseHelper = FirebaseHelper.getInstance();

        // Check if edit mode
        checkEditMode();

        // Initialize UI
        initializeViews();
        setupToolbar();
        loadCategories();
        setupClickListeners();

        // If edit mode, populate fields
        if (isEditMode && currentMenuItem != null) {
            populateFields();
        }
    }

    private void checkEditMode() {
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra(Constants.KEY_IS_EDIT_MODE, false);
        if (isEditMode) {
            currentMenuItem = (MenuItem) intent.getSerializableExtra(Constants.KEY_MENU_ITEM);
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        ivMenuItemImage = findViewById(R.id.ivMenuItemImage);
        llUploadOptions = findViewById(R.id.llUploadOptions);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnSave = findViewById(R.id.btnSave);
        etItemName = findViewById(R.id.etItemName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        etPrepTime = findViewById(R.id.etPrepTime);
        etAllergens = findViewById(R.id.etAllergens);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        switchAvailable = findViewById(R.id.switchAvailable);
        progressBar = findViewById(R.id.progressBar);

        categoryList = new ArrayList<>();
    }

    private void setupToolbar() {
        if (isEditMode) {
            toolbar.setTitle("Edit Menu Item");
        } else {
            toolbar.setTitle("Add Menu Item");
        }
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadCategories() {
        firebaseHelper.getCategoriesCollection(restaurantId)
                .orderBy("displayOrder", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();
                    List<String> categoryNames = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        categoryList.addAll(queryDocumentSnapshots.toObjects(Category.class));
                        for (Category category : categoryList) {
                            categoryNames.add(category.getName());
                        }
                    }
                    // Setup spinner adapter
                    categoryAdapter = new ArrayAdapter<>(AddEditMenuItemActivity.this,
                            android.R.layout.simple_spinner_item, categoryNames);
                    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(categoryAdapter);

                    // If edit mode, select current category
                    if (isEditMode && currentMenuItem != null) {
                        selectCategory(currentMenuItem.getCategoryId());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading categories", e);
                    Toast.makeText(AddEditMenuItemActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                });
    }

    private void selectCategory(String categoryId) {
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId().equals(categoryId)) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void setupClickListeners() {
        // Camera button
        btnCamera.setOnClickListener(v -> openCamera());

        // Gallery button
        btnGallery.setOnClickListener(v -> openGallery());

        // Image click to change
        ivMenuItemImage.setOnClickListener(v -> llUploadOptions.setVisibility(View.VISIBLE));

        // Save button
        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, Constants.REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Constants.REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == Constants.REQUEST_IMAGE_CAPTURE) {
                // Handle camera result
                Bundle extras = data.getExtras();
                if (extras != null) {
                    selectedImageBitmap = (Bitmap) extras.get("data");
                    ivMenuItemImage.setImageBitmap(selectedImageBitmap);
                    llUploadOptions.setVisibility(View.GONE);
                }
            } else if (requestCode == Constants.REQUEST_IMAGE_PICK) {
                // Handle gallery result
                selectedImageUri = data.getData();
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), selectedImageUri);
                    ivMenuItemImage.setImageBitmap(selectedImageBitmap);
                    llUploadOptions.setVisibility(View.GONE);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void populateFields() {
        etItemName.setText(currentMenuItem.getName());
        etPrice.setText(String.valueOf(currentMenuItem.getPrice()));
        etDescription.setText(currentMenuItem.getDescription());
        etPrepTime.setText(String.valueOf(currentMenuItem.getPreparationTime()));
        etAllergens.setText(currentMenuItem.getAllergens());
        switchAvailable.setChecked(currentMenuItem.isAvailable());

        // Load image
        if (currentMenuItem.getImageUrl() != null && !currentMenuItem.getImageUrl().isEmpty()) {
            uploadedImageUrl = currentMenuItem.getImageUrl();
            Glide.with(this)
                    .load(currentMenuItem.getImageUrl())
                    .into(ivMenuItemImage);
            llUploadOptions.setVisibility(View.GONE);
        }
    }

    private void validateAndSave() {
        String name = etItemName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String prepTimeStr = etPrepTime.getText().toString().trim();
        String allergens = etAllergens.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etItemName.setError("Item name is required");
            etItemName.requestFocus();
            return;
        }

        if (name.length() < Constants.MIN_ITEM_NAME_LENGTH) {
            etItemName.setError("Name must be at least " + Constants.MIN_ITEM_NAME_LENGTH + " characters");
            etItemName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                etPrice.setError("Price must be greater than 0");
                etPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price format");
            etPrice.requestFocus();
            return;
        }

        if (spinnerCategory.getSelectedItemPosition() == -1) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        int prepTime = Constants.DEFAULT_PREP_TIME;
        if (!TextUtils.isEmpty(prepTimeStr)) {
            try {
                prepTime = Integer.parseInt(prepTimeStr);
            } catch (NumberFormatException e) {
                prepTime = Constants.DEFAULT_PREP_TIME;
            }
        }

        // Check if image needs to be uploaded
        if (selectedImageBitmap != null) {
            uploadImageAndSaveItem(name, description, price, prepTime, allergens);
        } else {
            saveMenuItem(name, description, price, prepTime, allergens, uploadedImageUrl);
        }
    }

    private void uploadImageAndSaveItem(String name, String description, double price,
                                        int prepTime, String allergens) {
        showLoading(true);

        // Convert bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, Constants.IMAGE_QUALITY, baos);
        byte[] imageData = baos.toByteArray();

        // Generate unique filename
        String filename = System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = firebaseHelper.getMenuImagesReference().child(filename);

        // Upload image
        UploadTask uploadTask = imageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Get download URL
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                uploadedImageUrl = uri.toString();
                saveMenuItem(name, description, price, prepTime, allergens, uploadedImageUrl);
            }).addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            showLoading(false);
            Toast.makeText(this, "Failed to upload image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void saveMenuItem(String name, String description, double price,
                              int prepTime, String allergens, String imageUrl) {
        showLoading(true);

        Category selectedCategory = categoryList.get(spinnerCategory.getSelectedItemPosition());
        String itemId;

        if (isEditMode && currentMenuItem != null) {
            // Update existing item
            itemId = currentMenuItem.getId();
        } else {
            // Generate new ID
            itemId = firebaseHelper.generateUniqueId(firebaseHelper.getMenuItemsCollection(restaurantId));
        }

        MenuItem menuItem = new MenuItem(
                itemId,
                name,
                description,
                price,
                selectedCategory.getId(),
                selectedCategory.getName(),
                imageUrl != null ? imageUrl : "",
                switchAvailable.isChecked(),
                prepTime,
                allergens,
                restaurantId
        );

        if (isEditMode) {
            menuItem.setCreatedAt(currentMenuItem.getCreatedAt());
            menuItem.setUpdatedAt(System.currentTimeMillis());
        }

        // Save to Firebase
        firebaseHelper.getMenuItemsCollection(restaurantId).document(itemId)
                .set(menuItem)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(this, "Menu item saved successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to save: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}