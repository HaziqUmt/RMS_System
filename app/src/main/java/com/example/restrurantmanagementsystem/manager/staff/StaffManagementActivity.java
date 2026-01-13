package com.example.restrurantmanagementsystem.manager.staff;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.manager.staff.adapters.StaffAdapter;
import com.example.restrurantmanagementsystem.models.User;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class StaffManagementActivity extends AppCompatActivity implements StaffAdapter.OnStaffClickListener {

    private static final String TAG = "StaffManagementActivity";

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddStaff;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;

    private StaffAdapter adapter;
    private List<User> staffList;
    private FirebaseFirestore db;
    private String restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_management);

        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadStaffMembers();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewStaff);
        fabAddStaff = findViewById(R.id.fabAddStaff);
        progressBar = findViewById(R.id.progressBar);
        llEmptyState = findViewById(R.id.llEmptyState);

        staffList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new StaffAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAddStaff.setOnClickListener(v -> {
            Intent intent = new Intent(StaffManagementActivity.this, AddStaffActivity.class);
            intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
            startActivityForResult(intent, Constants.REQUEST_ADD_STAFF);
        });
    }

    private void loadStaffMembers() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection(Constants.USERS_COLLECTION)
                .whereEqualTo("restaurantId", restaurantId)
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);
                    if (e != null) {
                        Log.e(TAG, "Error loading staff", e);
                        return;
                    }

                    if (snapshots != null) {
                        staffList.clear();
                        for (User user : snapshots.toObjects(User.class)) {
                            // Don't show the manager themselves in the staff list
                            if (!Constants.ROLE_MANAGER.equals(user.role)) {
                                staffList.add(user);
                            }
                        }
                        adapter.setStaffList(staffList);

                        if (staffList.isEmpty()) {
                            llEmptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            llEmptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onDeleteClick(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Staff Member")
                .setMessage("Are you sure you want to remove " + user.name + "?")
                .setPositiveButton("Remove", (dialog, which) -> deleteStaffMember(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteStaffMember(User user) {
        // Note: This only deletes from Firestore. 
        // In a real app, you'd also need to disable/delete their Auth account.
        db.collection(Constants.USERS_COLLECTION).document(user.uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Staff member removed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to remove staff", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ADD_STAFF && resultCode == RESULT_OK) {
            loadStaffMembers();
        }
    }
}