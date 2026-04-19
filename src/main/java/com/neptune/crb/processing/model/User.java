package com.neptune.crb.processing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

// @Data
//@NoArgsConstructor
//@AllArgsConstructor
public class User {
    private String userName;
    private String password;
    private String role;
    private String businessUnit;
    private String names;
    private String userRole;

    public User() {
    }

    public User(String userName, String password, String role, String businessUnit, String names,String userRole) {
        this.userName = userName;
        this.password = password;
        this.role = role;
        this.businessUnit = businessUnit;
        this.names = names;
        this.userRole = userRole;
    }

    @Override
    public String toString()
    {
        return "User{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", businessUnit='" + businessUnit + '\'' +
                ", names='" + names + '\'' +
                ", userRole='" + userRole + '\'' +
                '}';
    }

    public String getUserRole()
    {
        return userRole;
    }

    public void setUserRole(String userRole)
    {
        this.userRole = userRole;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }
}
