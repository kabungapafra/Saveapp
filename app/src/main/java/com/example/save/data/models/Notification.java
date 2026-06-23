package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class Notification {

    // Server fields (populated when fetched from API)
    @SerializedName("id")
    private String serverId;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("type")
    private String type;

    @SerializedName("is_read")
    private boolean isRead;

    @SerializedName("created_at")
    private String createdAt;

    // Local-only field for adapter keying; not from JSON
    private long localId;

    public Notification(String title, String message, String type) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = false;
        this.localId = System.currentTimeMillis();
    }

    // ---- getters / setters ----

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    /** Legacy long id — kept so existing adapter code compiles unchanged. */
    public long getId() { return localId; }
    public void setId(long id) { this.localId = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { this.isRead = read; }

    public String getCreatedAt() { return createdAt; }

    /** Returns epoch millis derived from createdAt string, or localId as fallback. */
    public long getTimestamp() {
        if (createdAt != null && !createdAt.isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                java.util.Date d = sdf.parse(createdAt);
                if (d != null) return d.getTime();
            } catch (Exception ignored) {}
            // Try ISO with microseconds
            try {
                String trimmed = createdAt.length() > 19 ? createdAt.substring(0, 19) : createdAt;
                java.text.SimpleDateFormat sdf2 =
                        new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                sdf2.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                java.util.Date d2 = sdf2.parse(trimmed);
                if (d2 != null) return d2.getTime();
            } catch (Exception ignored) {}
        }
        return localId;
    }

    public void setTimestamp(long ts) { this.localId = ts; }
}
