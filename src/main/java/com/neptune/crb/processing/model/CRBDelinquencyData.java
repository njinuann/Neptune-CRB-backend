package com.neptune.crb.processing.model;

import java.util.List;


public class CRBDelinquencyData {
 private List<CRBDelinquencyReport> delinquencyReport;

 public List<CRBDelinquencyReport> getDelinquencyReport()
 {
  return delinquencyReport;
 }

 public void setDelinquencyReport(List<CRBDelinquencyReport> delinquencyReport)
 {
  this.delinquencyReport = delinquencyReport;
 }

 public CRBDelinquencyData(List<CRBDelinquencyReport> delinquencyReport)
 {
  this.delinquencyReport = delinquencyReport;
 }

 public CRBDelinquencyData()
 {
 }
}