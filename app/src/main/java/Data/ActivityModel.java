package Data;

import java.util.Date;

public class ActivityModel {
    private String title;
    private String date;
    private double amount;
    private boolean isPositive;

    public ActivityModel(String title, String date, double amount, boolean isPositive) {
        this.title = title;
        this.date = date;
        this.amount = amount;
        this.isPositive = isPositive;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isPositive() {
        return isPositive;
    }
}
