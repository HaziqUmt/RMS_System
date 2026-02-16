package com.example.restrurantmanagementsystem.manager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Order;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.waiter.adapters.WaiterOrderAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllOrdersActivity extends AppCompatActivity {

    private static final String TAG = "AllOrdersActivity";

    private Toolbar toolbar;
    private MaterialButton btnDateRange;
    private ImageButton btnClearFilter;
    private ChipGroup chipGroupStatus;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;

    private WaiterOrderAdapter adapter;
    private List<Order> orderList;
    private String restaurantId;
    private FirebaseFirestore db;
    
    private long startDate, endDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_orders);

        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        
        // Default to today
        setTodayRange();
        loadOrders();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        btnDateRange = findViewById(R.id.btnDateRange);
        btnClearFilter = findViewById(R.id.btnClearFilter);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerViewOrders);
        progressBar = findViewById(R.id.progressBar);
        llEmptyState = findViewById(R.id.llEmptyState);
        orderList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new WaiterOrderAdapter(this, null); // No click listener needed for now
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        btnDateRange.setOnClickListener(v -> showDateRangePicker());
        
        btnClearFilter.setOnClickListener(v -> {
            setTodayRange();
            loadOrders();
            btnClearFilter.setVisibility(View.GONE);
        });

        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            applyStatusFilter();
        });

        swipeRefresh.setOnRefreshListener(this::loadOrders);
    }

    private void setTodayRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        startDate = cal.getTimeInMillis();
        
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        endDate = cal.getTimeInMillis();
        
        btnDateRange.setText("Today");
    }

    private void showDateRangePicker() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Date Range")
                .build();

        picker.show(getSupportFragmentManager(), "DATE_PICKER");
        picker.addOnPositiveButtonClickListener(selection -> {
            startDate = selection.first;
            endDate = selection.second;
            
            // Adjust end date to end of day
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(endDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            endDate = cal.getTimeInMillis();

            btnDateRange.setText(dateFormat.format(new Date(startDate)) + " - " + dateFormat.format(new Date(endDate)));
            btnClearFilter.setVisibility(View.VISIBLE);
            loadOrders();
        });
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection(Constants.ORDERS_PATH)
                .whereEqualTo("restaurantId", restaurantId)
                .whereGreaterThanOrEqualTo("orderTime", startDate)
                .whereLessThanOrEqualTo("orderTime", endDate)
                .orderBy("orderTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    if (snapshots != null) {
                        orderList = snapshots.toObjects(Order.class);
                        applyStatusFilter();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    Log.e(TAG, "Error loading orders", e);
                    Toast.makeText(this, "Error loading orders", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyStatusFilter() {
        int checkedId = chipGroupStatus.getCheckedChipId();
        String status = "All";
        
        if (checkedId == R.id.chipPending) status = Constants.ORDER_STATUS_PENDING;
        else if (checkedId == R.id.chipServed) status = Constants.ORDER_STATUS_SERVED;
        else if (checkedId == R.id.chipCancelled) status = Constants.ORDER_STATUS_CANCELLED;
        
        adapter.setOrders(orderList);
        adapter.filterByStatus(status);
        
        if (adapter.getItemCount() == 0) {
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            llEmptyState.setVisibility(View.GONE);
        }
    }
}
