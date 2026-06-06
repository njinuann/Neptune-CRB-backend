package com.neptune.crb.processing.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.neptune.crb.processing.component.ResponseData;
import com.neptune.crb.processing.config.ReportScheduleConfig;
import com.neptune.crb.processing.model.CrbReportType;
import com.neptune.crb.processing.model.CsvFileResult;
import com.neptune.crb.processing.model.XApiResponse;
import com.neptune.crb.processing.repository.CrbReportTypeRepository;
import com.neptune.crb.processing.repository.SubmissionRepository;
import com.neptune.crb.processing.util.AXWorker;
import com.neptune.crb.processing.util.CsvUtil;
import com.neptune.crb.processing.util.MXCaller;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.neptune.crb.processing.util.Logger.logError;
import static com.neptune.crb.processing.util.Logger.logEvent;

@Service
public class ReportingService
{
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);


    private final AXWorker axWorker;
    private final ReportScheduleConfig scheduleConfig;
    private MXCaller caller;
    private final ApiClient apiClient;
    private final SubmissionRepository submissionRepository;
    private final Environment environment;
    private final CrbReportTypeRepository crbReportTypeRepository;

    @Autowired
    public ReportingService(JdbcTemplate jdbcTemplate,
                            AXWorker axWorker,
                            CaffeineTokenService tokenService,
                            ReportScheduleConfig scheduleConfig,
                            MXCaller caller,
                            ApiClient apiClient,
                            SubmissionRepository submissionRepository, Environment environment, CrbReportTypeRepository crbReportTypeRepository)
    {

        this.apiClient = apiClient;
        this.submissionRepository = submissionRepository;
        this.axWorker = axWorker;
        this.scheduleConfig = scheduleConfig;
        this.caller = caller;
        this.environment = environment;
        this.crbReportTypeRepository = crbReportTypeRepository;
    }

    private static final Map<String, String> REPORT_TYPE_MAPPING = Map.of(
            "CE", "processIndividualConsumer",
            "CI", "processNonIndividualConsumer",
            "DP", "processDeleteCustomers",
            "CA", "processCreditApp",
            "GI", "processGuarantorInfo",
            "SI", "processStakeHolderInfo",
            "CR", "collateralInfo"
    );

    public void processReportsBySchedule(String scheduleType)
    {
        try
        {
            logEvent("Starting " + scheduleType + " report processing...");

            List<CrbReportType> scheduledReports = crbReportTypeRepository.getReportsByScheduleType(scheduleType);

            if (scheduledReports.isEmpty())
            {
                logEvent("No enabled reports found for schedule type: " + scheduleType);
                return;
            }

            logEvent("Found " + scheduledReports.size() + " reports scheduled for " + scheduleType);

            // Process reports in priority order
            for (CrbReportType report : scheduledReports)
            {
                processSingleReport(report, scheduleType);
            }

            logEvent(scheduleType + " report processing completed successfully.");

        } catch (Exception e)
        {
            logError("Error processing " + scheduleType + " reports", e);
        }
    }

    private void processSingleReport(CrbReportType report, String scheduleType)
    {
        String reportType = report.getReportType();
        String reportName = report.getReportNm();

        try
        {
            logEvent("Processing report: " + reportName + " (Priority: " + report.getPriority() + ")");

            String methodName = REPORT_TYPE_MAPPING.get(reportType);
            if (methodName != null)
            {
                //  processReportViewData(reportType, false);
                boolean processed = processReportAPIData(reportType, false);
                logEvent("======== file processed: " + reportName + " - " + (processed ? "duccessful" : "Failed"));
                // Update last run date
                crbReportTypeRepository.updateLastRunDate(reportType);

                logEvent("Successfully processed: " + reportName);
            }
            else
            {
                logEvent("No processor found for report type: " + reportType);
            }

        } catch (Exception e)
        {
            logError("Failed to process report: " + reportName, e);
        }
    }

    public void callAllReports(String scheduleType)
    {
        try
        {
            caller = new MXCaller();
            long startTime = System.currentTimeMillis();

            logEvent("Starting scheduled report processing for: " + scheduleType);

            // use service that considers schedule type and priority
            processReportsBySchedule(scheduleType);

            long duration = System.currentTimeMillis() - startTime;
            caller.setDuration(duration + " Ms");

            logEvent("Scheduled processing completed in " + duration + "ms");

            writeToLog(caller);

        } catch (Exception e)
        {
            logError("Error in scheduled report processing", e);
        } finally
        {
            logEvent("Processing completed for schedule: " + scheduleType);
        }
    }

    private String viewQuery(String reportType)
    {
        if (reportType == null)
        {
            return "";
        }
        return switch (reportType.toUpperCase())
        {
            case "DP" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_DAILY_PAYMENT ORDER BY ACCOUNT_NUMBER ";
            case "DE" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_DELETE  ORDER BY ACCOUNT_NUMBER ";
            case "CI" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_NON_INDIVIDUAL_CONSUMER  ORDER BY ACCOUNT_NUMBER ";
            case "CE" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_INDIVIDUAL_CONSUMER  ORDER BY ACCOUNT_NUMBER ";
            case "CA" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_CREDIT_APP_INFORMATION  ORDER BY ACCOUNT_NUMBER ";
            case "CR" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_COLLATERAL_INFORMATION";
            case "SI" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_STAKEHOLDER_INFORMATION  ORDER BY ACCOUNT_NUMBER ";
            case "GI" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_GUARANTOR_INFORMATION  ORDER BY PRIMARY_ID_DOC_NO  ";
            case "SV" ->
                    "SELECT  TRX_ID, SUCCESS, HAS_ERROR, API_CODE, API_CODE_DESCRIPTION, MESSAGE, REFERENCE_CODE,  " +
                            " DATA_REF AS report_type, LIVE_STATUS, RECORDS_COUNT, RECORDS_REJECTED, STATUS_ID, STATUS_NAME,  " +
                            " TEMPLATE_VERSION, IS_FINAL_SUBMISSION, RECORDS_IMPORTED, EMPTY_RECORDS, RECORDS_PASSED,  " +
                            " ERROR_COUNT, ERROR_LOG_STATUS, POST_LIVE_ERROR_STATUS,  " +
                            " POST_ERROR_COUNT, SUBMISSION_TYPE, SUBMISSION_TEMPLATE_TYPE, META_FIRST, META_LAST, " +
                            " TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI:SS') AS CREATED_AT FROM " + axWorker.channelSchemaName + ".CRB_VALIDATION_STATUS WHERE TRUNC(created_at) >= TRUNC(SYSDATE) ORDER BY CREATED_AT DESC";
            case "VE" ->
                    "SELECT ID, TRX_ID, SUCCESS, HAS_ERROR, API_CODE, API_CODE_DESCRIPTION, COLUMN_NAME, COLUMN_INDEX, ROW_INDEX, VALUE_PROVIDED, DATA_RULE_ID_TYPE_ID, ERROR_DESCRIPTION, CLIENT_NUMBER, ACCOUNT_NUMBER, TO_CHAR(CREATED_DATE, 'YYYY-MM-DD HH24:MI:SS') AS CREATED_DATE " +
                            " FROM " + axWorker.channelSchemaName + ".CRB_ERROR_LOGS WHERE TRUNC(CREATED_DATE) >= TRUNC(SYSDATE) ORDER BY id";
            case "VS" ->
                    "SELECT  TO_CHAR(CREATED_DATE, 'YYYY-MM-DD HH24:MI:SS') AS CREATED_DATE ,REPORT_TYPE, TRX_ID, SUCCESS, HAS_ERROR,API_CODE,API_CODE_DESCRIPTION " +
                            " FROM " + axWorker.channelSchemaName + ".CRB_REQUEST_DATA WHERE TRUNC(CREATED_DATE) >= TRUNC(SYSDATE)  ORDER BY 1";
            case "SM" ->
                    "SELECT id,viewName,category,recordCount,passedCount,rejectedCount,status,submittedBy, TO_CHAR(CREATED_DATE, 'YYYY-MM-DD HH24:MI:SS') AS submittedAt " +
                            " FROM " + axWorker.channelSchemaName + ".V_CRB_SUBMISSION_SUMMARY ORDER BY 1";

            default -> "";
        };
    }

    private String viewCsvQuery(String reportType)
    {
        if (reportType == null)
        {
            return "";
        }
        return switch (reportType.toUpperCase())
        {
            case "DP" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_CSV_DAILY_PAYMENT";
            case "DE" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_CSV_DELETE";
            case "CI" -> "SELECT * FROM " + axWorker.coreSchemaName + ".V_CRB_CSV_NON_INDIVIDUAL_CONSUMER";
            case "CE" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_CSV_INDIVIDUAL_CONSUMER";
            case "CA" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_CSV_CREDIT_APP_INFORMATION";
            case "CR" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_CSV_COLLATERAL_INFORMATION";
            case "SI" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_CSV_STAKEHOLDER_INFORMATION";
            case "GI" -> "SELECT * FROM " + axWorker.channelSchemaName + ".V_CRB_CSV_GUARANTOR_INFORMATION";
            case "SV" ->
                    "SELECT  TRX_ID, SUCCESS, HAS_ERROR, API_CODE, API_CODE_DESCRIPTION, MESSAGE, REFERENCE_CODE,  " +
                            " DATA_REF AS report_type, LIVE_STATUS, RECORDS_COUNT, RECORDS_REJECTED, STATUS_ID, STATUS_NAME,  " +
                            " TEMPLATE_VERSION, IS_FINAL_SUBMISSION, RECORDS_IMPORTED, EMPTY_RECORDS, RECORDS_PASSED,  " +
                            " ERROR_COUNT, ERROR_LOG_STATUS, POST_LIVE_ERROR_STATUS,  " +
                            " POST_ERROR_COUNT, SUBMISSION_TYPE, SUBMISSION_TEMPLATE_TYPE, META_FIRST, META_LAST, " +
                            " TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI:SS') AS CREATED_AT FROM " + axWorker.channelSchemaName + ".CRB_VALIDATION_STATUS WHERE TRUNC(created_at) >= TRUNC(SYSDATE) ORDER BY CREATED_AT DESC";
            case "VE" ->
                    "SELECT ID, TRX_ID, SUCCESS, HAS_ERROR, API_CODE, API_CODE_DESCRIPTION, COLUMN_NAME, COLUMN_INDEX, ROW_INDEX, VALUE_PROVIDED, DATA_RULE_ID_TYPE_ID, ERROR_DESCRIPTION, CLIENT_NUMBER, ACCOUNT_NUMBER, TO_CHAR(CREATED_DATE, 'YYYY-MM-DD HH24:MI:SS') AS CREATED_DATE " +
                            " FROM " + axWorker.channelSchemaName + ".CRB_ERROR_LOGS WHERE TRUNC(CREATED_DATE) >= TRUNC(SYSDATE) ORDER BY id";
            case "VS" ->
                    "SELECT  TO_CHAR(CREATED_DATE, 'YYYY-MM-DD HH24:MI:SS') AS CREATED_DATE ,REPORT_TYPE, TRX_ID, SUCCESS, HAS_ERROR,API_CODE,API_CODE_DESCRIPTION " +
                            " FROM " + axWorker.channelSchemaName + ".CRB_REQUEST_DATA WHERE TRUNC(CREATED_DATE) >= TRUNC(SYSDATE)  ORDER BY 1";

            default -> "";
        };
    }


    public List<Map<String, Object>> getReportViewData(
            String reportType,int page,
            int pageSize)
    {
        try
        {
            int offset = (page - 1) * pageSize;
            String baseQuery =
                    processCsv() ? viewCsvQuery(reportType) : viewQuery(reportType);

            String paginatedQuery =
                    baseQuery + " OFFSET " + offset +
                            " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";

            List<Map<String, Object>> data =
                    submissionRepository.queryListItems(paginatedQuery);

//            String query = processCsv() ? viewCsvQuery(reportType) : viewQuery(reportType);
//            List<Map<String, Object>> data = submissionRepository.queryListItems(query);
            logEvent(data);

            return data;
        } catch (Exception e)
        {
            logError("Error processing reports for " + reportType, e);
            return null;
        }
    }

    public List<Map<String, Object>> getErrorResponse(String reportType)
    {
        logEvent("Report Type", reportType);
        try
        {
            logEvent("about to call submissionRepository");
            String referenceCode = submissionRepository.getReportReferenceByCode(reportType);
            logEvent("Report referenceCode", referenceCode);

            String validationResponse = apiClient.getValidationStatus(referenceCode, reportType, referenceCode);
            String validationReferenceCode = extractReferenceCode(validationResponse);

            if (validationReferenceCode == null || validationReferenceCode.trim().isEmpty())
            {
                logEvent("Validation reference code is null or empty");
                return Collections.emptyList();
            }

            try
            {
                String errorResponse = apiClient.fetchCSVErrorLog(validationReferenceCode);

                if (errorResponse == null || errorResponse.trim().isEmpty())
                {
                    logEvent("Error response from API is null or empty");
                    return Collections.emptyList();
                }

                return parseErrorQueryResponse(errorResponse);
            } catch (Exception e)
            {
                logError("Error fetching or parsing error log", e);
                return Collections.emptyList();
            }

        } catch (Exception e)
        {
            throw new RuntimeException("Failed to get Error Response: " + e.getMessage(), e);
        }
    }
