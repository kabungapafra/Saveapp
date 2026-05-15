package com.example.save.data.models;

public class Invitation {
    private String phone;
    private String date;
    private Status status;

    public enum Status {
        PENDING, EXPIRED
    }

    public Invitation(String phone, String date, Status status) {
        this.phone = phone;
        this.date = date;
        this.status = status;
    }

    public String getPhone() { return phone; }
    public String getDate() { return date; }
    public Status getStatus() { return status; }
}
