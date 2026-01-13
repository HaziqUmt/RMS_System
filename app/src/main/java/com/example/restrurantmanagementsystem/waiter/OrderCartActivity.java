package com.example.restrurantmanagementsystem.waiter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Order;
import com.example.restrurantmanagementsystem.models.OrderItem;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.example.restrurantmanagementsystem.waiter.adapters.CartItemAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class OrderCartActivity extends AppCompatActivity implements CartItemAdapter.OnCartItemClickListener {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private TextView tvSubtotal, tvTax, tvTotal;
    private EditText etOrderNotes;
    private Button btnSendToKitchen;
    private ProgressBar progressBar;

    private CartItemAdapter adapter;
    private List<OrderItem> cartItems;
    private String restaurantId;
    private String tableId;
    private int tableNumber;
    
    private double subtotal = 0;
    private double tax = 0;
    private double total = 0;
    private static final double TAX_RATE = 0.10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_cart);

        Intent intent = getIntent();
        restaurantId = intent.getStringExtra(Constants.KEY_RESTAURANT_ID);
        tableId = intent.getStringExtra(Constants.KEY_TABLE_ID);
        tableNumber = intent.getIntExtra(Constants.KEY_TABLE_NUMBER, 0);
        cartItems = (ArrayList<OrderItem>) intent.getSerializableExtra("cart_items");

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        calculateTotals();

        btnSendToKitchen.setOnClickListener(v -> sendOrderToKitchen());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);
        etOrderNotes = findViewById(R.id.etOrderNotes);
        btnSendToKitchen = findViewById(R.id.btnSendToKitchen);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new CartItemAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.setCartItems(cartItems);
    }

    private void calculateTotals() {
        subtotal = 0;
        for (OrderItem item : cartItems) {
            subtotal += item.getSubtotal();
        }
        tax = subtotal * TAX_RATE;
        total = subtotal + tax;

        tvSubtotal.setText(String.format("$%.2f", subtotal));
        tvTax.setText(String.format("$%.2f", tax));
        tvTotal.setText(String.format("$%.2f", total));
    }

    @Override
    public void onUpdateQuantity(OrderItem item, int quantity) {
        item.setQuantity(quantity);
        item.setSubtotal(item.getPrice() * quantity);
        adapter.notifyDataSetChanged();
        calculateTotals();
    }

    @Override
    public void onRemoveItem(OrderItem item) {
        cartItems.remove(item);
        adapter.notifyDataSetChanged();
        calculateTotals();
        if (cartItems.isEmpty()) {
            finish();
        }
    }

    private void sendOrderToKitchen() {
        if (cartItems.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);
        btnSendToKitchen.setEnabled(false);

        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String orderId = db.collection(Constants.ORDERS_PATH).document().getId();
        
        String waiterId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Ideally fetch waiter name from profile, for now using "Waiter"
        String waiterName = "Waiter"; 

        Order order = new Order(
                orderId,
                restaurantId,
                tableId,
                tableNumber,
                waiterId,
                waiterName,
                Constants.ORDER_STATUS_PENDING,
                cartItems,
                total,
                tax,
                subtotal,
                etOrderNotes.getText().toString().trim(),
                System.currentTimeMillis()
        );

        // Save order
        db.collection(Constants.ORDERS_PATH).document(orderId).set(order)
                .addOnSuccessListener(aVoid -> {
                    // Update table status
                    firebaseHelper.getTablesCollection(restaurantId).document(tableId)
                            .update("status", Constants.TABLE_STATUS_OCCUPIED)
                            .addOnSuccessListener(aVoid1 -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Order sent to kitchen!", Toast.LENGTH_SHORT).show();
                                
                                Intent intent = new Intent(this, MyOrdersActivity.class);
                                intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSendToKitchen.setEnabled(true);
                    Toast.makeText(this, "Failed to send order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
