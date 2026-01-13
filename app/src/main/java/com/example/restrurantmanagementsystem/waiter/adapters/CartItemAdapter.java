package com.example.restrurantmanagementsystem.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartViewHolder> {

    private Context context;
    private List<OrderItem> cartItems;
    private OnCartItemClickListener listener;

    public interface OnCartItemClickListener {
        void onUpdateQuantity(OrderItem item, int quantity);
        void onRemoveItem(OrderItem item);
    }

    public CartItemAdapter(Context context, OnCartItemClickListener listener) {
        this.context = context;
        this.cartItems = new ArrayList<>();
        this.listener = listener;
    }

    public void setCartItems(List<OrderItem> items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        OrderItem item = cartItems.get(position);
        holder.tvName.setText(item.getMenuItemName());
        holder.tvPrice.setText(String.format("$%.2f", item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvSubtotal.setText(String.format("$%.2f", item.getSubtotal()));

        if (item.getSpecialInstructions() != null && !item.getSpecialInstructions().isEmpty()) {
            holder.tvInstructions.setVisibility(View.VISIBLE);
            holder.tvInstructions.setText("Notes: " + item.getSpecialInstructions());
        } else {
            holder.tvInstructions.setVisibility(View.GONE);
        }

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context).load(item.getImageUrl()).into(holder.ivItem);
        } else {
            holder.ivItem.setImageResource(R.drawable.placeholder_food);
        }

        holder.btnPlus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateQuantity(item, item.getQuantity() + 1);
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (listener != null && item.getQuantity() > 1) {
                listener.onUpdateQuantity(item, item.getQuantity() - 1);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveItem(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItem;
        TextView tvName, tvPrice, tvQuantity, tvSubtotal, tvInstructions;
        ImageButton btnPlus, btnMinus, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItem = itemView.findViewById(R.id.ivCartItem);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            tvInstructions = itemView.findViewById(R.id.tvInstructions);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}