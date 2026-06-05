package com.neptune.crb.processing.model;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 30-Apr-26
 */
public class CRBDelinquencyResponseWrapper {
 private CRBDelinquencyReport report;
 private ReportData reportData;

 public CRBDelinquencyResponseWrapper(CRBDelinquencyReport report, ReportData reportData) {
  this.report = report;
  this.reportData = reportData;
 }

 public CRBDelinquencyReport getReport()
 {
  return report;
 }

 public void setReport(CRBDelinquencyReport report)
 {
  this.report = report;
 }

 public ReportData getReportData()
 {
  return reportData;
 }

 public void setReportData(ReportData reportData)
 {
  this.reportData = reportData;
 }
}
