package com.neptune.crb.processing.service;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 23-Mar-26
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neptune.crb.processing.config.CRBConfig;
import com.neptune.crb.processing.model.*;
import com.neptune.crb.processing.repository.CRBDelinquencyRepository;
import com.neptune.crb.processing.util.AXWorker;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.neptune.crb.processing.util.Logger.logEvent;

@Slf4j
@Service
public class CRBDelinquencyService {

    private final CRBDelinquencyRepository repository;
    private final MetropolService metropolClient;
    private final AXWorker axWorker;

    private final CRBConfig crbConfig;
    private final PdfExtractionService pdfExtractionService;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> inFlightRequests = ConcurrentHashMap.newKeySet();

    private String fingerprint(String id, int amount) {
        return id + "-" + amount;
    }

    public CRBDelinquencyService(MetropolService metropolClient, CRBDelinquencyRepository repository, AXWorker axWorker, CRBConfig crbConfig, PdfExtractionService pdfExtractionService) {
        this.metropolClient = metropolClient;
        this.repository = repository;
        this.axWorker = axWorker;
        this.crbConfig = crbConfig;
        this.pdfExtractionService = pdfExtractionService;
    }

    @Transactional
    public CRBDelinquencyResponseWrapper checkDelinquencyById(CRBDelinquencyRequest request, String userId) {
        logEvent("delinquency-check ", "Delinquency check request - Identity: " +
                request.getIdentityNumber(), request.getLoanAmount());
        logEvent("delinquency-check ", "Delinquency check request -   Amount: %s" +
                request.getLoanAmount());

        CRBDelinquencySearch delinquentCheckRequest = new CRBDelinquencySearch();
        delinquentCheckRequest.setIdentityNumber(request.getIdentityNumber());
        delinquentCheckRequest.setLoanAmount(new BigDecimal(request.getLoanAmount()));
        delinquentCheckRequest.setIdentityType(request.getIdentityType());

        CRBDelinquencyReport report = processAndHandleFallback(delinquentCheckRequest, userId);

        return new CRBDelinquencyResponseWrapper(report, null);
    }

    public CRBDelinquencyResponseWrapper checkDelinquency(DelinquentCheckRequest request, String userId) {


        CreditApplicationData creditApplicationData = repository.fetchLoanDataByCreditAppID(request.getCreditApplicationId());
        return executeWithMDC(() -> {

            CRBDelinquencySearch search = buildSearch(
                    creditApplicationData.getIdNumber(),
                    creditApplicationData.getLoanAmount(),
                    "001"
            );

            CRBDelinquencyReport report = processAndHandleFallback(search, userId);

            // 🔥 READ PDF HERE
            ReportData reportData = new ReportData();
            String base64 = report.getPdfReport();
            if (!axWorker.isBlank(base64)) {
                // String base64 = encodePdfFromResources("crbreport.pdf");
                reportData.setReferenceNumber(generateTransactionId());
                reportData.setCrbReport(base64);
                reportData.setDocumentName(generateTransactionId() + "-" + request.getCreditApplicationId() + ".pdf");
                reportData.setDocumentType(63L);
                System.out.println("PDF Read ====> " + reportData.toString());
            }
//            else{
//                // TODO : Remove this bit to have once live
//                base64 = encodePdfFromResources("crbreport.pdf");
//                reportData.setReferenceNumber(generateTransactionId());
//                reportData.setCrbReport(base64);
//                reportData.setDocumentName(generateTransactionId() + "-" + request.getCreditApplicationId() + ".pdf");
//                reportData.setDocumentType(63L);
//               // report.setPdfReport(base64);
//            }
            return new CRBDelinquencyResponseWrapper(report, reportData);

        }, creditApplicationData.getIdNumber(), request.getCrbVendorType(), request.getCreditApplicationId());
    }

    private CRBDelinquencyReport processAndHandleFallback(CRBDelinquencySearch request, String userId) {
        try {
            CRBDelinquencyReport report = processCRB(request, userId);

            // TODO: Remove mocked data on production
            return report;
//            return report.getHasError()
//                    ? mockedData(request)
//                    : report;

        } catch (Exception e) {
            log.error("CRB processing failed", e);
            //return mockedData(request);
            return new CRBDelinquencyReport();
        }
    }

