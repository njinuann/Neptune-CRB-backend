package com.neptune.crb.processing.model;

public class UserDto {

    private String loginId;
    private String names;
    private String role;

    public UserDto() {
    }

    public UserDto(String loginId, String names, String role) {
        this.loginId = loginId;
        this.names = names;
        this.role = role;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}