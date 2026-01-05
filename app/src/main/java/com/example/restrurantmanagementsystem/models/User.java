package com.example.restrurantmanagementsystem.models;

public class User {
    public String uid;
    public String name;
    public String email;
    public String role;
    public String restaurantId;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String name, String email, String role, String restaurantId) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.role = role;
        this.restaurantId = restaurantId;
    }
}