package com.example.restrurantmanagementsystem.chef.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Order;
import com.example.restrurantmanagementsystem.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ChefOrdersAdapter extends RecyclerView.Adapter<ChefOrdersAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orders;
    private final OnOrderStatusChangeListener listener;

    public interface OnOrderStatusChangeListener {
        void onStatusChange(Order order, String newStatus);
    }

    public ChefOrdersAdapter(Context context, OnOrderStatusChangeListener listener) {
        this.context = context;
        this.orders = new ArrayList<>();
        this.listener = listener;
    }

    public void setOrders(List<Order> newOrders) {
        orders.clear();
        orders.addAll(newOrders);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chef_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableNumber, tvOrderId, tvOrderTime, tvSpecialNotes;
        RecyclerView rvItems;
        LinearLayout llNotes;
        Button btnAction;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableNumber = itemView.findViewById(R.id.tvTableNumber);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvSpecialNotes = itemView.findViewById(R.id.tvSpecialNotes);
            rvItems = itemView.findViewById(R.id.recyclerViewOrderItems);
            llNotes = itemView.findViewById(R.id.llNotes);
            btnAction = itemView.findViewById(R.id.btnStatusAction);
        }

        public void bind(Order order) {
            tvTableNumber.setText("Table " + order.getTableNumber());
            tvOrderId.setText("Order #" + (order.getId().length() > 8 ? order.getId().substring(0, 8).toUpperCase() : order.getId()));
            
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(order.getOrderTime(), 
                    System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            tvOrderTime.setText(relativeTime);

            if (order.getSpecialNotes() != null && !order.getSpecialNotes().isEmpty()) {
                llNotes.setVisibility(View.VISIBLE);
                tvSpecialNotes.setText(order.getSpecialNotes());
            } else {
                llNotes.setVisibility(View.GONE);
            }

            // Inner RecyclerView for items
            ChefOrderItemsAdapter itemsAdapter = new ChefOrderItemsAdapter(order.getItems());
            rvItems.setLayoutManager(new LinearLayoutManager(context));
            rvItems.setAdapter(itemsAdapter);

            // Button Action based on status
            String status = order.getStatus();
            if (Constants.ORDER_STATUS_PENDING.equals(status)) {
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setText("START COOKING");
                btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.colorPrimary)));
                btnAction.setOnClickListener(v -> listener.onStatusChange(order, Constants.ORDER_STATUS_IN_PROGRESS));
            } else if (Constants.ORDER_STATUS_IN_PROGRESS.equals(status)) {
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setText("MARK AS READY");
                btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(context.getColor(R.color.colorAccent)));
                btnAction.setOnClickListener(v -> listener.onStatusChange(order, Constants.ORDER_STATUS_READY));
            } else {
                btnAction.setVisibility(View.GONE);
            }
        }
    }
}
