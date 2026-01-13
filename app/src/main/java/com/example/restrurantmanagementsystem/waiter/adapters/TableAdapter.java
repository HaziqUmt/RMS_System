package com.example.restrurantmanagementsystem.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Table;
import com.example.restrurantmanagementsystem.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {

    private Context context;
    private List<Table> tableList;
    private OnTableClickListener listener;

    public interface OnTableClickListener {
        void onTableClick(Table table);
    }

    public TableAdapter(Context context, OnTableClickListener listener) {
        this.context = context;
        this.tableList = new ArrayList<>();
        this.listener = listener;
    }

    public void setTables(List<Table> tables) {
        this.tableList = tables;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_table, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        Table table = tableList.get(position);
        holder.tvTableNumber.setText("Table " + table.getTableNumber());
        holder.tvCapacity.setText("Capacity: " + table.getCapacity());
        
        String status = table.getStatus();
        holder.tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));

        // Set colors based on status
        int statusColor;
        int iconTint;

        switch (status.toLowerCase()) {
            case Constants.TABLE_STATUS_OCCUPIED:
                statusColor = ContextCompat.getColor(context, R.color.statusCancelled); // Red
                iconTint = ContextCompat.getColor(context, R.color.statusCancelled);
                break;
            case Constants.TABLE_STATUS_RESERVED:
                statusColor = ContextCompat.getColor(context, R.color.statusPending); // Orange
                iconTint = ContextCompat.getColor(context, R.color.statusPending);
                break;
            case Constants.TABLE_STATUS_AVAILABLE:
            default:
                statusColor = ContextCompat.getColor(context, R.color.statusReady); // Green
                iconTint = ContextCompat.getColor(context, R.color.statusReady);
                break;
        }

        holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));
        holder.ivTableIcon.setImageTintList(android.content.res.ColorStateList.valueOf(iconTint));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTableClick(table);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }

    static class TableViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableNumber, tvCapacity, tvStatus;
        ImageView ivTableIcon;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableNumber = itemView.findViewById(R.id.tvTableNumber);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivTableIcon = itemView.findViewById(R.id.ivTableIcon);
        }
    }
}