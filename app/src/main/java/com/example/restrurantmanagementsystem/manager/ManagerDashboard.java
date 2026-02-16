package com.example.restrurantmanagementsystem.manager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.auth.LoginActivity;
import com.example.restrurantmanagementsystem.manager.menu.MenuManagementActivity;
import com.example.restrurantmanagementsystem.manager.staff.StaffManagementActivity;
import com.example.restrurantmanagementsystem.models.Order;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManagerDashboard extends AppCompatActivity {

    private static final String TAG = "ManagerDashboard";

    // UI Components
    private TextView tvManagerName, tvCurrentDate;
    private TextView tvTotalOrders, tvTotalRevenue, tvActiveOrders, tvStaffOnline;
    private LinearLayout llLogout;
    private Button btnViewAllOrders, btnViewAnalytics, btnManageMenu, btnManageStaff;
    private Button btnReports, btnSettings;
    private TextView tvViewAll;
    private FloatingActionButton fabChat;

    private String restaurantId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        initializeViews();

        // Set up current date
        setCurrentDate();

        // Set up button click listeners
        setupClickListeners();
        
        // Load real-time dashboard data
        loadDashboardData();
    }

    private void initializeViews() {
        // Header views
        tvManagerName = findViewById(R.id.tvManagerName);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        llLogout = findViewById(R.id.llLogout);

        // Statistics views
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvActiveOrders = findViewById(R.id.tvActiveOrders);
        tvStaffOnline = findViewById(R.id.tvStaffOnline);

        // Button views
        btnViewAllOrders = findViewById(R.id.btnViewAllOrders);
        btnViewAnalytics = findViewById(R.id.btnViewAnalytics);
        btnManageMenu = findViewById(R.id.btnManageMenu);
        btnManageStaff = findViewById(R.id.btnManageStaff);
        btnReports = findViewById(R.id.btnReports);
        btnSettings = findViewById(R.id.btnSettings);
        
        // Update Settings button to "Manage Tables"
        btnSettings.setText("Manage\nTables");
        btnSettings.setCompoundDrawablesWithIntrinsicBounds(0, android.R.drawable.ic_menu_today, 0, 0);

        // Other views
        tvViewAll = findViewById(R.id.tvViewAll);
        
        // AI Chat FAB
        fabChat = findViewById(R.id.fabChat);
        if (fabChat == null) {
            // If not in layout yet, we'll need to add it or skip for now
            Log.w(TAG, "Chat FAB not found in layout");
        }
    }

    private void setCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        tvCurrentDate.setText(currentDate);
    }

    private void loadDashboardData() {
        String managerName = getIntent().getStringExtra("managerName");
        if (managerName != null && !managerName.isEmpty()) {
            tvManagerName.setText(managerName);
        } else {
            tvManagerName.setText("Manager");
        }

        // Real-time statistics listener
        db.collection(Constants.ORDERS_PATH)
                .whereEqualTo("restaurantId", restaurantId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null) {
                        List<Order> orders = snapshots.toObjects(Order.class);
                        updateStats(orders);
                    }
                });
        
        // Staff online count
        db.collection(Constants.USERS_COLLECTION)
                .whereEqualTo("restaurantId", restaurantId)
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots != null) {
                        tvStaffOnline.setText(String.valueOf(snapshots.size()));
                    }
                });
    }

    private void updateStats(List<Order> orders) {
        int totalToday = 0;
        int active = 0;
        double revenueToday = 0;
        
        long startOfDay = getStartOfDayTimestamp();

        for (Order order : orders) {
            if (!order.getStatus().equals(Constants.ORDER_STATUS_SERVED) && 
                !order.getStatus().equals(Constants.ORDER_STATUS_CANCELLED)) {
                active++;
            }
            
            if (order.getOrderTime() >= startOfDay) {
                totalToday++;
                if (order.getStatus().equals(Constants.ORDER_STATUS_SERVED)) {
                    revenueToday += order.getTotalAmount();
                }
            }
        }

        tvTotalOrders.setText(String.valueOf(totalToday));
        tvActiveOrders.setText(String.valueOf(active));
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

    private void setupClickListeners() {
        llLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ManagerDashboard.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnViewAllOrders.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboard.this, AllOrdersActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            startActivity(intent);
        });

        btnViewAnalytics.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboard.this, AnalyticsActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            startActivity(intent);
        });

        btnManageMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboard.this, MenuManagementActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            intent.putExtra("isAdmin", true);
            startActivity(intent);
        });

        btnManageStaff.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboard.this, StaffManagementActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboard.this, TableManagementActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            startActivity(intent);
        });
        
        if (fabChat != null) {
            fabChat.setOnClickListener(v -> {
                Intent intent = new Intent(ManagerDashboard.this, ChatActivity.class);
                intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
                startActivity(intent);
            });
        }
    }
}