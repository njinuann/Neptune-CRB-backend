package com.neptune.crb.processing.model;

import lombok.Data;

@Data
public class CreditScoreData {
//    private String metroScore;
//    private String ppi;
//    private String probabilityOfDefault;
//
//    public String getMetroScore() {
//        return metroScore;
//    }
//
//    public void setMetroScore(String metroScore) {
//        this.metroScore = metroScore;
//    }
//
//    public String getPpi() {
//        return ppi;
//    }
//
//    public void setPpi(String ppi) {
//        this.ppi = ppi;
//    }
//
//    public String getProbabilityOfDefault() {
//        return probabilityOfDefault;
//    }
//
//    public void setProbabilityOfDefault(String probabilityOfDefault) {
//        this.probabilityOfDefault = probabilityOfDefault;
//    }
// Core
// ===== Core Risk Metrics =====
private String metroScore;
    private String ppi;
    private String probabilityOfDefault;

    // ===== Identity =====
    private String customerName;
    private String registrationNumber;
    private String reportDate;
    private String referenceNumber;
    private String requestingInstitution;

    // ===== Credit Behavior =====
    private Integer totalAccounts;
    private Integer nonPerformingAccounts;
    private Integer performingWithDefaultHistory;
    private Integer performingCleanAccounts;

    // ===== Derived Fields =====
    private String ppiInterpretation;
    private String riskGrade;
}
