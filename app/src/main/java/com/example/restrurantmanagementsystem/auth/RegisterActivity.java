package com.example.restrurantmanagementsystem.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.restrurantmanagementsystem.MainActivity;
import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Restaurant;
import com.example.restrurantmanagementsystem.models.User;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText etRestaurantName;
    private EditText etManagerName;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnRegister;
    private TextView tvLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etRestaurantName = findViewById(R.id.etRestaurantName);
        etManagerName = findViewById(R.id.nameEditText);
        etEmail = findViewById(R.id.emailEditText);
        etPassword = findViewById(R.id.passwordEditText);
        btnRegister = findViewById(R.id.registerButton);
        tvLogin = findViewById(R.id.loginTextView);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> registerRestaurant());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });
    }

    private void registerRestaurant() {
        String restaurantName = etRestaurantName.getText().toString().trim();
        String managerName = etManagerName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(restaurantName) || TextUtils.isEmpty(managerName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            String restaurantId = db.collection(Constants.RESTAURANTS_PATH).document().getId();

                            // Create Restaurant Object
                            Restaurant restaurant = new Restaurant(restaurantId, restaurantName, userId);

                            // Create User Object
                            User user = new User(userId, managerName, email, "Manager", restaurantId);

                            // Save Restaurant to Firestore
                            db.collection(Constants.RESTAURANTS_PATH).document(restaurantId).set(restaurant)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Restaurant created successfully.");
                                        // Save User to Firestore
                                        db.collection(Constants.USERS_COLLECTION).document(userId).set(user)
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Log.d(TAG, "Manager user created successfully.");
                                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error creating user", e);
                                                    Toast.makeText(RegisterActivity.this, "Error creating user.", Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error creating restaurant", e);
                                        Toast.makeText(RegisterActivity.this, "Error creating restaurant.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}