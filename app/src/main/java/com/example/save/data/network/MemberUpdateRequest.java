package com.example.save.data.network;

public class MemberUpdateRequest {
    private String name;
    private String role;
    private Boolean is_active;
    private String profile_image;

    public MemberUpdateRequest(String name, String role, Boolean is_active) {
        this.name = name;
        this.role = role;
        this.is_active = is_active;
    }

    /** Build a request that updates only the member's avatar (base64). */
    public static MemberUpdateRequest forProfileImage(String base64) {
        MemberUpdateRequest req = new MemberUpdateRequest(null, null, null);
        req.profile_image = base64;
        return req;
    }

    public String getName() { return name; }
    public String getRole() { return role; }
    public Boolean getIsActive() { return is_active; }
    public String getProfileImage() { return profile_image; }
    public void setProfileImage(String profile_image) { this.profile_image = profile_image; }
}
