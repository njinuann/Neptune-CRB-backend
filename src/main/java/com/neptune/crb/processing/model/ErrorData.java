package com.neptune.crb.processing.model;

import lombok.Data;

@Data
public class ErrorData<T> {
    private final String code;
    private final String error;
    private final String message;

    // Manual builder
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private String code;
        private String error;
        private String message;

        public Builder<T> code(String code) {
            this.code = code;
            return this;
        }

        public Builder<T> error(String error) {
            this.error = error;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public ErrorData<T> build() {
            return new ErrorData<>(code, error, message);
        }
    }

    // All-args constructor (required for final fields)
    public ErrorData(String code, String error, String message) {
        this.code = code;
        this.error = error;
        this.message = message;
    }

    // Static factory method
    public static <T> ErrorData<T> of(String code, String error, String message) {
        return new ErrorData<>(code, error, message);
    }
}