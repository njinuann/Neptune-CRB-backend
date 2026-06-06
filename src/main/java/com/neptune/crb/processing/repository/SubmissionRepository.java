package com.neptune.crb.processing.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neptune.crb.processing.component.ResponseData;
import com.neptune.crb.processing.model.ValidationStatusResponse;
import com.neptune.crb.processing.model.XApiResponse;
import com.neptune.crb.processing.util.AXWorker;
import com.neptune.crb.processing.util.JsonUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.neptune.crb.processing.util.Logger.logEvent;

@Repository
public class SubmissionRepository
{
    private final JdbcTemplate jdbcTemplate;
    private final AXWorker axWorker;

    @Autowired
    public SubmissionRepository(JdbcTemplate jdbcTemplate, AXWorker axWorker)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.axWorker = axWorker;
    }

    public void saveSubmissionDetails(String requestCode, String dataType, String trxId,
                                      ValidationStatusResponse validationResponse)
    {
        // Extract validation data
        ValidationStatusResponse.ValidationData validationData = validationResponse.getData().get(0);

        jdbcTemplate.update(
                "INSERT INTO submission_details " +
                        "(request_code, data_type, trx_id, reference_code, template_version, " +
                        "data_ref, is_final_submission, records_count, empty_records, " +
                        "records_passed, records_rejected, status, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)",
                requestCode,
                dataType,
                trxId,
                validationData.getReference_code(),
                validationData.getTemplate_version(),
                validationData.getData_ref(),
                validationData.getIs_final_submission(),
                validationData.getRecords_count(),
                validationData.getEmpty_records(),
                validationData.getRecords_passed(),
                validationData.getRecords_rejected(),
                validationData.getStatus()
        );
    }

    public void saveReportRequest(XApiResponse response, String reportType)
    {
        String sql = "INSERT INTO " + axWorker.channelSchemaName + ".CRB_REQUEST_DATA (success, has_error, api_code, api_code_description, trx_id, report_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Object[] params = {
                response.isSuccess() ? "Y" : "N",
                response.isHas_error() ? "Y" : "N",
                response.getApi_code(),
                response.getApi_code_description(),
                response.getTrx_id(),
                reportType
        };
        String debugSql = String.format(
                sql.replace("?", "%s"),
                params[0], params[1],
                "'" + params[2] + "'", "'" + params[3] + "'", "'" + params[4] + "'", "'" + params[5] + "',"
        );

        logEvent("Executing SQL: {}", debugSql);

        jdbcTemplate.update(sql,
                response.isSuccess() ? "Y" : "N",
                response.isHas_error() ? "Y" : "N",
                response.getApi_code(),
                response.getApi_code_description(),
                response.getTrx_id(),
                reportType
        );

    }

    //    public void saveValidationStatus(String jsonResponse)
