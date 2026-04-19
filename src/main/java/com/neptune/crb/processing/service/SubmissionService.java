package com.neptune.crb.processing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neptune.crb.processing.model.InitialApiResponse;
import com.neptune.crb.processing.model.ValidationStatusResponse;
import com.neptune.crb.processing.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final ObjectMapper objectMapper;
    private final ApiClient apiClient;

@Autowired
    public SubmissionService(SubmissionRepository submissionRepository,
                             ObjectMapper objectMapper,
                             ApiClient apiClient) {
        this.submissionRepository = submissionRepository;
        this.objectMapper = objectMapper;
        this.apiClient = apiClient;
    }

    public void processInitialResponse(String jsonResponse) throws IOException
    {
        InitialApiResponse response = objectMapper.readValue(jsonResponse, InitialApiResponse.class);

        if (response.isSuccess() && response.getData() != null) {
            for (InitialApiResponse.RequestData requestData : response.getData()) {
                // Call validation status for each request code
                String validationResponseJson = apiClient.getValidationStatus(
                        requestData.getRequest_code(),
                        requestData.getRequest_data_type(),
                        response.getTrx_id()
                );

                ValidationStatusResponse validationResponse = objectMapper.readValue(
                        validationResponseJson,
                        ValidationStatusResponse.class
                );

                // Save to database
                submissionRepository.saveSubmissionDetails(
                        requestData.getRequest_code(),
                        requestData.getRequest_data_type(),
                        response.getTrx_id(),
                        validationResponse
                );
            }
        }
    }


}
