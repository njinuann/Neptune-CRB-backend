package com.neptune.crb.processing.model.creditInfo;

import lombok.Data;

@Data
public class IprsRequest {

    private CustomFields customFields;
    private boolean consent;

    @Data
    public static class CustomFields {
        private String identifierType;
        private String identifierNumber;
    }
}