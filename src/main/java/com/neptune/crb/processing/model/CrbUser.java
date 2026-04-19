package com.neptune.crb.processing.model;

import java.time.LocalDateTime;

public class CrbUser
{
    private String loginId;
    private String userName;
    private String role;
    private String crbUser;
    private String status;

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    public CrbUser() {}

    public CrbUser(String loginId, String userName, String role,String crbUser,String status, LocalDateTime createdDate, LocalDateTime lastModifiedDate)
    {
        this.loginId = loginId;
        this.userName = userName;
        this.role = role;
        this.crbUser = crbUser;
        this.status = status;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLoginId()
    {
        return loginId;
    }

    public void setLoginId(String loginId)
    {
        this.loginId = loginId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getCrbUser()
    {
        return crbUser;
    }

    public void setCrbUser(String crbUser)
    {
        this.crbUser = crbUser;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public LocalDateTime getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate)
    {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModifiedDate()
    {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate)
    {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public String toString()
    {
        return "CrbUser{" +
                "loginId='" + loginId + '\'' +
                ", userName='" + userName + '\'' +
                ", role='" + role + '\'' +
                ", crbUser='" + crbUser + '\'' +
                ", status='" + status + '\'' +
                ", createdDate=" + createdDate +
                ", lastModifiedDate=" + lastModifiedDate +
                '}';
    }
}
