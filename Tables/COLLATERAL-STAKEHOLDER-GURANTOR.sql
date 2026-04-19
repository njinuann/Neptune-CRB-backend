 
 
CREATE OR REPLACE FORCE VIEW WAKULIMALIVE.V_CRB_STAKEHOLDER_INFORMATION
(
   SURNAME,
   FORENAME1,
   FORENAME2,
   FORNAME3,
   SALUTATION,
   DATE_OF_BIRTH,
   GENDER,
   NATIONALITY,
   BRID,
   ACCOUNT_NUMBER,
   PRIMARY_IDENTIFICATION,
   PRIMARY_ID_DOC_NUMBER,
   SECONDARY_ID_DOC_TYPE,
   SECONDARY_ID_DOC_NUMBER,
   OTHER_ID_DOC_TYPE,
   OTHER_ID_DOC_NUMBER,
   EMAIL,
   COMPANY_REGISTRATION_NUMBER,
   PREVIOUS_REGISTRATION_NUMBER,
   COMPANY_PIN_NUMBER,
   COMPANY_VAT_NUMBER,
   STAKEHOLDER_TYPE,
   PERCENTAGE_OF_SHARES,
   MOBILE_TEL_NUMBER,
   HOME_TEL_NUMBER,
   WORK_TEL_NUMBER,
   POSTAL_ADDRESS1,
   POSTAL_ADDRESS2,
   TOWN,
   COUNTRY,
   POSTAL_CODE,
   PHYSICAL_ADDRESS1,
   PHYSICAL_ADDRESS2,
   PHYSICAL_ADDRESS3,
   PLOT_NUMBER,
   POSTAL_LOCATION_TOWN,
   POSTAL_LOCATION_COUNTRY
)
AS
   SELECT   DISTINCT
            B.LAST_NM AS SURNAME,
            B.FIRST_NM AS FORENAME1,
            B.LAST_NM AS FORENAME2,
            NULL AS FORNAME3,
            C.TITLE_DESC AS SALUTATION,
            B.BIRTH_DT AS Date_Of_Birth,
            B.GENDER_TY AS GENDER,
            CASE WHEN E.NATIONALITY_DESC = 'KENYAN' THEN 'KE' END
               AS NATIONALITY,
            F.CUST_NO AS BRID,
            D.ACCT_NO AS ACCOUNT_NUMBER,
            LPAD (1, 3, '0') AS PRIMARY_IDENTIFICATION,
            B.SOCIAL_SECURITY_NO AS PRIMARY_ID_DOC_NUMBER,
            NULL AS SECONDARY_ID_DOC_TYPE,
            NULL AS SECONDARY_ID_DOC_NUMBER,
            NULL AS OTHER_ID_DOC_TYPE,
            NULL AS OTHER_ID_DOC_NUMBER,
            (SELECT   X.CONTACT
               FROM   CUSTOMER_CONTACT_MODE X
              WHERE       X.CUST_CONTACT_MODE_ID = H.CUST_CONTACT_MODE_ID
                      AND X.CUST_ID = H.CUST_ID
                      AND CONTACT_MODE_ID IN (237))
               AS EMAIL,
            G.REGISTRATION_NO AS COMPANY_REGISTRATION_NUMBER,
            NULL AS PREVIOUS_REGISTRATION_NUMBER,
            NULL AS COMPANY_PIN_NUMBER,
            NULL AS COMPANY_VAT_NUMBER,
            'A' AS STAKEHOLDER_TYPE,
            NULL AS PERCENTAGE_OF_SHARES,
            (SELECT   X.CONTACT
               FROM   CUSTOMER_CONTACT_MODE X
              WHERE       X.CUST_CONTACT_MODE_ID = H.CUST_CONTACT_MODE_ID
                      AND X.CUST_ID = H.CUST_ID
                      AND X.CONTACT_MODE_ID = H.CONTACT_MODE_ID)
               --AND X.CONTACT_MODE_ID in(256)
               AS MOBILE_TEL_NUMBER,
            NULL AS HOME_TEL_NUMBER,
            NULL AS WORK_TEL_NUMBER,
            I.ADDR_LINE_1 AS POSTAL_ADDRESS1,
            I.ADDR_LINE_2 AS POSTAL_ADDRESS2,
            I.CITY AS TOWN,
            CASE WHEN J.CNTRY_NM = 'KENYA' THEN 'KE' END AS COUNTRY,
            I.POSTCD AS POSTAL_CODE,
            I.STATE AS PHYSICAL_ADDESS1,
            NULL AS PHYSICAL_ADDESS2,
            NULL AS PHYSICAL_ADDESS3,
            NULL AS PLOT_NUMBER,
            I.CITY AS POSTAL_LOCATION_TOWN,
            CASE WHEN J.CNTRY_NM = 'KENYA' THEN 'KE' END
               AS POSTAL_LOCATION_COUNTRY
     FROM   ACCOUNT_RELATIONSHIP A,
            PERSON B,
            TITLE_REF C,
            ACCOUNT D,
            NATIONALITY_REF E,
            CUSTOMER F,
            ORGANISATION G,
            CUSTOMER_CONTACT_MODE H,
            ADDRESS I,
            COUNTRY J,
            CUSTOMER_ADDRESS K
    WHERE       A.CUST_ID = B.CUST_ID
            AND C.TITLE_ID = B.TITLE_ID
            AND A.ACCT_ID = D.ACCT_ID
            AND B.NATIONALITY_ID = E.NATIONALITY_ID
            AND F.CUST_ID = B.CUST_ID
            AND G.CUST_ID = D.CUST_ID
            AND F.CUST_ID = H.CUST_ID(+)
            AND J.CNTRY_ID = I.CNTRY_ID
            AND I.ADDR_ID = K.ADDR_ID
            AND K.CUST_ID = F.CUST_ID
            AND REGISTRATION_NO IS NOT NULL AND REGISTRATION_NO NOT LIKE '%----%'
            AND (SELECT   X.CONTACT
                   FROM   CUSTOMER_CONTACT_MODE X
                  WHERE       X.CUST_CONTACT_MODE_ID = H.CUST_CONTACT_MODE_ID
                          AND X.CUST_ID = H.CUST_ID
                          AND X.CONTACT_MODE_ID = 237
                          AND X.CONTACT_MODE_ID IN (237)) IS NOT NULL




