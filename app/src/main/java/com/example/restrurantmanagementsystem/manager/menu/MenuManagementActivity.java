package com.example.restrurantmanagementsystem.manager.menu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.manager.menu.adapters.MenuItemAdapter;
import com.example.restrurantmanagementsystem.models.Category;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MenuManagementActivity extends AppCompatActivity implements MenuItemAdapter.OnMenuItemClickListener {

    private static final String TAG = "MenuManagementActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private EditText etSearch;
    private Button btnManageCategories;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddItem;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;

    // Data
    private MenuItemAdapter adapter;
    private List<com.example.restrurantmanagementsystem.models.MenuItem> menuItemList;
    private List<Category> categoryList;
    private String currentCategoryFilter = "all";
    private FirebaseHelper firebaseHelper;
    private String restaurantId;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_management);

        // Get info from intent
        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        // Initialize Firebase
        firebaseHelper = FirebaseHelper.getInstance();

        // Initialize UI
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        // Load data
        loadCategories();
        loadMenuItems();
        
        // Hide admin-only UI if not admin
        if (!isAdmin) {
            fabAddItem.setVisibility(View.GONE);
            btnManageCategories.setVisibility(View.GONE);
            toolbar.setTitle("Restaurant Menu");
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        etSearch = findViewById(R.id.etSearch);
        btnManageCategories = findViewById(R.id.btnManageCategories);
        recyclerView = findViewById(R.id.recyclerViewMenuItems);
        fabAddItem = findViewById(R.id.fabAddItem);
        progressBar = findViewById(R.id.progressBar);
        llEmptyState = findViewById(R.id.llEmptyState);

        menuItemList = new ArrayList<>();
        categoryList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new MenuItemAdapter(this, this);
        adapter.setAdmin(isAdmin);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // FAB - Add new menu item
        fabAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(MenuManagementActivity.this, AddEditMenuItemActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            startActivityForResult(intent, Constants.REQUEST_ADD_MENU_ITEM);
        });

        // Manage Categories button
        btnManageCategories.setOnClickListener(v -> {
            Intent intent = new Intent(MenuManagementActivity.this, CategoryManagementActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            startActivityForResult(intent, Constants.REQUEST_MANAGE_CATEGORIES);
        });

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Tab selection - filter by category
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    currentCategoryFilter = "all";
                    adapter.filterByCategory("all");
                } else {
                    Category category = categoryList.get(position - 1);
                    currentCategoryFilter = category.getId();
                    adapter.filterByCategory(category.getId());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories: Fetching categories from Firestore...");
        firebaseHelper.getCategoriesCollection(restaurantId)
                .orderBy("displayOrder", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error loading categories", e);
                        return;
                    }

                    Log.d(TAG, "onEvent: Categories snapshot received.");
                    if (snapshots != null) {
                        categoryList.clear();
                        categoryList.addAll(snapshots.toObjects(Category.class));
                        Log.d(TAG, "Total categories fetched: " + categoryList.size());

                        // Add "All" tab
                        tabLayout.removeAllTabs();
                        tabLayout.addTab(tabLayout.newTab().setText("All"));

                        for (Category category : categoryList) {
                            tabLayout.addTab(tabLayout.newTab().setText(category.getName()));
                        }

                        if (categoryList.isEmpty() && isAdmin) {
                            Toast.makeText(MenuManagementActivity.this, "No categories found. Add categories first.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void loadMenuItems() {
        showLoading(true);
        Log.d(TAG, "loadMenuItems: Fetching menu items from Firestore for restaurant: " + restaurantId);
        firebaseHelper.getMenuItemsCollection(restaurantId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error loading menu items", e);
                        showLoading(false);
                        Toast.makeText(MenuManagementActivity.this, "Failed to load menu items", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "onEvent: Menu items snapshot received.");
                    if (snapshots != null) {
                        menuItemList.clear();
                        menuItemList.addAll(snapshots.toObjects(com.example.restrurantmanagementsystem.models.MenuItem.class));
                        Log.d(TAG, "Total menu items fetched: " + menuItemList.size());

                        adapter.setMenuItems(menuItemList);
                    }
                    showLoading(false);

                    if (menuItemList.isEmpty()) {
                        llEmptyState.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        llEmptyState.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onEditClick(com.example.restrurantmanagementsystem.models.MenuItem menuItem) {
        if (!isAdmin) return;
        Intent intent = new Intent(this, AddEditMenuItemActivity.class);
        intent.putExtra(Constants.KEY_MENU_ITEM, menuItem);
        intent.putExtra(Constants.KEY_IS_EDIT_MODE, true);
        intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
        startActivityForResult(intent, Constants.REQUEST_EDIT_MENU_ITEM);
    }

    @Override
    public void onDeleteClick(com.example.restrurantmanagementsystem.models.MenuItem menuItem) {
        if (!isAdmin) return;
        new AlertDialog.Builder(this)
                .setTitle("Delete Menu Item")
                .setMessage("Are you sure you want to delete \"" + menuItem.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMenuItem(menuItem))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onAvailabilityChanged(com.example.restrurantmanagementsystem.models.MenuItem menuItem, boolean isAvailable) {
        if (!isAdmin) return;
        menuItem.setAvailable(isAvailable);
        menuItem.setUpdatedAt(System.currentTimeMillis());

        firebaseHelper.getMenuItemsCollection(restaurantId).document(menuItem.getId())
                .set(menuItem)
                .addOnSuccessListener(aVoid -> {
                    String status = isAvailable ? "available" : "unavailable";
                    Toast.makeText(this, menuItem.getName() + " marked as " + status,
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update availability",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteMenuItem(com.example.restrurantmanagementsystem.models.MenuItem menuItem) {
        firebaseHelper.getMenuItemsCollection(restaurantId).document(menuItem.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Menu item deleted successfully",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete menu item: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            loadMenuItems();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // Refresh data after add/edit/category management
            loadMenuItems();
            loadCategories();
        }
    }
}