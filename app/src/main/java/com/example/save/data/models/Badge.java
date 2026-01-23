package com.example.save.data.models;

public class Badge {
    private String name;
    private int iconResId;
    private boolean isUnlocked;

    public Badge(String name, int iconResId, boolean isUnlocked) {
        this.name = name;
        this.iconResId = iconResId;
        this.isUnlocked = isUnlocked;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }
}
