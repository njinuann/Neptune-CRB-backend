package com.neptune.crb.processing.model;

import java.math.BigDecimal;

public class CreditApplicationData {
    private String idNumber;
    private BigDecimal loanAmount;
    private String idType;

    public CreditApplicationData() {
    }

    public CreditApplicationData(String idNumber, BigDecimal loanAmount, String idType) {
        this.idNumber = idNumber;
        this.loanAmount = loanAmount;
        this.idType = idType;
    }


    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }


    @Override
    public String toString() {
        return "CreditApplicationData{" +
                "idNumber='" + idNumber + '\'' +
                ", loanAmount=" + loanAmount +
                ", idType='" + idType + '\'' +
                '}';
    }
}
