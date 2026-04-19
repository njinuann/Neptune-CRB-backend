package com.neptune.crb.processing.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;


public class CRBDelinquencyRequest {
    @NotBlank(message = "Identity number is required")
    private String identityNumber;

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be positive")
    private Integer loanAmount;

    private String identityType;

    public @NotBlank(message = "Identity number is required") String getIdentityNumber()
    {
        return identityNumber;
    }

    public void setIdentityNumber(@NotBlank(message = "Identity number is required") String identityNumber)
    {
        this.identityNumber = identityNumber;
    }

    public @NotNull(message = "Loan amount is required") @Positive(message = "Loan amount must be positive") Integer getLoanAmount()
    {
        return loanAmount;
    }

    public void setLoanAmount(@NotNull(message = "Loan amount is required") @Positive(message = "Loan amount must be positive") Integer loanAmount)
    {
        this.loanAmount = loanAmount;
    }

    public String getIdentityType()
    {
        return identityType;
    }

    public void setIdentityType(String identityType)
    {
        this.identityType = identityType;
    }

    @Override
    public String toString()
    {
        return "CRBDelinquencyRequest{" +
                "identityNumber='" + identityNumber + '\'' +
                ", loanAmount=" + loanAmount +
                ", identityType='" + identityType + '\'' +
                '}';
    }
}