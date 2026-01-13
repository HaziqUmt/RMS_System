package com.example.restrurantmanagementsystem.waiter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Category;
import com.example.restrurantmanagementsystem.models.MenuItem;
import com.example.restrurantmanagementsystem.models.OrderItem;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.example.restrurantmanagementsystem.waiter.adapters.MenuBrowseAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewOrderActivity extends AppCompatActivity implements MenuBrowseAdapter.OnMenuClickListener {

    private static final String TAG = "NewOrderActivity";

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private EditText etSearch;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ExtendedFloatingActionButton fabCart;

    private MenuBrowseAdapter adapter;
    private List<MenuItem> menuItemList;
    private List<Category> categoryList;
    private Map<String, OrderItem> cartItems; // Key: MenuItem ID
    
    private String restaurantId;
    private String tableId;
    private int tableNumber;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);

        Intent intent = getIntent();
        restaurantId = intent.getStringExtra(Constants.KEY_RESTAURANT_ID);
        tableId = intent.getStringExtra(Constants.KEY_TABLE_ID);
        tableNumber = intent.getIntExtra(Constants.KEY_TABLE_NUMBER, 0);

        firebaseHelper = FirebaseHelper.getInstance();
        cartItems = new HashMap<>();

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadCategories();
        loadMenuItems();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        etSearch = findViewById(R.id.etSearch);
        recyclerView = findViewById(R.id.recyclerViewMenuItems);
        progressBar = findViewById(R.id.progressBar);
        fabCart = findViewById(R.id.fabCart);

        menuItemList = new ArrayList<>();
        categoryList = new ArrayList<>();
    }

    private void setupToolbar() {
        toolbar.setTitle("Table " + tableNumber);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new MenuBrowseAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
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

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    adapter.filterByCategory("all");
                } else {
                    adapter.filterByCategory(categoryList.get(position - 1).getId());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        fabCart.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, OrderCartActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            intent.putExtra(Constants.KEY_TABLE_ID, tableId);
            intent.putExtra(Constants.KEY_TABLE_NUMBER, tableNumber);
            
            // Pass cart items
            ArrayList<OrderItem> itemsList = new ArrayList<>(cartItems.values());
            intent.putExtra("cart_items", itemsList);
            startActivity(intent);
        });
    }

    private void loadCategories() {
        firebaseHelper.getCategoriesCollection(restaurantId)
                .orderBy("displayOrder", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null) {
                        categoryList.clear();
                        categoryList.addAll(snapshots.toObjects(Category.class));
                        
                        tabLayout.removeAllTabs();
                        tabLayout.addTab(tabLayout.newTab().setText("All"));
                        for (Category category : categoryList) {
                            tabLayout.addTab(tabLayout.newTab().setText(category.getName()));
                        }
                    }
                });
    }

    private void loadMenuItems() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getMenuItemsCollection(restaurantId)
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(snapshots -> {
                    progressBar.setVisibility(View.GONE);
                    if (snapshots != null) {
                        menuItemList.clear();
                        menuItemList.addAll(snapshots.toObjects(MenuItem.class));
                        adapter.setMenuItems(menuItemList);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading menu", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onAddToCart(MenuItem item, int quantity) {
        showSpecialInstructionsDialog(item);
    }

    private void showSpecialInstructionsDialog(MenuItem item) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_special_instructions, null);
        EditText etInstructions = view.findViewById(R.id.etInstructions);

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Add to Cart", (dialog, which) -> {
                    String instructions = etInstructions.getText().toString().trim();
                    addToCart(item, 1, instructions);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addToCart(MenuItem item, int quantity, String instructions) {
        OrderItem orderItem = new OrderItem(
                null, // id will be set when saving order
                null, // orderId will be set when saving order
                item.getId(),
                item.getName(),
                quantity,
                item.getPrice(),
                item.getPrice() * quantity,
                instructions,
                item.getImageUrl()
        );
        
        cartItems.put(item.getId(), orderItem);
        updateCartFab();
        updateAdapterQuantities();
    }

    @Override
    public void onUpdateQuantity(MenuItem item, int quantity) {
        if (quantity <= 0) {
            cartItems.remove(item.getId());
        } else {
            OrderItem orderItem = cartItems.get(item.getId());
            if (orderItem != null) {
                orderItem.setQuantity(quantity);
                orderItem.setSubtotal(orderItem.getPrice() * quantity);
            }
        }
        updateCartFab();
        updateAdapterQuantities();
    }

    private void updateCartFab() {
        int totalItems = 0;
        for (OrderItem item : cartItems.values()) {
            totalItems += item.getQuantity();
        }
        fabCart.setText("View Cart (" + totalItems + ")");
    }

    private void updateAdapterQuantities() {
        Map<String, Integer> quantities = new HashMap<>();
        for (String itemId : cartItems.keySet()) {
            quantities.put(itemId, cartItems.get(itemId).getQuantity());
        }
        adapter.updateCartQuantities(quantities);
    }
}
