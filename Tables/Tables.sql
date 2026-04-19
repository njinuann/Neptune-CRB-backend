SELECT * FROM CRB_RESPONSE_DATA

DESCRIBE channelmanagertest.CRB_REQUEST_DATA;


INSERT INTO channelmanagertest.CRB_REQUEST_DATA (success, has_error, api_code, api_code_description, trx_id) 
VALUES ('Y', 'N', '1010', 'Data Submitted Successful', 'KWPT-VKHX-20250613') 

ALTER TABLE CRB_RESPONSE_DATA
ADD CREATE_DATE DATE DEFAULT SYSDATE;


CREATE TABLE crb_validation_status (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trx_id VARCHAR2(36) NOT NULL,
    reference_code VARCHAR2(50),
    data_ref VARCHAR2(10) NOT NULL,
    live_status NUMBER,
    records_count NUMBER,
    records_rejected NUMBER,
    status NUMBER NOT NULL,
    success NUMBER(1) NOT NULL CHECK (success IN (0,1)),
    has_error NUMBER(1) NOT NULL CHECK (has_error IN (0,1)),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_trx_id UNIQUE (trx_id)
);

COMMENT ON TABLE crb_validation_status IS 'Stores CRB validation status responses';
COMMENT ON COLUMN crb_validation_status.success IS '1=true, 0=false';
COMMENT ON COLUMN crb_validation_status.has_error IS '1=true, 0=false';

CREATE INDEX idx_crb_status_data_ref ON crb_validation_status(data_ref);
CREATE INDEX idx_crb_status_created ON crb_validation_status(created_at);
=============================
CREATE TABLE channelmanagertest.CRB_REQUEST_DATA (
    success CHAR(1) NOT NULL,
    has_error CHAR(1) NOT NULL,
    api_code VARCHAR2(20) NOT NULL,
    api_code_description VARCHAR2(2000),
    trx_id VARCHAR2(100) NOT NULL,
    created_date TIMESTAMP DEFAULT SYSTIMESTAMP
);

-- Add primary key if needed
ALTER TABLE channelmanagertest.CRB_REQUEST_DATA 
ADD CONSTRAINT pk_crb_request_data PRIMARY KEY (trx_id);

-- Add comments for documentation
COMMENT ON TABLE channelmanagertest.CRB_REQUEST_DATA IS 'Stores API response data for CRB requests';
COMMENT ON COLUMN channelmanagertest.CRB_REQUEST_DATA.success IS 'Y/N flag indicating success';
COMMENT ON COLUMN channelmanagertest.CRB_REQUEST_DATA.has_error IS 'Y/N flag indicating error';

======================


CREATE TABLE validation_status (
    -- Core fields from root
    trx_id VARCHAR2(36) NOT NULL,
    success NUMBER(1) NOT NULL,
    has_error NUMBER(1) NOT NULL,
    api_code NUMBER(10),
    api_code_description VARCHAR2(500),
    message VARCHAR2(1000),
    
    -- Data array fields
    reference_code VARCHAR2(50) NOT NULL,
    data_ref VARCHAR2(10),
    live_status NUMBER(1),
    records_count NUMBER(10),
    records_rejected NUMBER(10),
    status_id NUMBER(10),
    status_name VARCHAR2(100),
    
    -- Additional fields from data array
    cug_id VARCHAR2(100),
    template_version VARCHAR2(20),
    is_final_submission NUMBER(1),
    records_imported NUMBER(10),
    empty_records NUMBER(10),
    records_passed NUMBER(10),
    error_count NUMBER(10),
    download_url VARCHAR2(500),
    postlive_error_url VARCHAR2(500),
    error_log_status VARCHAR2(100),
    post_live_error_status VARCHAR2(100),
    post_error_count NUMBER(10),
    submission_type VARCHAR2(1),
    submission_template_type VARCHAR2(1),
    
    -- Meta fields
    meta_first NUMBER(1),
    meta_last NUMBER(1),
    meta_number NUMBER(10),
    meta_size NUMBER(10),
    meta_number_of_elements NUMBER(10),
    meta_total_pages NUMBER(10),
    meta_total_elements NUMBER(10),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT SYSTIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Constraints
    CONSTRAINT pk_validation_status PRIMARY KEY (trx_id)
);

COMMENT ON TABLE validation_status IS 'Stores comprehensive validation status information from API responses';

====================
CREATE TABLE error_logs (
    id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trx_id VARCHAR2(100) NOT NULL,
    success NUMBER(1) DEFAULT 0 NOT NULL,
    has_error NUMBER(1) DEFAULT 1 NOT NULL,
    api_code NUMBER(10),
    api_code_description VARCHAR2(500),
    column_name VARCHAR2(100),
    column_index NUMBER(10),
    row_index NUMBER(10),
    value_provided VARCHAR2(4000),
    data_rule_id_type_id NUMBER(10),
    error_description VARCHAR2(4000),
    client_number VARCHAR2(50),
    account_number VARCHAR2(50),
    created_date TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT chk_success CHECK (success IN (0, 1)),
    CONSTRAINT chk_has_error CHECK (has_error IN (0, 1))
);

-- Create indexes for better query performance
CREATE INDEX idx_error_logs_trx_id ON error_logs(trx_id);
CREATE INDEX idx_error_logs_client_num ON error_logs(client_number);
CREATE INDEX idx_error_logs_account_num ON error_logs(account_number);
CREATE INDEX idx_error_logs_created_date ON error_logs(created_date);