//    {
//        try
//        {
//            JSONObject root = new JSONObject(jsonResponse);
//            JSONObject data = root.getJSONArray("data").getJSONObject(0);
//
//            jdbcTemplate.update(
//                    "INSERT INTO crb_validation_status (" +
//                            "trx_id, reference_code, data_ref, live_status, " +
//                            "records_count, records_rejected, status, " +
//                            "success, has_error, created_at" +
//                            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
//                    root.getString("trx_id"),
//                    data.getString("reference_code"),
//                    data.getString("data_ref"),
//                    data.getInt("liveStatus"),
//                    data.getInt("records_count"),
//                    data.getInt("records_rejected"),
//                    data.getInt("status"),
//                    root.getBoolean("success"),
//                    root.getBoolean("has_error"),
//                    new Timestamp(System.currentTimeMillis())
//            );
//        } catch (Exception e)
//        {
//            throw new RuntimeException("Failed to save validation status", e);
//        }
//    }
//    public void saveValidationStatus(String jsonResponse)
//    {
//        try
//        {
//            JSONObject root = new JSONObject(jsonResponse);
//            JSONObject data = root.getJSONArray("data").getJSONObject(0);
//            JSONObject status = data.getJSONObject("status");
//            JSONObject meta = root.getJSONObject("meta");
//            JSONObject sort = meta.getJSONObject("sort");
//
//            jdbcTemplate.update(
//                    "INSERT INTO crb_validation_status (" +
//                            "trx_id, success, has_error, api_code, api_code_description, message, " +
//                            "reference_code, data_ref, live_status, records_count, records_rejected, " +
//                            "status_id, status_name, cug_id, template_version, is_final_submission, " +
//                            "records_imported, empty_records, records_passed, error_count, download_url, " +
//                            "postlive_error_url, error_log_status, post_live_error_status, post_error_count, " +
//                            "submission_type, submission_template_type, meta_first, meta_last, meta_number, " +
//                            "meta_size, meta_number_of_elements, meta_total_pages, meta_total_elements, " +
//                            "created_at" +
//                            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)",
//                    root.getString("trx_id"),
//                    root.getBoolean("success") ? 1 : 0,
//                    root.getBoolean("has_error") ? 1 : 0,
//                    root.optInt("api_code", 0),
//                    root.optString("api_code_description", null),
//                    root.optString("message", null),
//                    data.getString("reference_code"),
//                    data.optString("data_ref", null),
//                    data.optInt("liveStatus", 0),
//                    data.optInt("records_count", 0),
//                    data.optInt("records_rejected", 0),
//                    status.optInt("id", 0),
//                    status.optString("name", null),
//                    data.optString("cugId", null),
//                    data.optString("template_version", null),
//                    data.optBoolean("is_final_submission", false) ? 1 : 0,
//                    data.optInt("records_imported", 0),
//                    data.optInt("empty_records", 0),
//                    data.optInt("records_passed", 0),
//                    data.optInt("error_count", 0),
//                    data.optString("download_url", null),
//                    data.optString("postlive_error_url", null),
//                    data.optString("error_log_status", null),
//                    data.optString("post_live_error_status", null),
//                    data.optInt("post_error_count", 0),
//                    data.optString("submission_type", null),
//                    data.optString("submission_template_type", null),
//                    meta.optBoolean("first", false) ? 1 : 0,
//                    meta.optBoolean("last", false) ? 1 : 0,
//                    meta.optInt("number", 0),
//                    meta.optInt("size", 0),
//                    meta.optInt("number_of_elements", 0),
//                    meta.optInt("total_pages", 0),
//                    meta.optInt("total_elements", 0)
//            );
//        } catch (Exception e)
//        {
//            throw new RuntimeException("Failed to save validation status", e);
//        }
//    }
    public String saveValidationStatus(String jsonResponse, int attempt)
    {
        String referenceCode = "";
        try
        {
            JSONObject root = new JSONObject(jsonResponse);
            JSONObject data = root.getJSONArray("data").getJSONObject(0);
            JSONObject status = data.getJSONObject("status");
            JSONObject meta = root.getJSONObject("meta");
            JSONObject sort = meta.getJSONObject("sort");


            Object[] params = {
                    // Root object fields
                    root.getString("trx_id"),                      // trx_id
                    root.getBoolean("success") ? 1 : 0,            // success
                    root.getBoolean("has_error") ? 1 : 0,          // has_error
                    root.optInt("api_code", 0),                    // api_code
                    root.optString("api_code_description", null),   // api_code_description
                    root.optString("message", null),                // message

                    // Data object fields
                    data.getString("reference_code"),               // reference_code
                    data.optString("data_ref", null),              // data_ref
                    data.optInt("liveStatus", 0),                  // live_status
                    data.optInt("records_count", 0),               // records_count
                    data.optInt("records_rejected", 0),            // records_rejected
                    status.optInt("id", 0),                        // status_id
                    status.optString("name", null),                // status_name
                    data.optString("cugId", null),                 // cug_id
                    data.optString("template_version", null),      // template_version
                    data.optBoolean("is_final_submission", false) ? 1 : 0, // is_final_submission
                    data.optInt("records_imported", 0),            // records_imported
                    data.optInt("empty_records", 0),                // empty_records
                    data.optInt("records_passed", 0),               // records_passed
                    data.optInt("error_count", 0),                  // error_count
                    data.optString("download_url", null),           // download_url
                    data.optString("postlive_error_url", null),     // postlive_error_url
                    data.optString("error_log_status", null),       // error_log_status
                    data.optString("post_live_error_status", null), // post_live_error_status
                    data.optInt("post_error_count", 0),             // post_error_count
                    data.optString("submission_type", null),        // submission_type
                    data.optString("submission_template_type", null), // submission_template_type

                    // Meta fields
                    meta.optBoolean("first", false) ? 1 : 0,       // meta_first
                    meta.optBoolean("last", false) ? 1 : 0,        // meta_last
                    meta.optInt("number", 0),                      // meta_number
                    meta.optInt("size", 0),                        // meta_size
                    meta.optInt("number_of_elements", 0),          // meta_number_of_elements
                    meta.optInt("total_pages", 0),                 // meta_total_pages
                    meta.optInt("total_elements", 0)               // meta_total_elements

            };


            String sql = "INSERT INTO crb_validation_status (" +
                    "trx_id, success, has_error, api_code, api_code_description, message, " +
                    "reference_code, data_ref, live_status, records_count, records_rejected, " +
                    "status_id, status_name, cug_id, template_version, is_final_submission, " +
                    "records_imported, empty_records, records_passed, error_count, download_url, " +
                    "postlive_error_url, error_log_status, post_live_error_status, post_error_count, " +
                    "submission_type, submission_template_type, meta_first, meta_last, meta_number, " +
                    "meta_size, meta_number_of_elements, meta_total_pages, meta_total_elements, " +
                    "created_at" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSTIMESTAMP)";
            logSQLWithParameters(sql, params);

            if (attempt < 2 && status.getInt("id") == 5)
            {
                jdbcTemplate.update(sql, params);
            }
            else if (status.getInt("id") != 5)
            {
                jdbcTemplate.update(sql, params);
            }

            referenceCode = data.getString("reference_code");
            logEvent("referenceCode ", referenceCode);
        } catch (Exception e)
        {
            throw new RuntimeException("Failed to save validation status", e);
        }
        return referenceCode;
    }

    // Helper method to log SQL with parameters
    private void logSQLWithParameters(String sql, Object[] params)
    {

        System.out.println("SQL to execute:");
        System.out.println(sql);
        System.out.println("\nParameters:");


        for (int i = 0; i < params.length; i++)
        {
            System.out.printf("%2d: %-8s -> %s%n",
                    i + 1,
                    params[i] != null ? params[i].getClass().getSimpleName() : "NULL",
                    params[i]);
        }

        // Log a formatted version with values substituted (for debugging only)
        System.out.println("\nFormatted SQL (debug only):");
        System.out.println(formatSQLWithValues(sql, params));
    }


    private String formatSQLWithValues(String sql, Object[] params)
    {
        String formatted = sql;
        for (Object param : params)
        {
            String value;
            if (param == null)
            {
                value = "NULL";
            }
            else if (param instanceof String || param instanceof Character)
            {
                value = "'" + param.toString().replace("'", "''") + "'";
            }
            else if (param instanceof java.util.Date || param instanceof java.sql.Date || param instanceof java.sql.Timestamp)
            {
                value = "TO_TIMESTAMP('" + param + "', 'YYYY-MM-DD HH24:MI:SS.FF')";
            }
            else
            {
                value = param.toString();
            }
            formatted = formatted.replaceFirst("\\?", value);
        }
        return formatted;
    }

    public List<Map<String, Object>> getRecentStatuses(String dataRef, int days)
    {
        return jdbcTemplate.queryForList(
                "SELECT * FROM crb_validation_status " +
                        "WHERE data_ref = ? AND created_at >= SYSDATE - ? " +
                        "ORDER BY created_at DESC",
                dataRef, days
        );
    }

    //    public List<Map<String, Object>> queryListItems(String query)