    private CRBDelinquencyReport processCRB(CRBDelinquencySearch request, String userId) {

        String transactionId = generateTransactionId();

        log.info("Processing CRB request. txnId={}, identity={}",
                transactionId, request.getIdentityNumber());

        JSONObject response = safeCall(
                request.getIdentityNumber(),
                request.getLoanAmount().intValue()
        );

        log.info("Metropol response: {}", response.toString());

        CRBDelinquencyReport report = mapToReport(response);
        report.setTransactionId(transactionId);

        // TODO: Remove mocked CRB PDF in prod
        // report.setPdfReport(encodePdfFromResources("crbreport.pdf"));

        log.info("Mapped response");

        if (!axWorker.isBlank(report.getPdfReport())) {
            CreditScoreData creditScoreData;
            creditScoreData = pdfExtractionService.extract(report.getPdfReport());
            String code = creditScoreData.getPpi();
            String creditScore = creditScoreData.getMetroScore();
            log.info("code {}, creditScore {}", code, creditScore);
        }


        try {
            saveRecord(request, report, mapper.writeValueAsString(request), response.toString(),
                    userId, LocalDateTime.now(), getResponseTime(response));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return report;
    }

    private JSONObject safeCall(String id, int amount) {

        String key = fingerprint(id, amount);
        System.out.println(key);
        if (!inFlightRequests.add(key)) {
            log.warn("Duplicate request blocked for {}", key);
            throw new RuntimeException("Duplicate request in progress");
        }

        try {
            return metropolClient.getReport(id, amount);
        } finally {
            log.info("key removed {} ", key);
            inFlightRequests.remove(key);
        }
    }

    private String encodePdfFromResources(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource("report/" + fileName);

            try (InputStream inputStream = resource.getInputStream()) {
                byte[] bytes = inputStream.readAllBytes();
                return Base64.getEncoder().encodeToString(bytes);
            }

        } catch (Exception e) {
            log.error("Failed to read PDF from resources", e);
            throw new RuntimeException("PDF processing failed");
        }
    }

    private CRBDelinquencySearch buildSearch(String identity, Number amount, String type) {
        CRBDelinquencySearch search = new CRBDelinquencySearch();
        search.setIdentityNumber(identity);
        search.setLoanAmount(new BigDecimal(amount.toString()));
        search.setIdentityType(type);
        return search;
    }

//    private CRBDelinquencyReport mockedData(CRBDelinquencySearch request) {
//
//        boolean listed = true;
//
//        CRBDelinquencyReport report = new CRBDelinquencyReport();
//        report.setCreditScore("303");
//        report.setDelinquencyCode("005");
//        report.setDelinquencySummary("Historical delinquency True ~ " + getScoreDescription(303));
//        report.setDelinquencyDescription(getDelinquencyCode("005"));
//        report.setHasError(false);
//        report.setIdentityType("National Id");
//        report.setIdentityTypeDescription("National Id");
//        report.setIdentityNumber(request.getIdentityNumber());
//        report.setTransactionId(generateTransactionId());
//
//        report.setIsGuarantor("false");
//        report.setHasFraud("false");
//        report.setListed("yes");
//        report.setListingStatus("listed");
//
//        return report;
//    }

