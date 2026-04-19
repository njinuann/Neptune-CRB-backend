-- WAKULIMATEST.V_CRB_INDIVIDUAL_CONSUMER source

CREATE OR REPLACE FORCE EDITIONABLE VIEW "WAKULIMATEST"."V_CRB_INDIVIDUAL_CONSUMER" ("SURNAME", "FORENAME_1", "FORENAME_2", "FORNAME_3", "TRADING_NM", "DATE_OF_BIRTH", "BRID", "ACCOUNT_NUMBER", "OLD_ACCT_NUMBER", "GENDER", "NATIONALITY", "MARITAL_STATUS", "PRIMARY_IDENTIFICATION", "PRIMARY_DOC_NO", "SECONDARY_DOC_TYPE", "SECONDARY_DOC_NO", "OTHER_DOC_TYPE", "OTHER_DOC_NO", "PASSPORT_CNTRY_CODE", "MOBILE_NUMBER", "HOME_TELEPHONE", "WORK_TELEPHONE", "POSTAL_ADDRESS_1", "POSTAL_ADDRESS_2", "POSTAL_LOCATION_TOWN", "POSTAL_LOCATION_COUNTRY", "POSTAL_CODE", "PHYSICAL_ADDESS_1", "PHYSICAL_ADDESS_2", "PLOT_NO", "LOCATION_TOWN", "LOCATION_COUNTRY", "TYPE_OF_RESIDENCY", "PIN_NO", "EMAIL", "EMPLOYER_NAME", "EMPLOYER_IND_TYPE", "EMPLOYMENT_DATE", "EMPLYMENT_TYPE", "INCOME_AMT", "LENDERS_REG_NAME", "LENDERS_TRADING_NAME", "LENDERS_BRANCH_NAME", "LENDERS_BRANCH_CODE", "ACCOUNT_INDICATOR", "ACCOUNT_PRODUCT_TYPE", "ACCOUNT_OPENED", "REPAYMENT_DUE_DT", "DISBURSED_AMT", "CURRENCY_OF_FACILITY", "CLEAR_BAL_IN_KE", "CLEAR_BAL", "OVERDUE_BALANCE", "OVERDUE_DATE", "DAYSINARREARS", "NO_INSTALL_IN_ARREARS", "P_NPL_INDICATOR", "ACCOUNT_STATUS", "ACCOUNT_STATUS_DT", "ACCOUNT_CLOSURE_REASON", "REPAYMENT_PERIOD", "DEFERRED_PAYMENT_DT", "DEFERRED_PAYMENT_AMT", "PAYMENT_FREQUENCY", "DISBURSED_DT", "INSTALLMENT_AMT", "LAST_INSTALLMENT_DT", "LAST_PAYMENT_AMT", "TYPE_OF_SECURITY", "GROUPID") AS 
  select distinct p.last_nm as surname, p.first_nm as forename_1, p.middle_nm as forename_2, '' as forname_3, '' as trading_nm,
to_char (p.birth_dt, 'YYYYMMDD') as date_of_birth, c.cust_no as brid, ac.acct_no as account_number, '' as old_acct_number, p.gender_ty as gender,
'KE' as nationality, p.marital_st as marital_status, '001' as primary_identification, p.social_security_no primary_doc_no,
'' as secondary_doc_type, '' as secondary_doc_no, '' as other_doc_type, '' as other_doc_no, '' as passport_cntry_code,
(select ccm.contact from customer_contact_mode ccm where ccm.cust_id = ac.cust_id and ccm.contact_mode_id = 237) as mobile_number,
'' as home_telephone, 
'' as work_telephone,
'' as postal_address_1,
'' as postal_address_2,
'' as postal_location_town,
'' as postal_location_country,
'' as postal_code,
'' as physical_addess_1,
'' as physical_addess_2, 
'' as plot_no, 
'' as location_town, 
'KE' location_country, 
'' as type_of_residency, 
'' as pin_no, '' as email,
'' as employer_name, 
'006' as employer_ind_type, 
'' as employment_date, 
'' as emplyment_type,
round(nvl((select gross_annual_income from customer_employment cee where cee.cust_id = ac.cust_id and cee.rec_st = 'A'),0),0) as income_amt,
(select param_value from ctrl_parameter where param_cd = 'S04') as lenders_reg_name,
(select param_value from ctrl_parameter where param_cd = 'S04') as lenders_trading_name,
bu.bu_nm as lenders_branch_name, ('N179' || (lpad (bu.bu_no, 3, '0'))) as lenders_branch_code,/*adjust this*/
'S' as account_indicator, 'H' as account_product_type, to_char(ac.create_dt,'YYYYMMDD') as account_opened,
case
    when las.delinquent_dt is not null
    then nvl(to_char(ld.due_dt, 'YYYYMMDD'),null)
else
    nvl((select to_char(due_dt, 'YYYYMMDD') from ln_acct_repmnt_event le where le.acct_id = ac.acct_id
    and le.due_dt = (select max(lee.due_dt) from ln_acct_repmnt_event lee where lee.acct_id = le.acct_id
    and lee.due_dt <= (select to_date(display_value, 'DD/MM/YYYY') from ctrl_parameter cp where cp.param_cd = 'S02'))),
    (SELECT TO_CHAR(TO_DATE(DISPLAY_VALUE, 'DD/MM/YYYY'), 'YYYYMMDD') FROM CTRL_PARAMETER CP WHERE CP.PARAM_CD = 'S02'))