//    {
//        return jdbcTemplate.queryForList(query)
//                .stream()
//                .map(row -> {
//                    Map<String, Object> newRow = new HashMap<>();
//                    row.forEach((key, value) -> {
//                        String snakeKey = convertToSnakeCase(key); // Implement this helper
//                        newRow.put(snakeKey, value);
//                    });
//                    return newRow;
//                })
//                .collect(Collectors.toList());
//    }

    public String getReportReferenceByCode(String reportType)
    {
        String sql = "SELECT TRX_ID  FROM CRB_REQUEST_DATA " +
                "WHERE report_type = ?  ORDER BY created_date DESC " +
                "FETCH FIRST 1 ROWS ONLY";

        try
        {
            logSqlWithParameters(sql, reportType);
            return jdbcTemplate.queryForObject(sql, String.class, reportType);
        } catch (Exception e)
        {
            e.printStackTrace();
            return "null";
        }
    }

//    public List<Map<String, Object>> queryListOrderedItems(String query)
//    {
//        return jdbcTemplate.query(
//                query,
//                (rs, rowNum) -> {
//                    ResultSetMetaData metaData = rs.getMetaData();
//                    int columnCount = metaData.getColumnCount();
//                    Map<String, Object> row = new LinkedHashMap<>(); // Preserves insertion order
//
//                    for (int i = 1; i <= columnCount; i++)
//                    {
//                        String columnName = metaData.getColumnName(i).toLowerCase();
//                        Object columnValue = rs.getObject(i);
//                        row.put(columnName, columnValue);
//                    }
//                    return row;
//                }
//        );
//    }
    public List<Map<String, Object>> queryListOrderedItems(String query) {
        return jdbcTemplate.query(
                query,
                (rs, rowNum) -> {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    // Maintain insertion order exactly as DB returns
                    Map<String, Object> row = new LinkedHashMap<>();

                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i).toLowerCase();
                        Object columnValue = rs.getObject(i);
                        row.put(columnName, columnValue);
                    }

                    return row;   // No sorting needed
                }
        );
    }

    public List<Map<String, Object>> queryListItems(String query)
    {
        if (axWorker.processCsv.equals("Y"))
        {
            return queryListOrderedItems(query);
        }
        return jdbcTemplate.query(
                query,
                (rs, rowNum) -> {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    // Temporary map to hold columns (unsorted)
                    Map<String, Object> tempRow = new HashMap<>();

                    for (int i = 1; i <= columnCount; i++)
                    {
                        String columnName = metaData.getColumnName(i).toLowerCase();
                        Object columnValue = rs.getObject(i);
                        tempRow.put(columnName, columnValue);
                    }

                    // sort keys alphabetically and insert into LinkedHashMap
                    Map<String, Object> sortedRow = new LinkedHashMap<>();
                    tempRow.keySet().stream()
                            .sorted() // sorts (a → z)
                            .forEach(key -> sortedRow.put(key, tempRow.get(key)));

                    return sortedRow;
                }
        );
    }

    public List<Map<String, Object>> queryListItemsMaintainCase(String query)
    {
        if (axWorker.processCsv.equals("Y"))
        {
            return queryListOrderedItems(query);
        }
        return jdbcTemplate.query(
                query,
                (rs, rowNum) -> {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    // Temporary map to hold columns (unsorted)
                    Map<String, Object> tempRow = new HashMap<>();

                    for (int i = 1; i <= columnCount; i++)
                    {
                        String columnName = metaData.getColumnName(i);
                        Object columnValue = rs.getObject(i);
                        tempRow.put(columnName, columnValue);
                    }

                    // sort keys alphabetically and insert into LinkedHashMap
                    Map<String, Object> sortedRow = new LinkedHashMap<>();
                    tempRow.keySet().stream()
                            .sorted() // sorts (a → z)
                            .forEach(key -> sortedRow.put(key, tempRow.get(key)));

                    return sortedRow;
                }
        );
    }
    public void batchInsert(List<String> jsonResponses)
    {
        jdbcTemplate.batchUpdate(
                "INSERT INTO crb_validation_status (...) VALUES (?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                new BatchPreparedStatementSetter()
                {
                    public void setValues(PreparedStatement ps, int i) throws SQLException
                    {
                        JSONObject root = new JSONObject(jsonResponses.get(i));
                        JSONObject data = root.getJSONArray("data").getJSONObject(0);
                        // Set all parameters...

                        ps.setString(1, root.getString("trx_id"));
                        ps.setString(2, data.optString("reference_code", null));
                        ps.setString(3, data.getString("data_ref"));
                        ps.setInt(4, data.optInt("liveStatus", 0));
                        ps.setInt(5, data.optInt("records_count", 0));
                        ps.setInt(6, data.optInt("records_rejected", 0));
                        ps.setInt(7, data.getInt("status"));
                        ps.setInt(8, root.getBoolean("success") ? 1 : 0);
                        ps.setInt(9, root.getBoolean("has_error") ? 1 : 0);

                    }

                    public int getBatchSize()
                    {
                        return jsonResponses.size();
                    }
                }
        );
    }

    private String convertToSnakeCase(String key)
    {
        return key.toLowerCase();
    }

    public void saveCrbCallBackResponse(ResponseData requestDTO)
    {
        String sql = "INSERT INTO " + this.axWorker.channelSchemaName + ".CRB_RESPONSE_DATA ( request_code, reference_code, template_version, status, record_count, records_passed, records_rejected) VALUES (?, ?, ?, ?, ?, ?, ?)";
        this.jdbcTemplate.update(sql, new Object[]{requestDTO.getRequestCode(), requestDTO.getReferenceCode(), requestDTO.getTemplateVersion(), requestDTO.getStatus(), requestDTO.getRecordCount(), requestDTO.getRecordsPassed(), requestDTO.getRecordsRejected()});
    }

    public void saveCrbRequest(ResponseData requestDTO)
    {
        String sql = "INSERT INTO " + this.axWorker.channelSchemaName + ".CRB_REQUEST_DATA ( request_code, reference_code, template_version, status, record_count, records_passed, records_rejected) VALUES (?, ?, ?, ?, ?, ?, ?)";
        this.jdbcTemplate.update(sql, new Object[]{requestDTO.getRequestCode(), requestDTO.getReferenceCode(), requestDTO.getTemplateVersion(), requestDTO.getStatus(), requestDTO.getRecordCount(), requestDTO.getRecordsPassed(), requestDTO.getRecordsRejected()});
    }

    public void saveErrorLog(String jsonString, String reportType)
    {
        try
        {
            // Parse JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonString);

            // Extract main fields
            String trxId = rootNode.get("trx_id").asText();
            boolean success = rootNode.get("success").asBoolean();
            boolean hasError = rootNode.get("has_error").asBoolean();
            int apiCode = rootNode.get("api_code").asInt();
            String apiCodeDescription = rootNode.get("api_code_description").asText();

            // Check for "data" array
            JsonNode dataArray = rootNode.get("data");
            if (dataArray != null && dataArray.isArray())
            {
                for (JsonNode dataItem : dataArray)
                {
                    String valueProvided = JsonUtils.getText(dataItem, "value_provided");//dataItem.get("value_provided").asText(null);
                    int dataRuleIdTypeId = JsonUtils.getInt(dataItem, "data_rule_id_type_id", 0);//dataItem.get("data_rule_id_type_id").asInt();
                    String errorDescription = JsonUtils.getText(dataItem, "error_description");//dataItem.get("error_description").asText();
                    String clientNumber = JsonUtils.getText(dataItem, "client_number");// dataItem.get("client_number").asText();
                    String accountNumber = JsonUtils.getText(dataItem, "account_number");// dataItem.get("account_number").asText();

                    jdbcTemplate.update(
                            "INSERT INTO crb_error_logs (trx_id, success, has_error, api_code, api_code_description, " +
                                    "value_provided, data_rule_id_type_id, error_description,client_number,account_number,REPORT_TYPE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            trxId, success, hasError, apiCode, apiCodeDescription, valueProvided, dataRuleIdTypeId, errorDescription, clientNumber, accountNumber, reportType
                    );
                }
            }
            else
            {
                // If no "data" array, insert main fields only
                jdbcTemplate.update(
                        "INSERT INTO crb_error_logs (trx_id, success, has_error, api_code, api_code_description,REPORT_TYPE) VALUES (?, ?, ?, ?, ?, ?)",
                        trxId, success, hasError, apiCode, apiCodeDescription, reportType
                );
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error saving error logs", e);
        }
    }

    public void saveErrorLog_old(String jsonResponse) throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        String trxId = rootNode.path("trx_id").asText();

        // Extract common header values
        boolean success = rootNode.path("success").asBoolean();
        boolean hasError = rootNode.path("has_error").asBoolean();
        int apiCode = rootNode.path("api_code").asInt();
        String apiCodeDescription = rootNode.path("api_code_description").asText();

        // Process content array with batch insert
        JsonNode contentNode = rootNode.path("data").path("content");

        if (contentNode.isArray())
        {
            // Print the base SQL statement
            String sql = "INSERT INTO crb_error_logs (" +
                    "trx_id, success, has_error, api_code, api_code_description, " +
                    "column_name, column_index, row_index, value_provided, " +
                    "data_rule_id_type_id, error_description, client_number, account_number" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            System.out.println("SQL to be executed:");
            System.out.println(sql);
            System.out.println("\nParameters for each row:");


            jdbcTemplate.batchUpdate(
                    sql,
                    new BatchPreparedStatementSetter()
                    {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException
                        {
                            JsonNode itemNode = contentNode.get(i);

                            // Prepare parameter values
                            Object[] params = {
                                    // Header values
                                    trxId,
                                    success ? 1 : 0,
                                    hasError ? 1 : 0,
                                    apiCode,
                                    apiCodeDescription,

                                    // Content values
                                    itemNode.path("column_name").asText(),
                                    itemNode.path("column_index").asInt(),
                                    itemNode.path("row_index").asInt(),
                                    itemNode.path("value_provided").asText(),
                                    itemNode.path("data_rule_id_type_id").asInt(),
                                    itemNode.path("error_description").asText(),
                                    itemNode.path("client_number").asText(),
                                    itemNode.path("account_number").asText()
                            };


                            //System.out.println("Formatted SQL for row " + (i+1) + ":");
                            System.out.println(formatSQLWithValues(sql, params));
                            System.out.println("\nRow " + (i + 1) + " parameters:");
                            for (int j = 0; j < params.length; j++)
                            {
                                System.out.printf("  Param %2d: %s%n", j + 1,
                                        params[j] != null ? params[j].toString() : "NULL");
                            }


                            ps.setString(1, (String) params[0]);
                            ps.setInt(2, (Integer) params[1]);
                            ps.setInt(3, (Integer) params[2]);
                            ps.setInt(4, (Integer) params[3]);
                            ps.setString(5, (String) params[4]);
                            ps.setString(6, (String) params[5]);
                            ps.setInt(7, (Integer) params[6]);
                            ps.setInt(8, (Integer) params[7]);
                            ps.setString(9, (String) params[8]);
                            ps.setInt(10, (Integer) params[9]);
                            ps.setString(11, (String) params[10]);
                            ps.setString(12, (String) params[11]);
                            ps.setString(13, (String) params[12]);
                        }

                        @Override
                        public int getBatchSize()
                        {
                            int size = contentNode.size();
                            System.out.println("\nTotal rows to insert: " + size);
                            return size;
                        }
                    }
            );
            System.out.println("Batch insert completed successfully");
        }
        else
        {
            String sql = "INSERT INTO crb_error_logs (" +
                    "trx_id, success, has_error, api_code, api_code_description" +
                    ") VALUES (?, ?, ?, ?, ?)";

            Object[] params = {
                    trxId,
                    success ? 1 : 0,
                    hasError ? 1 : 0,
                    apiCode,
                    apiCodeDescription};

            System.out.println("SQL to be executed:");
            System.out.println(sql);
            System.out.println("\nParameters for each row:");

            logSQLWithParameters(sql, params);

            // Execute the query
            jdbcTemplate.update(sql, params);

        }
    }

    private String formatSQLWithValues2(String sql, Object[] params)
    {
        String formatted = sql;
        for (Object param : params)
        {
            String value;
            if (param == null)
            {
                value = "NULL";
            }
            else if (param instanceof String)
            {
                value = "'" + param.toString().replace("'", "''") + "'";
            }
            else if (param instanceof java.util.Date)
            {
                value = "TO_DATE('" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(param) + "', 'YYYY-MM-DD HH24:MI:SS')";
            }
            else
            {
                value = param.toString();
            }
            formatted = formatted.replaceFirst("\\?", value);
        }
        return formatted;
    }

    private void logSqlWithParameters(String sql, Object... params)
    {
        logEvent("SQL: %s", sql);
        for (int i = 0; i < params.length; i++)
        {
            String paramLog = String.format("Parameter %d: %s", i + 1, params[i]);
            logEvent(paramLog);
        }
    }
}