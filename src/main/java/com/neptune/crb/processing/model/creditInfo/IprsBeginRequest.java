package com.neptune.crb.processing.model.creditInfo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class IprsBeginRequest {

//    private IdentifierType identifierType;
//
//    private String identifierNumber;
//
//    private Boolean consent;
@NotNull
@JsonProperty("IdentifierType")
private IdentifierType identifierType;

    @NotBlank
    @JsonProperty("IdentifierNumber")
    private String identifierNumber;

    @NotNull
    @JsonProperty("Consent")
    private Boolean consent;
}