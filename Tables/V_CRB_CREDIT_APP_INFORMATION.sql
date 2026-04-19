-- WAKULIMALIVE.V_CRB_CREDIT_APP_INFORMATION source

CREATE OR REPLACE FORCE EDITIONABLE VIEW "WAKULIMALIVE"."V_CRB_CREDIT_APP_INFORMATION" ("LENDERS_REGISTERED_NAME", "LENDERS_TRADING_NAME", "LENDERS_BRANCH_NAME", "LENDERS_BRANCH_CODE", "PRIMARY_ID_DOC_TYPE", "PRIMARY_ID_DOC_NO", "REGISTERED_NAME", "SECONDARY_ID_DOC_TYPE", "SECONDARY_ID_DOC_NO", "OTHER_ID_DOC_TYPE", "OTHER_ID_DOC_NO", "CLIENT_NUMBER", "CLIENT_TYPE", "PIN_NUMBER", "ACCOUNT_NUMBER", "APPLICATION_DATE", "APPLICATION_NO", "FACILITY_APPLICATION_TYPE", "FORENAME1", "FORENAME2", "FORENAME3", "APPLICATION_AMOUNT", "APPLICATION_CURRENCY", "PRODUCT_TYPE", "LOAN_TERM", "APPLICATION_STATUS", "APPLICATION_DECLINE_REASON_CODES", "APPLICATION_STATUS_DATE", "SURNAME", "TRADING_NAME") AS 
  SELECT lenders_registered_name,
   lenders_trading_name,
   lenders_branch_name,
   lenders_branch_code,
   primary_id_doc_type,
   primary_id_doc_no,
   lenders_registered_name AS registered_name,
   secondary_id_doc_type,
   secondary_id_doc_no,
   other_id_doc_type,
   other_id_doc_no,
   client_number,
   client_type,
   pin_number,
   account_number,
   application_date, 
   application_no,
   facility_application_type,
   forename1,
   forename2,
   forename3,
   application_amount,
   application_currency,
   product_type,
   loan_term,
   application_status,
   application_decline_reason_codes,
   application_status_date,
   surname,
   trading_name 
   FROM (
          SELECT  
    cp.param_value AS lenders_registered_name,
    cp.param_value AS lenders_trading_name,
    A.bu_nm AS lenders_branch_name,
    ('N179' || LPAD(A.bu_no, 3, '0')) AS lenders_branch_code,
    '005' AS primary_id_doc_type,
    B.registration_no AS primary_id_doc_no,
    '' AS secondary_id_doc_type,
    '' AS secondary_id_doc_no,
    '' AS other_id_doc_type,
    '' AS other_id_doc_no,
    C.cust_no AS client_number,
    '' AS client_type,
    '' AS pin_number,
    D.acct_no AS account_number,
    TO_CHAR(E.appl_dt, 'YYYYMMDD') AS application_date,
    'B' AS appl_type,
    E.ref_no AS application_no,
    'S' AS facility_application_type,
    B.organisation_nm AS forename1, 
    B.organisation_nm AS forename2, 
    '' AS forename3,
    LTRIM(TO_CHAR(E.approved_limit_amt, '999999999999.99')) AS application_amount,
    'KES' AS application_currency,
    'H' AS product_type, -- simplified case
    E.term_value AS loan_term,
    CASE
        WHEN E.rec_st = 'A' THEN 'F'
        WHEN E.rec_st = 'I' THEN 'B'
        WHEN E.rec_st = 'S' THEN 'A'
        WHEN E.rec_st = 'C' THEN 'D'
    END AS application_status,
    '' AS application_decline_reason_codes,
    TO_CHAR(E.cr_reqd_dt, 'YYYYMMDD') AS application_status_date, 
    B.organisation_nm AS surname,
    '' AS trading_name
FROM ctrl_parameter cp
JOIN business_unit A 
    ON 1=1
JOIN customer C 
    ON A.bu_id = C.main_branch_id
JOIN organisation B 
    ON B.cust_id = C.cust_id
JOIN loan_account G 
    ON G.cust_id = C.cust_id
JOIN credit_appl E 
    ON E.cust_id = C.cust_id
   AND G.appl_id = E.appl_id
JOIN product F 
    ON F.prod_id = E.appl_prod_id
JOIN account D 
    ON D.prod_id = F.prod_id
   AND D.acct_no = G.acct_no
WHERE cp.param_cd = 'S04'
  AND C.cust_ty_id IN (
        SELECT cust_ty_id 
        FROM customer_type_ref 
        WHERE cust_cat = 'PER'
  )
  AND E.rec_st NOT IN ('L', 'U')
   --for non individual 
   UNION ALL
           SELECT  
    cp.param_value AS lenders_registered_name,
    cp.param_value AS lenders_trading_name,
    A.bu_nm AS lenders_branch_name,
    ('N179' || LPAD(A.bu_no, 3, '0')) AS lenders_branch_code,
    '005' AS primary_id_doc_type,
    B.registration_no AS primary_id_doc_no,
    '' AS secondary_id_doc_type,
    '' AS secondary_id_doc_no,
    '' AS other_id_doc_type,
    '' AS other_id_doc_no,
    C.cust_no AS client_number,
    '' AS client_type,
    '' AS pin_number,
    D.acct_no AS account_number,
    TO_CHAR(E.appl_dt, 'YYYYMMDD') AS application_date,
    'B' AS appl_type,
    E.ref_no AS application_no,
    'S' AS facility_application_type,
    B.organisation_nm AS forename1, 
    B.organisation_nm AS forename2, 
    '' AS forename3,
    LTRIM(TO_CHAR(E.approved_limit_amt, '999999999999.99')) AS Vapplication_amount,
    'KES' AS application_currency,
    'H' AS product_type, -- simplified case
    E.term_value AS loan_term,
    CASE
        WHEN E.rec_st = 'A' THEN 'F'
        WHEN E.rec_st = 'I' THEN 'B'
        WHEN E.rec_st = 'S' THEN 'A'
        WHEN E.rec_st = 'C' THEN 'D'
    END AS application_status,
    '' AS application_decline_reason_codes,
    TO_CHAR(E.cr_reqd_dt, 'YYYYMMDD') AS application_status_date, 
    B.organisation_nm AS surname,
    '' AS trading_name
FROM ctrl_parameter cp
JOIN business_unit A 
    ON 1=1
JOIN customer C 
    ON A.bu_id = C.main_branch_id
JOIN organisation B 
    ON B.cust_id = C.cust_id
JOIN loan_account G 
    ON G.cust_id = C.cust_id
JOIN credit_appl E 
    ON E.cust_id = C.cust_id
   AND G.appl_id = E.appl_id
JOIN product F 
    ON F.prod_id = E.appl_prod_id
JOIN account D 
    ON D.prod_id = F.prod_id
   AND D.acct_no = G.acct_no
WHERE cp.param_cd = 'S04'
  AND C.cust_ty_id IN (
        SELECT cust_ty_id 
        FROM customer_type_ref 
        WHERE cust_cat != 'PER'
  )
  AND E.rec_st NOT IN ('L', 'U'));