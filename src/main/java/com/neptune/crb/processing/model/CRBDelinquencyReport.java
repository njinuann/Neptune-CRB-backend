package com.neptune.crb.processing.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CRBDelinquencyReport {
 private String apiCode;
 private String apiCodeDescription;
 private String delinquencyCode;
 private String delinquencyDescription;
 private String delinquencySummary;
 private Boolean hasError;
 private String identityNumber;
 private String identityType;
 private String identityTypeDescription;
 private Long loanAmount;
 private String transactionId;
 private String listingStatus;
 private String creditScore;
 private String hasFraud;
 private String isGuarantor;
 private boolean isListed;

 public CRBDelinquencyReport()
 {
 }

 public CRBDelinquencyReport(String apiCode, String apiCodeDescription, String delinquencyCode, String delinquencyDescription, String delinquencySummary, Boolean hasError, String identityNumber, String identityType, String identityTypeDescription, Long loanAmount, String transactionId, String listingStatus,String creditScore, String isGuarantor,String hasFraud,boolean isListed)
 {
  this.apiCode = apiCode;
  this.apiCodeDescription = apiCodeDescription;
  this.delinquencyCode = delinquencyCode;
  this.delinquencyDescription = delinquencyDescription;
  this.delinquencySummary = delinquencySummary;
  this.hasError = hasError;
  this.identityNumber = identityNumber;
  this.identityType = identityType;
  this.identityTypeDescription = identityTypeDescription;
  this.loanAmount = loanAmount;
  this.transactionId = transactionId;
  this.listingStatus = listingStatus;
  this.creditScore =creditScore;
  this.isGuarantor =isGuarantor;
  this.hasFraud =hasFraud;
  this.isListed = isListed;
 }

 public boolean getIsListed()
 {
  return isListed;
 }

 public void setIsListed(boolean isListed)
 {
  this.isListed = isListed;
 }

 public String getCreditScore()
 {
  return creditScore;
 }

 public void setCreditScore(String creditScore)
 {
  this.creditScore = creditScore;
 }

 public String getHasFraud()
 {
  return hasFraud;
 }

 public void setHasFraud(String hasFraud)
 {
  this.hasFraud = hasFraud;
 }

 public String getIsGuarantor()
 {
  return isGuarantor;
 }

 public void setIsGuarantor(String isGuarantor)
 {
  this.isGuarantor = isGuarantor;
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

 public Boolean getHasError()
 {
  return hasError;
 }

 public void setHasError(Boolean hasError)
 {
  this.hasError = hasError;
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

 public String getIdentityTypeDescription()
 {
  return identityTypeDescription;
 }

 public void setIdentityTypeDescription(String identityTypeDescription)
 {
  this.identityTypeDescription = identityTypeDescription;
 }

 public Long getLoanAmount()
 {
  return loanAmount;
 }

 public void setLoanAmount(Long loanAmount)
 {
  this.loanAmount = loanAmount;
 }

 public String getTransactionId()
 {
  return transactionId;
 }

 public void setTransactionId(String transactionId)
 {
  this.transactionId = transactionId;
 }

 public String getListingStatus()
 {
  return listingStatus;
 }

 public void setListingStatus(String listingStatus)
 {
  this.listingStatus = listingStatus;
 }

 @Override
 public String toString()
 {
  return "CRBDelinquencyReport{" +
          "apiCode='" + apiCode + '\'' +
          ", apiCodeDescription='" + apiCodeDescription + '\'' +
          ", delinquencyCode='" + delinquencyCode + '\'' +
          ", delinquencyDescription='" + delinquencyDescription + '\'' +
          ", delinquencySummary='" + delinquencySummary + '\'' +
          ", hasError=" + hasError +
          ", identityNumber='" + identityNumber + '\'' +
          ", identityType='" + identityType + '\'' +
          ", identityTypeDescription='" + identityTypeDescription + '\'' +
          ", loanAmount=" + loanAmount +
          ", transactionId='" + transactionId + '\'' +
          ", listingStatus='" + listingStatus + '\'' +
          '}';
 }


}
