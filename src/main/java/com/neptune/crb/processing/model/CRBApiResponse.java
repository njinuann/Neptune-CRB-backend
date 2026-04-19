package com.neptune.crb.processing.model;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 23-Mar-26
 */

import com.fasterxml.jackson.annotation.JsonInclude;

//@Data
//@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CRBApiResponse {
    private String responseCode;
    private String responseMessage;
    private CRBDelinquencyData data;

    public CRBApiResponse(String responseCode, String responseMessage, CRBDelinquencyData data)
    {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.data = data;
    }

    public String getResponseCode()
    {
        return responseCode;
    }

    public void setResponseCode(String responseCode)
    {
        this.responseCode = responseCode;
    }

    public String getResponseMessage()
    {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage)
    {
        this.responseMessage = responseMessage;
    }

    public CRBDelinquencyData getData()
    {
        return data;
    }

    public void setData(CRBDelinquencyData data)
    {
        this.data = data;
    }

}
