package com.neptune.crb.processing.model;

public class XApiResponse {
    private boolean success;
    private boolean has_error;
    private int api_code;
    private String api_code_description;
    private String trx_id;

    // Constructors, getters, and setters
    public XApiResponse() {}

    public XApiResponse(boolean success, boolean has_error, int api_code,
                       String api_code_description, String trx_id) {
        this.success = success;
        this.has_error = has_error;
        this.api_code = api_code;
        this.api_code_description = api_code_description;
        this.trx_id = trx_id;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public boolean isHas_error() { return has_error; }
    public void setHas_error(boolean has_error) { this.has_error = has_error; }

    public int getApi_code() { return api_code; }
    public void setApi_code(int api_code) { this.api_code = api_code; }

    public String getApi_code_description() { return api_code_description; }
    public void setApi_code_description(String api_code_description) { this.api_code_description = api_code_description; }

    public String getTrx_id() { return trx_id; }
    public void setTrx_id(String trx_id) { this.trx_id = trx_id; }

    @Override
    public String toString()
    {
        return "XApiResponse{" +
                "success=" + success +
                ", has_error=" + has_error +
                ", api_code=" + api_code +
                ", api_code_description='" + api_code_description + '\'' +
                ", trx_id='" + trx_id + '\'' +
                '}';
    }
}
