package com.example.restrurantmanagementsystem.auth;

public class User {
    public String role;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String role) {
        this.role = role;
    }
}