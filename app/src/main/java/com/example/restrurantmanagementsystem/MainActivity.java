package com.example.restrurantmanagementsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.restrurantmanagementsystem.auth.LoginActivity;
import com.example.restrurantmanagementsystem.chef.ChefDashboard;
import com.example.restrurantmanagementsystem.manager.ManagerDashboard;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.waiter.WaiterDashboard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting role check.");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "User is not authenticated. Redirecting to LoginActivity.");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String uid = currentUser.getUid();
        Log.d(TAG, "User authenticated with UID: " + uid);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(Constants.USERS_COLLECTION).document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        String restaurantId = documentSnapshot.getString("restaurantId");
                        String name = documentSnapshot.getString("name");
                        
                        Log.d(TAG, "Successfully fetched role: " + role + " for restaurant: " + restaurantId);

                        Intent intent;
                        if ("Manager".equals(role)) {
                            Log.d(TAG, "Redirecting to ManagerDashboard.");
                            intent = new Intent(MainActivity.this, ManagerDashboard.class);
                        } else if ("Waiter".equals(role)) {
                            Log.d(TAG, "Redirecting to WaiterDashboard.");
                            intent = new Intent(MainActivity.this, WaiterDashboard.class);
                        } else if ("Chef".equals(role)) {
                            Log.d(TAG, "Redirecting to ChefDashboard.");
                            intent = new Intent(MainActivity.this, ChefDashboard.class);
                        } else {
                            Log.w(TAG, "Role not recognized: '" + role + "'. Redirecting to LoginActivity.");
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                            return;
                        }
                        
                        intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
                        intent.putExtra("managerName", name); // For ManagerDashboard name display
                        startActivity(intent);
                        finish();
                        
                    } else {
                        Log.w(TAG, "User document does not exist in Firestore. Redirecting to LoginActivity.");
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user document from Firestore", e);
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                });
    }
}