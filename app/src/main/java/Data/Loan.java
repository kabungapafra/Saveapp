package Data;

import java.util.Date;

public class Loan {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_PAID = "PAID";

    private String id;
    private String memberId;
    private String memberName;
    private double amount;
    private double interest;
    private String reason;
    private Date dateRequested;
    private Date dueDate;
    private String status;
    private double repaidAmount;

    public Loan(String id, String memberId, String memberName, double amount, double interest, String reason,
            Date dateRequested, String status) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.amount = amount;
        this.interest = interest;
        this.reason = reason;
        this.dateRequested = dateRequested;
        this.status = status;
        this.repaidAmount = 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public double getAmount() {
        return amount;
    }

    public double getInterest() {
        return interest;
    }

    public String getReason() {
        return reason;
    }

    public Date getDateRequested() {
        return dateRequested;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getRepaidAmount() {
        return repaidAmount;
    }

    public void setRepaidAmount(double repaidAmount) {
        this.repaidAmount = repaidAmount;
    }

    public double getTotalDue() {
        return amount + interest;
    }

    public int getRepaymentProgress() {
        if (getTotalDue() == 0)
            return 0;
        return (int) ((repaidAmount / getTotalDue()) * 100);
    }
}
