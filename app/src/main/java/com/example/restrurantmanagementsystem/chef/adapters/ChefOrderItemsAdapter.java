package com.example.restrurantmanagementsystem.chef.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.OrderItem;

import java.util.List;

public class ChefOrderItemsAdapter extends RecyclerView.Adapter<ChefOrderItemsAdapter.ViewHolder> {

    private final List<OrderItem> items;

    public ChefOrderItemsAdapter(List<OrderItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chef_order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = items.get(position);
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvItemName.setText(item.getMenuItemName());
        
        if (item.getSpecialInstructions() != null && !item.getSpecialInstructions().isEmpty()) {
            holder.tvSpecialInstructions.setVisibility(View.VISIBLE);
            holder.tvSpecialInstructions.setText("Notes: " + item.getSpecialInstructions());
        } else {
            holder.tvSpecialInstructions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuantity, tvItemName, tvSpecialInstructions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvSpecialInstructions = itemView.findViewById(R.id.tvSpecialInstructions);
        }
    }
}