//
//    private List<Map<String, Object>> parseErrorQueryResponse(String jsonResponse) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            Map<String, Object> responseMap = objectMapper.readValue(
//                    jsonResponse,
//                    new TypeReference<Map<String, Object>>() {}
//            );
//
//            // Safely get the data field
//            Object dataObj = responseMap.get("data");
//
//            // Handle case when data is null or not the expected type
//            if (dataObj == null) {
//                return Collections.emptyList();
//            }
//
//            // Check if data is a Map (expected structure)
//            if (dataObj instanceof Map) {
//                Map<String, Object> data = (Map<String, Object>) dataObj;
//                Object contentObj = data.get("content");
//
//                // Check if content is a List
//                if (contentObj instanceof List) {
//                    return (List<Map<String, Object>>) contentObj;
//                }
//            }
//            // Handle case when data is an empty array or unexpected type
//            else if (dataObj instanceof List) {
//                logEvent("Data field is an array instead of object: " + dataObj);
//                return Collections.emptyList();
//            }
//
//            return Collections.emptyList();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to parse JSON response: " + e.getMessage(), e);
//        }
//    }

    private List<Map<String, Object>> parseErrorQueryResponse(String jsonResponse)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            // Parse as JsonNode for more flexible navigation
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Check if response indicates success
            if (!rootNode.path("success").asBoolean())
            {
                logEvent("API response indicates failure");
                return Collections.emptyList();
            }

            JsonNode dataNode = rootNode.path("data");

            // Handle different data types safely
            if (dataNode.isObject())
            {
                // Data is an object with content field
                JsonNode contentNode = dataNode.path("content");
                if (contentNode.isArray())
                {
                    // Convert JSON array to List<Map>
                    return objectMapper.convertValue(contentNode,
                            new TypeReference<List<Map<String, Object>>>()
                            {
                            });
                }
            }
            else if (dataNode.isArray())
            {
                // Data is directly an array (empty array in your case)
                logEvent("Data field is an array with " + dataNode.size() + " elements");
                if (dataNode.size() > 0)
                {
                    // If array contains objects, convert them
                    return objectMapper.convertValue(dataNode,
                            new TypeReference<List<Map<String, Object>>>()
                            {
                            });
                }
            }

            return Collections.emptyList();

        } catch (Exception e)
        {
            throw new RuntimeException("Failed to parse JSON response: " + e.getMessage(), e);
        }
    }

    public String extractReferenceCode(String jsonResponse)
    {
        if (jsonResponse == null || jsonResponse.trim().isEmpty())
        {
            logError("JSON response is null or empty", null);
            return null;
        }

        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Check if response is successful
            boolean success = rootNode.path("success").asBoolean();
            if (!success)
            {
                logEvent("API response indicates failure");
                return null;
            }

            // Navigate to data array
            JsonNode dataArray = rootNode.path("data");
            if (!dataArray.isArray() || dataArray.isEmpty())
            {
                logEvent("No data found in response");
                return null;
            }

            JsonNode firstDataElement = dataArray.get(0);
            if (!firstDataElement.has("reference_code"))
            {
                logEvent("reference_code field not found in response");
                return null;
            }

            String referenceCode = firstDataElement.path("reference_code").asText(null);
            logEvent("Extracted reference_code: " + referenceCode);
            return referenceCode;

        } catch (Exception e)
        {
            logError("Error extracting reference_code from JSON response", e);
            return null;
        }
    }

    public List<Map<String, Object>> getDataMetrics()
    {
        try
        {
            String query = "SELECT *  " +
                    " FROM " + axWorker.channelSchemaName + ".V_CRB_VALIDATION_SUMMARY_VIEW " +
                    " ORDER BY CASE WHEN category = 'TOTAL' THEN 2 ELSE 1 END, records_count DESC";
            List<Map<String, Object>> data = submissionRepository.queryListItems(query);
            logEvent(data);

            return data;
        } catch (Exception e)
        {
            logError("Error processing reports for report metrics", e);
            return null;
        }
    }

    public Map<String, Object> getReportSummary() {
        try {
            String query =
                    "SELECT cvs.TRX_ID AS \"id\", " +
                            " CASE WHEN cvs.DATA_REF = 'CE' THEN 'Individual Consumer' " +
                            "      WHEN cvs.DATA_REF = 'CI' THEN 'Non Individual Consumer' " +
                            "      WHEN cvs.DATA_REF = 'DP' THEN 'Daily Payment' " +
                            " END AS \"viewName\", " +
                            " cvs.DATA_REF AS \"category\", " +
                            " cvs.RECORDS_COUNT AS \"recordCount\", " +
                            " cvs.RECORDS_PASSED AS \"passedCount\", " +
                            " cvs.RECORDS_REJECTED AS \"rejectedCount\", " +
                            " 'completed' AS \"status\", " +
                            " 'NEPTUNE' AS \"submittedBy\", " +
                            " TO_CHAR(cvs.CREATED_AT, 'YYYY-MM-DD\"T\"HH24:MI:SS') AS \"submittedAt\" " +
                            " FROM " + axWorker.channelSchemaName + ".CRB_VALIDATION_STATUS cvs  ORDER BY cvs.CREATED_AT desc";

            List<Map<String, Object>> submissions =
                    submissionRepository.queryListItemsMaintainCase(query);

            Map<String, Object> response = new HashMap<>();
            response.put("submissions", submissions);

//            Map<String, Object> response = new HashMap<>();
//            response.put("data", data);

            return response;

        } catch (Exception e) {
            logError("Error processing reports for report metrics", e);
            return Collections.emptyMap();
        }
    }

