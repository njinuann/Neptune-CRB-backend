SELECT
  parent.id   AS second_level_office_id,
  parent.name AS second_level_office_name,
  IFNULL(periods.currencyName, periods.currency) AS currency,
  periods.period_no AS `Weeks In Arrears (Up To)`,
  IFNULL(agg.loan_count, 0) AS `No Of Loans`,
  IFNULL(agg.prinPaid, 0.0) AS `Principal Paid`,
  IFNULL(agg.intPaid, 0.0) AS `Interest Paid`
FROM
  -- second-level parents (children of root/head office)
  (SELECT id, name, hierarchy
   FROM m_office
   WHERE parent_id = 1
  ) parent
CROSS JOIN
  -- periods x currencies grid (only currencies that have active loans)
  (
    SELECT pers.period_no, pers.pid, curs.code AS currency, curs.name AS currencyName
    FROM
      (SELECT 'On Schedule' AS period_no, 1 AS pid UNION ALL
       SELECT '1',2 UNION ALL SELECT '2',3 UNION ALL SELECT '3',4 UNION ALL
       SELECT '4',5 UNION ALL SELECT '5',6 UNION ALL SELECT '6',7 UNION ALL
       SELECT '7',8 UNION ALL SELECT '8',9 UNION ALL SELECT '9',10 UNION ALL
       SELECT '10',11 UNION ALL SELECT '11',12 UNION ALL SELECT '12',13 UNION ALL
       SELECT '12+',14
      ) pers
    CROSS JOIN
      (
        SELECT DISTINCT ml.currency_code AS code, moc.name
        FROM m_loan ml
        JOIN m_organisation_currency moc ON moc.code = ml.currency_code
        WHERE ml.loan_status_id = 300
      ) curs
  ) periods
LEFT JOIN
  -- aggregated loans rolled up to the second-level parent
  (
    SELECT
      parent.id AS parent_office_id,
      ml.currency_code AS currency,
      CASE
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 1 THEN 'On Schedule'
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 8 THEN '1'
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 15 THEN '2'
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 22 THEN '3'
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 29 THEN '4'
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 36 THEN '5'
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 43 THEN '6'
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 50 THEN '7'
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 57 THEN '8'
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 64 THEN '9'
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 71 THEN '10'
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 78 THEN '11'
        WHEN DATEDIFF(CURDATE(), IFNULL(laa.overdue_since_date_derived, CURDATE())) < 85 THEN '12'
        ELSE '12+'
      END AS arrPeriod,
      COUNT(DISTINCT ml.id) AS loan_count,
      SUM(ml.principal_disbursed_derived) AS principal,
      SUM(ml.principal_repaid_derived) AS prinPaid,
      SUM(ml.interest_repaid_derived) AS intPaid
    FROM m_office parent
    JOIN m_office child
      ON child.hierarchy LIKE CONCAT(parent.hierarchy, '%')
     AND child.id <> parent.id
    JOIN m_client mc ON mc.office_id = child.id
    JOIN m_loan ml ON ml.client_id = mc.id
                  AND ml.loan_status_id = 300
    LEFT JOIN m_loan_arrears_aging laa ON laa.loan_id = ml.id
    WHERE parent.parent_id = 1
    GROUP BY parent.id, ml.currency_code, arrPeriod
  ) agg
  ON agg.parent_office_id = parent.id
 AND agg.currency = periods.currency
 AND agg.arrPeriod = periods.period_no

ORDER BY parent.name, periods.currency, periods.pid;
