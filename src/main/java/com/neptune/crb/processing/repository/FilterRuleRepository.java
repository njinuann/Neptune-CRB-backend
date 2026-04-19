package com.neptune.crb.processing.repository;

import com.neptune.crb.processing.model.FilterRule;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 15-Apr-26
 */
@Repository
public class FilterRuleRepository
{

 private final JdbcTemplate jdbcTemplate;

 // RowMapper
 private final RowMapper<FilterRule> rowMapper = (rs, rowNum) -> {
  FilterRule rule = new FilterRule();
  rule.setId(rs.getString("id"));
  rule.setName(rs.getString("name"));
  rule.setCategory(rs.getString("category"));
  rule.setField(rs.getString("field"));
  rule.setOperator(rs.getString("operator"));
  rule.setValue(rs.getString("value"));
  rule.setActive(rs.getString("active"));
  rule.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
  return rule;
 };

    public FilterRuleRepository(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -----------------------------------
 // 1. GET (with pagination)
 // -----------------------------------
 public List<FilterRule> findAll(int offset, int limit)
 {
  String sql = """
            SELECT * FROM crb_report_filter_rules
            ORDER BY created_at DESC
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
        """;

  return jdbcTemplate.query(sql, rowMapper, offset, limit);
 }

 public int countAll()
 {
  String sql = "SELECT COUNT(*) FROM crb_report_filter_rules";
  return jdbcTemplate.queryForObject(sql, Integer.class);
 }

 // -----------------------------------
 // 2. GET Active Rules
 // -----------------------------------
 public List<FilterRule> findActive()
 {
  String sql = """
            SELECT * FROM crb_report_filter_rules
            WHERE active = 'Y'
        """;

  return jdbcTemplate.query(sql, rowMapper);
 }

 // -----------------------------------
 // 3. FIND BY ID
 // -----------------------------------
 public Optional<FilterRule> findById(String id)
 {
  String sql = """
            SELECT * FROM crb_report_filter_rules
            WHERE id = ?
        """;

  List<FilterRule> list = jdbcTemplate.query(sql, rowMapper, id);
  return list.stream().findFirst();
 }

 // -----------------------------------
 // 4. CREATE
 // -----------------------------------
 public int save(FilterRule rule)
 {
  String sql = """
            INSERT INTO crb_report_filter_rules
            (id, name, category, field, operator, value, active, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

  return jdbcTemplate.update(sql,
          rule.getId(),
          rule.getName(),
          rule.getCategory(),
          rule.getField(),
          rule.getOperator(),
          rule.getValue(),
          rule.getActive(),
          Timestamp.valueOf(rule.getCreatedAt())
  );
 }

 // -----------------------------------
 // 5. UPDATE
 // -----------------------------------
 public int update(String id, FilterRule rule)
 {
  String sql = """
            UPDATE crb_report_filter_rules
            SET name = ?, category = ?, field = ?, operator = ?, value = ?, active = ?
            WHERE id = ?
        """;

  return jdbcTemplate.update(sql,
          rule.getName(),
          rule.getCategory(),
          rule.getField(),
          rule.getOperator(),
          rule.getValue(),
          rule.getActive(),
          id
  );
 }

 // -----------------------------------
 // 6. DELETE
 // -----------------------------------
 public int delete(String id)
 {
  String sql = """
            DELETE FROM crb_report_filter_rules
            WHERE id = ?
        """;

  return jdbcTemplate.update(sql, id);
 }
}