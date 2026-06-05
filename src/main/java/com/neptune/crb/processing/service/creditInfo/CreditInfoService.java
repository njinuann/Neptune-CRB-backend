package com.neptune.crb.processing.service.creditInfo;

import com.neptune.crb.processing.model.creditInfo.CompanyCreditRequest;
import com.neptune.crb.processing.model.creditInfo.IndividualCreditRequest;
import com.neptune.crb.processing.model.creditInfo.IprsBeginRequest;

public interface CreditInfoService {

    Object beginIndividualCreditReport(IndividualCreditRequest request);

    Object beginCompanyCreditReport(CompanyCreditRequest request);

    Object beginIprsQuery(IprsBeginRequest request);

    String endQuery(String token);

    byte[] endQueryPdf(String token);
}