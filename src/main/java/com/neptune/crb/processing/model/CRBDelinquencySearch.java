package com.neptune.crb.processing.model;

import java.math.BigDecimal;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 13-Apr-26
 */
public class CRBDelinquencySearch
{
    private String identityNumber;
    private BigDecimal loanAmount;
    private String identityType;

    public String getIdentityNumber()
    {
        return identityNumber;
    }

    public void setIdentityNumber(String identityNumber)
    {
        this.identityNumber = identityNumber;
    }

    public BigDecimal getLoanAmount()
    {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount)
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
}
