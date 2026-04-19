package com.neptune.crb.processing.repository;

import com.neptune.crb.processing.model.CrbReportType;
import com.neptune.crb.processing.util.AXWorker;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CrbReportTypeRepository
{
    private final JdbcTemplate jdbcTemplate;
    private final AXWorker axWorker;

    public CrbReportTypeRepository(JdbcTemplate jdbcTemplate, AXWorker axWorker)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.axWorker = axWorker;
    }

    public List<CrbReportType> getAllReportTypes()
    {
        String sql = "SELECT REPORT_TYPE, REPORT_NM, ENABLED, PRIORITY, SCHEDULE_TYPE, " +
                "LAST_RUN_DATE, CREATED_DATE, LAST_UPDATED_DATE " +
                "FROM " + axWorker.channelSchemaName + ".CRB_REPORT_TYPE " +
                "ORDER BY PRIORITY, REPORT_TYPE";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapReportType(rs));
    }

    public List<CrbReportType> getAllActiveReportTypes()
    {
        String sql = "SELECT REPORT_TYPE, REPORT_NM, ENABLED, PRIORITY, SCHEDULE_TYPE, " +
                "LAST_RUN_DATE, CREATED_DATE, LAST_UPDATED_DATE " +
                "FROM " + axWorker.channelSchemaName + ".CRB_REPORT_TYPE " +
                "WHERE ENABLED = 'Y' " +
                "ORDER BY PRIORITY, REPORT_TYPE";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapReportType(rs));
    }

    public List<CrbReportType> getReportsByScheduleType(String scheduleType)
    {
        String sql = "SELECT REPORT_TYPE, REPORT_NM, ENABLED, PRIORITY, SCHEDULE_TYPE, " +
                "LAST_RUN_DATE, CREATED_DATE, LAST_UPDATED_DATE " +
                "FROM " + axWorker.channelSchemaName + ".CRB_REPORT_TYPE " +
                "WHERE ENABLED = 'Y' AND SCHEDULE_TYPE = ? " +
                "ORDER BY PRIORITY, REPORT_TYPE";
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapReportType(rs), scheduleType.toUpperCase());
    }

    public List<Map<String, String>> getActiveReportTypesAsMap()
    {
        String sql = "SELECT REPORT_TYPE, REPORT_NM, ENABLED, PRIORITY, SCHEDULE_TYPE " +
                "FROM " + axWorker.channelSchemaName + ".CRB_REPORT_TYPE " +
                "WHERE ENABLED = 'Y' " +
                "ORDER BY PRIORITY, REPORT_TYPE";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, String> reportMap = new HashMap<>();
            reportMap.put("reportType", rs.getString("REPORT_TYPE"));
            reportMap.put("reportNm", rs.getString("REPORT_NM"));
            reportMap.put("enabled", rs.getString("ENABLED"));
            reportMap.put("priority", rs.getString("PRIORITY"));
            reportMap.put("scheduleType", rs.getString("SCHEDULE_TYPE"));
            return reportMap;
        });
    }

    public int upsertReportType(String reportType, String reportNm, String enabled,
                                Integer priority, String scheduleType)
    {
        String sql = "MERGE INTO " + axWorker.channelSchemaName + ".CRB_REPORT_TYPE target " +
                "USING (SELECT ? AS REPORT_TYPE, ? AS REPORT_NM, ? AS ENABLED, " +
                "? AS PRIORITY, ? AS SCHEDULE_TYPE FROM dual) source " +
                "ON (target.REPORT_TYPE = source.REPORT_TYPE) " +
                "WHEN MATCHED THEN " +
                "    UPDATE SET " +
                "        target.REPORT_NM = source.REPORT_NM, " +
                "        target.ENABLED = source.ENABLED, " +
                "        target.PRIORITY = source.PRIORITY, " +
                "        target.SCHEDULE_TYPE = source.SCHEDULE_TYPE, " +
                "        target.LAST_UPDATED_DATE = SYSDATE " +
                "WHEN NOT MATCHED THEN " +
                "    INSERT (REPORT_TYPE, REPORT_NM, ENABLED, PRIORITY, SCHEDULE_TYPE, " +
                "            CREATED_DATE, LAST_UPDATED_DATE) " +
                "    VALUES (source.REPORT_TYPE, source.REPORT_NM, source.ENABLED, " +
                "            source.PRIORITY, source.SCHEDULE_TYPE, SYSDATE, SYSDATE)";

        return jdbcTemplate.update(sql, reportType, reportNm, enabled, priority, scheduleType);
    }

    public int upsertReportType(CrbReportType reportType)
    {
        return upsertReportType(
                reportType.getReportType(),
                reportType.getReportNm(),
                reportType.getEnabled(),
                reportType.getPriority(),
                reportType.getScheduleType()
        );
    }

    public int updateLastRunDate(String reportType)
    {
        String sql = "UPDATE " + axWorker.channelSchemaName + ".CRB_REPORT_TYPE " +
                "SET LAST_RUN_DATE = SYSDATE, LAST_UPDATED_DATE = SYSDATE " +
                "WHERE REPORT_TYPE = ?";
        return jdbcTemplate.update(sql, reportType);
    }

    public int deleteReportType(String reportType)
    {
        String sql = "DELETE FROM " + axWorker.channelSchemaName + ".CRB_REPORT_TYPE WHERE REPORT_TYPE = ?";
        return jdbcTemplate.update(sql, reportType);
    }

    public CrbReportType getReportTypeById(String reportType)
    {
        String sql = "SELECT REPORT_TYPE, REPORT_NM, ENABLED, PRIORITY, SCHEDULE_TYPE, " +
                "LAST_RUN_DATE, CREATED_DATE, LAST_UPDATED_DATE " +
                "FROM " + axWorker.channelSchemaName + ".CRB_REPORT_TYPE " +
                "WHERE REPORT_TYPE = ?";
        try
        {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapReportType(rs), reportType);
        } catch (EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    public CrbReportType getActiveReportTypeByCode(String reportType)
    {
        String sql = "SELECT REPORT_TYPE, REPORT_NM, ENABLED, PRIORITY, SCHEDULE_TYPE, " +
                "LAST_RUN_DATE, CREATED_DATE, LAST_UPDATED_DATE " +
                "FROM " + axWorker.channelSchemaName + ".CRB_REPORT_TYPE " +
                "WHERE REPORT_TYPE = ? AND ENABLED = 'Y'";
        try
        {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapReportType(rs), reportType);
        } catch (EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    public int[] batchUpsertReportTypes(List<CrbReportType> reportTypes)
    {
        String sql = "MERGE INTO " + axWorker.channelSchemaName + ".CRB_REPORT_TYPE target " +
                "USING (SELECT ? AS REPORT_TYPE, ? AS REPORT_NM, ? AS ENABLED, " +
                "? AS PRIORITY, ? AS SCHEDULE_TYPE FROM dual) source " +
                "ON (target.REPORT_TYPE = source.REPORT_TYPE) " +
                "WHEN MATCHED THEN " +
                "    UPDATE SET " +
                "        target.REPORT_NM = source.REPORT_NM, " +
                "        target.ENABLED = source.ENABLED, " +
                "        target.PRIORITY = source.PRIORITY, " +
                "        target.SCHEDULE_TYPE = source.SCHEDULE_TYPE, " +
                "        target.LAST_UPDATED_DATE = SYSDATE " +
                "WHEN NOT MATCHED THEN " +
                "    INSERT (REPORT_TYPE, REPORT_NM, ENABLED, PRIORITY, SCHEDULE_TYPE, " +
                "            CREATED_DATE, LAST_UPDATED_DATE) " +
                "    VALUES (source.REPORT_TYPE, source.REPORT_NM, source.ENABLED, " +
                "            source.PRIORITY, source.SCHEDULE_TYPE, SYSDATE, SYSDATE)";

        return jdbcTemplate.batchUpdate(sql,
                new BatchPreparedStatementSetter()
                {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException
                    {
                        CrbReportType type = reportTypes.get(i);
                        ps.setString(1, type.getReportType());
                        ps.setString(2, type.getReportNm());
                        ps.setString(3, type.getEnabled());
                        ps.setInt(4, type.getPriority());
                        ps.setString(5, type.getScheduleType());
                    }

                    @Override
                    public int getBatchSize()
                    {
                        return reportTypes.size();
                    }
                });
    }

    private CrbReportType mapReportType(java.sql.ResultSet rs) throws SQLException
    {
        CrbReportType reportType = new CrbReportType();
        reportType.setReportType(rs.getString("REPORT_TYPE"));
        reportType.setReportNm(rs.getString("REPORT_NM"));
        reportType.setEnabled(rs.getString("ENABLED"));
        reportType.setPriority(rs.getInt("PRIORITY"));
        reportType.setScheduleType(rs.getString("SCHEDULE_TYPE"));
        reportType.setLastRunDate(rs.getDate("LAST_RUN_DATE"));
        reportType.setCreatedDate(rs.getDate("CREATED_DATE"));
        reportType.setLastUpdatedDate(rs.getDate("LAST_UPDATED_DATE"));
        return reportType;
    }
}