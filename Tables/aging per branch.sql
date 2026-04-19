-- CHANNELMANAGER.V_MICRO_LOAN_DATA source

CREATE OR REPLACE
FORCE EDITIONABLE VIEW "CHANNELMANAGER"."V_MICRO_LOAN_DATA" ("CREATE_DT",
"DAYS_ELAPSED",
"CANSPLIT",
"TERM_CD",
"TERM_VALUE",
"PROD_ID",
"PROD_CD",
"PROD_DESC",
"CUST_ID",
"ACCT_NO",
"INDEX_RATE_ID",
"INDEX_RATE",
"FULL_INTEREST",
"DISBURSEMENT_LIMIT",
"FULL_TERM_INTEREST",
"HALF_INTEREST_RATE",
"HALF_INTEREST",
"FULL_TERM_REPAYMENT",
"HALF_TERM_REPAYMENT",
"PRINCIPAL_BAL") AS 
  SELECT
	create_dt,
	DAYS_ELAPSED,
	CANSPLIT,
	TERM_CD,
	TERM_VALUE,
	prod_id,
	PROD_CD,
	PROD_DESC,
	cust_id,
	acct_no,
	INDEX_RATE_ID,
	index_Rate,
	full_Interest,
	disbursement_limit,
	full_Term_Interest,
	half_Interest_Rate,
	half_Interest,
	full_Term_Interest + disbursement_limit + penalty
             AS full_term_repayment,
	half_Interest + disbursement_limit AS half_term_repayment,
	principal_bal
FROM
	(
	SELECT
		start_dt AS create_dt,
		(
		SELECT
			COUNT (*)
		FROM
			(
			SELECT
				TRUNC (start_dt) + LEVEL - 1 AS date_value
			FROM
				DUAL
			CONNECT BY
				TRUNC (start_dt) + LEVEL - 1 <=
                                         TRUNC (
                                            (
				SELECT
					TO_DATE (DISPLAY_VALUE,
					'DD/MM/YYYY')
				FROM
					testing.CTRL_PARAMETER
				WHERE
					PARAM_CD = 'S02')))
		WHERE
			TO_CHAR (date_value,
			'D') != '7')
                     AS days_elapsed,
		CASE
			WHEN ( prod_id = 74
				OR prod_id = 77
				OR prod_id = 76
				OR prod_id = 33
				OR prod_id = 128)
                     THEN
                        'SPLIT'
			ELSE
                        'FULL'
		END
                     AS CANSPLIT,
		TERM_CD,
		TERM_VALUE,
		prod_id,
		PROD_CD,
		PROD_DESC,
		cust_id,
		acct_no,
		INDEX_RATE_ID,
		index_Rate,
		( (index_Rate / 12) / 100) full_Interest,
		disbursement_limit,
		CASE
			WHEN prod_id = 128
                     THEN
                        disbursement_limit * ( (index_Rate) / 100)
			ELSE
                        disbursement_limit * ( (index_Rate / 12) / 100)
		END
                     AS full_Term_Interest,
		half_Interest_Rate,
		(disbursement_limit * half_Interest_Rate) AS half_Interest,
		penalty,
		principal_bal
	FROM
		(
		SELECT
			la.start_dt,
			LA.TERM_CD,
			LA.TERM_VALUE,
			ac.prod_id,
			PR.PROD_CD,
			PR.PROD_DESC,
			ac.cust_id,
			las.acct_no,
			la.disbursement_limit,
			lai.INDEX_RATE_ID,
			CASE
				WHEN las.CALCULATED_EFFECTIVE_INT_RATE = 0
					OR las.CALCULATED_EFFECTIVE_INT_RATE
                                        IS NULL
                             THEN
                                IR.INDEX_RATE
                             /* (  IR.INDEX_RATE
                               + NVL (lai.MARGIN_RATE, 0) / 100)*/
					ELSE
                                las.CALCULATED_EFFECTIVE_INT_RATE
				END
                             AS index_Rate,
				DECODE (ac.prod_id,
				33,
				0.12,
				74,
				0.1,
				75,
				0.15,
				76,
				0.1,
				77,
				0.12,
				78,
				0.1,
				79,
				0.06,
				80,
				0.12,
				84,
				0.12,
				86,
				0.12,
				128,
				0.24)
                              half_Interest_Rate,
				NVL (
                             (
				SELECT
					SUM (le.amt_unpaid)
				FROM
					testing.ln_acct_repmnt_event le
				WHERE
					le.acct_id = las.acct_id
					AND le.event_type = 'CHARGE'
					AND le.rec_st IN ('N', 'P')),
                             0)
                             penalty,
                          -las.ledger_bal AS principal_bal
			FROM
				testing.loan_account_summary las,
				testing.loan_account_interest lai,
				testing.loan_account la,
				testing.account ac,
				testing.index_rate ir,
				testing.PRODUCT PR
			WHERE
				las.acct_id = la.acct_id
				AND las.acct_id = lai.acct_id
				AND lai.acct_id = la.acct_id
				AND la.acct_id = ac.acct_id
				AND PR.PROD_ID = AC.PROD_ID
				AND las.UNDISBURSED_BAL = 0
				AND lai.INDEX_RATE_ID = IR.INDEX_RATE_ID
						  ));