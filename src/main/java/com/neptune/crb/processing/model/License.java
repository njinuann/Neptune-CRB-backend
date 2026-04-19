package com.neptune.crb.processing.model;

import java.time.LocalDate;
import java.util.List;

public class License {
    private String licenseId;
    private String customerName;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private boolean perpetual = false;
    private List<String> features;
    private int maxUsers;
    private String version;
    private String signature;
    private String publicKey;

    // Constructors, getters, and setters
    public License() {}

    public License(String licenseId, String customerName, LocalDate expirationDate, boolean perpetual) {
        this.licenseId = licenseId;
        this.customerName = customerName;
        this.issueDate = LocalDate.now();
        this.expirationDate = expirationDate;
        this.perpetual = perpetual;
    }

    // Getters and setters for all fields
    public String getLicenseId() { return licenseId; }
    public void setLicenseId(String licenseId) { this.licenseId = licenseId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public boolean isPerpetual() { return perpetual; }
    public void setPerpetual(boolean perpetual) { this.perpetual = perpetual; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public int getMaxUsers() { return maxUsers; }
    public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    /**
     * Creates a string representation for signing
     */
    public String toSignableString() {
        return String.format("%s|%s|%s|%s|%b|%s|%d|%s",
                licenseId, customerName, issueDate, expirationDate,
                perpetual, String.join(",", features), maxUsers, version);
    }

    @Override
    public String toString()
    {
        return "License{" +
                "licenseId='" + licenseId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", issueDate=" + issueDate +
                ", expirationDate=" + expirationDate +
                ", perpetual=" + perpetual +
                ", features=" + features +
                ", maxUsers=" + maxUsers +
                ", version='" + version + '\'' +
                ", signature='" + signature + '\'' +
                ", publicKey='" + publicKey + '\'' +
                '}';
    }
}