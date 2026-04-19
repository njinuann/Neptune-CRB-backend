package com.neptune.crb.processing.model;

public class CoreUser {
    private String loginId;
    private String fullName;

    // Constructors, getters, and setters
    public CoreUser() {}

    public CoreUser(String loginId, String fullName) {
        this.loginId = loginId;
        this.fullName = fullName;
    }

    public String getLoginId()
    {
        return loginId;
    }

    public void setLoginId(String loginId)
    {
        this.loginId = loginId;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }
}

