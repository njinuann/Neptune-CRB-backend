package com.neptune.crb.processing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "crb.metropol")
public class CRBConfig {
    private String url;
    private String publicKey;
    private String privateKey;
    private int connectionTimeout = 30000;
    private int readTimeout = 30000;
    private String identityType = "001";
    private int reportType = 2;

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }

    public String getPrivateKey()
    {
        return privateKey;
    }

    public void setPrivateKey(String privateKey)
    {
        this.privateKey = privateKey;
    }

    public int getConnectionTimeout()
    {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout)
    {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout()
    {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout)
    {
        this.readTimeout = readTimeout;
    }

    public String getIdentityType()
    {
        return identityType;
    }

    public void setIdentityType(String identityType)
    {
        this.identityType = identityType;
    }

    public int getReportType()
    {
        return reportType;
    }

    public void setReportType(int reportType)
    {
        this.reportType = reportType;
    }
}