end as repayment_due_dt,
round(ltrim((to_char(abs(la.disbursement_limit), '999999999999.99'))),0)||'00' as disbursed_amt,
cu.crncy_cd as currency_of_facility,
case when ltrim(nvl(to_char (abs (las.cleared_bal), '999999999999.99'),'0.00')) = '.00'
    then '0.00'
    else
        round(ltrim(nvl(to_char(abs(las.cleared_bal), '999999999999.99'),'0.00')),0)||'00'
end as clear_bal_in_ke,
case when ltrim(nvl(to_char(abs (las.cleared_bal), '999999999999.99'), '0.00')) = '.00'
    then '0.00'
    else round(ltrim(nvl(to_char(abs (las.cleared_bal), '999999999999.99'),'0.00')),0)||'00'
end as clear_bal,
round(ltrim(nvl((to_char (abs (ld.amt_unpaid), '999999999999.99')),'0.00')),0)||'00' as overdue_balance,
nvl(to_char(ld.due_dt, 'YYYYMMDD'), null) as overdue_date,
case
    when to_char(nvl((select to_date(display_value, 'dd/MM/yyyy') from ctrl_parameter where param_cd = 'S02') - (ld.due_dt), 0), '9999') <= 0
    then '000'
    else
        to_char((select to_date(display_value, 'dd/MM/yyyy') from ctrl_parameter where param_cd = 'S02') - (ld.due_dt),'9999')
end as daysinarrears,
nvl((select count(distinct(le.due_dt)) from ln_acct_repmnt_event le where le.acct_id = ac.acct_id and le.rec_st in ('N','P')
and le.due_dt < (select to_date(display_value, 'DD/MM/YYYY') from ctrl_parameter where param_cd = 'S02')),0) as no_install_in_arrears,
/*case
    when ac.risk_class_id = 552 then 'A'
    when ac.risk_class_id = 553 then 'B'
    when ac.risk_class_id = 554 then 'C'
    when ac.risk_class_id = 555 then 'D'
    when ac.risk_class_id = 556 then 'E'
    when ac.rec_st in ('L') then 'A'
    else 'NONE'
end*/
case
    when ac.risk_class_id = 552 then 'A'
    when ac.risk_class_id = 553 then 'B'
    when ac.risk_class_id = 554 then 'C'
    when ac.risk_class_id = 555 then 'D'
    when ac.risk_class_id = 556 then 'E'
    when ac.rec_st in ('L') then 'A'
    else 'NONE'
end
as p_npl_indicator,
case
    when ac.rec_st = 'L' then 'A'
    when ac.rec_st = 'D' then 'B'
    when ac.rec_st = 'W' then 'C'
    when ac.rec_st in ('A','Q','N','B') then 'F'
    else 'N'
end as account_status,
to_char(nvl(to_date(to_char (ac.sys_create_ts, 'dd/MM/yyyy'),'dd/MM/yyyy'),sysdate),'YYYYMMDD') as account_status_dt,
'' as account_closure_reason,
la.term_value as repayment_period,
'' as deferred_payment_dt,
'' as deferred_payment_amt,
la.term_cd as payment_frequency,
to_char(la.start_dt,'YYYYMMDD') as disbursed_dt,
round(ltrim((to_char(((select sum(le.repmnt_amt) from ln_acct_repmnt_event le where le.acct_id = ac.acct_id
and le.due_dt = (select min(lee.due_dt) from ln_acct_repmnt_event lee
where lee.acct_id = le.acct_id) group by le.due_dt)), '999999999999.99'))), 0)||'00' as installment_amt,
to_char(nvl(to_date (to_char(las.last_payment_dt, 'dd/MM/yyyy'), 'dd/MM/yyyy'), la.start_dt),'YYYYMMDD') as last_installment_dt,
round(las.last_payment_amt, 0)||'00' last_payment_amt,
'S' as type_of_security,
(select max(cc.cust_no) from customer cc, group_member gm where cc.cust_id = gm.group_cust_id and gm.member_cust_id = c.cust_id
and gm.rec_st = 'A') groupid
from person p, customer c, account ac, business_unit bu, product pd, currency cu, credit_appl ca, loan_account la, loan_account_summary las,
(select le.acct_id, min(le.due_dt) due_dt, sum(le.amt_unpaid) amt_unpaid from ln_acct_repmnt_event le
where le.rec_st in ('N', 'P') and le.event_type in ('PRINCIPAL','INTEREST')
and le.due_dt < (select to_date (display_value, 'dd/MM/yyyy') from ctrl_parameter where param_cd = 'S02') group by le.acct_id) ld
where p.cust_id = c.cust_id
and p.cust_id = ac.cust_id
and ac.main_branch_id = bu.bu_id
and ac.prod_id = pd.prod_id
and ac.crncy_id = cu.crncy_id
and ac.acct_id = la.acct_id
and la.appl_id = ca.appl_id
and ac.acct_id = las.acct_id
and ac.acct_id = ld.acct_id
and ac.rec_st IN ('A','N','Q','B');