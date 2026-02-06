package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RepaymentScheduleResponse {
    @SerializedName("total_repayment")
    private double totalRepayment;

    @SerializedName("monthly_installment")
    private double monthlyInstallment;

    @SerializedName("interest_amount")
    private double interestAmount;

    @SerializedName("schedule")
    private List<RepaymentScheduleItem> schedule;

    public double getTotalRepayment() {
        return totalRepayment;
    }

    public void setTotalRepayment(double totalRepayment) {
        this.totalRepayment = totalRepayment;
    }

    public double getMonthlyInstallment() {
        return monthlyInstallment;
    }

    public void setMonthlyInstallment(double monthlyInstallment) {
        this.monthlyInstallment = monthlyInstallment;
    }

    public double getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(double interestAmount) {
        this.interestAmount = interestAmount;
    }

    public List<RepaymentScheduleItem> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<RepaymentScheduleItem> schedule) {
        this.schedule = schedule;
    }
}
