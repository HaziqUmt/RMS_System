package com.example.restrurantmanagementsystem.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.manager.menu.MenuManagementActivity;
import com.example.restrurantmanagementsystem.manager.staff.StaffManagementActivity;
import com.example.restrurantmanagementsystem.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ManagerDashboard extends AppCompatActivity {

    // UI Components
    private TextView tvManagerName, tvCurrentDate;
    private TextView tvTotalOrders, tvTotalRevenue, tvActiveOrders, tvStaffOnline;
    private ImageView ivProfile;
    private Button btnViewAllOrders, btnViewAnalytics, btnManageMenu, btnManageStaff;
    private Button btnReports, btnSettings;
    private TextView tvViewAll;

    private String restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);

        // Initialize UI components
        initializeViews();

        // Set up current date
        setCurrentDate();

        // Load dashboard data
        loadDashboardData();

        // Set up button click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        // Header views
        tvManagerName = findViewById(R.id.tvManagerName);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        ivProfile = findViewById(R.id.ivProfile);

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
    }

    private void setCurrentDate() {
        // Format current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        tvCurrentDate.setText(currentDate);
    }

    private void loadDashboardData() {
        // For now, using dummy data
        String managerName = getIntent().getStringExtra("managerName");
        if (managerName != null && !managerName.isEmpty()) {
            tvManagerName.setText(managerName);
        } else {
            tvManagerName.setText("Manager");
        }

        tvTotalOrders.setText("0");
        tvTotalRevenue.setText("$0");
        tvActiveOrders.setText("0");
        tvStaffOnline.setText("0");
    }

    private void setupClickListeners() {
        // Profile icon click
        ivProfile.setOnClickListener(v -> {
            Toast.makeText(ManagerDashboard.this, "Profile clicked", Toast.LENGTH_SHORT).show();
        });

        // View All Orders button
        btnViewAllOrders.setOnClickListener(v -> {
            Toast.makeText(ManagerDashboard.this, "View All Orders clicked", Toast.LENGTH_SHORT).show();
        });

        // View Analytics button
        btnViewAnalytics.setOnClickListener(v -> {
            Toast.makeText(ManagerDashboard.this, "View Analytics clicked", Toast.LENGTH_SHORT).show();
        });

        // Manage Menu button
        btnManageMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboard.this, MenuManagementActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            intent.putExtra("isAdmin", true); // Manager can edit
            startActivity(intent);
        });

        // Manage Staff button
        btnManageStaff.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboard.this, StaffManagementActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            startActivity(intent);
        });

        // Reports button
        btnReports.setOnClickListener(v -> {
            Toast.makeText(ManagerDashboard.this, "View Reports clicked", Toast.LENGTH_SHORT).show();
        });

        // Manage Tables button (reusing Settings button)
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboard.this, TableManagementActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            startActivity(intent);
        });

        // View All recent activity
        tvViewAll.setOnClickListener(v -> {
            Toast.makeText(ManagerDashboard.this, "View All Activity clicked", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }
}