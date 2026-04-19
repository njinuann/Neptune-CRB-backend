package com.neptune.crb.processing.entity;

import com.neptune.crb.processing.model.CRBDelinquencyRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class CRBDelinquencyRowMapper implements RowMapper<CRBDelinquencyRecord> {

    @Override
    public CRBDelinquencyRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        CRBDelinquencyRecord record = new CRBDelinquencyRecord();
        record.setId(rs.getLong("id"));
        record.setTransactionId(rs.getString("transaction_id"));
        record.setIdentityNumber(rs.getString("identity_number"));
        record.setIdentityType(rs.getString("identity_type"));
        record.setLoanAmount(rs.getInt("loan_amount"));
        record.setApiCode(rs.getString("api_code"));
        record.setApiCodeDescription(rs.getString("api_code_description"));
        record.setDelinquencyCode(rs.getString("delinquency_code"));
        record.setDelinquencyDescription(rs.getString("delinquency_description"));
        record.setDelinquencySummary(rs.getString("delinquency_summary"));
        record.setListingStatus(rs.getString("listing_status"));
        record.setHasError(rs.getBoolean("has_error"));
        record.setRawRequest(rs.getString("raw_request"));
        record.setRawResponse(rs.getString("raw_response"));
        record.setUserId(rs.getString("user_id"));
        record.setQueryTime(rs.getTimestamp("query_time").toLocalDateTime());
        record.setResponseTimeMs(rs.getInt("response_time_ms"));
        return record;
    }
}
