package com.example.restrurantmanagementsystem.chef;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.auth.LoginActivity;
import com.example.restrurantmanagementsystem.chef.adapters.ChefOrdersAdapter;
import com.example.restrurantmanagementsystem.models.Order;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChefDashboard extends AppCompatActivity implements ChefOrdersAdapter.OnOrderStatusChangeListener {

    private static final String TAG = "ChefDashboard";

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout llEmptyKitchen, llLogout;

    private ChefOrdersAdapter adapter;
    private List<Order> allOrdersList;
    private FirebaseFirestore db;
    private String restaurantId;
    private ListenerRegistration ordersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef_dashboard);

        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();
        setupTabs();
        setupClickListeners();
        loadKitchenOrders();
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayoutStatus);
        recyclerView = findViewById(R.id.recyclerViewOrders);
        progressBar = findViewById(R.id.progressBar);
        llEmptyKitchen = findViewById(R.id.llEmptyKitchen);
        llLogout = findViewById(R.id.llLogout);
        allOrdersList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new ChefOrdersAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterOrdersByTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        llLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ChefDashboard.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadKitchenOrders() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Listen for all orders that are not served or cancelled
        Query kitchenQuery = db.collection(Constants.ORDERS_PATH)
                .whereEqualTo("restaurantId", restaurantId)
                .whereIn("status", List.of(Constants.ORDER_STATUS_PENDING, 
                                          Constants.ORDER_STATUS_IN_PROGRESS, 
                                          Constants.ORDER_STATUS_READY))
                .orderBy("orderTime", Query.Direction.ASCENDING);

        ordersListener = kitchenQuery.addSnapshotListener((snapshots, e) -> {
            progressBar.setVisibility(View.GONE);
            if (e != null) {
                Log.e(TAG, "Listen failed.", e);
                return;
            }

            if (snapshots != null) {
                allOrdersList.clear();
                allOrdersList.addAll(snapshots.toObjects(Order.class));
                filterOrdersByTab(tabLayout.getSelectedTabPosition());
            }
        });
    }

    private void filterOrdersByTab(int position) {
        List<Order> filteredList = new ArrayList<>();
        
        switch (position) {
            case 0: // Active (Pending + In Progress)
                for (Order o : allOrdersList) {
                    if (o.getStatus().equals(Constants.ORDER_STATUS_PENDING) || 
                        o.getStatus().equals(Constants.ORDER_STATUS_IN_PROGRESS)) {
                        filteredList.add(o);
                    }
                }
                break;
            case 1: // Pending
                for (Order o : allOrdersList) {
                    if (o.getStatus().equals(Constants.ORDER_STATUS_PENDING)) {
                        filteredList.add(o);
                    }
                }
                break;
            case 2: // In Progress
                for (Order o : allOrdersList) {
                    if (o.getStatus().equals(Constants.ORDER_STATUS_IN_PROGRESS)) {
                        filteredList.add(o);
                    }
                }
                break;
        }

        adapter.setOrders(filteredList);
        
        if (filteredList.isEmpty()) {
            llEmptyKitchen.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            llEmptyKitchen.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStatusChange(Order order, String newStatus) {
        progressBar.setVisibility(View.VISIBLE);
        
        long timestamp = System.currentTimeMillis();
        String timeField = newStatus.equals(Constants.ORDER_STATUS_READY) ? "readyTime" : "startTime";

        db.collection(Constants.ORDERS_PATH).document(order.getId())
                .update("status", newStatus, timeField, timestamp)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Order status updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersListener != null) {
            ordersListener.remove();
        }
    }
}
