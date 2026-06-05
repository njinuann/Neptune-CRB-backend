package com.neptune.crb.processing.model;

import java.util.List;


public class CRBDelinquencyData {

    private List<CRBDelinquencyReport> delinquencyReport;
    private ReportData reportData;

    public CRBDelinquencyData() {}

    public CRBDelinquencyData(List<CRBDelinquencyReport> delinquencyReport,
                              ReportData reportData) {
        this.delinquencyReport = delinquencyReport;
        this.reportData = reportData;
    }

    public List<CRBDelinquencyReport> getDelinquencyReport() {
        return delinquencyReport;
    }

    public void setDelinquencyReport(List<CRBDelinquencyReport> delinquencyReport) {
        this.delinquencyReport = delinquencyReport;
    }

    public ReportData getReportData() {
        return reportData;
    }

    public void setReportData(ReportData reportData) {
        this.reportData = reportData;
    }
}