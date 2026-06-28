package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReorderRequest {
    private List<PositionEntry> positions;

    public ReorderRequest(List<PositionEntry> positions) {
        this.positions = positions;
    }

    public List<PositionEntry> getPositions() { return positions; }

    public static class PositionEntry {
        @SerializedName("member_id")
        private String memberId;
        private int position;

        public PositionEntry(String memberId, int position) {
            this.memberId = memberId;
            this.position = position;
        }

        public String getMemberId() { return memberId; }
        public int getPosition() { return position; }
    }
}
