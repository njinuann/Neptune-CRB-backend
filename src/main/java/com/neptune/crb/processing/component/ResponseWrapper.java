package com.neptune.crb.processing.component;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class ResponseWrapper
{

    @JsonProperty("request_code")
    private String requestCode;
    private List<ResponseData> data;

    public String getRequestCode()
    {
        return requestCode;
    }

    public void setRequestCode(String requestCode)
    {
        this.requestCode = requestCode;
    }

    public List<ResponseData> getData()
    {
        return data;
    }

    public void setData(List<ResponseData> data)
    {
        this.data = data;
    }

    @Override
    public String toString()
    {
        return "ResponseWrapper{" +
                "requestCode='" + requestCode + '\'' +
                ", data=" + data +
                '}';
    }
}
