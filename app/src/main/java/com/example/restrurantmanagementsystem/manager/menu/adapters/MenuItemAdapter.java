package com.example.restrurantmanagementsystem.manager.menu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder> {

    private Context context;
    private List<MenuItem> menuItemList;
    private List<MenuItem> menuItemListFull; // For search functionality
    private OnMenuItemClickListener listener;

    public interface OnMenuItemClickListener {
        void onEditClick(MenuItem menuItem);
        void onDeleteClick(MenuItem menuItem);
        void onAvailabilityChanged(MenuItem menuItem, boolean isAvailable);
    }

    public MenuItemAdapter(Context context, OnMenuItemClickListener listener) {
        this.context = context;
        this.menuItemList = new ArrayList<>();
        this.menuItemListFull = new ArrayList<>();
        this.listener = listener;
    }

    public void setMenuItems(List<MenuItem> items) {
        this.menuItemList = items;
        this.menuItemListFull = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public void filterByCategory(String categoryId) {
        if (categoryId == null || categoryId.isEmpty() || categoryId.equals("all")) {
            menuItemList = new ArrayList<>(menuItemListFull);
        } else {
            menuItemList = new ArrayList<>();
            for (MenuItem item : menuItemListFull) {
                if (item.getCategoryId().equals(categoryId)) {
                    menuItemList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void filter(String query) {
        menuItemList = new ArrayList<>();
        if (query.isEmpty()) {
            menuItemList.addAll(menuItemListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (MenuItem item : menuItemListFull) {
                if (item.getName().toLowerCase().contains(lowerCaseQuery) ||
                        item.getDescription().toLowerCase().contains(lowerCaseQuery) ||
                        item.getCategoryName().toLowerCase().contains(lowerCaseQuery)) {
                    menuItemList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu_item, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        MenuItem menuItem = menuItemList.get(position);
        holder.bind(menuItem);
    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivEdit, ivDelete;
        TextView tvName, tvPrice, tvCategory, tvDescription, tvPrepTime, tvUnavailable;
        SwitchCompat switchAvailability;
        View viewOverlay;

        public MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivMenuItemImage);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvName = itemView.findViewById(R.id.tvMenuItemName);
            tvPrice = itemView.findViewById(R.id.tvMenuItemPrice);
            tvCategory = itemView.findViewById(R.id.tvMenuItemCategory);
            tvDescription = itemView.findViewById(R.id.tvMenuItemDescription);
            tvPrepTime = itemView.findViewById(R.id.tvPrepTime);
            tvUnavailable = itemView.findViewById(R.id.tvUnavailable);
            switchAvailability = itemView.findViewById(R.id.switchAvailability);
            viewOverlay = itemView.findViewById(R.id.viewUnavailableOverlay);
        }

        public void bind(MenuItem menuItem) {
            // Set item details
            tvName.setText(menuItem.getName());
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", menuItem.getPrice()));
            tvCategory.setText(menuItem.getCategoryName());
            tvDescription.setText(menuItem.getDescription());
            tvPrepTime.setText(menuItem.getPreparationTime() + " min");

            // Load image
            if (menuItem.getImageUrl() != null && !menuItem.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(menuItem.getImageUrl())
                        .placeholder(R.drawable.card_background)
                        .error(R.drawable.card_background)
                        .centerCrop()
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.card_background);
            }

            // Set availability
            switchAvailability.setChecked(menuItem.isAvailable());
            if (!menuItem.isAvailable()) {
                viewOverlay.setVisibility(View.VISIBLE);
                tvUnavailable.setVisibility(View.VISIBLE);
            } else {
                viewOverlay.setVisibility(View.GONE);
                tvUnavailable.setVisibility(View.GONE);
            }

            // Click listeners
            ivEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(menuItem);
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(menuItem);
                }
            });

            switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null && buttonView.isPressed()) {
                    listener.onAvailabilityChanged(menuItem, isChecked);
                }
            });
        }
    }
}