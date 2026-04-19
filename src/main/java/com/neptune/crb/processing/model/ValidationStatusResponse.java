package com.neptune.crb.processing.model;

import java.util.List;

// For the validation status response
public class ValidationStatusResponse {
    private boolean success;
    private boolean has_error;
    private int api_code;
    private String api_code_description;
    private String message;
    private String trx_id;
    private List<ValidationData> data;
    private MetaData meta;

    public MetaData getMeta()
    {
        return meta;
    }

    public void setMeta(MetaData meta)
    {
        this.meta = meta;
    }

    public List<ValidationData> getData()
    {
        return data;
    }

    public void setData(List<ValidationData> data)
    {
        this.data = data;
    }

    public String getTrx_id()
    {
        return trx_id;
    }

    public void setTrx_id(String trx_id)
    {
        this.trx_id = trx_id;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getApi_code_description()
    {
        return api_code_description;
    }

    public void setApi_code_description(String api_code_description)
    {
        this.api_code_description = api_code_description;
    }

    public int getApi_code()
    {
        return api_code;
    }

    public void setApi_code(int api_code)
    {
        this.api_code = api_code;
    }

    public boolean isHas_error()
    {
        return has_error;
    }

    public void setHas_error(boolean has_error)
    {
        this.has_error = has_error;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public static class ValidationData {
        private Integer liveStatus;
        private String submissionType;
        private String reference_code;
        private String template_version;
        private String data_ref;
        private Boolean is_final_submission;
        private Integer records_imported;
        private Integer records_count;
        private Integer empty_records;
        private Integer records_passed;
        private Integer records_rejected;
        private Integer error_count;
        private String download_url;
        private String error_log_status;
        private Integer status;

        // Getters and setters

        public Integer getLiveStatus()
        {
            return liveStatus;
        }

        public void setLiveStatus(Integer liveStatus)
        {
            this.liveStatus = liveStatus;
        }

        public String getSubmissionType()
        {
            return submissionType;
        }

        public void setSubmissionType(String submissionType)
        {
            this.submissionType = submissionType;
        }

        public String getReference_code()
        {
            return reference_code;
        }

        public void setReference_code(String reference_code)
        {
            this.reference_code = reference_code;
        }

        public String getTemplate_version()
        {
            return template_version;
        }

        public void setTemplate_version(String template_version)
        {
            this.template_version = template_version;
        }

        public String getData_ref()
        {
            return data_ref;
        }

        public void setData_ref(String data_ref)
        {
            this.data_ref = data_ref;
        }

        public Boolean getIs_final_submission()
        {
            return is_final_submission;
        }

        public void setIs_final_submission(Boolean is_final_submission)
        {
            this.is_final_submission = is_final_submission;
        }

        public Integer getRecords_imported()
        {
            return records_imported;
        }

        public void setRecords_imported(Integer records_imported)
        {
            this.records_imported = records_imported;
        }

        public Integer getRecords_count()
        {
            return records_count;
        }

        public void setRecords_count(Integer records_count)
        {
            this.records_count = records_count;
        }

        public Integer getEmpty_records()
        {
            return empty_records;
        }

        public void setEmpty_records(Integer empty_records)
        {
            this.empty_records = empty_records;
        }

        public Integer getRecords_passed()
        {
            return records_passed;
        }

        public void setRecords_passed(Integer records_passed)
        {
            this.records_passed = records_passed;
        }

        public Integer getRecords_rejected()
        {
            return records_rejected;
        }

        public void setRecords_rejected(Integer records_rejected)
        {
            this.records_rejected = records_rejected;
        }

        public Integer getError_count()
        {
            return error_count;
        }

        public void setError_count(Integer error_count)
        {
            this.error_count = error_count;
        }

        public String getDownload_url()
        {
            return download_url;
        }

        public void setDownload_url(String download_url)
        {
            this.download_url = download_url;
        }

        public String getError_log_status()
        {
            return error_log_status;
        }

        public void setError_log_status(String error_log_status)
        {
            this.error_log_status = error_log_status;
        }

        public Integer getStatus()
        {
            return status;
        }

        public void setStatus(Integer status)
        {
            this.status = status;
        }
    }

    public static class MetaData {
        // Meta fields if needed
        
    }

    public static class ErrorGenerationResponse {
        private boolean success;
        private boolean has_error;
        private int api_code;
        private String api_code_description;
        private String trx_id;
        private ErrorGenerationData data;

        public boolean isSuccess()
        {
            return success;
        }

        public void setSuccess(boolean success)
        {
            this.success = success;
        }

        public boolean isHas_error()
        {
            return has_error;
        }

        public void setHas_error(boolean has_error)
        {
            this.has_error = has_error;
        }

        public int getApi_code()
        {
            return api_code;
        }

        public void setApi_code(int api_code)
        {
            this.api_code = api_code;
        }

        public String getApi_code_description()
        {
            return api_code_description;
        }

        public void setApi_code_description(String api_code_description)
        {
            this.api_code_description = api_code_description;
        }

        public String getTrx_id()
        {
            return trx_id;
        }

        public void setTrx_id(String trx_id)
        {
            this.trx_id = trx_id;
        }

        public ErrorGenerationData getData()
        {
            return data;
        }

        public void setData(ErrorGenerationData data)
        {
            this.data = data;
        }

        public static class ErrorGenerationData {
            private String reference_code;
            private int record_count;
            private int record_passed;
            private int record_rejected;
            private int error_count;
            private String template_version;
            private String file_type;
            private String batch_file_type;

            public String getReference_code()
            {
                return reference_code;
            }

            public void setReference_code(String reference_code)
            {
                this.reference_code = reference_code;
            }

            public int getRecord_count()
            {
                return record_count;
            }

            public void setRecord_count(int record_count)
            {
                this.record_count = record_count;
            }

            public int getRecord_passed()
            {
                return record_passed;
            }

            public void setRecord_passed(int record_passed)
            {
                this.record_passed = record_passed;
            }

            public int getRecord_rejected()
            {
                return record_rejected;
            }

            public void setRecord_rejected(int record_rejected)
            {
                this.record_rejected = record_rejected;
            }

            public int getError_count()
            {
                return error_count;
            }

            public void setError_count(int error_count)
            {
                this.error_count = error_count;
            }

            public String getTemplate_version()
            {
                return template_version;
            }

            public void setTemplate_version(String template_version)
            {
                this.template_version = template_version;
            }

            public String getFile_type()
            {
                return file_type;
            }

            public void setFile_type(String file_type)
            {
                this.file_type = file_type;
            }

            public String getBatch_file_type()
            {
                return batch_file_type;
            }

            public void setBatch_file_type(String batch_file_type)
            {
                this.batch_file_type = batch_file_type;
            }
        }
    }

    public static class ErrorLogResponse {
        private boolean success;
        private boolean has_error;
        private int api_code;
        private String api_code_description;
        private String trx_id;
        private ErrorLogData data;

        public boolean isSuccess()
        {
            return success;
        }

        public void setSuccess(boolean success)
        {
            this.success = success;
        }

        public boolean isHas_error()
        {
            return has_error;
        }

        public void setHas_error(boolean has_error)
        {
            this.has_error = has_error;
        }

        public int getApi_code()
        {
            return api_code;
        }

        public void setApi_code(int api_code)
        {
            this.api_code = api_code;
        }

        public String getApi_code_description()
        {
            return api_code_description;
        }

        public void setApi_code_description(String api_code_description)
        {
            this.api_code_description = api_code_description;
        }

        public String getTrx_id()
        {
            return trx_id;
        }

        public void setTrx_id(String trx_id)
        {
            this.trx_id = trx_id;
        }

        public ErrorLogData getData()
        {
            return data;
        }

        public void setData(ErrorLogData data)
        {
            this.data = data;
        }

        public static class ErrorLogData {
            private String validPartner;
            private String submittedBy;
            private String submissionType;
            private String reference_code;
            private String template_version;
            private String submission_date;
            private String file_size;
            private String file_name;
            private String status;
            private String records_count;
            private String records_imported;
            private String records_passed;
            private String records_rejected;
            private String error_count;
            private String is_final_submission;
            private String download_url;
            private String file_url;
            private String error_log_status;

            public String getValidPartner()
            {
                return validPartner;
            }

            public void setValidPartner(String validPartner)
            {
                this.validPartner = validPartner;
            }

            public String getSubmittedBy()
            {
                return submittedBy;
            }

            public void setSubmittedBy(String submittedBy)
            {
                this.submittedBy = submittedBy;
            }

            public String getSubmissionType()
            {
                return submissionType;
            }

            public void setSubmissionType(String submissionType)
            {
                this.submissionType = submissionType;
            }

            public String getReference_code()
            {
                return reference_code;
            }

            public void setReference_code(String reference_code)
            {
                this.reference_code = reference_code;
            }

            public String getTemplate_version()
            {
                return template_version;
            }

            public void setTemplate_version(String template_version)
            {
                this.template_version = template_version;
            }

            public String getSubmission_date()
            {
                return submission_date;
            }

            public void setSubmission_date(String submission_date)
            {
                this.submission_date = submission_date;
            }

            public String getFile_size()
            {
                return file_size;
            }

            public void setFile_size(String file_size)
            {
                this.file_size = file_size;
            }

            public String getFile_name()
            {
                return file_name;
            }

            public void setFile_name(String file_name)
            {
                this.file_name = file_name;
            }

            public String getStatus()
            {
                return status;
            }

            public void setStatus(String status)
            {
                this.status = status;
            }

            public String getRecords_count()
            {
                return records_count;
            }

            public void setRecords_count(String records_count)
            {
                this.records_count = records_count;
            }

            public String getRecords_imported()
            {
                return records_imported;
            }

            public void setRecords_imported(String records_imported)
            {
                this.records_imported = records_imported;
            }

            public String getRecords_passed()
            {
                return records_passed;
            }

            public void setRecords_passed(String records_passed)
            {
                this.records_passed = records_passed;
            }

            public String getRecords_rejected()
            {
                return records_rejected;
            }

            public void setRecords_rejected(String records_rejected)
            {
                this.records_rejected = records_rejected;
            }

            public String getError_count()
            {
                return error_count;
            }

            public void setError_count(String error_count)
            {
                this.error_count = error_count;
            }

            public String getIs_final_submission()
            {
                return is_final_submission;
            }

            public void setIs_final_submission(String is_final_submission)
            {
                this.is_final_submission = is_final_submission;
            }

            public String getDownload_url()
            {
                return download_url;
            }

            public void setDownload_url(String download_url)
            {
                this.download_url = download_url;
            }

            public String getFile_url()
            {
                return file_url;
            }

            public void setFile_url(String file_url)
            {
                this.file_url = file_url;
            }

            public String getError_log_status()
            {
                return error_log_status;
            }

            public void setError_log_status(String error_log_status)
            {
                this.error_log_status = error_log_status;
            }
        }
    }
}
