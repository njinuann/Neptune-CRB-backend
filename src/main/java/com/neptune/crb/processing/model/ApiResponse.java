package com.neptune.crb.processing.model;

import java.time.Instant;


public class ApiResponse<T> {
    private String responseCode;
    private String responseMessage;
    private T data;
    private String timestamp;  // Add a timestamp field
    private String path;

    public ApiResponse(String responseCode, String responseMessage, T data, String path) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.data = data;
        this.timestamp = Instant.now().toString(); // Auto-fill the current time
        this.path = path;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
