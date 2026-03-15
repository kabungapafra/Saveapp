package com.example.save.data.models;

public class Invitation {
    private String email;
    private String date;
    private Status status;

    public enum Status {
        PENDING, EXPIRED
    }

    public Invitation(String email, String date, Status status) {
        this.email = email;
        this.date = date;
        this.status = status;
    }

    public String getEmail() { return email; }
    public String getDate() { return date; }
    public Status getStatus() { return status; }
}
