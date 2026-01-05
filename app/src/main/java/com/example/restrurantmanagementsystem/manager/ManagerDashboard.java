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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

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
        // TODO: Replace with actual data from Firebase/Database

        // For now, using dummy data
        String managerName = getIntent().getStringExtra("managerName");
        if (managerName != null && !managerName.isEmpty()) {
            tvManagerName.setText(managerName);
        } else {
            tvManagerName.setText("Manager");
        }

        // Set dummy statistics (replace with real data later)
        tvTotalOrders.setText("0");
        tvTotalRevenue.setText("$0");
        tvActiveOrders.setText("0");
        tvStaffOnline.setText("0");

        // TODO: Fetch real-time data from database
        // fetchDashboardStatistics();
    }

    private void setupClickListeners() {
        // Profile icon click
        ivProfile.setOnClickListener(v -> {
            // TODO: Navigate to Profile screen
            Toast.makeText(ManagerDashboard.this, "Profile clicked", Toast.LENGTH_SHORT).show();
        });

        // View All Orders button
        btnViewAllOrders.setOnClickListener(v -> {
            // TODO: Navigate to All Orders screen
            Toast.makeText(ManagerDashboard.this, "View All Orders clicked", Toast.LENGTH_SHORT).show();
        });

        // View Analytics button
        btnViewAnalytics.setOnClickListener(v -> {
            // TODO: Navigate to Analytics screen
            Toast.makeText(ManagerDashboard.this, "View Analytics clicked", Toast.LENGTH_SHORT).show();
        });

        // Manage Menu button
        btnManageMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerDashboard.this, MenuManagementActivity.class);
            startActivity(intent);
        });

        // Manage Staff button
        btnManageStaff.setOnClickListener(v -> {
            // TODO: Navigate to Staff Management screen
            Toast.makeText(ManagerDashboard.this, "Manage Staff clicked", Toast.LENGTH_SHORT).show();
        });

        // Reports button
        btnReports.setOnClickListener(v -> {
            // TODO: Navigate to Reports screen
            Toast.makeText(ManagerDashboard.this, "View Reports clicked", Toast.LENGTH_SHORT).show();
        });

        // Settings button
        btnSettings.setOnClickListener(v -> {
            // TODO: Navigate to Settings screen
            Toast.makeText(ManagerDashboard.this, "Settings clicked", Toast.LENGTH_SHORT).show();
        });

        // View All recent activity
        tvViewAll.setOnClickListener(v -> {
            // TODO: Navigate to Activity History screen
            Toast.makeText(ManagerDashboard.this, "View All Activity clicked", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private String getTodayDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}