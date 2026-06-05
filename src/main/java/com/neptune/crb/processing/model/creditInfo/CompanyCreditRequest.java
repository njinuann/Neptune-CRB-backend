package com.neptune.crb.processing.model.creditInfo;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyCreditRequest {

    @NotBlank
    @JsonProperty("RegistrationNumber")
    private String registrationNumber;

    @NotNull
    @JsonProperty("Consent")
    private Boolean consent;
}
