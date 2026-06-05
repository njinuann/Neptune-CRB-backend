package com.neptune.crb.processing.model;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 30-Apr-26
 */
public class ReportData
{
    private String referenceNumber;
    private String crbReport; // Base64
    private String documentName;
    private Long documentType;

    public Long getDocumentType()
    {
        return documentType;
    }

    public void setDocumentType(Long documentType)
    {
        this.documentType = documentType;
    }

    public String getReferenceNumber()
    {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber)
    {
        this.referenceNumber = referenceNumber;
    }

    public String getCrbReport()
    {
        return crbReport;
    }

    public void setCrbReport(String crbReport)
    {
        this.crbReport = crbReport;
    }

    public String getDocumentName()
    {
        return documentName;
    }

    public void setDocumentName(String documentName)
    {
        this.documentName = documentName;
    }

    @Override
    public String toString()
    {
        return "ReportData{" +
                "referenceNumber='" + referenceNumber + '\'' +
                ", crbReport='" + crbReport + '\'' +
                ", documentName='" + documentName + '\'' +
                '}';
    }
}
