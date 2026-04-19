package com.neptune.crb.processing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neptune.crb.processing.config.WebConfig;
import com.neptune.crb.processing.model.XApiResponse;
import com.neptune.crb.processing.repository.SubmissionRepository;
import com.neptune.crb.processing.util.AXWorker;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static com.neptune.crb.processing.util.Logger.logError;
import static com.neptune.crb.processing.util.Logger.logEvent;

@Service
public class ApiClient
{

    private final AXWorker axWorker;
    private final CaffeineTokenService tokenService;
    private final SubmissionRepository submissionRepository;
    private final WebConfig webConfig;

    @Autowired
    public ApiClient(OkHttpClient okHttpClient, AXWorker axWorker, CaffeineTokenService tokenService, SubmissionRepository submissionRepository, WebConfig webConfig)
    {
        this.axWorker = axWorker;
        this.tokenService = tokenService;
        this.submissionRepository = submissionRepository;
        this.webConfig = webConfig;
    }

    public void apiGet(String submissionURL)
    {
        try
        {
            String token = tokenService.getToken();
            OkHttpClient client = webConfig.okHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            Request request = new Request.Builder()
                    .url(submissionURL)
                    .get()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("cache-control", "no-cache")
                    .build();

            try (Response response = client.newCall(request).execute())
            {
                String responseBody = response.body().string();
                logEvent("API Response", responseBody);
                if (!response.isSuccessful())
                {
                    throw new RuntimeException("API call failed: " + response.code() + " - " + response.message());
                }
                ObjectMapper objectMapper = new ObjectMapper();
                XApiResponse apiResponse = objectMapper.readValue(responseBody, XApiResponse.class);

                // Save the response to database
                //  saveCrbRequest(apiResponse);
            }
        } catch (IOException | RuntimeException e)
        {
            logError("Error processing Api call", e);
            throw new RuntimeException(e);
        }
    }

