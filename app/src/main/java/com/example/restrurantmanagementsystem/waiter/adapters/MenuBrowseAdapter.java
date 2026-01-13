package com.example.restrurantmanagementsystem.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuBrowseAdapter extends RecyclerView.Adapter<MenuBrowseAdapter.MenuViewHolder> {

    private Context context;
    private List<MenuItem> menuItemList;
    private List<MenuItem> filteredList;
    private OnMenuClickListener listener;
    private Map<String, Integer> cartQuantities;

    public interface OnMenuClickListener {
        void onAddToCart(MenuItem item, int quantity);
        void onUpdateQuantity(MenuItem item, int quantity);
    }

    public MenuBrowseAdapter(Context context, OnMenuClickListener listener) {
        this.context = context;
        this.menuItemList = new ArrayList<>();
        this.filteredList = new ArrayList<>();
        this.listener = listener;
        this.cartQuantities = new HashMap<>();
    }

    public void setMenuItems(List<MenuItem> items) {
        this.menuItemList = items;
        this.filteredList = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(menuItemList);
        } else {
            for (MenuItem item : menuItemList) {
                if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filterByCategory(String categoryId) {
        filteredList.clear();
        if (categoryId.equals("all")) {
            filteredList.addAll(menuItemList);
        } else {
            for (MenuItem item : menuItemList) {
                if (item.getCategoryId().equals(categoryId)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void updateCartQuantities(Map<String, Integer> quantities) {
        this.cartQuantities = quantities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu_browse, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = filteredList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvDescription.setText(item.getDescription());
        holder.tvPrice.setText(String.format("$%.2f", item.getPrice()));

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context).load(item.getImageUrl()).into(holder.ivItem);
        } else {
            holder.ivItem.setImageResource(R.drawable.placeholder_food);
        }

        Integer quantity = cartQuantities.get(item.getId());
        if (quantity != null && quantity > 0) {
            holder.btnAdd.setVisibility(View.GONE);
            holder.llQuantity.setVisibility(View.VISIBLE);
            holder.tvQuantity.setText(String.valueOf(quantity));
        } else {
            holder.btnAdd.setVisibility(View.VISIBLE);
            holder.llQuantity.setVisibility(View.GONE);
        }

        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(item, 1);
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            if (listener != null) {
                int newQty = (quantity != null ? quantity : 0) + 1;
                listener.onUpdateQuantity(item, newQty);
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (listener != null && quantity != null && quantity > 0) {
                listener.onUpdateQuantity(item, quantity - 1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItem;
        TextView tvName, tvDescription, tvPrice, tvQuantity;
        Button btnAdd;
        LinearLayout llQuantity;
        ImageButton btnPlus, btnMinus;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItem = itemView.findViewById(R.id.ivMenuItem);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvDescription = itemView.findViewById(R.id.tvItemDescription);
            tvPrice = itemView.findViewById(R.id.tvItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            llQuantity = itemView.findViewById(R.id.llQuantityControl);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}