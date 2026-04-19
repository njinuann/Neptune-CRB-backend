-- WAKULIMATEST.V_NON_INDIVIDUAL_CONSUMER source
--SELECT * FROM V_CRB_NON_INDIVIDUAL_CONSUMER
CREATE OR REPLACE
FORCE EDITIONABLE VIEW "WAKULIMATEST"."V_CRB_NON_INDIVIDUAL_CONSUMER" 
("REGISTEReD_NAME",
"TRADING_NAME",
"REGISTRATION_DATE",
"REGISTRATION_NUMBER" , 
"PREVIOUS_REGISTRATION_NUMBER",
"NATIONALITY",
"CLIENT_NUMBER",
"ACCOUNT_NUMBER",
"OLD_ACCOUNT_NUMBER",
"COMPANY_TYPE",
"INDUSTRY_CODE",
"ANNUAL_TURNOVER",
"PIN_NUMBER",
"VAT_NUMBER",
"NUMBER_OF_SHARES", 
"TRADING_STATUS",
"TRADING_STATUS_DATE",
"MAIN_TELEPHONE_NUMBER",
"OTHER_TELEPHONE_NUMBER",
"POSTAL_ADDRESS1",
"POSTAL_ADDRESS2",
"TOWN",
"COUNTRY",
"POST_CODE", 
"PHYSICAL_ADDESS1",
"PHYSICAL_ADDRESS2",
"PLOT_NUMBER",
"PHYSICAL_LOCATION_TOWN",
"PHYSICAL_LOCATION_COUNTRY",
"LENDERS_REGISTERED_NAME",
"LENDERS_TRADING_NAME", 
"LENDERS_BRANCH_NAME",
"LENDERS_BRANCH_CODE",
"ACCOUNT_INDICATOR",
"ACCOUNT_HOLDER_TYPE",
"ACCOUNT_PRODUCT_TYPE",
"DATE_ACCOUNT_OPENED",
"INSTALLMENT_DUE_DATE",
"PRUDENTIAL_RISK_CLASSIFICATION", 
"ORIGINAL_AMOUNT",
"CURRENCY_FACILITY",
"AMOUNT_IN_KES",
"CURRENT_BALANCE",
"OVERDUE_BALANCE",
"OVERDUE_DATE",
"DAYS_ARREARS",
"INSTALLMENT_ARREARS",
"ACCOUNT_STATUS", 
"ACCOUNT_STATUS_DATE",
"ACCOUNT_CLOSURE_REASON",
"REPAYMENT_PERIOD",
"DEFERRED_PAYMENT_DATE",
"DEFERRED_PAYMENT_AMOUNT",
"PAYMENT_FREQUENCY",
"DISBURSEMENT_DATE",
"INSTALLMENT_AMOUNT",
"LATEST_PAYMENT_DATE",
"LATEST_PAYMENT_AMOUNT",
"SECURITY_TYPE"
) AS  
  SELECT "REGISTERED_NAME",
"TRADING_NAME",
"REGISTRATION_DATE",
"REGISTRATION_NUMBER" , 
"PREVIOUS_REGISTRATION_NUMBER",
"NATIONALITY",
"CLIENT_NUMBER",
"ACCOUNT_NUMBER",
"OLD_ACCOUNT_NUMBER",
"COMPANY_TYPE",
"INDUSTRY_CODE",
"ANNUAL_TURNOVER",
"PIN_NUMBER",
"VAT_NUMBER",
"NUMBER_OF_SHARES", 
"TRADING_STATUS",
"TRADING_STATUS_DATE",
"MAIN_TELEPHONE_NUMBER",
"OTHER_TELEPHONE_NUMBER",
"POSTAL_ADDRESS1",
"POSTAL_ADDRESS2",
"TOWN",
"COUNTRY",
"POST_CODE", 
"PHYSICAL_ADDESS1",
"PHYSICAL_ADDRESS2",
"PLOT_NUMBER",
"PHYSICAL_LOCATION_TOWN",
"PHYSICAL_LOCATION_COUNTRY",
"LENDERS_REGISTERED_NAME",
"LENDERS_TRADING_NAME", 
"LENDERS_BRANCH_NAME",
"LENDERS_BRANCH_CODE",
"ACCOUNT_INDICATOR",
"ACCOUNT_HOLDER_TYPE",
"ACCOUNT_PRODUCT_TYPE",
"DATE_ACCOUNT_OPENED",
"INSTALLMENT_DUE_DATE",
CASE 
     WHEN DAYS_ARREARS >=0 AND DAYS_ARREARS <=30 THEN 'A'
    WHEN DAYS_ARREARS >=31 AND DAYS_ARREARS <=90 THEN 'B'
    WHEN DAYS_ARREARS >=91 AND DAYS_ARREARS <=180 THEN 'C'
     WHEN DAYS_ARREARS >=180 AND DAYS_ARREARS <=360 THEN 'D'
    WHEN DAYS_ARREARS >360 THEN 'E' END AS "PRUDENTIAL_RISK_CLASSIFICATION",
