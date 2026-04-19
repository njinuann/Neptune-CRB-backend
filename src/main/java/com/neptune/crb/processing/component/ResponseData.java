package com.neptune.crb.processing.component;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


public class ResponseData
{

    private String requestCode;
    @JsonProperty("reference_code")
    private String referenceCode;
    @JsonProperty("template_version")
    private String templateVersion;
    private String status;
    @JsonProperty("record_count")
    private int recordCount;
    @JsonProperty("records_passed")
    private int recordsPassed;
    @JsonProperty("records_rejected")
    private int recordsRejected;

    public String getRequestCode()
    {
        return requestCode;
    }

    public void setRequestCode(String requestCode)
    {
        this.requestCode = requestCode;
    }

    public String getReferenceCode()
    {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode)
    {
        this.referenceCode = referenceCode;
    }

    public String getTemplateVersion()
    {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion)
    {
        this.templateVersion = templateVersion;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public int getRecordCount()
    {
        return recordCount;
    }

    public void setRecordCount(int recordCount)
    {
        this.recordCount = recordCount;
    }

    public int getRecordsPassed()
    {
        return recordsPassed;
    }

    public void setRecordsPassed(int recordsPassed)
    {
        this.recordsPassed = recordsPassed;
    }

    public int getRecordsRejected()
    {
        return recordsRejected;
    }

    public void setRecordsRejected(int recordsRejected)
    {
        this.recordsRejected = recordsRejected;
    }

    @Override
    public String toString()
    {
        return "ResponseData{" +
                "requestCode='" + requestCode + '\'' +
                ", referenceCode='" + referenceCode + '\'' +
                ", templateVersion='" + templateVersion + '\'' +
                ", status='" + status + '\'' +
                ", recordCount=" + recordCount +
                ", recordsPassed=" + recordsPassed +
                ", recordsRejected=" + recordsRejected +
                '}';
    }
}