--=======================================

 
/* Formatted on 29/05/2019 12:39:48 (QP5 v5.114.809.3010) */
CREATE OR REPLACE FORCE VIEW WAKULIMALIVE.V_CRB_COLLATERAL_INFORMATION
(
   lenders_registered_name,
   lenders_trading_name,
   lenders_branch_name,
   lenders_branch_code,
   client_number,
   account_number,
   primary_id_doc_type,
   primary_id_doc_no,
   secondary_id_doc_no,
   secondary_id_doc_type,
   other_id_doc_type,
   other_id_doc_no,
   pin_number,
   vat_number,
   collateral_type,
   collateral_reference_number,
   collateral_valuation_amount,
   collateral_currency,
   collateral_forced_sale_value,
   next_value_date,
   collateral_expiry_date,
   recovery_type,
   last_evaluation_date,
   shared_collateral,
   portion_shared_collateral,
   multiple_shared_collateral
)
AS
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
            B.CUST_NO AS client_number,
            C.ACCT_NO AS account_number,
            LPAD (1, 3, '0') AS primary_id_doc_type,
            F.SOCIAL_SECURITY_NO AS primary_id_doc_no,
            '' AS secondary_id_doc_no,
            '' AS secondary_id_doc_type,
            '' AS other_id_doc_type,
            '' AS other_id_doc_no,
            '' AS pin_number,
            '' AS vat_number,
            CASE
               WHEN D.COLLATERAL_CAT_CD = 'PROP' THEN 'B'
               WHEN D.COLLATERAL_CAT_CD = 'VEH' THEN 'G'
               WHEN D.COLLATERAL_CAT_CD = 'FI' THEN 'C'
            END
               AS COLLATERAL_TYPE,
            E.COLLATERAL_REF_NO AS COLLATERAL_REF_NO,
            LTRIM ( (TO_CHAR (E.COLLATERAL_MARKET_VALUE, '999999999999.99')))
               AS COLLATERAL_LAST_VAL_AMT,
            G.CRNCY_CD AS COLLATERAL_CRNCY,
            LTRIM ( (TO_CHAR (E.COLLATERAL_MARKET_VALUE, '999999999999.99')))
               AS COLLATERAL_FORCED_SALE_VALUE, 
            '' AS next_value_date, 
            '' COLLATERAL_EXP_DT,
            LTRIM ( (TO_CHAR (E.COLLATERAL_MARKET_VALUE, '999999999999.99')))
               AS recovery_type,                           --'N/A' AS EMPTYDATA8,
            NVL (
               (SELECT   X.VERIFIED_DT
                  FROM   COLLATERAL_REVIEW X
                 WHERE   X.COLLATERAL_ID = E.COLLATERAL_ID
                         AND X.COLLATERAL_REV_ID =
                               (SELECT   MAX (X.COLLATERAL_REV_ID)
                                  FROM   COLLATERAL_REVIEW X
                                 WHERE   X.COLLATERAL_ID = E.COLLATERAL_ID)),
               C.CREATE_DT
            )
               AS last_evaluation_date,
            'N' AS SHARED_COLLATERAL,
            '' AS portion_shared_collateral,
            'Y' AS MULTIPLE_COLLATERAL
     FROM   BUSINESS_UNIT A,
            CUSTOMER B,
            ACCOUNT C,
            COLLATERAL_TYPE D,
            COLLATERAL E,
            PERSON F,
            CURRENCY G
    WHERE       A.BU_ID = B.MAIN_BRANCH_ID
            AND B.CUST_ID = C.CUST_ID
            AND F.CUST_ID = B.CUST_ID
            AND D.COLLATERAL_TY_ID = E.COLLATERAL_TY_ID
            AND E.CUST_ID(+) = B.CUST_ID
            AND E.COLLATERAL_CRNCY_ID = G.CRNCY_ID
            AND B.CUST_TY_ID IN ( select CUST_TY_ID FROM customer_type_ref WHERE CUST_CAT = 'PER')
            AND C.PROD_CAT_TY = 'LN'
            AND C.REC_ST != 'L'
   --for non individual customer
   UNION ALL
   SELECT   (SELECT   param_value
               FROM   ctrl_parameter
              WHERE   param_cd = 'S04')
               AS LENDERS_REG_NAME,
            (SELECT   param_value
               FROM   ctrl_parameter
              WHERE   param_cd = 'S04')
               AS LENDERS_TRADING_NAME,
            A.BU_NM AS LENDERS_BRANCH_NAME,
            ('N179' || (LPAD (A.BU_NO, 3, '0'))) AS LENDERS_BRANCH_CODE,
            B.CUST_NO AS BRID,
            C.ACCT_NO AS ACCOUNT_NUMBER,
            LPAD (5, 3, '0') AS PRIMARY_IDENTIFICATION,
            F.REGISTRATION_NO AS PRIMARY_IDENTIFICATION_NUMBER,
            '' AS EMPTYDATA,
            '' AS EMPTYDATA,
            '' AS EMPTYDATA,
            '' AS EMPTYDATA,
            '' AS PIN_NO,
            '' AS EMPTYDATA,
            CASE
               WHEN D.COLLATERAL_CAT_CD = 'PROP' THEN 'B'
               WHEN D.COLLATERAL_CAT_CD = 'VEH' THEN 'G'
               WHEN D.COLLATERAL_CAT_CD = 'FI' THEN 'C'
            END
               AS COLLATERAL_TYPE,
            E.COLLATERAL_REF_NO AS COLLATERAL_REF_NO,
            LTRIM ( (TO_CHAR (E.COLLATERAL_MARKET_VALUE, '999999999999.99')))
               AS COLLATERAL_LAST_VAL_AMT,
            G.CRNCY_CD AS COLLATERAL_CRNCY,
            LTRIM ( (TO_CHAR (E.COLLATERAL_MARKET_VALUE, '999999999999.99')))
               AS COLLATERAL_FORCED_SALE_VALUE, 
            '' AS next_value_date, 
            '' COLLATERAL_EXP_DT,
            LTRIM ( (TO_CHAR (E.COLLATERAL_MARKET_VALUE, '999999999999.99')))
               AS EMPTYDATA,
            NVL (
               (SELECT   X.VERIFIED_DT
                  FROM   COLLATERAL_REVIEW X
                 WHERE   X.COLLATERAL_ID = E.COLLATERAL_ID
                         AND X.COLLATERAL_REV_ID =
                               (SELECT   MAX (X.COLLATERAL_REV_ID)
                                  FROM   COLLATERAL_REVIEW X
                                 WHERE   X.COLLATERAL_ID = E.COLLATERAL_ID)),
               C.CREATE_DT
            )
               AS LAST_VAL_DT,
            'N' AS SHARED_COLLATERAL,
            '' AS portion_shared_collateral,
            'Y' AS MULTIPLE_COLLATERAL
     FROM   BUSINESS_UNIT A,
            CUSTOMER B,
            ACCOUNT C,
            COLLATERAL_TYPE D,
            COLLATERAL E,
            ORGANISATION F,
            CURRENCY G
    WHERE       A.BU_ID = B.MAIN_BRANCH_ID
            AND B.CUST_ID = C.CUST_ID
            AND F.CUST_ID = B.CUST_ID
            AND D.COLLATERAL_TY_ID = E.COLLATERAL_TY_ID
            AND E.CUST_ID(+) = B.CUST_ID
            AND E.COLLATERAL_CRNCY_ID = G.CRNCY_ID
            AND B.CUST_TY_ID NOT IN ( select CUST_TY_ID FROM customer_type_ref WHERE CUST_CAT = 'PER')
            AND C.PROD_CAT_TY = 'LN'
            AND C.REC_ST != 'L'; 



