package com.example.save;

public class OnboardingItem {
    private int image;
    private String title;
    private String subtitle;

    public OnboardingItem(int image, String title, String subtitle) {
        this.image = image;
        this.title = title;
        this.subtitle = subtitle;
    }

    public int getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}