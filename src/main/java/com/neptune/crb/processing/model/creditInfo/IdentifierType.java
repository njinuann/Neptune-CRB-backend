package com.neptune.crb.processing.model.creditInfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IdentifierType {

    NATIONAL_ID("national_id"),
    KRAPIN("krapin"),
    PLATE("plate"),
    DRIVING_LICENSE("dl"),
    PHONE("phone"),
    BUSINESS("business"),
    BANK("bank");

    private final String value;

    IdentifierType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static IdentifierType fromValue(String value) {
        for (IdentifierType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid IdentifierType: " + value);
    }
}
