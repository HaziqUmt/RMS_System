package com.example.restrurantmanagementsystem.models;

import java.io.Serializable;

public class Category implements Serializable {
    private String id;
    private String name;
    private int displayOrder;
    private String restaurantId;
    private long createdAt;

    // Empty constructor required for Firebase
    public Category() {
    }

    // Constructor
    public Category(String id, String name, int displayOrder, String restaurantId) {
        this.id = id;
        this.name = name;
        this.displayOrder = displayOrder;
        this.restaurantId = restaurantId;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}