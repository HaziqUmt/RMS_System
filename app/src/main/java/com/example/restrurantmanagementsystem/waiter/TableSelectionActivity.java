package com.example.restrurantmanagementsystem.waiter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Table;
import com.example.restrurantmanagementsystem.utils.Constants;
import com.example.restrurantmanagementsystem.utils.FirebaseHelper;
import com.example.restrurantmanagementsystem.waiter.adapters.TableAdapter;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class TableSelectionActivity extends AppCompatActivity implements TableAdapter.OnTableClickListener {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout llEmptyState;
    private TableAdapter adapter;
    private List<Table> tableList;
    private String restaurantId;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_selection);

        restaurantId = getIntent().getStringExtra(Constants.KEY_RESTAURANT_ID);
        firebaseHelper = FirebaseHelper.getInstance();

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadTables();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewTables);
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
                        } else {
                            llEmptyState.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onTableClick(Table table) {
        if (Constants.TABLE_STATUS_OCCUPIED.equals(table.getStatus())) {
            Toast.makeText(this, "Table is already occupied", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, NewOrderActivity.class);
        intent.putExtra(Constants.KEY_RESTAURANT_ID, restaurantId);
        intent.putExtra(Constants.KEY_TABLE_ID, table.getId());
        intent.putExtra(Constants.KEY_TABLE_NUMBER, table.getTableNumber());
        startActivity(intent);
    }
}