    public XApiResponse sendJsonToApi(String jsonData, String submissionURL, String reportType)
    {
        logEvent("submissionURL", submissionURL);
        try
        {
            String token = tokenService.getToken();
            OkHttpClient client = webConfig.okHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody body = RequestBody.create(jsonData, JSON);
            Request request = new Request.Builder()
                    .url(submissionURL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("cache-control", "no-cache")
                    .build();

            try (Response response = client.newCall(request).execute())
            {
                String responseBody = response.body().string();
                logEvent("API Response", responseBody);
                if (!response.isSuccessful())
                {
                    throw new RuntimeException("API call failed: " + response.code() + " - " + response.message());
                }
                ObjectMapper objectMapper = new ObjectMapper();
                XApiResponse apiResponse = objectMapper.readValue(responseBody, XApiResponse.class);
                logEvent("apiResponse", apiResponse.toString());

                submissionRepository.saveReportRequest(apiResponse, reportType);
                return apiResponse;
            }
        } catch (IOException | RuntimeException e)
        {
            logError("Error processing Api call", e);
            return null;
        }
    }

    public XApiResponse sendCsvToApi(File file,
                                     String submissionURL,
                                     String reportType,
                                     String callbackUrl)
    {
        logEvent("SUBMIT CSV FILE", submissionURL);
        logEvent("SUBMIT CSV FILE", reportType);
        logEvent("SUBMIT CSV FILE", callbackUrl);
        logEvent("SUBMIT CSV FILE", file.getName());

        try
        {
            String token = tokenService.getToken();
            OkHttpClient client = webConfig.okHttpClient();

            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("templateVersion", "4")
                    .addFormDataPart("submissionType", Objects.equals(reportType, "DP") ? "D" : "M")
                    .addFormDataPart("callbackUrl", callbackUrl)
                    .addFormDataPart("finalSubmissionState", "true")
                    .addFormDataPart(
                            "file",
                            file.getName(),
                            RequestBody.create(file, MediaType.parse("text/csv"))
                    );

            Request request = new Request.Builder()
                    .url(submissionURL)
                    .post(builder.build())
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("cache-control", "no-cache")
                    .build();

            try (Response response = client.newCall(request).execute())
            {
                String responseBody = response.body().string();
                logEvent("API Response", responseBody);

                if (!response.isSuccessful())
                {
                    throw new RuntimeException("API call failed: " + response.code() + " - " + response.message());
                }

                ObjectMapper objectMapper = new ObjectMapper();
                XApiResponse apiResponse = objectMapper.readValue(responseBody, XApiResponse.class);

                logEvent("apiResponse", apiResponse.toString());

                submissionRepository.saveReportRequest(apiResponse, reportType);

                return apiResponse;
            }

        } catch (Exception e)
        {
            logError("Error processing Api call", e);
            return null;
        }
    }

    private void sendJsonToApi(File jsonFile) throws Exception
    {
        String authUrl = String.format("%s:%s/%s/datasubmission/authenticate?grant_type=client_credentials",
                axWorker.baseUrl, Integer.parseInt(axWorker.port), axWorker.apiVersion);
        String token = tokenService.getToken();
        // Read JSON file content
        byte[] fileBytes = Files.readAllBytes(jsonFile.toPath());
        MediaType mediaType = MediaType.parse("application/json");

        // Create Request Body
        RequestBody requestBody = RequestBody.create(fileBytes, mediaType);

        // Build the HTTP request
        Request request = new Request.Builder()
                .url(authUrl)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        // Execute the request
        try (Response response = webConfig.okHttpClient().newCall(request).execute())
        {
            if (!response.isSuccessful())
            {
                throw new RuntimeException("Unexpected HTTP response: " + response);
            }
            System.out.println("API Response: " + response.body().string());
        }
    }


    public String getValidationStatus(String requestCode, String dateType, String trxId) throws IOException
    {
        String statusUrl = String.format("%s:%s/%s/datasubmission/upload/file?request_code=" + requestCode,
                axWorker.baseUrl, Integer.parseInt(axWorker.port), axWorker.apiVersion);

        String token = tokenService.getToken();

        Request request = new Request.Builder()
                .url(statusUrl)
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("X-Data-Type", dateType)
                .addHeader("X-TRX-ID", trxId)
                .build();

        try (Response response = webConfig.okHttpClient().newCall(request).execute())
        {
            String responseBody = response.body().string();
            logEvent("API Response", responseBody);
            if (!response.isSuccessful())
            {
                throw new RuntimeException("Validation API call failed: " + response.code());
            }
            ObjectMapper objectMapper = new ObjectMapper();
            // InitialApiResponse apiResponse = objectMapper.readValue(responseBody, InitialApiResponse.class);


            return responseBody;
        }
    }

    public String fetchCSVErrorLog(String requestCode)
    {
        try
        {
            logEvent("Here to fetch response", requestCode);
            String statusUrl = String.format("%s:%s/%s/datasubmission/error/geterrorlist?reference_code=" + requestCode,
                    axWorker.baseUrl, Integer.parseInt(axWorker.port), axWorker.apiVersion);

            String token = tokenService.getToken();

            Request request = new Request.Builder()
                    .url(statusUrl)
                    .get()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            try (Response response = webConfig.okHttpClient().newCall(request).execute())
            {
                String responseBody = response.body().string();
                logEvent("API Response", responseBody);
                logEvent("API Response code", response.code());
                if (!response.isSuccessful())
                {
                    logEvent("Validation API call failed: " + response.code());
                }
                return responseBody;
            }
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


    public boolean getSubmittedReports()
    {
        try
        {
            String submissionUrl = String.format("%s:%s/%s/datasubmission/upload/requests?submission_type=M",
                    axWorker.baseUrl, Integer.parseInt(axWorker.port), axWorker.apiVersion);
            logEvent(submissionUrl);

            apiGet(submissionUrl);
            return true;
        } catch (Exception e)
        {
            logError("Error processing reports for SubmittedReports ", e);
        }
        return false;
    }
}
