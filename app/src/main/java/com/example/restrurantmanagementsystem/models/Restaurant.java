package com.example.restrurantmanagementsystem.models;

public class Restaurant {
    public String id;
    public String name;
    public String ownerId;

    public Restaurant() {
        // Default constructor required for calls to DataSnapshot.getValue(Restaurant.class)
    }

    public Restaurant(String id, String name, String ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
    }
}