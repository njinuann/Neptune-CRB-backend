package com.neptune.crb.processing.model;

// SearchFilter.java
public class SearchFilter {
    private String field;
    private String value;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SearchFilter{" +
                "field='" + field + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