"ORIGINAL_AMOUNT",
"CURRENCY_FACILITY",
"AMOUNT_IN_KES",
"CURRENT_BALANCE",
"OVERDUE_BALANCE",
"OVERDUE_DATE",
"DAYS_ARREARS",
"INSTALLMENT_ARREARS",
"ACCOUNT_STATUS", 
"ACCOUNT_STATUS_DATE",
"ACCOUNT_CLOSURE_REASON",
"REPAYMENT_PERIOD",
"DEFERRED_PAYMENT_DATE",
"DEFERRED_PAYMENT_AMOUNT",
"PAYMENT_FREQUENCY",
"DISBURSEMENT_DATE",
"INSTALLMENT_AMOUNT",
"LATEST_PAYMENT_DATE",
"LATEST_PAYMENT_AMOUNT",
"SECURITY_TYPE" FROM (SELECT
	DISTINCT c.cust_nm AS registered_name,
	c.cust_nm AS trading_name,
	to_char(nvl(to_date(to_char(o.registration_dt, 'YYYYMMDD'), 'YYYYMMDD'),
(SELECT to_date (to_char (sysdate, 'YYYYMMDD'), 'YYYYMMDD') FROM dual)), 'YYYYMMDD') AS registration_date,
	o.registration_no AS REGISTRATION_NUMBER,
	'' AS previous_registration_number,
	'KE' AS nationality,
	c.cust_no AS client_number,
	ac.acct_no AS account_number,
	'' AS old_account_number,
	'K' company_type,
	/*Adjust this*/
	'006' AS INDUSTRY_CODE,
	round(nvl((c.annual_income_amt), 0), 0) AS annual_turnover,
	UPPER(c.tin) AS PIN_NUMBER,
	'' AS VAT_NUMBER,
	0 AS NUMBER_OF_SHARES,
	lpad (2,
	3,
	'0') AS TRADING_STATUS,
	'' AS TRADING_STATUS_DATE,
	(
	SELECT
		ccm.contact
	FROM
		customer_contact_mode ccm
	WHERE
		ccm.cust_id = c.cust_id
		AND ccm.contact_mode_id = 231) AS MAIN_TELEPHONE_NUMBER,
	'' AS OTHER_TELEPHONE_NUMBER,
	(
	SELECT
		ad.addr_line_1
	FROM
		customer_address cad,
		address ad
	WHERE
		cad.addr_id = ad.addr_id
		AND cad.cust_id = c.cust_id
		AND cad.primary_fg = 'Y') POSTAL_ADDRESS1,
	(
	SELECT
		ad.addr_line_2 --case WHEN ad.addr_line_2 LIKE '-%' THEN '' ELSE  ad.addr_line_2 END AS addr_line_2
	FROM
		customer_address cad,
		address ad
	WHERE
		cad.addr_id = ad.addr_id
		AND cad.cust_id = c.cust_id
		AND cad.primary_fg = 'Y') AS POSTAL_ADDRESS2,
	(
  SELECT ad.city 
  FROM customer_address cad, address ad
  WHERE cad.addr_id = ad.addr_id
    AND cad.cust_id = c.cust_id
    AND cad.primary_fg = 'Y'
) AS TOWN,
	'KE' AS country,
	(
	SELECT
		ad.postcd
	FROM
		customer_address cad,
		address ad
	WHERE
		cad.addr_id = ad.addr_id
		AND cad.cust_id = c.cust_id
		AND cad.primary_fg = 'Y') AS post_code,
	(
	SELECT
		ad.state
	FROM
		customer_address cad,
		address ad
	WHERE
		cad.addr_id = ad.addr_id
		AND cad.cust_id = c.cust_id
		AND cad.primary_fg = 'Y') AS physical_addess1,
	'' AS physical_address2,
	'' AS plot_number,
--	(	SELECT 	ad.state	FROM customer_address cad, address ad
--	WHERE
--		cad.addr_id = ad.addr_id
--		AND cad.cust_id = c.cust_id
--		AND cad.primary_fg = 'Y') AS physical_location_town,
	'' AS physical_location_town,
	'KE' AS PHYSICAL_LOCATION_COUNTRY,
	(
	SELECT
		param_value
	FROM
		ctrl_parameter
	WHERE
		param_cd = 'S04') AS LENDERS_REGISTERED_NAME,
	(
	SELECT
		param_value
	FROM
		ctrl_parameter
	WHERE
		param_cd = 'S04') AS LENDERS_TRADING_NAME,
	bu.bu_nm AS LENDERS_BRANCH_NAME,
	('N179' || (LPAD (bu.bu_no, 3, '0'))) AS LENDERS_BRANCH_CODE,
	'S' AS account_indicator,
	'A' account_holder_type,
	'D' account_product_type,
	to_char(ac.create_dt, 'YYYYMMDD') AS DATE_ACCOUNT_OPENED,
	nvl((SELECT to_char(due_dt, 'YYYYMMDD') FROM ln_acct_repmnt_event le WHERE le.acct_id = ac.acct_id
AND le.due_dt = (SELECT max(lee.due_dt) FROM ln_acct_repmnt_event lee WHERE lee.acct_id = le.acct_id
AND lee.due_dt <= (SELECT to_date(display_value, 'DD/MM/YYYY') FROM ctrl_parameter cp WHERE cp.param_cd = 'S02'))),
(SELECT to_char(to_date(display_value, 'DD/MM/YYYY'), 'YYYYMMDD') FROM ctrl_parameter cp WHERE cp.param_cd = 'S02')) installment_due_date,
	CASE
		WHEN ac.risk_class_id = 552 THEN 'A'
		WHEN ac.risk_class_id = 553 THEN 'B'
		WHEN ac.risk_class_id = 554 THEN 'C'
		WHEN ac.risk_class_id = 555 THEN 'D'
		WHEN ac.risk_class_id = 556 THEN 'E'
		WHEN ac.rec_st IN ('L') THEN 'A'
		ELSE 'NONE'
	END
AS prudential_risk_classiffication,
	round(ltrim((to_char((la.disbursement_limit), '999999999999.99'))), 0)|| 00 AS original_amount,
	cu.crncy_cd AS currency_facility,
	CASE
		WHEN pd.prod_cat_ty = 'LN'
    THEN
        CASE
			WHEN ltrim((to_char((abs(las.ledger_bal)), '999999999999.99'))) = '00'
            THEN '00'
			ELSE round(ltrim((to_char((abs(las.ledger_bal)), '999999999999.99'))), 0)|| '00'
		END
	END AS amount_in_kes,
	CASE
		WHEN pd.prod_cat_ty = 'LN'
    THEN
        CASE
			WHEN ltrim((to_char((abs(las.ledger_bal)), '999999999999.99'))) = '00'
            THEN '00'
			ELSE round(ltrim((to_char((abs(las.ledger_bal)), '999999999999.99'))), 0)|| '00'
		END
	END AS current_balance,
	round(ltrim(nvl((to_char(abs (ld.amt_unpaid), '999999999999.99')), '0')), 0)|| '00' AS overdue_balance,
	nvl(to_char(ld.due_dt, 'YYYYMMDD'), NULL) AS overdue_date,
	nvl(CASE WHEN pd.prod_cat_ty = 'LN' THEN (SELECT to_date(display_value, 'dd/MM/yyyy')
FROM ctrl_parameter WHERE param_cd = 'S02') - (ld.due_dt) END, 0) AS days_arrears,
	nvl((SELECT count(DISTINCT (le.due_dt)) FROM ln_acct_repmnt_event le WHERE le.acct_id = ac.acct_id AND le.rec_st IN ('N', 'P')
AND le.due_dt < (SELECT to_date (display_value, 'DD/MM/YYYY') FROM ctrl_parameter WHERE param_cd = 'S02')), 0) AS installment_arrears,
	CASE
		WHEN ac.rec_st = 'L' THEN 'A'
		WHEN ac.rec_st = 'D' THEN 'B'
		WHEN ac.rec_st = 'W' THEN 'C'
		WHEN ac.rec_st IN ('F', 'Q', 'N', 'B') THEN 'F'
		ELSE 'N'
	END AS account_status,
	to_char(ac.status_effective_dt, 'YYYYMMDD') AS ACCOUNT_STATUS_DATE,
	'' AS account_closure_reason,
	CASE
		WHEN pd.prod_cat_ty IN ('LN', 'TDEP') THEN la.term_value
	END AS repayment_period,
	'' AS deferred_payment_date,
	'' AS deferred_payment_amOUNT,
	'M' AS payment_frequency,
	to_char(la.start_dt, 'YYYYMMDD') AS disbursement_date,
	CASE
		WHEN ltrim((to_char(((SELECT sum(le.repmnt_amt) FROM ln_acct_repmnt_event le WHERE le.acct_id = ac.acct_id
    AND le.due_dt = (SELECT min(lee.due_dt) FROM ln_acct_repmnt_event lee WHERE lee.acct_id = le.acct_id)
    )), '999999999999.99'))) = '.00'
    THEN
        '0.00'
		ELSE
        round(ltrim((to_char(((SELECT sum(le.repmnt_amt) FROM ln_acct_repmnt_event le
        WHERE le.acct_id = ac.acct_id
        AND le.due_dt = (SELECT min(lee.due_dt) FROM ln_acct_repmnt_event lee WHERE lee.acct_id = le.acct_id))),
    '999999999999.99'))), 0)|| '00'
	END AS INSTALLMENT_AMOUNT,
	to_char(to_date(to_char(las.last_payment_dt, 'dd/MM/yyyy'), 'dd/MM/yyyy'), 'YYYYMMDD') AS latest_payment_date,
	round(ltrim((to_char((las.last_payment_amt), '999999999999.99'))), 0)|| '00' latest_payment_amount,
	'S' AS security_TYPE
FROM
	customer c,
	organisation o,
	customer_type_ref ctr,
	business_unit bu,
	product pd,
	currency cu,
	account ac,
	loan_account la,
	loan_account_summary las,
	(
	SELECT
		le.acct_id,
		min(le.due_dt)due_dt,
		sum(le.amt_unpaid) amt_unpaid
	FROM
		ln_acct_repmnt_event le
	WHERE
		le.rec_st IN ('N', 'P')
			AND le.event_type IN ('PRINCIPAL', 'INTEREST')
				AND le.due_dt < (
				SELECT
					to_date(display_value, 'dd/MM/yyyy')
				FROM
					ctrl_parameter
				WHERE
					param_cd = 'S02')
			GROUP BY
				le.acct_id) ld
WHERE
	c.cust_id = o.cust_id
	AND c.cust_ty_id = ctr.cust_ty_id
	AND bu.bu_id = ac.main_branch_id
	AND c.cust_id = ac.cust_id
	AND pd.prod_id = ac.prod_id
	AND ac.acct_id = la.acct_id
	AND ac.acct_id = las.acct_id
	AND ac.acct_id = ld.acct_id(+)
	AND ac.rec_st IN ('A', 'N', 'Q', 'B'));