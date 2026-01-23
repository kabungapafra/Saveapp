package com.example.save.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SyncManager {

    private static SyncManager instance;
    private final Context context;
    private final SharedPreferences prefs;
    private static final String PREF_NAME = "SyncQueue";
    private static final String KEY_PENDING_OPERATIONS = "pending_operations";

    public static class PendingOperation {
        public String type; // "CONTRIBUTION", "LOAN_REQUEST", "LOAN_REPAYMENT", etc.
        public String data; // JSON data
        public long timestamp;

        public PendingOperation(String type, String data) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        public PendingOperation(String type, String data, long timestamp) {
            this.type = type;
            this.data = data;
            this.timestamp = timestamp;
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("type", type);
            json.put("data", data);
            json.put("timestamp", timestamp);
            return json;
        }

        public static PendingOperation fromJSON(JSONObject json) throws JSONException {
            return new PendingOperation(
                    json.getString("type"),
                    json.getString("data"),
                    json.getLong("timestamp"));
        }
    }

    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) {
            instance = new SyncManager(context);
        }
        return instance;
    }

    /**
     * Add an operation to the sync queue
     */
    public void queueOperation(String type, String data) {
        List<PendingOperation> operations = getPendingOperations();
        operations.add(new PendingOperation(type, data));
        savePendingOperations(operations);
    }

    /**
     * Get all pending operations
     */
    public List<PendingOperation> getPendingOperations() {
        String jsonString = prefs.getString(KEY_PENDING_OPERATIONS, null);
        List<PendingOperation> operations = new ArrayList<>();

        if (jsonString == null) {
            return operations;
        }

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                operations.add(PendingOperation.fromJSON(jsonObject));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return operations;
    }

    /**
     * Save pending operations
     */
    private void savePendingOperations(List<PendingOperation> operations) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (PendingOperation operation : operations) {
                jsonArray.put(operation.toJSON());
            }
            prefs.edit().putString(KEY_PENDING_OPERATIONS, jsonArray.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear a specific operation from the queue
     */
    public void removeOperation(PendingOperation operation) {
        List<PendingOperation> operations = getPendingOperations();
        List<PendingOperation> toRemove = new ArrayList<>();

        // Find matching operations
        for (PendingOperation op : operations) {
            if (op.type.equals(operation.type) &&
                    op.data.equals(operation.data) &&
                    op.timestamp == operation.timestamp) {
                toRemove.add(op);
            }
        }

        operations.removeAll(toRemove);
        savePendingOperations(operations);
    }

    /**
     * Clear all pending operations
     */
    public void clearAllOperations() {
        prefs.edit().remove(KEY_PENDING_OPERATIONS).apply();
    }

    /**
     * Get count of pending operations
     */
    public int getPendingCount() {
        return getPendingOperations().size();
    }

    /**
     * Sync all pending operations when online
     */
    public void syncPendingOperations(SyncCallback callback) {
        List<PendingOperation> operations = getPendingOperations();

        if (operations.isEmpty()) {
            callback.onSyncComplete(0, 0);
            return;
        }

        // Process each operation
        int successCount = 0;
        int failureCount = 0;

        for (PendingOperation operation : new ArrayList<>(operations)) {
            try {
                // Here you would implement the actual sync logic
                // For now, we'll just simulate success
                boolean success = processSyncOperation(operation);

                if (success) {
                    removeOperation(operation);
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception e) {
                failureCount++;
            }
        }

        callback.onSyncComplete(successCount, failureCount);
    }

    /**
     * Process a single sync operation
     * Override this method to implement actual sync logic
     */
    protected boolean processSyncOperation(PendingOperation operation) {
        // Implement actual sync logic based on operation type
        // This is a placeholder that should be overridden
        return true;
    }

    public interface SyncCallback {
        void onSyncComplete(int successCount, int failureCount);
    }
}
