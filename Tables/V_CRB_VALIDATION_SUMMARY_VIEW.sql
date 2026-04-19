-- CHANNELMANAGER.V_CRB_VALIDATION_SUMMARY_VIEW source

CREATE OR REPLACE FORCE EDITIONABLE VIEW "CHANNELMANAGER"."V_CRB_VALIDATION_SUMMARY_VIEW" ("CATEGORY", "RECORDS_COUNT", "RECORD_BATCHES", "REJECTED_COUNT", "IMPORTED_COUNT", "PASSED_COUNT", "PCT_REJECTED", "PCT_IMPORTED", "PCT_PASSED", "SHARE_OF_TOTAL") AS 
  WITH base AS (
    SELECT 
        DATA_REF AS category,
        SUM(RECORDS_COUNT) AS records_count,
        SUM(RECORDS_REJECTED) AS rejected_count,
        SUM(RECORDS_IMPORTED) AS imported_count,
        SUM(RECORDS_PASSED) AS passed_count,
        COUNT(*) AS record_batches
    FROM CHANNELMANAGER.CRB_VALIDATION_STATUS
    GROUP BY DATA_REF
),
totals AS (
    SELECT
        SUM(records_count) AS total_records,
        SUM(record_batches) AS total_batches,
        SUM(rejected_count) AS total_rejected,
        SUM(imported_count) AS total_imported,
        SUM(passed_count) AS total_passed
    FROM base
),
detailed AS (
    SELECT
        b.category,
        b.records_count,
        b.record_batches,
        b.rejected_count,
        b.imported_count,
        b.passed_count,
        ROUND(b.rejected_count / NULLIF(b.records_count, 0) * 100, 2) AS pct_rejected,
        ROUND(b.imported_count / NULLIF(b.records_count, 0) * 100, 2) AS pct_imported,
        ROUND(b.passed_count / NULLIF(b.records_count, 0) * 100, 2) AS pct_passed,
        ROUND(b.records_count / NULLIF(t.total_records, 0) * 100, 2) AS share_of_total
    FROM base b
    CROSS JOIN totals t
),
grand_total AS (
    SELECT
        'TOTAL' AS category,
        t.total_records AS records_count,
        t.total_batches AS record_batches,
        t.total_rejected AS rejected_count,
        t.total_imported AS imported_count,
        t.total_passed AS passed_count,
        ROUND(t.total_rejected / NULLIF(t.total_records, 0) * 100, 2) AS pct_rejected,
        ROUND(t.total_imported / NULLIF(t.total_records, 0) * 100, 2) AS pct_imported,
        ROUND(t.total_passed / NULLIF(t.total_records, 0) * 100, 2) AS pct_passed,
        100.00 AS share_of_total
    FROM totals t
)
SELECT "CATEGORY","RECORDS_COUNT","RECORD_BATCHES","REJECTED_COUNT","IMPORTED_COUNT","PASSED_COUNT","PCT_REJECTED","PCT_IMPORTED","PCT_PASSED","SHARE_OF_TOTAL" FROM detailed
UNION ALL
SELECT "CATEGORY","RECORDS_COUNT","RECORD_BATCHES","REJECTED_COUNT","IMPORTED_COUNT","PASSED_COUNT","PCT_REJECTED","PCT_IMPORTED","PCT_PASSED","SHARE_OF_TOTAL" FROM grand_total;