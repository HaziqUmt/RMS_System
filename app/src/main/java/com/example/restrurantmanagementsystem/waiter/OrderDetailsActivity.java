package com.example.restrurantmanagementsystem.waiter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Order;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.example.restrurantmanagementsystem.waiter.adapters.CartItemAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvTableNumber, tvStatus, tvOrderId, tvOrderTime;
    private TextView tvSubtotal, tvTax, tvTotal, tvSpecialNotes;
    private RecyclerView recyclerView;
    private Button btnAction;
    private ProgressBar progressBar;

    private Order order;
    private String restaurantId;
    private FirebaseFirestore db;
    private ListenerRegistration orderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        order = (Order) getIntent().getSerializableExtra(Constants.KEY_ORDER);
        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupToolbar();
        
        if (order != null) {
            listenToOrderUpdates();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTableNumber = findViewById(R.id.tvTableNumber);
        tvStatus = findViewById(R.id.tvStatus);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderTime = findViewById(R.id.tvOrderTime);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);
        tvSpecialNotes = findViewById(R.id.tvSpecialNotes);
        recyclerView = findViewById(R.id.recyclerViewOrderItems);
        btnAction = findViewById(R.id.btnAction);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void listenToOrderUpdates() {
        orderListener = db.collection(Constants.ORDERS_PATH).document(order.getId())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        order = snapshot.toObject(Order.class);
                        displayOrderDetails();
                    }
                });
    }

    private void displayOrderDetails() {
        if (order == null) return;

        tvTableNumber.setText("Table " + order.getTableNumber());
        tvOrderId.setText("Order #" + (order.getId().length() > 8 ? order.getId().substring(0, 8).toUpperCase() : order.getId()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        tvOrderTime.setText(sdf.format(new Date(order.getOrderTime())));

        tvSubtotal.setText(String.format("$%.2f", order.getSubtotal()));
        tvTax.setText(String.format("$%.2f", order.getTax()));
        tvTotal.setText(String.format("$%.2f", order.getTotalAmount()));

        if (order.getSpecialNotes() != null && !order.getSpecialNotes().isEmpty()) {
            findViewById(R.id.tvSpecialNotesLabel).setVisibility(View.VISIBLE);
            tvSpecialNotes.setVisibility(View.VISIBLE);
            tvSpecialNotes.setText(order.getSpecialNotes());
        } else {
            findViewById(R.id.tvSpecialNotesLabel).setVisibility(View.GONE);
            tvSpecialNotes.setVisibility(View.GONE);
        }

        updateStatusUI();

        CartItemAdapter adapter = new CartItemAdapter(this, null);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.setCartItems(order.getItems());
    }

    private void updateStatusUI() {
        String status = order.getStatus();
        tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1).replace("_", " "));
        
        btnAction.setVisibility(View.GONE);
        if (Constants.ORDER_STATUS_READY.equals(status)) {
            btnAction.setVisibility(View.VISIBLE);
            btnAction.setText("MARK AS SERVED");
            btnAction.setOnClickListener(v -> updateOrderStatus(Constants.ORDER_STATUS_SERVED));
        } else if (Constants.ORDER_STATUS_SERVED.equals(status)) {
            btnAction.setVisibility(View.VISIBLE);
            btnAction.setText("CREATE BILL");
            btnAction.setOnClickListener(v -> createBill());
        }
    }

    private void updateOrderStatus(String newStatus) {
        progressBar.setVisibility(View.VISIBLE);
        btnAction.setEnabled(false);

        db.collection(Constants.ORDERS_PATH).document(order.getId())
                .update("status", newStatus, "servedTime", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnAction.setEnabled(true);
                    Toast.makeText(this, "Order updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnAction.setEnabled(true);
                    Toast.makeText(this, "Failed to update order", Toast.LENGTH_SHORT).show();
                });
    }

    private void createBill() {
        progressBar.setVisibility(View.VISIBLE);
        btnAction.setEnabled(false);

        FirebaseHelper.getInstance().getTablesCollection(restaurantId).document(order.getTableId())
                .update("status", Constants.TABLE_STATUS_AVAILABLE)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Bill created and table is now available", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnAction.setEnabled(true);
                    Toast.makeText(this, "Error clearing table", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
        }
    }
}