//    public void processReportViewData(String reportType, boolean log)
//    {
//        log = Objects.equals(axWorker.logPayload, "Y");
//        logEvent("LOG ENABLED ", log);
//        int waitingTime = Integer.parseInt(axWorker.crbResponseWaitPeriod);
//        XApiResponse xApiResponse;
//        try
//        {
//            String submissionUrl = String.format("%s:%s/%s/datasubmission/submit/data",
//                    axWorker.baseUrl, Integer.parseInt(axWorker.port), axWorker.apiVersion);
//
//            String query = viewQuery(reportType);
//            List<Map<String, Object>> data = submissionRepository.queryListItems(query);
//
//            Map<String, Object> jsonPayload = new HashMap<>();
//            jsonPayload.put("template_version", 4);
//            jsonPayload.put("callback_url", axWorker.callBackUrl);
//            jsonPayload.put("institution_ref", "B");
//            jsonPayload.put("is_final_submission", true);
//            jsonPayload.put("submission_date", new SimpleDateFormat("YYYYMMdd").format(new Date()));
//            jsonPayload.put("submission_type", "M");
//            jsonPayload.put("data_ref", reportType);
//            jsonPayload.put("data", data);
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//            objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>()
//            {
//                @Override
//                public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
//                        throws IOException
//                {
//                    gen.writeString("");
//                }
//            });
//
//            String jsonString = objectMapper.writeValueAsString(jsonPayload);
//            logEvent(log ? jsonString : "");
//            logEvent(submissionUrl);
//
//            xApiResponse = apiClient.sendJsonToApi(jsonString, submissionUrl, reportType);
//            logEvent(xApiResponse);
//
//            scheduler.schedule(
//                    new StatusCheckTask(
//                            xApiResponse.getTrx_id(),
//                            reportType,
//                            xApiResponse.getTrx_id(),
//                            0, // initial attempt
//                            reportType
//                    ),
//                    waitingTime, TimeUnit.MINUTES
//            );
//        } catch (Exception e)
//        {
//            logError("Error processing reports for " + reportType, e);
//        }
//    }

    private Map<String, Object> convertNullsToEmptyStrings(Map<String, Object> map)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            if (entry.getValue() == null)
            {
                result.put(entry.getKey(), "");
            }
            else
            {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private boolean processCsv()
    {
        return Objects.equals(axWorker.processCsv, "Y");
    }

    public boolean reportEnabled(String reportType)
    {
        CrbReportType crbReportType = crbReportTypeRepository.getReportTypeById(reportType);
        return crbReportType.isEnabled();
    }

    public boolean processReportAPIData(String reportType, boolean log)
    {
        if (processCsv())
        {
            return processCsvReportAPIData(reportType);
        }

        logEvent("LOG ENABLED ", log);
        int waitingTime = Integer.parseInt(axWorker.crbResponseWaitPeriod);
        XApiResponse xApiResponse;
        try
        {
            String submissionUrl = String.format("%s:%s/%s/datasubmission/submit/data",
                    axWorker.baseUrl, Integer.parseInt(axWorker.port), axWorker.apiVersion);

            String query = viewQuery(reportType);
            List<Map<String, Object>> data = submissionRepository.queryListItems(query);

//            List<Map<String, Object>> processedData = data.stream()
//                    .map(this::convertNullsToEmptyStrings)
//                    .toList();


            Map<String, Object> jsonPayload = new LinkedHashMap<>(); // maintain ordering
            jsonPayload.put("template_version", 4);
            jsonPayload.put("callback_url", axWorker.callBackUrl);
            jsonPayload.put("institution_ref", "B");
            jsonPayload.put("is_final_submission", true);
            jsonPayload.put("submission_date", new SimpleDateFormat("YYYYMMdd").format(new Date()));
            jsonPayload.put("submission_type", "M");
            jsonPayload.put("data_ref", reportType);
            jsonPayload.put("data", data);

            ObjectMapper objectMapper = new ObjectMapper();
            // objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>()
            {
                @Override
                public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers)
                        throws IOException
                {
                    gen.writeString("");
                }
            });
            String jsonString = objectMapper.writeValueAsString(jsonPayload);
            logEvent(log ? jsonString : "");
            logEvent(submissionUrl);

            xApiResponse = apiClient.sendJsonToApi(jsonString, submissionUrl, reportType);
            logEvent(xApiResponse);

            scheduler.schedule(
                    new StatusCheckTask(
                            xApiResponse.getTrx_id(),
                            reportType,
                            xApiResponse.getTrx_id(),
                            0, // initial attempt
                            reportType
                    ),
                    waitingTime, TimeUnit.MINUTES
            );

            return true;
        } catch (Exception e)
        {
            logError("Error processing reports for " + reportType, e);
        }
        return false;
    }

    public boolean processCsvReportAPIData(String reportType)
    {
        boolean log = Objects.equals(axWorker.logPayload, "Y");
        logEvent("LOG ENABLED ", log);

        int waitingTime = Integer.parseInt(axWorker.crbResponseWaitPeriod);

        try
        {
            String submissionUrl = String.format("%s:%s/%s/datasubmission/submit/file",
                    axWorker.baseUrl, Integer.parseInt(axWorker.port), axWorker.apiVersion);

            // Query data
            String query = viewCsvQuery(reportType);
            List<Map<String, Object>> data = submissionRepository.queryListItems(query);

            if (data == null || data.isEmpty())
            {
                logEvent("No data to submit for type: " + reportType);
                return false;
            }

            // ✅ Convert query result → CSV
            //   File csvFile = CsvUtil.toCsvFile(reportType, data);
            CsvFileResult result = CsvUtil.toCsvFile(reportType, data, true);
            File csvFile = result.getFile();
            int count = result.getRecordCount();

            logEvent("CSV created: " + csvFile.getAbsolutePath());
            logEvent("Rows written: " + count);


            logEvent("Uploading CSV File: " + csvFile.getAbsolutePath());

            // ✅ POST multipart request
            XApiResponse xApiResponse = apiClient.sendCsvToApi(
                    csvFile,
                    submissionUrl,
                    reportType,
                    axWorker.callBackUrl
            );

            if (xApiResponse == null)
            {
                logError("Null API response");
                return false;
            }

            // ✅ Schedule async status check
            scheduler.schedule(
                    new StatusCheckTask(
                            xApiResponse.getTrx_id(),
                            reportType,
                            xApiResponse.getTrx_id(),
                            0,
                            reportType
                    ),
                    waitingTime,
                    TimeUnit.MINUTES
            );

            return true;

        } catch (Exception e)
        {
            logError("Error processing reports for " + reportType, e);
            return false;
        }
    }


    public void writeToLog(MXCaller mXCaller)
    {
        logEvent("<crb>" + "\r\n" + mXCaller + "\r\n" + "</crb>");

    }

    public void saveCrbCallBackResponse(ResponseData requestData)
    {
        submissionRepository.saveCrbCallBackResponse(requestData);
    }

    //runnable
    private class StatusCheckTask implements Runnable
    {
        private final String requestCode;
        private final String dateType;
        private final String trxId;
        private final String reportType;
        private final int attempt;

        StatusCheckTask(String requestCode, String dateType, String trxId, int attempt, String reportType)
        {
            this.requestCode = requestCode;
            this.dateType = dateType;
            this.trxId = trxId;
            this.attempt = attempt;
            this.reportType = reportType;
        }

        @Override
        public void run()
        {
            try
            {
                int waitingTime = Integer.parseInt(axWorker.crbResponseWaitPeriod);
                String statusJson = apiClient.getValidationStatus(requestCode, dateType, trxId);
                String returnedTrxId = submissionRepository.saveValidationStatus(statusJson, attempt);

                JSONObject response = new JSONObject(statusJson);
                JSONObject data = response.getJSONArray("data").getJSONObject(0);
                JSONObject status = data.getJSONObject("status");

//                if (status.getInt("id") != 6 && attempt < 2)
//                { // Processing status and < max attempts
//                    scheduler.schedule(
//                            new StatusCheckTask(requestCode, dateType, trxId, attempt + 1),
//                            waitingTime, TimeUnit.MINUTES
//                    );
//                }
                if (status.getInt("id") == 5)
                {
                    logEvent("Waiting for extra " + waitingTime + " minutes to check status");
                    scheduler.schedule(
                            new StatusCheckTask(requestCode, dateType, trxId, attempt + 1, reportType),
                            waitingTime, TimeUnit.MINUTES
                    );
                }
                else
                {
                    logEvent("Processing Status  = " + status.getString("name"));
                    logEvent("Records Passed  = " + data.optInt("records_passed", 0));
                    logEvent("Records Rejected  = " + data.optInt("records_rejected", 0));
                    if (data.optInt("records_rejected", 0) > 0 || status.optInt("id", 0) != 9)
                    {
                        logEvent("fetching errors");
                        String errorLog = apiClient.fetchCSVErrorLog(returnedTrxId);
                        logEvent("errorLog Response", errorLog);
                        submissionRepository.saveErrorLog(errorLog, reportType);
                    }
                    else
                    {
                        logEvent("Records Processed  = " + data.getInt("records_passed"));
                    }
                }

            } catch (Exception e)
            {
                logError("Status check failed for trxId: " + trxId, e);

                if (attempt < 2)
                { // Retry on failure
                    scheduler.schedule(
                            new StatusCheckTask(requestCode, dateType, trxId, attempt + 1, reportType),
                            5, TimeUnit.MINUTES
                    );
                }
            }
        }
    }
}

