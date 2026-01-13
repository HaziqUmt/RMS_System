package com.example.restrurantmanagementsystem.waiter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.restrurantmanagementsystem.R;
import com.example.restrurantmanagementsystem.models.Order;
import com.example.restrurantmanagementsystem.models.OrderItem;
import com.example.restrurantmanagementsystem.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WaiterOrderAdapter extends RecyclerView.Adapter<WaiterOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private List<Order> filteredList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public WaiterOrderAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = new ArrayList<>();
        this.filteredList = new ArrayList<>();
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orderList = orders;
        this.filteredList = new ArrayList<>(orders);
        notifyDataSetChanged();
    }

    public void filterByStatus(String status) {
        filteredList.clear();
        if (status.equals("All")) {
            filteredList.addAll(orderList);
        } else {
            for (Order order : orderList) {
                if (order.getStatus().equalsIgnoreCase(status)) {
                    filteredList.add(order);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_waiter_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = filteredList.get(position);
        holder.tvTableNumber.setText("Table " + order.getTableNumber());
        holder.tvOrderTotal.setText(String.format("$%.2f", order.getTotalAmount()));
        
        // Items summary
        StringBuilder itemsSummary = new StringBuilder();
        for (int i = 0; i < order.getItems().size(); i++) {
            OrderItem item = order.getItems().get(i);
            itemsSummary.append(item.getQuantity()).append("x ").append(item.getMenuItemName());
            if (i < order.getItems().size() - 1) {
                itemsSummary.append(", ");
            }
        }
        holder.tvOrderItems.setText(itemsSummary.toString());

        // Status badge
        String status = order.getStatus();
        holder.tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1).replace("_", " "));
        
        int statusColor;
        switch (status) {
            case Constants.ORDER_STATUS_PENDING:
                statusColor = ContextCompat.getColor(context, R.color.statusPending);
                break;
            case Constants.ORDER_STATUS_IN_PROGRESS:
                statusColor = ContextCompat.getColor(context, R.color.statusInProgress);
                break;
            case Constants.ORDER_STATUS_READY:
                statusColor = ContextCompat.getColor(context, R.color.statusReady);
                break;
            case Constants.ORDER_STATUS_SERVED:
                statusColor = ContextCompat.getColor(context, R.color.statusServed);
                break;
            default:
                statusColor = ContextCompat.getColor(context, R.color.textColorSecondary);
                break;
        }
        holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));

        // Time
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        holder.tvOrderTime.setText(sdf.format(new Date(order.getOrderTime())));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTableNumber, tvStatus, tvOrderItems, tvOrderTime, tvOrderTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTableNumber = itemView.findViewById(R.id.tvTableNumber);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}