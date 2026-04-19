package com.neptune.crb.processing.model;

import lombok.Data;
import java.time.LocalDateTime;


public class CRBDelinquencyRecord {
    private Long id;
    private String transactionId;
    private String identityNumber;
    private String identityType;
    private Integer loanAmount;
    private String apiCode;
    private String apiCodeDescription;
    private String delinquencyCode;
    private String delinquencyDescription;
    private String delinquencySummary;
    private String listingStatus;
    private Boolean hasError;
    private String rawRequest;
    private String rawResponse;
    private String userId;
    private LocalDateTime queryTime;
    private Integer responseTimeMs;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }

    public String getIdentityNumber()
    {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber)
    {
        this.identityNumber = identityNumber;
    }

    public String getIdentityType()
    {
        return identityType;
    }

    public void setIdentityType(String identityType)
    {
        this.identityType = identityType;
    }

    public Integer getLoanAmount()
    {
        return loanAmount;
    }

    public void setLoanAmount(Integer loanAmount)
    {
        this.loanAmount = loanAmount;
    }

    public String getApiCode()
    {
        return apiCode;
    }

    public void setApiCode(String apiCode)
    {
        this.apiCode = apiCode;
    }

    public String getApiCodeDescription()
    {
        return apiCodeDescription;
    }

    public void setApiCodeDescription(String apiCodeDescription)
    {
        this.apiCodeDescription = apiCodeDescription;
    }

    public String getDelinquencyCode()
    {
        return delinquencyCode;
    }

    public void setDelinquencyCode(String delinquencyCode)
    {
        this.delinquencyCode = delinquencyCode;
    }

    public String getDelinquencyDescription()
    {
        return delinquencyDescription;
    }

    public void setDelinquencyDescription(String delinquencyDescription)
    {
        this.delinquencyDescription = delinquencyDescription;
    }

    public String getDelinquencySummary()
    {
        return delinquencySummary;
    }

    public void setDelinquencySummary(String delinquencySummary)
    {
        this.delinquencySummary = delinquencySummary;
    }

    public String getListingStatus()
    {
        return listingStatus;
    }

    public void setListingStatus(String listingStatus)
    {
        this.listingStatus = listingStatus;
    }

    public Boolean getHasError()
    {
        return hasError;
    }

    public void setHasError(Boolean hasError)
    {
        this.hasError = hasError;
    }

    public String getRawRequest()
    {
        return rawRequest;
    }

    public void setRawRequest(String rawRequest)
    {
        this.rawRequest = rawRequest;
    }

    public String getRawResponse()
    {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse)
    {
        this.rawResponse = rawResponse;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public LocalDateTime getQueryTime()
    {
        return queryTime;
    }

    public void setQueryTime(LocalDateTime queryTime)
    {
        this.queryTime = queryTime;
    }

    public Integer getResponseTimeMs()
    {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Integer responseTimeMs)
    {
        this.responseTimeMs = responseTimeMs;
    }
}
