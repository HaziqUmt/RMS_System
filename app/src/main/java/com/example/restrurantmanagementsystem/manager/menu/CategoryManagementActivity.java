package com.example.restrurantmanagementsystem.manager.menu;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.manager.menu.adapters.CategoryAdapter;
import com.example.restrurantmanagementsystem.models.Category;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity
        implements CategoryAdapter.OnCategoryClickListener {

    private static final String TAG = "CategoryManagementAct";

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddCategory;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;

    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        firebaseHelper = FirebaseHelper.getInstance();

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadCategories();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewCategories);
        fabAddCategory = findViewById(R.id.fabAddCategory);
        progressBar = findViewById(R.id.progressBar);
        llEmptyState = findViewById(R.id.llEmptyState);

        categoryList = new ArrayList<>();
    }

    private void setupToolbar() {
        toolbar.setTitle("Manage Categories");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new CategoryAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "loadCategories: Fetching categories from Firestore...");

        firebaseHelper.getCategoriesCollection()
                .orderBy("displayOrder", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error loading categories", e);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CategoryManagementActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "onEvent: Categories snapshot received.");
                    if (snapshots != null) {
                        categoryList.clear();
                        categoryList.addAll(snapshots.toObjects(Category.class));
                        Log.d(TAG, "Total categories fetched: " + categoryList.size());
                        adapter.setCategories(categoryList);
                    }
                    progressBar.setVisibility(View.GONE);

                    if (categoryList.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        llEmptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void showAddCategoryDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);

        new AlertDialog.Builder(this)
                .setTitle("Add Category")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String categoryName = etCategoryName.getText().toString().trim();
                    if (!TextUtils.isEmpty(categoryName)) {
                        addCategory(categoryName);
                    } else {
                        Toast.makeText(this, "Category name is required",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addCategory(String categoryName) {
        Log.d(TAG, "addCategory: Adding category: " + categoryName);
        String categoryId = firebaseHelper.generateUniqueId(firebaseHelper.getCategoriesCollection());
        int displayOrder = categoryList.size() + 1;

        Category category = new Category(categoryId, categoryName, displayOrder,
                Constants.DEFAULT_RESTAURANT_ID);

        firebaseHelper.getCategoriesCollection().document(categoryId)
                .set(category)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Category added successfully to Firestore.");
                    Toast.makeText(this, "Category added successfully",
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add category", e);
                    Toast.makeText(this, "Failed to add category: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEditClick(Category category) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        etCategoryName.setText(category.getName());

        new AlertDialog.Builder(this)
                .setTitle("Edit Category")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etCategoryName.getText().toString().trim();
                    if (!TextUtils.isEmpty(newName)) {
                        category.setName(newName);
                        updateCategory(category);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeleteClick(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete \"" + category.getName() + "\"?\n\n" +
                        "Note: Menu items in this category won't be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> deleteCategory(category))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateCategory(Category category) {
        firebaseHelper.getCategoriesCollection().document(category.getId())
                .set(category)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Category updated successfully",
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update category",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteCategory(Category category) {
        firebaseHelper.getCategoriesCollection().document(category.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Category deleted successfully",
                            Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete category",
                            Toast.LENGTH_SHORT).show();
                });
    }
}