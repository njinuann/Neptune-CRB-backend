 
CREATE OR REPLACE FORCE VIEW wakulimatest.V_CREDIT_APP_INFORMATION
AS
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
   SELECT   (SELECT   param_value
               FROM   ctrl_parameter
              WHERE   param_cd = 'S04')
               AS lenders_registered_name,
            (SELECT   param_value
               FROM   ctrl_parameter
              WHERE   param_cd = 'S04')
               AS lenders_trading_name,
            A.BU_NM AS lenders_branch_name,
            ('N179' || (LPAD (A.BU_NO, 3, '0'))) AS lenders_branch_code,
            LPAD (1, 3, '0') AS primary_id_doc_type,
            B.SOCIAL_SECURITY_NO AS primary_id_doc_no, 
            '' AS secondary_id_doc_type,
            '' AS secondary_id_doc_no,
            '' AS other_id_doc_type,
           '' AS other_id_doc_no,
            C.CUST_NO AS client_number,
            '' AS client_type,
            '' AS pin_number,
            G.ACCT_NO AS account_number,
            E.APPL_DT AS application_date,
            'A' AS APPL_TYPE,
            E.REF_NO AS application_no,
            'S' AS facility_application_type,
            b.first_nm as forename1, 
			b.middle_nm as forename2, 
            '' AS forename3,
            LTRIM ( (TO_CHAR (E.APPROVED_LIMIT_AMT, '999999999999.99')))
               AS application_amount,
            'KES' AS application_currency,
            --F.PROD_CD AS PRODUCT_TYPE,
            
            CASE
               WHEN F.PROD_CD IN ('502', '507') THEN 'C'
               WHEN F.PROD_CD IN ('503', '505') THEN 'F'
               WHEN F.PROD_CD NOT IN ('503', '505', '502', '507') THEN 'H'
            END
               AS product_type,
            E.TERM_VALUE AS loan_term,
            CASE
               WHEN E.REC_ST = 'A' THEN 'F'
               WHEN E.REC_ST = 'I' THEN 'B'
               WHEN E.REC_ST = 'S' THEN 'A'
               WHEN E.REC_ST = 'C' THEN 'D'
            END
               AS application_status,
            '' AS application_decline_reason_codes,
            E.CR_REQD_DT AS application_status_date,
             b.LAST_nm AS  surname,
           '' AS trading_name
            
     FROM   BUSINESS_UNIT A,
            PERSON B,
            CUSTOMER C,
            CREDIT_APPL E,
            PRODUCT F,
            LOAN_ACCOUNT G,
            ACCOUNT D
    WHERE       B.CUST_ID = C.CUST_ID
            AND A.BU_ID = C.MAIN_BRANCH_ID
            AND C.CUST_ID = G.CUST_ID
            AND C.CUST_ID = E.CUST_ID
            AND E.APPL_PROD_ID = F.PROD_ID
            AND D.PROD_ID = F.PROD_ID
            AND D.ACCT_NO = G.ACCT_NO
            AND C.CUST_TY_ID IN ( SELECT CUST_TY_ID FROM CUSTOMER_TYPE_REF ctr WHERE CUST_CAT ='PER')
            AND E.REC_ST NOT IN ('L', 'U')
            AND G.APPL_ID = E.APPL_ID
   --for non individual 
   UNION ALL
   SELECT   (SELECT   param_value
               FROM   ctrl_parameter
              WHERE   param_cd = 'S04')
               AS lenders_registered_name,
            (SELECT   param_value
               FROM   ctrl_parameter
              WHERE   param_cd = 'S04')
               AS lenders_trading_name,
            A.BU_NM AS lenders_branch_name,
            ('N179' || (LPAD (A.BU_NO, 3, '0'))) AS lenders_branch_code,
            LPAD (5, 3, '0') AS primary_id_doc_type,
            B.REGISTRATION_NO AS primary_id_doc_no,
            '' AS secondary_id_doc_type,
            '' AS secondary_id_doc_no,
            '' AS other_id_doc_type,
            '' AS other_id_doc_no,
            C.CUST_NO AS client_number,
            '' AS client_type,
            '' AS pin_number,
            D.ACCT_NO AS ACCOUNT_NUMBER,
            E.APPL_DT AS application_date,
            'B' AS APPL_TYPE,
            E.REF_NO AS application_no,
            'S' AS facility_application_type,
			b.ORGANISATION_NM as forename1, 
			b.ORGANISATION_NM as forename2, 
            '' AS forename3,
            LTRIM ( (TO_CHAR (E.APPROVED_LIMIT_AMT, '999999999999.99')))
               AS APPL_AMT,
            'KES' AS application_currency,
            CASE
               WHEN F.PROD_CD IN ('502', '507') THEN 'C'
               WHEN F.PROD_CD IN ('503', '505') THEN 'F'
               WHEN F.PROD_CD NOT IN ('503', '505', '502', '507') THEN 'H'
            END
               AS product_type,
            E.TERM_VALUE AS loan_term,
            CASE
               WHEN E.REC_ST = 'A' THEN 'F'
               WHEN E.REC_ST = 'I' THEN 'B'
               WHEN E.REC_ST = 'S' THEN 'A'
               WHEN E.REC_ST = 'C' THEN 'D'
            END
               AS application_status,
            '' AS application_decline_reason_codes,
            E.CR_REQD_DT AS application_status_date, 
            B.ORGANISATION_NM as surname,
            '' AS trading_name
     FROM   BUSINESS_UNIT A,
            ORGANISATION B,
            CUSTOMER C,
            ACCOUNT D,
            CREDIT_APPL E,
            PRODUCT F,
            LOAN_ACCOUNT G
    WHERE       B.CUST_ID = C.CUST_ID
            AND A.BU_ID = C.MAIN_BRANCH_ID
            AND C.CUST_ID = G.CUST_ID
            AND C.CUST_ID = E.CUST_ID
            AND E.APPL_PROD_ID = F.PROD_ID
            AND D.PROD_ID = F.PROD_ID
            AND D.ACCT_NO = G.ACCT_NO
            AND C.CUST_TY_ID IN  ( SELECT CUST_TY_ID FROM CUSTOMER_TYPE_REF ctr WHERE CUST_CAT !='PER')
            AND E.REC_ST NOT IN ('L', 'U')
            AND G.APPL_ID = E.APPL_ID);


