package com.example.restrurantmanagementsystem.waiter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Order;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.example.restrurantmanagementsystem.waiter.adapters.CartItemAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderDetailsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvTableNumber, tvStatus, tvOrderId, tvOrderTime;
    private TextView tvSubtotal, tvTax, tvTotal, tvSpecialNotes;
    private RecyclerView recyclerView;
    private Button btnAction;
    private ProgressBar progressBar;

    private Order order;
    private String restaurantId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        order = (Order) getIntent().getSerializableExtra(Constants.KEY_ORDER);
        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupToolbar();
        displayOrderDetails();
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

    private void displayOrderDetails() {
        if (order == null) return;

        tvTableNumber.setText("Table " + order.getTableNumber());
        tvOrderId.setText("Order #" + order.getId().substring(0, 8).toUpperCase());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        tvOrderTime.setText(sdf.format(new Date(order.getOrderTime())));

        tvSubtotal.setText(String.format("$%.2f", order.getSubtotal()));
        tvTax.setText(String.format("$%.2f", order.getTax()));
        tvTotal.setText(String.format("$%.2f", order.getTotalAmount()));

        if (order.getSpecialNotes() != null && !order.getSpecialNotes().isEmpty()) {
            findViewById(R.id.tvSpecialNotesLabel).setVisibility(View.VISIBLE);
            tvSpecialNotes.setVisibility(View.VISIBLE);
            tvSpecialNotes.setText(order.getSpecialNotes());
        }

        // Setup status and action button
        updateStatusUI();

        // Setup items list (Reuse CartItemAdapter in a read-only way or create simple one)
        // For simplicity, using CartItemAdapter but disabling interactions
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
            btnAction.setOnClickListener(v -> Toast.makeText(this, "Billing feature coming soon", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateOrderStatus(String newStatus) {
        progressBar.setVisibility(View.VISIBLE);
        btnAction.setEnabled(false);

        db.collection(Constants.ORDERS_PATH).document(order.getId())
                .update("status", newStatus, "servedTime", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    order.setStatus(newStatus);
                    updateStatusUI();
                    Toast.makeText(this, "Order updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    
                    // If served, we might want to update table status back to available 
                    // or keep it occupied until bill is paid. Usually it's occupied.
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnAction.setEnabled(true);
                    Toast.makeText(this, "Failed to update order", Toast.LENGTH_SHORT).show();
                });
    }
}
