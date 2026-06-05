package com.neptune.crb.processing.model.creditInfo;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IndividualCreditRequest {

    private String nationalId;
    private String alienId;
    private String serviceId;
    private String passportNumber;

    @NotNull
    private Boolean consent;
}
