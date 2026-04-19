--Snapshot Date (Mandatory)	Client Type (Mandatory)	Surname (Conditional)	Forename 1 (Conditional)	Forename 2 (Conditional)	Forename 3 (Conditional)	Company Name (Conditional)	Primary Identification Document Type (Mandatory)	Primary Identification Doc Number (Mandatory)	Account Number (Mandatory)	Currency of Facility (Mandatory)	Original Amount (Mandatory)	Payment Amount (Mandatory)	Payment Date (Mandatory)	Current Balance (Mandatory)	Installments in Arrears (Mandatory)	Days in Arrears (Mandatory)	Account Status (Mandatory)
--SELECT * FROM V_CRB_DAILY_PAYMENT

CREATE OR REPLACE VIEW V_CRB_DAILY_PAYMENT
AS
SELECT   ACCOUNT_NUMBER, ACCOUNT_STATUS,CLIENT_TYPE,trading_nm AS COMPANY_NAME,  CURRENCY_FACILITY, CURRENT_BALANCE, DAYS_ARREARS, 
FORENAME1, FORENAME2, FORENAME3, INSTALLMENT_ARREARS,ORIGINAL_AMOUNT,PAYMENT_AMOUNT,PAYMENT_DATE,  PRIMARY_ID_DOC_NO, PRIMARY_ID_DOC_TYPE, SNAPSHOT_DATE, SURNAME
FROM(         
select DISTINCT to_char (SYSDATE , 'YYYYMMDD') as snapshot_date,
'A' AS client_type, 
p.last_nm as surname, 
p.first_nm as forename1, 
p.middle_nm as forename2, 
'' as forename3, 
'' as trading_nm,
'001' AS  primary_id_doc_type, 
p.social_security_no AS primary_id_doc_no,
ac.acct_no as account_number, 
cu.crncy_cd as currency_Facility,
round(ltrim((to_char(abs(la.disbursement_limit), '999999999999.99'))),0)||'00' AS  original_amount,
round(las.last_payment_amt, 0)||'00' AS payment_amount,
CASE WHEN round(las.last_payment_amt, 0) > 0 THEN to_char(nvl(to_date (to_char(las.last_payment_dt, 'dd/MM/yyyy'), 'dd/MM/yyyy'), la.start_dt),'YYYYMMDD') ELSE '' END AS  payment_date,
'000' AS current_balance,
'000' AS installment_arrears,
0 AS days_arrears,
'A' AS Account_status
from person p, customer c, account ac, business_unit bu, product pd, currency cu, credit_appl ca, loan_account la, loan_account_summary las
where p.cust_id = c.cust_id
and p.cust_id = ac.cust_id
and ac.main_branch_id = bu.bu_id
and ac.prod_id = pd.prod_id
and ac.crncy_id = cu.crncy_id 
and ac.acct_id = la.acct_id
and la.appl_id = ca.appl_id
and ac.acct_id = las.acct_id
AND las.last_payment_dt >= (select to_date (display_value, 'dd/MM/yyyy') from ctrl_parameter where param_cd = 'S02')-1
AND ac.rec_st IN ('L') 
);

SELECT las.last_payment_dt, las.* FROM loan_account_summary las, account ac WHERE las.ACCT_NO =ac.ACCT_NO and
 las.last_payment_dt = (select to_date (display_value, 'dd/MM/yyyy') from ctrl_parameter where param_cd = 'S02')-1 AND ac.REC_ST ='L'
ACCT_NO IN ('20110102372003','21610100124804')

SELECT * FROM LN_ACCT_REPMNT_EVENT lare WHERE acct_id = 
