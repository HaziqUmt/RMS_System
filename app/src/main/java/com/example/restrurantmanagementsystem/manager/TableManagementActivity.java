package com.example.restrurantmanagementsystem.manager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Table;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.example.restrurantmanagementsystem.waiter.adapters.TableAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TableManagementActivity extends AppCompatActivity implements TableAdapter.OnTableClickListener {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddTable;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;

    private TableAdapter adapter;
    private List<Table> tableList;
    private String restaurantId;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_management);

        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        firebaseHelper = FirebaseHelper.getInstance();

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadTables();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewTables);
        fabAddTable = findViewById(R.id.fabAddTable);
        progressBar = findViewById(R.id.progressBar);
        llEmptyState = findViewById(R.id.llEmptyState);
        tableList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new TableAdapter(this, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAddTable.setOnClickListener(v -> showAddTableDialog());
    }

    private void loadTables() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getTablesCollection(restaurantId)
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);
                    if (e != null) {
                        Toast.makeText(this, "Error loading tables", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        tableList.clear();
                        tableList.addAll(snapshots.toObjects(Table.class));
                        adapter.setTables(tableList);

                        if (tableList.isEmpty()) {
                            llEmptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            llEmptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void showAddTableDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_table, null);
        EditText etTableNumber = dialogView.findViewById(R.id.etTableNumber);
        EditText etCapacity = dialogView.findViewById(R.id.etCapacity);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String numberStr = etTableNumber.getText().toString().trim();
                    String capacityStr = etCapacity.getText().toString().trim();

                    if (!TextUtils.isEmpty(numberStr) && !TextUtils.isEmpty(capacityStr)) {
                        addTable(Integer.parseInt(numberStr), Integer.parseInt(capacityStr));
                    } else {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addTable(int tableNumber, int capacity) {
        String id = firebaseHelper.getTablesCollection(restaurantId).document().getId();
        Table table = new Table(id, tableNumber, capacity, Constants.TABLE_STATUS_AVAILABLE, restaurantId);

        firebaseHelper.getTablesCollection(restaurantId).document(id)
                .set(table)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Table added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add table", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onTableClick(Table table) {
        // Manager can delete table on click
        new AlertDialog.Builder(this)
                .setTitle("Delete Table")
                .setMessage("Are you sure you want to delete Table " + table.getTableNumber() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    firebaseHelper.getTablesCollection(restaurantId).document(table.getId()).delete();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
