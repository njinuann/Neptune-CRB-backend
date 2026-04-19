package com.neptune.crb.processing.model;



import org.intellij.lang.annotations.Pattern;

import java.sql.Date;
public class CrbReportType {
    private String reportType;
    private String reportNm;
    private String enabled;
    private Integer priority;
    private String scheduleType;
    private Date lastRunDate;
    private Date createdDate;
    private Date lastUpdatedDate;

    // Constructors
    public CrbReportType() {}

    public CrbReportType(String reportType, String reportNm, String enabled,
                         Integer priority, String scheduleType) {
        this.reportType = reportType;
        this.reportNm = reportNm;
        this.enabled = enabled;
        this.priority = priority;
        this.scheduleType = scheduleType;
    }

    // Getters and Setters
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getReportNm() { return reportNm; }
    public void setReportNm(String reportNm) { this.reportNm = reportNm; }

    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getScheduleType() { return scheduleType; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }

    public Date getLastRunDate() { return lastRunDate; }
    public void setLastRunDate(Date lastRunDate) { this.lastRunDate = lastRunDate; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Date getLastUpdatedDate() { return lastUpdatedDate; }
    public void setLastUpdatedDate(Date lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }

    public boolean isEnabled() {
        return "Y".equalsIgnoreCase(this.enabled);
    }

    public boolean shouldRunOnSchedule(String currentSchedule) {
        return isEnabled() && currentSchedule.equalsIgnoreCase(this.scheduleType);
    }
}