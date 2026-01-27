package com.example.save.data.network;

import java.util.List;
import java.util.Map;
import com.example.save.data.local.entities.TransactionEntity;

public class ReportResponse {
    private Map<String, Double> summary;
    private List<TransactionEntity> transactions;
    private Map<String, Integer> statistics;

    public Map<String, Double> getSummary() {
        return summary;
    }

    public void setSummary(Map<String, Double> summary) {
        this.summary = summary;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public Map<String, Integer> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, Integer> statistics) {
        this.statistics = statistics;
    }
}
