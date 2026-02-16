package com.example.restrurantmanagementsystem.waiter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.auth.LoginActivity;
import com.example.restrurantmanagementsystem.manager.menu.MenuManagementActivity;
import com.example.restrurantmanagementsystem.models.Order;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.example.restrurantmanagementsystem.waiter.adapters.WaiterOrderAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WaiterDashboard extends AppCompatActivity implements WaiterOrderAdapter.OnOrderClickListener {

    private static final String TAG = "WaiterDashboard";

    private TextView tvWaiterName, tvCurrentDate;
    private TextView tvActiveOrdersCount, tvServedTodayCount, tvTotalRevenue;
    private Button btnNewOrder, btnMyOrders, btnViewMenu;
    private LinearLayout llLogout;
    private RecyclerView recyclerViewRecentOrders;

    private String restaurantId;
    private String waiterId;
    private WaiterOrderAdapter adapter;
    private List<Order> recentOrders;
    private FirebaseHelper firebaseHelper;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiter_dashboard);

        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        waiterId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseHelper = FirebaseHelper.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        setCurrentDate();
        loadDashboardData();
    }

    private void initializeViews() {
        tvWaiterName = findViewById(R.id.tvWaiterName);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvActiveOrdersCount = findViewById(R.id.tvActiveOrdersCount);
        tvServedTodayCount = findViewById(R.id.tvServedTodayCount);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        btnNewOrder = findViewById(R.id.btnNewOrder);
        btnMyOrders = findViewById(R.id.btnMyOrders);
        btnViewMenu = findViewById(R.id.btnViewMenu);
        llLogout = findViewById(R.id.llLogout);
        recyclerViewRecentOrders = findViewById(R.id.recyclerViewRecentOrders);

        String waiterName = getIntent().getStringExtra("managerName");
        if (waiterName != null) tvWaiterName.setText(waiterName);
    }

    private void setupRecyclerView() {
        recentOrders = new ArrayList<>();
        adapter = new WaiterOrderAdapter(this, this);
        recyclerViewRecentOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecentOrders.setAdapter(adapter);
    }

    private void setupClickListeners() {
        llLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(WaiterDashboard.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnNewOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, TableSelectionActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            startActivity(intent);
        });

        btnMyOrders.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyOrdersActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            startActivity(intent);
        });

        btnViewMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuManagementActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            intent.putExtra("isAdmin", false);
            startActivity(intent);
        });
    }

    private void setCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        tvCurrentDate.setText(dateFormat.format(new Date()));
    }

    private void loadDashboardData() {
        // Real-time listener for waiter's orders
        db.collection(Constants.ORDERS_PATH)
                .whereEqualTo("restaurantId", restaurantId)
                .whereEqualTo("waiterId", waiterId)
                .orderBy("orderTime", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        List<Order> allOrders = snapshots.toObjects(Order.class);
                        updateStats(allOrders);
                        
                        // Update Recent Orders (last 5)
                        recentOrders.clear();
                        for (int i = 0; i < Math.min(5, allOrders.size()); i++) {
                            recentOrders.add(allOrders.get(i));
                        }
                        adapter.setOrders(recentOrders);
                    }
                });
    }

    private void updateStats(List<Order> orders) {
        int active = 0;
        int servedToday = 0;
        double revenueToday = 0;
        
        long startOfDay = getStartOfDayTimestamp();

        for (Order order : orders) {
            // Count active orders (anything not served or cancelled)
            if (!order.getStatus().equals(Constants.ORDER_STATUS_SERVED) && 
                !order.getStatus().equals(Constants.ORDER_STATUS_CANCELLED)) {
                active++;
            }
            
            // Daily stats
            if (order.getOrderTime() >= startOfDay) {
                if (order.getStatus().equals(Constants.ORDER_STATUS_SERVED)) {
                    servedToday++;
                    revenueToday += order.getTotalAmount();
                }
            }
        }

        tvActiveOrdersCount.setText(String.valueOf(active));
        tvServedTodayCount.setText(String.valueOf(servedToday));
        tvTotalRevenue.setText(String.format("$%.0f", revenueToday));
    }

    private long getStartOfDayTimestamp() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, OrderDetailsActivity.class);
        intent.putExtra(Constants.KEY_ORDER, order);
        intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
        startActivity(intent);
    }
}