    private CRBDelinquencyReport mapToReport(JSONObject json) {

        String encoding = "base64";
        String code = json.optString("delinquency_code", json.optString("delinquencyCode", ""));

        String creditScore = json.optString("credit_score", "0");
        String pdfData = json.optString("report", "");

        CreditScoreData creditScoreData;
        if (!axWorker.isBlank(pdfData)) {
            creditScoreData = pdfExtractionService.extract(pdfData);
            code = creditScoreData.getPpi();
            creditScore = creditScoreData.getMetroScore();
        }

        boolean listed = "004".equals(code) || "005".equals(code);
        String rating = getScoreDescription(json.optInt("credit_score", 0));


        return new CRBDelinquencyReport(
                json.optString("api_code", ""),
                json.optString("api_code_description", ""),
                code,
                getDelinquencyDescription(code),
                json.optString("delinquency_summary", ""),
                json.optBoolean("has_error", false),
                json.optString("identity_number", ""),
                json.optString("identity_type", ""),
                getIdentityTypeDescription(json.optString("identity_type", "")),
                json.has("loan_amount") ? json.optLong("loan_amount") : null,
                listed ? "listed" : "not listed",
                json.optString("trx_id", ""),
                creditScore,
                json.optString("is_gurantor", ""),
                json.optString("has_fraud", ""),
                listed ? "yes" : "no",
                rating,
                pdfData,
                axWorker.isBlank(pdfData) ? "" : encoding
        );
    }

    private <T> T executeWithMDC(SupplierWithException<T> action,
                                 Object identity,
                                 Object type,
                                 Object amount) {

        MDC.put("requestId", UUID.randomUUID().toString().substring(0, 8));
        if (identity != null) MDC.put("identityNumber", identity.toString());
        if (type != null) MDC.put("type", type.toString());
        if (amount != null) MDC.put("amount", amount.toString());

        try {
            return action.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            MDC.clear();
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }

    // ================= LOOKUPS =================

    public String getScoreDescription(int score) {
        if (score >= 725) return "Very Good (Low risk)";
        if (score >= 660) return "Good";
        if (score >= 560) return "Moderate Risk";
        if (score >= 400) return "Very Poor";
        return "High default risk";
    }

    public String getDelinquencyCode(String code) {
        return switch (code) {
            case "001" -> "ID not found";
            case "002" -> "No credit info";
            case "003" -> "No NPA";
            case "004" -> "Currently delinquent";
            case "005" -> "Historical NPA";
            default -> "Unknown";
        };
    }

    private String getDelinquencyDescription(String code) {
        return getDelinquencyCode(code);
    }

    private String getIdentityTypeDescription(String type) {
        return switch (type) {
            case "001" -> "National ID";
            case "002" -> "Passport";
            case "003" -> "Service ID";
            case "004" -> "Alien Registration";
            case "005" -> "Business Registration";
            default -> "Unknown";
        };
    }

    private String generateTransactionId() {
        return "CRB-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase();
    }

    private void saveRecord(CRBDelinquencySearch request, CRBDelinquencyReport report,
                            String rawRequest, String rawResponse, String userId,
                            LocalDateTime queryTime, int responseTimeMs) {
        CRBDelinquencyRecord record = new CRBDelinquencyRecord();
        record.setTransactionId(report.getTransactionId());
        record.setIdentityNumber(request.getIdentityNumber());
        record.setIdentityType(request.getIdentityType() != null ? request.getIdentityType() : "001");
        record.setLoanAmount(Integer.parseInt(request.getLoanAmount().toPlainString()));
        record.setApiCode(report.getApiCode());
        record.setApiCodeDescription(report.getApiCodeDescription());
        record.setDelinquencyCode(report.getDelinquencyCode());
        record.setDelinquencyDescription(report.getDelinquencyDescription());
        record.setDelinquencySummary(report.getDelinquencySummary());
        record.setListingStatus(report.getListingStatus());
        record.setHasError(report.getHasError());
        record.setRawRequest(rawRequest);
        record.setRawResponse(rawResponse);
        record.setUserId(userId);
        record.setQueryTime(queryTime);
        record.setResponseTimeMs(responseTimeMs);

        repository.save(record);
    }

    private String buildRawRequest(CRBDelinquencySearch request) {
        JSONObject json = new JSONObject();
        json.put("identity_number", request.getIdentityNumber());
        json.put("loan_amount", request.getLoanAmount());
        json.put("identity_type", request.getIdentityType() != null ? request.getIdentityType() : "001");
        return json.toString();
    }

    private int getResponseTime(JSONObject json) {
        return json.has("_response_time_ms") ? json.getInt("_response_time_ms") : 0;
    }


}