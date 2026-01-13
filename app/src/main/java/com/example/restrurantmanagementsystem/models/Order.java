package com.example.restrurantmanagementsystem.models;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private String id;
    private String restaurantId;
    private String tableId;
    private int tableNumber;
    private String waiterId;
    private String waiterName;
    private String status; // pending, in_progress, ready, served, cancelled
    private List<OrderItem> items;
    private double totalAmount;
    private double tax;
    private double subtotal;
    private String specialNotes;
    private long orderTime;
    private long readyTime;
    private long servedTime;

    public Order() {
    }

    public Order(String id, String restaurantId, String tableId, int tableNumber, String waiterId, String waiterName, String status, List<OrderItem> items, double totalAmount, double tax, double subtotal, String specialNotes, long orderTime) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.waiterId = waiterId;
        this.waiterName = waiterName;
        this.status = status;
        this.items = items;
        this.totalAmount = totalAmount;
        this.tax = tax;
        this.subtotal = subtotal;
        this.specialNotes = specialNotes;
        this.orderTime = orderTime;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRestaurantId() { return restaurantId; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }
    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }
    public String getWaiterId() { return waiterId; }
    public void setWaiterId(String waiterId) { this.waiterId = waiterId; }
    public String getWaiterName() { return waiterName; }
    public void setWaiterName(String waiterName) { this.waiterName = waiterName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String specialNotes) { this.specialNotes = specialNotes; }
    public long getOrderTime() { return orderTime; }
    public void setOrderTime(long orderTime) { this.orderTime = orderTime; }
    public long getReadyTime() { return readyTime; }
    public void setReadyTime(long readyTime) { this.readyTime = readyTime; }
    public long getServedTime() { return servedTime; }
    public void setServedTime(long servedTime) { this.servedTime = servedTime; }
}
