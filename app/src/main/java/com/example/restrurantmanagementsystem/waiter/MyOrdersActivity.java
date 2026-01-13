package com.example.restrurantmanagementsystem.waiter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Order;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.example.restrurantmanagementsystem.waiter.adapters.WaiterOrderAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity implements WaiterOrderAdapter.OnOrderClickListener {

    private MaterialToolbar toolbar;
    private ChipGroup chipGroupStatus;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;

    private WaiterOrderAdapter adapter;
    private List<Order> orderList;
    private String restaurantId;
    private String waiterId;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        waiterId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseHelper = FirebaseHelper.getInstance();

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupStatusFilter();
        loadOrders();

        swipeRefresh.setOnRefreshListener(this::loadOrders);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
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
        adapter = new WaiterOrderAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupStatusFilter() {
        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                adapter.filterByStatus("All");
            } else if (checkedId == R.id.chipPending) {
                adapter.filterByStatus(Constants.ORDER_STATUS_PENDING);
            } else if (checkedId == R.id.chipInProgress) {
                adapter.filterByStatus(Constants.ORDER_STATUS_IN_PROGRESS);
            } else if (checkedId == R.id.chipReady) {
                adapter.filterByStatus(Constants.ORDER_STATUS_READY);
            } else if (checkedId == R.id.chipServed) {
                adapter.filterByStatus(Constants.ORDER_STATUS_SERVED);
            }
        });
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getOrdersCollection()
                .whereEqualTo("restaurantId", restaurantId)
                .whereEqualTo("waiterId", waiterId)
                .orderBy("orderTime", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    if (e != null) {
                        Toast.makeText(this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        orderList.clear();
                        orderList.addAll(snapshots.toObjects(Order.class));
                        adapter.setOrders(orderList);

                        // Re-apply filter after data load
                        applyCurrentFilter();

                        if (orderList.isEmpty()) {
                            llEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            llEmptyState.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void applyCurrentFilter() {
        int checkedId = chipGroupStatus.getCheckedChipId();
        if (checkedId == R.id.chipAll) adapter.filterByStatus("All");
        else if (checkedId == R.id.chipPending) adapter.filterByStatus(Constants.ORDER_STATUS_PENDING);
        else if (checkedId == R.id.chipInProgress) adapter.filterByStatus(Constants.ORDER_STATUS_IN_PROGRESS);
        else if (checkedId == R.id.chipReady) adapter.filterByStatus(Constants.ORDER_STATUS_READY);
        else if (checkedId == R.id.chipServed) adapter.filterByStatus(Constants.ORDER_STATUS_SERVED);
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, OrderDetailsActivity.class);
        intent.putExtra(Constants.KEY_ORDER, order);
        intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
        startActivity(intent);
    }
}
