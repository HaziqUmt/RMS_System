package com.example.restrurantmanagementsystem.manager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.manager.adapters.ChatAdapter;
import com.example.restrurantmanagementsystem.models.ChatMessage;
import com.example.restrurantmanagementsystem.models.Order;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    private static final String API_KEY = "AIzaSyAFRn1YcCm-RGv-sZEs0SUgRtl4Z28eWXc"; 

    private RecyclerView recyclerViewChat;
    private ChatAdapter adapter;
    private EditText etMessage;
    private FloatingActionButton btnSend;
    private ProgressBar progressBar;
    
    private GenerativeModelFutures model;
    private String restaurantId;
    private String contextData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        
        initializeViews();
        setupAI();
        loadRestaurantContext();
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);

        adapter = new ChatAdapter();
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
        
        // Initial Greeting
        adapter.addMessage(new ChatMessage("Hello! I'm your AI Business Assistant. How can I help you today?", ChatMessage.TYPE_AI));
    }

    private void setupAI() {
        try {
            GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", API_KEY);
            model = GenerativeModelFutures.from(gm);
        } catch (Exception e) {
            Log.e(TAG, "AI Setup Error", e);
            Toast.makeText(this, "AI Setup Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRestaurantContext() {
        // Fetch current day data to give AI context
        FirebaseFirestore.getInstance().collection(Constants.ORDERS_PATH)
                .whereEqualTo("restaurantId", restaurantId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null) {
                        List<Order> orders = snapshots.toObjects(Order.class);
                        double revenue = 0;
                        int total = orders.size();
                        for (Order o : orders) {
                            if (Constants.ORDER_STATUS_SERVED.equals(o.getStatus())) {
                                revenue += o.getTotalAmount();
                            }
                        }
                        contextData = String.format("Current Restaurant Stats: Total Orders: %d, Total Revenue: $%.2f. ", total, revenue);
                    }
                });
    }

    private void sendMessage() {
        String prompt = etMessage.getText().toString().trim();
        if (prompt.isEmpty()) return;

        adapter.addMessage(new ChatMessage(prompt, ChatMessage.TYPE_USER));
        etMessage.setText("");
        recyclerViewChat.scrollToPosition(adapter.getItemCount() - 1);
        
        progressBar.setVisibility(View.VISIBLE);

        Content content = new Content.Builder()
                .addText("Context: You are a helpful restaurant management assistant. " + contextData + "\nQuestion: " + prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Executor executor = Executors.newSingleThreadExecutor();

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    String aiResponse = result.getText();
                    if (aiResponse == null || aiResponse.isEmpty()) {
                        aiResponse = "I'm sorry, I couldn't generate a response. Please try again.";
                    }
                    adapter.addMessage(new ChatMessage(aiResponse, ChatMessage.TYPE_AI));
                    recyclerViewChat.scrollToPosition(adapter.getItemCount() - 1);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "AI Generation Error", t);
                    Toast.makeText(ChatActivity.this, "AI Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }, executor);
    }
}
