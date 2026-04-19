package com.neptune.crb.processing.model;

import java.util.List;

// For the first response
public class InitialApiResponse {
    private boolean success;
    private boolean has_error;
    private int api_code;
    private String api_code_description;
    private String message;
    private String trx_id;
    private List<RequestData> data;
    private MetaData meta;

    // Getters and setters


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

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getTrx_id()
    {
        return trx_id;
    }

    public void setTrx_id(String trx_id)
    {
        this.trx_id = trx_id;
    }

    public List<RequestData> getData()
    {
        return data;
    }

    public void setData(List<RequestData> data)
    {
        this.data = data;
    }

    public MetaData getMeta()
    {
        return meta;
    }

    public void setMeta(MetaData meta)
    {
        this.meta = meta;
    }

    public static class RequestData {
        private String submission_type;
        private String request_code;
        private String created_date;
        private String request_data_type;

        // Getters and setters

        public String getSubmission_type()
        {
            return submission_type;
        }

        public void setSubmission_type(String submission_type)
        {
            this.submission_type = submission_type;
        }

        public String getRequest_code()
        {
            return request_code;
        }

        public void setRequest_code(String request_code)
        {
            this.request_code = request_code;
        }

        public String getCreated_date()
        {
            return created_date;
        }

        public void setCreated_date(String created_date)
        {
            this.created_date = created_date;
        }

        public String getRequest_data_type()
        {
            return request_data_type;
        }

        public void setRequest_data_type(String request_data_type)
        {
            this.request_data_type = request_data_type;
        }
    }

    public static class MetaData {
        // Meta fields if needed
    }
}

