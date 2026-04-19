package com.neptune.crb.processing.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 13-Apr-26
 */
public class DelinquentCheckRequest
{
    @NotNull(message = "Credit App Id is required")
    private Long creditApplicationId;
    @NotBlank(message = "CRB vendor Id is required")
    private String crbVendorType;


    public @NotNull(message = "Credit App Id is required") Long getCreditApplicationId()
    {
        return creditApplicationId;
    }

    public void setCreditApplicationId(@NotNull(message = "Credit App Id is required") Long creditApplicationId)
    {
        this.creditApplicationId = creditApplicationId;
    }

    public @NotBlank(message = "CRB vendor Id is required") String getCrbVendorType()
    {
        return crbVendorType;
    }

    public void setCrbVendorType(@NotBlank(message = "CRB vendor Id is required") String crbVendorType)
    {
        this.crbVendorType = crbVendorType;
    }
}