--===================================


/* Formatted on 29/05/2019 12:44:18 (QP5 v5.114.809.3010) */
--SELECT * FROM V_CRB_GUARANTOR_INFORMATION
CREATE OR REPLACE FORCE VIEW wakulimatest.V_CRB_GUARANTOR_INFORMATION
(
lenders_registered_name,
  lenders_trading_name,
   lenders_branch_name,
   lenders_branch_code,
   client_number,
   company_name,
   SURNAME,
   email,
   FORENAME1,
   FORENAME2,
   FORNAME3,
   dob_or_reg_date,
   gender,
   nationality,
   marital_status,
   guarantee_type,
   group_name,
   guarantor_relationship,
   guarantee_limit,
   primary_id_doc_type,
   primary_id_doc_no,
   secondary_id_doc,
   secondary_id_doc_no,
   other_id_doc_type,
   other_id_doc_no,
   employee_name,
   employment_type,
   salary_band,
   mobile_telephone_number,
   home_telephone_number,
   work_telephone_number,
   postal_address1,
   postal_address2,
   town,
   country,
   post_code,
   physical_address1,
   physical_address2,
   plot_number,
   physical_location_town,
   physical_location_country 
)
AS
SELECT  
lenders_registered_name,
  lenders_trading_name,
   lenders_branch_name,
   lenders_branch_code,
   client_number,
   company_name,
   SURNAME,
   email,
   FORENAME1,
   FORENAME2,
   FORNAME3,
   dob_or_reg_date,
   gender,
   nationality,
   marital_status,
   guarantee_type,
   group_name,
   guarantor_relationship,
   guarantee_limit,
   primary_id_doc_type,
   primary_id_doc_no,
   secondary_id_doc,
   secondary_id_doc_no,
   other_id_doc_type,
   other_id_doc_no,
   employee_name,
   employment_type,
   salary_band,
   mobile_telephone_number,
   home_telephone_number,
   work_telephone_number,
   postal_address1,
   postal_address2,
   town,
   country,
   post_code,
   physical_address1,
   physical_address2,
   plot_number,
   physical_location_town,
   physical_location_country 
   FROM (
   SELECT   DISTINCT
            (SELECT   param_value
               FROM   ctrl_parameter
              WHERE   param_cd = 'S04')
               AS lenders_registered_name,
            (SELECT   param_value
               FROM   ctrl_parameter
              WHERE   param_cd = 'S04')
               AS lenders_trading_name,
            A.BU_NM AS lenders_branch_name,
            ('N179' || (LPAD (A.BU_NO, 3, '0'))) AS lenders_branch_code,
            C.CUST_NO AS client_number,
           '' AS  company_name,
           '' AS email,
            D.ACCT_NO AS ACCOUNT_NUMBER,
            B.LAST_NM AS SURNAME,
            B.FIRST_NM AS FORENAME1,
            B.LAST_NM AS FORENAME2,
            B.PREF_FIRST_NM AS FORNAME3,
            B.BIRTH_DT AS dob_or_reg_date,
            B.GENDER_TY AS gender,
            CASE
               WHEN F.NATIONALITY_DESC = 'KENYAN' THEN 'KE'
               WHEN F.NATIONALITY_DESC = 'UGANDAN' THEN 'UG'
               WHEN F.NATIONALITY_DESC = 'TANZANIAN' THEN 'TZ'
               WHEN F.NATIONALITY_DESC = 'RWANDESE' THEN 'RW'
               WHEN F.NATIONALITY_DESC = 'SUDANESE' THEN 'SD'
               WHEN F.NATIONALITY_DESC = 'BURUNDIAN' THEN 'BI'
               WHEN F.NATIONALITY_DESC = 'ETHIOPIAN' THEN 'ET'
               WHEN F.NATIONALITY_DESC = 'INDIAN' THEN 'IN'
            END
               AS nationality,
            '' AS marital_status,
            'B' AS guarantee_type,
            (SELECT   o.organisation_nm
               FROM   group_member gm, organisation o
              WHERE       c.cust_id = gm.member_cust_id
                      AND gm.group_cust_id = o.cust_id
                      AND gm.rec_st = 'Active')
               AS group_name,
            'C' AS guarantor_relationship,
            LTRIM ( (TO_CHAR (E.GUARANTEED_AMT, '999999999999.99')))
               AS guarantee_limit,
            LPAD (1, 3, '0') AS primary_id_doc_type,
            B.SOCIAL_SECURITY_NO AS primary_id_doc_no,
            '' AS secondary_id_doc,
            '' AS secondary_id_doc_no,
            '' AS other_id_doc_type,
            '' AS other_id_doc_no,
            '' AS employee_name,
            '' AS employment_type,
            '' AS salary_band, 
            (SELECT   contact
               FROM   CUSTOMER_CONTACT_MODE
              WHERE   cust_id = c.cust_id AND CONTACT_MODE_ID IN (256))
               AS mobile_telephone_number,
            '' AS home_telephone_number,
            '' AS work_telephone_number,
            H.ADDR_LINE_1 AS postal_address1,
            H.ADDR_LINE_2 AS postal_address2,
            H.CITY AS town,
            CASE
               WHEN I.CNTRY_NM = 'KENYA' THEN 'KE'
               WHEN I.CNTRY_NM = 'UGANDA' THEN 'UG'
               WHEN I.CNTRY_NM = 'TANZANIA' THEN 'TZ'
               WHEN I.CNTRY_NM = 'RWANDA' THEN 'RW'
               WHEN I.CNTRY_NM = 'SUDAN' THEN 'SD'
               WHEN I.CNTRY_NM = 'BURUNDI' THEN 'BI'
               WHEN I.CNTRY_NM = 'ETHIOPIA' THEN 'ET'
               WHEN I.CNTRY_NM = 'INDIA' THEN 'IN'
            END
               AS country,
            H.POSTCD AS post_code,
            H.STATE AS physical_address1,
            '' AS physical_address2,
            '' AS plot_number,
            H.STATE AS physical_location_town,
            CASE
               WHEN I.CNTRY_NM = 'KENYA' THEN 'KE'
               WHEN I.CNTRY_NM = 'UGANDA' THEN 'UG'
               WHEN I.CNTRY_NM = 'TANZANIA' THEN 'TZ'
               WHEN I.CNTRY_NM = 'RWANDA' THEN 'RW'
               WHEN I.CNTRY_NM = 'SUDAN' THEN 'SD'
               WHEN I.CNTRY_NM = 'BURUNDI' THEN 'BI'
               WHEN I.CNTRY_NM = 'ETHIOPIA' THEN 'ET'
               WHEN I.CNTRY_NM = 'INDIA' THEN 'IN'
            END
               AS physical_location_country
     FROM   BUSINESS_UNIT A,
            PERSON B,
            CUSTOMER C,
            ACCOUNT D,
            GUARANTOR E, 
            NATIONALITY_REF F,
            --CUSTOMER_CONTACT_MODE G,
            ADDRESS H,
            COUNTRY I,
            CUSTOMER_ADDRESS J
    WHERE       C.CUST_ID = D.CUST_ID
            AND B.CUST_ID = C.CUST_ID
            AND B.CUST_ID = D.CUST_ID
            AND E.GUARANTOR_ID = C.CUST_ID
            AND A.BU_ID = C.MAIN_BRANCH_ID
            AND B.NATIONALITY_ID = F.NATIONALITY_ID
            AND H.CNTRY_ID = I.CNTRY_ID
            -- AND G.CUST_ID = C.CUST_ID
            AND H.ADDR_ID = J.ADDR_ID
            AND C.CUST_ID = J.CUST_ID
            AND C.CUST_TY_ID IN (select CUST_TY_ID FROM customer_type_ref WHERE CUST_CAT = 'PER')
   --for non individual customer
   UNION ALL
   SELECT   DISTINCT
            (SELECT   param_value
               FROM   ctrl_parameter
              WHERE   param_cd = 'S04')
               AS LENDERS_REG_NAME,
            (SELECT   param_value
               FROM   ctrl_parameter
              WHERE   param_cd = 'S04')
               AS LENDERS_TRADING_NAME,
            A.BU_NM AS LENDERS_BRANCH_NAME,
            ('N179' || (LPAD (A.BU_NO, 3, '0'))) AS LENDERS_BRANCH_CODE,
            C.CUST_NO AS CLIENT_NUMBER,
             '' AS  company_name,
           '' AS email,
            D.ACCT_NO AS ACCOUNT_NUMBER,
            B.ORGANISATION_NM AS SURNAME,
            NULL AS FORENAME_1,
            NULL AS FORENAME_2,
            NULL AS FORNAME_3,
            B.REGISTRATION_DT AS DATE_OF_BIRTH,
            'M' AS GENDER,
            CASE
               WHEN I.CNTRY_NM = 'KENYA' THEN 'KE'
               WHEN I.CNTRY_NM = 'UGANDA' THEN 'UG'
               WHEN I.CNTRY_NM = 'TANZANIA' THEN 'TZ'
               WHEN I.CNTRY_NM = 'RWANDA' THEN 'RW'
               WHEN I.CNTRY_NM = 'SUDAN' THEN 'SD'
               WHEN I.CNTRY_NM = 'BURUNDI' THEN 'BI'
               WHEN I.CNTRY_NM = 'ETHIOPIA' THEN 'ET'
               WHEN I.CNTRY_NM = 'INDIA' THEN 'IN'
            END
               AS NATIONALITY,
            NULL AS MARITAL_STATUS,
            'B' AS GUARANTEE_TYPE,
            '' AS GROUP_NAME,
            'B' AS GUARANTOR_RELATIONSHIP,
            LTRIM ( (TO_CHAR (E.GUARANTEED_AMT, '999999999999.99')))
               AS GUARANTEE_LIMIT,
            LPAD (5, 3, '0') AS PRIMARY_IDENTIFICATION,
            B.REGISTRATION_NO AS PRIMARY_IDENTIFICATION_NUMBER, 
            '' AS secondary_id_doc,
            '' AS secondary_id_doc_no,
            '' AS other_id_doc_type,
            '' AS other_id_doc_no,
            '' AS employee_name,
            '' AS employment_type,
            '' AS salary_band, 
            (SELECT   contact
               FROM   CUSTOMER_CONTACT_MODE
              WHERE   cust_id = c.cust_id AND CONTACT_MODE_ID IN (256))
               AS MOBILE_NUMBER,
            '' AS EMPTYDATA,
            '' AS EMPTYDATA,
            H.ADDR_LINE_1 AS POSTAL_ADDRESS_1,
            H.ADDR_LINE_2 AS POSTAL_ADDRESS_2,
            H.CITY AS POSTAL_LOCATION_TOWN,
            CASE
               WHEN I.CNTRY_NM = 'KENYA' THEN 'KE'
               WHEN I.CNTRY_NM = 'UGANDA' THEN 'UG'
               WHEN I.CNTRY_NM = 'TANZANIA' THEN 'TZ'
               WHEN I.CNTRY_NM = 'RWANDA' THEN 'RW'
               WHEN I.CNTRY_NM = 'SUDAN' THEN 'SD'
               WHEN I.CNTRY_NM = 'BURUNDI' THEN 'BI'
               WHEN I.CNTRY_NM = 'ETHIOPIA' THEN 'ET'
               WHEN I.CNTRY_NM = 'INDIA' THEN 'IN'
            END
               AS POSTAL_LOCATION_COUNTRY,
            H.POSTCD AS POSTAL_CODE,
            H.STATE AS PHYSICAL_ADDESS_1,
            '' AS EMPTYDATA,
            '' AS EMPTYDATA,
            H.STATE AS PHYSICAL_LOCATION_TOWN,
            CASE
               WHEN I.CNTRY_NM = 'KENYA' THEN 'KE'
               WHEN I.CNTRY_NM = 'UGANDA' THEN 'UG'
               WHEN I.CNTRY_NM = 'TANZANIA' THEN 'TZ'
               WHEN I.CNTRY_NM = 'RWANDA' THEN 'RW'
               WHEN I.CNTRY_NM = 'SUDAN' THEN 'SD'
               WHEN I.CNTRY_NM = 'BURUNDI' THEN 'BI'
               WHEN I.CNTRY_NM = 'ETHIOPIA' THEN 'ET'
               WHEN I.CNTRY_NM = 'INDIA' THEN 'IN'
            END
               AS PHYSICAL_LOCATION_COUNTRY
     FROM   BUSINESS_UNIT A,
            ORGANISATION B, 
            CUSTOMER C,
            ACCOUNT D,
            GUARANTOR E,
            --  CUSTOMER_CONTACT_MODE G,
            ADDRESS H,
            COUNTRY I,
            CUSTOMER_ADDRESS J
    WHERE       C.CUST_ID = D.CUST_ID
            AND B.CUST_ID = C.CUST_ID
            AND B.CUST_ID = D.CUST_ID
            AND E.GUARANTOR_ID = C.CUST_ID
            AND A.BU_ID = C.MAIN_BRANCH_ID
            AND H.CNTRY_ID = I.CNTRY_ID
            --  AND G.CUST_ID = C.CUST_ID
            AND H.ADDR_ID = J.ADDR_ID
            AND C.CUST_ID = J.CUST_ID
            AND C.CUST_TY_ID NOT IN (select CUST_TY_ID FROM customer_type_ref WHERE CUST_CAT = 'PER'));




