package com.eventmanagement.model;

public class User {

    private int userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String phone;
    private String role;
    private String staffDuty;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getRoleId() {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return 1;
        }
        if ("ORGANIZER".equalsIgnoreCase(role)) {
            return 2;
        }
        return 0;
    }

    public String getStaffDuty() {
        return staffDuty;
    }

    public void setRoleId(int role) {
        switch (role) {
            case 1:
                this.role = "ADMIN";
                break;
            case 2:
                this.role = "ORGANIZER";
                break;
            default:
                this.role = null;
                break;
        }
    }

    public void setStaffDuty(String staffDuty) {
        this.staffDuty = staffDuty;
    }
}