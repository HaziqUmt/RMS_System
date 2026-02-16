package com.example.restrurantmanagementsystem.manager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Order;
import com.example.restrurantmanagementsystem.models.OrderItem;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsActivity extends AppCompatActivity {

    private static final String TAG = "AnalyticsActivity";

    private Toolbar toolbar;
    private MaterialButton btnDateRange;
    private TextView tvTotalRevenue, tvTotalOrders, tvServedCount, tvCancelledCount, tvNoDataItems;
    private LinearLayout llTopItems;
    private ProgressBar progressBar;

    private String restaurantId;
    private FirebaseFirestore db;
    private long startDate, endDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupToolbar();
        setupDateFilter();
        
        // Default to last 7 days
        setLast7DaysRange();
        loadAnalyticsData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        btnDateRange = findViewById(R.id.btnDateRange);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvServedCount = findViewById(R.id.tvServedCount);
        tvCancelledCount = findViewById(R.id.tvCancelledCount);
        tvNoDataItems = findViewById(R.id.tvNoDataItems);
        llTopItems = findViewById(R.id.llTopItems);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupDateFilter() {
        btnDateRange.setOnClickListener(v -> showDateRangePicker());
    }

    private void setLast7DaysRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        endDate = cal.getTimeInMillis();
        
        cal.add(Calendar.DAY_OF_YEAR, -7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        startDate = cal.getTimeInMillis();
        
        btnDateRange.setText("Last 7 Days");
    }

    private void showDateRangePicker() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Analysis Period")
                .build();

        picker.show(getSupportFragmentManager(), "ANALYTICS_PICKER");
        picker.addOnPositiveButtonClickListener(selection -> {
            startDate = selection.first;
            endDate = selection.second;
            
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(endDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            endDate = cal.getTimeInMillis();

            btnDateRange.setText(dateFormat.format(new Date(startDate)) + " - " + dateFormat.format(new Date(endDate)));
            loadAnalyticsData();
        });
    }

    private void loadAnalyticsData() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection(Constants.ORDERS_PATH)
                .whereEqualTo("restaurantId", restaurantId)
                .whereGreaterThanOrEqualTo("orderTime", startDate)
                .whereLessThanOrEqualTo("orderTime", endDate)
                .get()
                .addOnSuccessListener(snapshots -> {
                    progressBar.setVisibility(View.GONE);
                    if (snapshots != null) {
                        List<Order> orders = snapshots.toObjects(Order.class);
                        processAnalytics(orders);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load analytics", Toast.LENGTH_SHORT).show();
                });
    }

    private void processAnalytics(List<Order> orders) {
        double revenue = 0;
        int served = 0;
        int cancelled = 0;
        Map<String, Integer> itemSales = new HashMap<>();

        for (Order order : orders) {
            if (Constants.ORDER_STATUS_SERVED.equals(order.getStatus())) {
                revenue += order.getTotalAmount();
                served++;
                
                // Track item counts
                for (OrderItem item : order.getItems()) {
                    String name = item.getMenuItemName();
                    itemSales.put(name, itemSales.getOrDefault(name, 0) + item.getQuantity());
                }
            } else if (Constants.ORDER_STATUS_CANCELLED.equals(order.getStatus())) {
                cancelled++;
            }
        }

        // Update UI
        tvTotalRevenue.setText(String.format("$%.2f", revenue));
        tvTotalOrders.setText("Total Orders: " + orders.size());
        tvServedCount.setText(String.valueOf(served));
        tvCancelledCount.setText(String.valueOf(cancelled));

        updateTopItemsList(itemSales);
    }

    private void updateTopItemsList(Map<String, Integer> itemSales) {
        llTopItems.removeAllViews();
        
        if (itemSales.isEmpty()) {
            llTopItems.addView(tvNoDataItems);
            return;
        }

        // Sort by sales count descending
        Map<String, Integer> sortedMap = itemSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
            View itemView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
            TextView text1 = itemView.findViewById(android.R.id.text1);
            TextView text2 = itemView.findViewById(android.R.id.text2);
            
            text1.setText(entry.getKey());
            text1.setTextSize(16);
            text1.setTextColor(getColor(R.color.textColorPrimary));
            
            text2.setText(entry.getValue() + " servings sold");
            text2.setTextColor(getColor(R.color.textColorSecondary));
            
            llTopItems.addView(itemView);
        }
    }
}
