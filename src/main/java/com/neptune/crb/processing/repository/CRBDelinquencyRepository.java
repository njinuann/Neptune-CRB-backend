package com.neptune.crb.processing.repository;

import com.neptune.crb.processing.entity.CRBDelinquencyRowMapper;
import com.neptune.crb.processing.model.CRBDelinquencyRecord;
import com.neptune.crb.processing.model.CreditApplicationData;
import com.neptune.crb.processing.util.AXWorker;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.neptune.crb.processing.util.Logger.logEvent;

@Slf4j
@Repository
public class CRBDelinquencyRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CRBDelinquencyRowMapper rowMapper;
    private final AXWorker axWorker;

    public CRBDelinquencyRepository(JdbcTemplate jdbcTemplate, CRBDelinquencyRowMapper rowMapper, AXWorker axWorker) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
        this.axWorker = axWorker;
    }

    public void save(CRBDelinquencyRecord record) {
        String sql = """
                INSERT INTO crb_delinquency_records (
                    transaction_id, identity_number, identity_type, loan_amount,
                    api_code, api_code_description, delinquency_code, delinquency_description,
                    delinquency_summary, listing_status, has_error, raw_request,
                    raw_response, user_id, query_time, response_time_ms
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql,
                record.getTransactionId(),
                record.getIdentityNumber(),
                record.getIdentityType(),
                record.getLoanAmount(),
                record.getApiCode(),
                record.getApiCodeDescription(),
                record.getDelinquencyCode(),
                record.getDelinquencyDescription(),
                record.getDelinquencySummary(),
                record.getListingStatus(),
                record.getHasError(),
                record.getRawRequest(),
                record.getRawResponse(),
                record.getUserId(),
                Timestamp.valueOf(record.getQueryTime()),
                record.getResponseTimeMs()
        );

        logEvent("Saved CRB record for transaction: {}", record.getTransactionId());
    }

    public Optional<CRBDelinquencyRecord> findByTransactionId(String transactionId) {
        String sql = "SELECT * FROM " + axWorker.channelSchemaName + ".crb_delinquency_records WHERE transaction_id = ?";
        List<CRBDelinquencyRecord> results = jdbcTemplate.query(sql, rowMapper, transactionId);
        return results.stream().findFirst();
    }

    public List<CRBDelinquencyRecord> findByIdentityNumber(String identityNumber) {
        String sql = "SELECT * FROM " + axWorker.channelSchemaName + ".crb_delinquency_records WHERE identity_number = ? ORDER BY query_time DESC";
        return jdbcTemplate.query(sql, rowMapper, identityNumber);
    }

    public List<CRBDelinquencyRecord> findByUserId(String userId) {
        String sql = "SELECT * FROM " + axWorker.channelSchemaName + ".crb_delinquency_records WHERE user_id = ? ORDER BY query_time DESC";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    public CreditApplicationData fetchLoanDataByCreditAppID(Long creditApplicationId) {
        String schema = axWorker.coreSchemaName;

        // DEBUG: Log the schema
        logEvent("Schema:", schema);
        logEvent("Application ID type:", creditApplicationId.getClass().getName());
        logEvent("Application ID value:", creditApplicationId);
//        String sql = "SELECT P.SOCIAL_SECURITY_NO, CA.PRIME_LIMIT_AMT " +
//                " FROM " + axWorker.coreSchemaName + ".CREDIT_APPL CA JOIN " + axWorker.coreSchemaName + ".PERSON P " +
//                " ON CA.CUST_ID=P.CUST_ID WHERE APPL_ID = ?";
        String sql = "SELECT P.SOCIAL_SECURITY_NO AS socialSecurityNo, " +
                "CA.PRIME_LIMIT_AMT AS primeLimitAmt " +
                "FROM " + schema.toUpperCase() + ".CREDIT_APPL CA " +
                "JOIN " + schema.toUpperCase() + ".PERSON P ON CA.CUST_ID = P.CUST_ID " +
                "WHERE CA.APPL_ID = ?";
        try {
            logEvent("fetch creditType ", sql);
            logEvent("fetch cr appl id ", creditApplicationId);
            return jdbcTemplate.queryForObject(
                    sql,
                    this::mapLoanDataRow,
                    creditApplicationId
            );
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
//public CreditApplicationData fetchLoanDataByCreditAppID(Long creditApplicationId) {
//    String schema = axWorker.coreSchemaName;
//
//    // DEBUG: Log the schema
//    logEvent("Schema:", schema);
//    logEvent("Application ID type:", creditApplicationId.getClass().getName());
//    logEvent("Application ID value:", creditApplicationId);
//
//    // DEBUG: Try with explicit type mapping
//    String sql = "SELECT P.SOCIAL_SECURITY_NO AS socialSecurityNo, " +
//            "CA.PRIME_LIMIT_AMT AS primeLimitAmt " +
//            "FROM " + schema.toUpperCase() + ".CREDIT_APPL CA " +
//            "JOIN " + schema.toUpperCase() + ".PERSON P ON CA.CUST_ID = P.CUST_ID " +
//            "WHERE CA.APPL_ID = ?";
//
//    try {
//        logEvent("sql:", sql);
//        // DEBUG: Try queryForList to see if any rows are returned
//        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, creditApplicationId);
//        logEvent("Number of results:", results.size());
//
//        if (results.isEmpty()) {
//            // Try with the exact same SQL you used manually
//            logEvent("No results found with parameter: " + creditApplicationId);
//
//            // DEBUG: Try a count query
//            String countSql = "SELECT COUNT(*) FROM " + schema + ".CREDIT_APPL WHERE APPL_ID = ?";
//            Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, creditApplicationId);
//            logEvent("Count of records with this APPL_ID:", count);
//
//            if (count == 0) {
//                // Get some sample IDs to see what's in the table
//                String sampleSql = "SELECT APPL_ID FROM " + schema + ".CREDIT_APPL FETCH FIRST 5 ROWS ONLY";
//                List<Long> sampleIds = jdbcTemplate.queryForList(sampleSql, Long.class);
//                logEvent("Sample APPL_IDs in table:", sampleIds);
//            }
//
//            return null; // or throw a more specific exception
//        }
//
//        return jdbcTemplate.queryForObject(
//                sql,
//                this::mapLoanDataRow,
//                creditApplicationId
//        );
//    } catch (DataAccessException e) {
//        logEvent("Error details:", e.getMessage());
//        throw new RuntimeException(e);
//    }
//}
    public CreditApplicationData mapLoanDataRow(ResultSet rs, int rowNum) throws SQLException {
        CreditApplicationData record = new CreditApplicationData();
        record.setIdNumber(rs.getString("socialSecurityNo"));
        record.setLoanAmount(rs.getBigDecimal("primeLimitAmt"));
        record.setIdType("001");
        return record;
    }

//    public CreditApplicationData mapLoanDataRow(ResultSet rs, int rowNum) throws SQLException {
////P.SOCIAL_SECURITY_NO, CA.PRIME_LIMIT_AMT
//        CreditApplicationData record = new CreditApplicationData();
//
//        record.setIdNumber(rs.getString("SOCIAL_SECURITY_NO"));
//        record.setLoanAmount(rs.getBigDecimal("PRIME_LIMIT_AMT"));
//        record.setIdType("001");
//
//        return record;
//    }
}
