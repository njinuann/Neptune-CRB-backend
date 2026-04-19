package com.neptune.crb.processing.repository;

import com.neptune.crb.processing.entity.CRBDelinquencyRowMapper;
import com.neptune.crb.processing.model.CRBDelinquencyRecord;
import com.neptune.crb.processing.util.AXWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static com.neptune.crb.processing.util.Logger.logEvent;

@Slf4j
@Repository
public class CRBDelinquencyRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CRBDelinquencyRowMapper rowMapper;
    private final AXWorker axWorker;

    public CRBDelinquencyRepository(JdbcTemplate jdbcTemplate, CRBDelinquencyRowMapper rowMapper, AXWorker axWorker)
    {
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

}
