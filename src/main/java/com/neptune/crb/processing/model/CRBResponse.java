package com.neptune.crb.processing.model;

public class CRBResponse
{
    private Long id;
    private String requestCode;
    private String referenceCode;
    private String templateVersion;
    private String status;
    private int recordCount;
    private int recordsPassed;
    private int recordsRejected;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

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
        return "CRBResponse{" +
                "id=" + id +
                ", requestCode='" + requestCode + '\'' +
                ", referenceCode='" + referenceCode + '\'' +
                ", templateVersion='" + templateVersion + '\'' +
                ", status='" + status + '\'' +
                ", recordCount=" + recordCount +
                ", recordsPassed=" + recordsPassed +
                ", recordsRejected=" + recordsRejected +
                '}';
    }
}
