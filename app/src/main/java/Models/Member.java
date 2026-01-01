package Models;

public class Member {
    private String name;
    private String role;
    private boolean isActive;
    private String phone;
    private String email;

    public Member(String name, String role, boolean isActive) {
        this(name, role, isActive, "0700000000", "email@example.com");
    }

    public Member(String name, String role, boolean isActive, String phone, String email) {
        this.name = name;
        this.role = role;
        this.isActive = isActive;
        this.phone = phone;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
