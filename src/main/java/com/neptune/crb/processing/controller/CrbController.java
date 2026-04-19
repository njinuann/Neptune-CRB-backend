package com.neptune.crb.processing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neptune.crb.processing.component.ResponseData;
import com.neptune.crb.processing.component.ResponseWrapper;
import com.neptune.crb.processing.model.*;
import com.neptune.crb.processing.repository.CrbReportTypeRepository;
import com.neptune.crb.processing.service.CRBDelinquencyService;
import com.neptune.crb.processing.service.ReportingService;
import com.neptune.crb.processing.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.neptune.crb.processing.util.Logger.logError;
import static com.neptune.crb.processing.util.Logger.logEvent;

@RestController
@RequestMapping("/api")
public class CrbController
{

    private final ReportingService reportingService;
    private final UserService userService;
    private final CrbReportTypeRepository crbReportTypeRepository;
    private final CRBDelinquencyService delinquencyService;

    @Autowired
    public CrbController(ReportingService reportingService, UserService userService, CrbReportTypeRepository crbReportTypeRepository, CRBDelinquencyService delinquencyService)
    {
        this.reportingService = reportingService;
        this.userService = userService;
        this.crbReportTypeRepository = crbReportTypeRepository;
        this.delinquencyService = delinquencyService;
    }

    @GetMapping("/echo")
    public String getData(HttpServletRequest request)
    {
        Enumeration<String> headerNames = request.getHeaderNames();
        System.out.println("Headers:");

        logEvent("Headers:");
        while (headerNames.hasMoreElements())
        {
            String headerName = headerNames.nextElement();
            logEvent(headerName, request.getHeader(headerName));
        }
        return "{responseCode:00," +
                "responseMessage: echo Successful}";
    }

    @PostMapping("/crbResponse")
    public String processCrbData(@RequestBody String jsonString)
    {
        try
        {
            logEvent("requestString", jsonString);
            ObjectMapper objectMapper = new ObjectMapper();
            ResponseWrapper requestWrapper = objectMapper.readValue(jsonString, ResponseWrapper.class);

            List<ResponseData> requestDataList = requestWrapper.getData();
            for (ResponseData requestData : requestDataList)
            {
                requestData.setRequestCode(requestWrapper.getRequestCode());
                logEvent("requestData", requestData);
                reportingService.saveCrbCallBackResponse(requestData);
            }
            return "Data processed successfully!";
        } catch (Exception e)
        {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }


    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody User user, HttpServletRequest request)
    {
        try
        {
            // Check for default user first
            if (isDefaultUser(user))
            {
                return createDefaultUserSuccessResponse(user, request);
            }

            // Existing regular user flow
            if (!userService.existsByReference(user.getUserName()))
            {
                logEvent("USER NOT ALLOWED ");
                return createUserNotAllowedResponse(user.getUserName(), request);
            }

            User authenticatedUser = userService.findUserByUserName(user.getUserName(), user.getPassword());
            return createSuccessResponse(authenticatedUser, request);

        } catch (Exception e)
        {
            logError("Error occurred: " + e.getMessage());
            return createErrorResponse("CRB_091", "Login Failed", e.getMessage(), request, HttpStatus.UNAUTHORIZED);
        }
    }

    private ResponseEntity<ApiResponse<?>> createDefaultUserSuccessResponse(User user, HttpServletRequest request)
    {
        Map<String, Object> userData = Map.of(
                "userName", "Neptune",
                "names", "Neptune Consultant",
                "role", "admin",
                "businessUnit", "Head office",
                "permissions", getUserPermissions("admin")
        );

        Map<String, Object> responseData = Map.of(
                "user", userData,
                "token", generateToken(user)
        );

        ApiResponse<?> response = new ApiResponse<>(
                "00",
                "Successful",
                responseData,
                request.getRequestURI()
        );

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<ApiResponse<?>> createSuccessResponse(User authenticatedUser, HttpServletRequest request)
    {
        String token = generateToken(authenticatedUser);
        List<String> permissions = getUserPermissions(authenticatedUser.getRole());

        Map<String, Object> userData = Map.of(
                "userName", authenticatedUser.getUserName(),
                "names", authenticatedUser.getNames(),
                "role", authenticatedUser.getRole(),
                "businessUnit", authenticatedUser.getBusinessUnit(),
                "permissions", permissions
        );

        Map<String, Object> responseData = Map.of(
                "user", userData,
                "token", token
        );

        ApiResponse<?> response = new ApiResponse<>(
                "00",
                "Successful",
                responseData,
                request.getRequestURI()
        );

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<ApiResponse<?>> createUserNotAllowedResponse(String userName, HttpServletRequest request)
    {
        Map<String, Object> responseData = Map.of("user", userName);
        return createErrorResponse("CRB_091", "User Not Allowed", responseData, request, HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<ApiResponse<?>> createErrorResponse(String code, String message,
                                                               Object data, HttpServletRequest request,
                                                               HttpStatus status)
    {
        ApiResponse<Object> errorResponse = new ApiResponse<>(
                code,
                message,
                data,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    @GetMapping("/data/users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fetchTheme(HttpServletRequest request)
    {
        try
        {
            Map<String, Object> usersData = userService.getAllUsers();

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    "00",
                    "Users fetched successfully",
                    usersData,
                    request.getRequestURI()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e)
        {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    "99",
                    "Error fetching users: " + e.getMessage(),
                    null,
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/data/adduser")
    public ResponseEntity<ApiResponse<?>> addUser(@RequestBody CrbUser user, HttpServletRequest request)
    {
        System.out.println("=================/data/adduser ==============");
        System.out.println(user.toString());

        try
        {
            long startTime = System.currentTimeMillis();

            user.setCreatedDate(LocalDateTime.now());
            user.setLastModifiedDate(LocalDateTime.now());

            int rowsAffected = userService.addUser(user);

            if (rowsAffected > 0)
            {  // FIXED: Check for positive number of rows affected
                Map<String, Object> usersData = userService.getUserById(user.getLoginId());

                ApiResponse<?> response = new ApiResponse<>(
                        "00",
                        "Successful",
                        usersData,
                        request.getRequestURI()
                );

                long endTime = System.currentTimeMillis();
                long timeTakenMs = endTime - startTime;
                double timeTakenSec = timeTakenMs / 1000.0;

                System.out.println("Response sent.");
                System.out.println("Time taken: " + timeTakenMs + " ms (" + timeTakenSec + " seconds)");

                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            else
            {
                // User not created - return proper error response
                ApiResponse<?> errorResponse = new ApiResponse<>(
                        "CRB_081",
                        "User creation failed",
                        "No rows affected during user creation",
                        request.getRequestURI()
                );
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e)
        {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    "CRB_080",
                    "FAILED",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/data/user/{loginId}")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable String loginId, HttpServletRequest request)
    {
        try
        {
            int rowsAffected = userService.deleteUser(loginId);

            if (rowsAffected > 0)
            {
                return ResponseEntity.ok(new ApiResponse<>(
                        "00",
                        "User deleted successfully",
                        null,
                        request.getRequestURI()
                ));
            }
            else
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
                        "CRB_081",
                        "User not found",
                        "No user exists with login ID: " + loginId,
                        request.getRequestURI()
                ));
            }
        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    "CRB_080",
                    "Deletion failed",
                    e.getMessage(),
                    request.getRequestURI()
            ));
        }
    }

    @GetMapping("/data/permissions")
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, Object>>>>> fetchPermissions(HttpServletRequest request)
    {

        List<Map<String, Object>> rolePermissions = new ArrayList<>();

        // Define permissions for different roles
        List<String> adminPermissions = List.of(
                "view_guarantor_info", "view_stakeholders", "view_collateral_info",
                "view_credit_app", "view_non_individual_customers", "view_individual_customers",
                "view_status_validation", "view_error_query", "delete_consumer",
                "daily_payment", "view_submission", "manage_users", "submit_reports", "download_reports"
        );

        List<String> userPermissions = List.of(
                "view_guarantor_info", "view_stakeholders", "view_collateral_info",
                "view_credit_app", "view_non_individual_customers", "view_individual_customers",
                "view_status_validation", "view_error_query", "delete_consumer",
                "daily_payment", "view_submission"
        );

        // Add admin permissions
        rolePermissions.add(Map.of(
                "id", "1",
                "role", "admin",
                "permissions", adminPermissions,
                "permissionCount", adminPermissions.size()
        ));

        // Add user permissions
        rolePermissions.add(Map.of(
                "id", "2",
                "role", "user",
                "permissions", userPermissions,
                "permissionCount", userPermissions.size()
        ));

        Map<String, List<Map<String, Object>>> data = Map.of("rolePermissions", rolePermissions);

        // Create API response
        ApiResponse<Map<String, List<Map<String, Object>>>> response = new ApiResponse<>(
                "00",
                "Successful",
                data,
                request.getRequestURI()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/views")
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, String>>>>> getViewItems(HttpServletRequest request)
    {
        List<Map<String, String>> views = new ArrayList<>();
        System.out.println("here for views");
        // Add your view items
        views.add(Map.of(
                "id", "individual-consumer",
                "name", "Individual Consumers",
                "icon", "users",
                "permission", "view_individual_customers",
                "endpoint", "/api/individual-consumer"

        ));
        views.add(Map.of(
                "id", "non-individual-consumer",
                "name", "Non Individual Customers",
                "icon", "users",
                "permission", "view_non_individual_customers",
                "endpoint", "/api/non-individual-consumer"

        ));
        views.add(Map.of(
                "id", "credit-app-info",
                "name", "Credit Information",
                "icon", "user-circle",
                "permission", "view_credit_app",
                "endpoint", "/api/credit-app-info"

        ));
        views.add(Map.of(
                "id", "collateral-info",
                "name", "Collateral Information",
                "icon", "user-circle",
                "permission", "view_collateral_info",
                "endpoint", "/api/collateral-info"

        ));
        views.add(Map.of(
                "id", "stakeholder-info",
                "name", "Stakeholder Information",
                "icon", "user-circle",
                "permission", "view_stakeholders",
                "endpoint", "/api/stakeholder-info"
        ));
        views.add(Map.of(
                "id", "guarantor-info",
                "name", "Guarantor Information",
                "icon", "currency-dollar",
                "permission", "view_guarantor_info",
                "endpoint", "/api/guarantor-info"
        ));

        views.add(Map.of(
                "id", "daily-payment",
                "name", "Daily Payment Info",
                "icon", "currency-dollar",
                "permission", "daily_payment",
                "endpoint", "/api/daily-payment"
        ));

        views.add(Map.of(
                "id", "view-submission",
                "name", "Check Submissions",
                "icon", "currency-dollar",
                "permission", "view_submission",
                "endpoint", "/api/view-submission"
        ));

        views.add(Map.of(
                "id", "status-validation",
                "name", "Status Validation",
                "icon", "currency-dollar",
                "permission", "view_status_validation",
                "endpoint", "/api/status-validation"
        ));

        views.add(Map.of(
                "id", "view-error-query",
                "name", "Check Errors",
                "icon", "currency-dollar",
                "permission", "view_error_query",
                "endpoint", "/api/error-query"
        ));


//        views.add(Map.of(
//                "id", "delete-consumer",
//                "name", "Delete Consumer",
//                "icon", "currency-dollar",
//                "permission", "delete_consumer",
//                "endpoint", "/api/delete-consumer"
//        ));


        // Wrap views in a map under the key "views"
        Map<String, List<Map<String, String>>> data = Map.of("views", views);

        // Create API response
        System.out.println("here for views " + data);
        ApiResponse<Map<String, List<Map<String, String>>>> response = new ApiResponse<>(
                "00",
                "Successful",
                data,
                request.getRequestURI()
        );

        return ResponseEntity.ok(response);
    }

    private String resolveViewById(String viewId)
    {
        if (viewId == null)
        {
            return "";
        }
        return switch (viewId.trim())
        {
            case "individual-consumer" -> "CE";
            case "non-individual-consumer" -> "CI";
            case "credit-app-info" -> "CA";
            case "collateral-info" -> "CR";
            case "stakeholder-info" -> "SI";
            case "guarantor-info" -> "GI";
            case "status-validation" -> "SV";
            case "view-error-query" -> "VE";
            case "daily-payment" -> "DP";
            case "delete-consumer" -> "DE";
            case "view-submission" -> "VS";
            case "view-submission-summary" -> "SM";

            default -> "";
        };
    }

    @GetMapping("/views/{id}/data")
    public ResponseEntity<ApiResponse<?>> getViewData(@PathVariable String id, HttpServletRequest request)
    {
        System.out.println("=================/views/{id}/data ============== " + id);
        System.out.println(id);
        try
        {
            String reportType = resolveViewById(id);
            System.out.println("=================reportType ============== " + reportType);
            List<Map<String, Object>> report = reportingService.getReportViewData(reportType);
            ApiResponse<?> response = new ApiResponse<>(
                    "00",
                    "Successful",
                    report,
                    request.getRequestURI()
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e)
        {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    "CRB_080",
                    "FAILED",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{viewId}/check-errors")
    public ResponseEntity<?> checkErrors(@PathVariable String viewId, @RequestParam String reportCode, HttpServletRequest request)
    {
        System.out.println("=================/views/{id}/data ============== " + viewId);
        System.out.println(viewId);
        try
        {
            String reportType = resolveViewById(viewId);
            System.out.println("=================reportType ============== " + reportType);
            List<Map<String, Object>> report = reportingService.getReportViewData(reportType);
            ApiResponse<?> response = new ApiResponse<>(
                    "00",
                    "Successful",
                    report,
                    request.getRequestURI()
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e)
        {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    "CRB_080",
                    "FAILED",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/views/{id}/submit")
    public ResponseEntity<ApiResponse<?>> submitViewData(@PathVariable String id, HttpServletRequest request)
    {
        System.out.println("=================/views/{id}/data ==============");
        System.out.println(id);
        List<Map<String, String>> views = new ArrayList<>();
        try
        {
            String reportType = resolveViewById(id);
            if (!reportingService.reportEnabled(reportType))
            {
                ApiResponse<Object> errorResponse = new ApiResponse<>(
                        "CRB_099",
                        "DISABLED",
                        "This report is not enabled for submission. Please contact System Admin!",
                        request.getRequestURI()
                );
                return new ResponseEntity<>(errorResponse, HttpStatus.PRECONDITION_FAILED);
            }

            boolean submitted = reportingService.processReportAPIData(reportType, true);
            if (submitted)
            {
                ApiResponse<?> response = new ApiResponse<>(
                        "00",
                        "Successful",
                        views,
                        request.getRequestURI()
                );
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            else
            {
                return new ResponseEntity<>(null, HttpStatus.PROCESSING);
            }

        } catch (Exception e)
        {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    "CRB_080",
                    "FAILED",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/views/{id}/summary")
    public ResponseEntity<ApiResponse<?>> getReportMetrics(@PathVariable String id, HttpServletRequest request)
    {
        System.out.println("=================/views/{id}/data ==============");
        System.out.println(id);
        try
        {
            String reportType = resolveViewById(id);
            List<Map<String, Object>> report = reportingService.getDataMetrics();
            ApiResponse<?> response = new ApiResponse<>(
                    "00",
                    "Successful",
                    report,
                    request.getRequestURI()
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e)
        {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    "CRB_080",
                    "FAILED",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/views/{id}/submissions")
    public ResponseEntity<ApiResponse<?>> getSubmissionSummary(@PathVariable String reportType, HttpServletRequest request)
    {
        System.out.println("=================/views/{id}/submissions ============== " + reportType);
        System.out.println(reportType);
        try
        {
            //   String reportType = id; //resolveViewById(id);
            System.out.println("=================reportType ============== " + reportType);
            List<Map<String, Object>> report = reportingService.getReportViewData(reportType);
            ApiResponse<?> response = new ApiResponse<>(
                    "00",
                    "Successful",
                    report,
                    request.getRequestURI()
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e)
        {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    "CRB_080",
                    "FAILED",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/report-types")
    public ApiResponse<Map<String, List<Map<String, String>>>> getReportTypes(HttpServletRequest request)
    {
        try
        {
            List<Map<String, String>> reportTypes = crbReportTypeRepository.getActiveReportTypesAsMap();

            Map<String, List<Map<String, String>>> data = new HashMap<>();
            data.put("reportTypes", reportTypes);

            return new ApiResponse<>(
                    "00",
                    "Successful",
                    data,
                    request.getRequestURI()
            );
        } catch (Exception e)
        {
            Map<String, List<Map<String, String>>> emptyData = new HashMap<>();
            emptyData.put("reportTypes", new ArrayList<>());

            return new ApiResponse<>(
                    "CRB_99",
                    "Failed to retrieve report types: " + e.getMessage(),
                    emptyData,
                    request.getRequestURI()
            );
        }
    }

    @GetMapping("/report-types/all")
    public ApiResponse<Map<String, List<CrbReportType>>> getAllReportTypes(HttpServletRequest request)
    {
        try
        {
            List<CrbReportType> reportTypes = crbReportTypeRepository.getAllReportTypes();

            Map<String, List<CrbReportType>> data = new HashMap<>();
            data.put("reportTypes", reportTypes);

            return new ApiResponse<>(
                    "00",
                    "Successful",
                    data,
                    request.getRequestURI()
            );
        } catch (Exception e)
        {
            Map<String, List<CrbReportType>> emptyData = new HashMap<>();
            emptyData.put("reportTypes", new ArrayList<>());

            return new ApiResponse<>(
                    "CRB_99",
                    "Failed to retrieve report types: " + e.getMessage(),
                    emptyData,
                    request.getRequestURI()
            );
        }
    }


    private Map<String, String> createItem(String id, String name)
    {
        Map<String, String> item = new HashMap<>();
        item.put("id", id);
        item.put("name", name);
        return item;
    }

    private List<String> getUserPermissions(String role)
    {
        if (role == null)
        {
            return List.of();
        }

        String normalizedRole = role.toLowerCase();

        return switch (normalizedRole)
        {
            case "system administrator", "admin" -> List.of(
                    "view_guarantor_info", "view_stakeholders", "view_collateral_info",
                    "view_credit_app", "view_non_individual_customers", "view_individual_customers",
                    "view_status_validation", "view_error_query", "delete_consumer",
                    "daily_payment", "view_submission", "users", "submit_reports", "download_reports"
            );
            case "user" -> List.of(
                    "view_guarantor_info", "view_stakeholders", "view_collateral_info",
                    "view_credit_app", "view_non_individual_customers", "view_individual_customers",
                    "view_status_validation", "view_error_query", "delete_consumer",
                    "daily_payment", "view_submission"
            );
            default -> List.of();
        };
    }


    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512); // Securely generated key

    private String generateToken(User user)
    {
        return Jwts.builder()
                .setSubject(user.getUserName())
                .claim("role", user.getRole())
                .claim("businessUnit", user.getBusinessUnit())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Token valid for 1 day
                .signWith(SECRET_KEY) // Use the securely generated key
                .compact();
    }

    private boolean isDefaultUser(User user)
    {
        return user.getUserName().equalsIgnoreCase("neptune") && user.getPassword().equalsIgnoreCase("password1");

    }

    @PostMapping("/report-types")
    public ApiResponse<Map<String, Object>> upsertReportType(
            @RequestBody CrbReportType request,
            HttpServletRequest httpRequest)
    {

        try
        {
            int affectedRows = crbReportTypeRepository.upsertReportType(
                    request.getReportType(),
                    request.getReportNm(),
                    request.getEnabled(),
                    request.getPriority(),
                    request.getScheduleType()
            );

            CrbReportType updatedRecord = crbReportTypeRepository.getReportTypeById(request.getReportType());

            Map<String, Object> data = new HashMap<>();
            data.put("affectedRows", affectedRows);
            data.put("reportType", updatedRecord);

            return new ApiResponse<>(
                    "00",
                    affectedRows > 0 ? "Operation successful" : "No changes made",
                    data,
                    httpRequest.getRequestURI()
            );

        } catch (Exception e)
        {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("affectedRows", 0);

            return new ApiResponse<>(
                    "CRB_99",
                    "Failed to upsert report type: " + e.getMessage(),
                    errorData,
                    httpRequest.getRequestURI()
            );
        }
    }

    // Batch UPSERT
    @PostMapping("/report-types/batch")
    public ApiResponse<Map<String, Object>> batchUpsertReportTypes(
            @RequestBody List<CrbReportType> requests,
            HttpServletRequest httpRequest)
    {

        try
        {
            // Convert requests to entities
            List<CrbReportType> reportTypes = requests.stream()
                    .map(req -> new CrbReportType(req.getReportType(), req.getReportNm(), req.getEnabled(), req.getPriority(), req.getScheduleType()))
                    .collect(Collectors.toList());

            int[] results = crbReportTypeRepository.batchUpsertReportTypes(reportTypes);
            int totalAffected = Arrays.stream(results).sum();

            Map<String, Object> data = new HashMap<>();
            data.put("totalAffectedRows", totalAffected);
            data.put("individualResults", results);
            data.put("processedCount", reportTypes.size());

            return new ApiResponse<>(
                    "00",
                    "Batch operation completed successfully",
                    data,
                    httpRequest.getRequestURI()
            );

        } catch (Exception e)
        {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("totalAffectedRows", 0);

            return new ApiResponse<>(
                    "CRB_99",
                    "Failed to batch upsert report types: " + e.getMessage(),
                    errorData,
                    httpRequest.getRequestURI()
            );
        }
    }

    // Delete endpoint
    @DeleteMapping("/report-types/{reportType}")
    public ApiResponse<Map<String, Object>> deleteReportType(
            @PathVariable String reportType,
            HttpServletRequest httpRequest)
    {

        try
        {
            int affectedRows = crbReportTypeRepository.deleteReportType(reportType);

            Map<String, Object> data = new HashMap<>();
            data.put("affectedRows", affectedRows);
            data.put("deletedReportType", reportType);

            return new ApiResponse<>(
                    "00",
                    affectedRows > 0 ? "Deleted successfully" : "Report type not found",
                    data,
                    httpRequest.getRequestURI()
            );

        } catch (Exception e)
        {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("affectedRows", 0);

            return new ApiResponse<>(
                    "CRB_99",
                    "Failed to delete report type: " + e.getMessage(),
                    errorData,
                    httpRequest.getRequestURI()
            );
        }
    }

    @GetMapping("/views/view-error-query/check-errors")
    public ResponseEntity<ApiResponse<?>> checkViewErrors(
            @RequestParam String reportCode,
            HttpServletRequest request)
    {
        System.out.println("=================/views/view-error-query/check-errors ==============");
        System.out.println("Report Code: " + reportCode);

        try
        {
            // Get the error data from the external API
            List<Map<String, Object>> errorData = reportingService.getErrorResponse(reportCode);

            ApiResponse<?> response = new ApiResponse<>(
                    "00",
                    "Successful",
                    errorData,  // This returns the List<Map<String, Object>>
                    request.getRequestURI()
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e)
        {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    "CRB_080",
                    "FAILED",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/crb/delinquency-enquiry")
    public ResponseEntity<CRBApiResponse> enquireDelinquency(
            @Valid @RequestBody DelinquentCheckRequest request,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "SYSTEM")
            String userId, HttpServletRequest httpServletRequest)
    {


        try
        {
            CRBDelinquencyReport report = delinquencyService.checkDelinquency(request, userId);

            if (report == null)
            {
                return ResponseEntity.ok(new CRBApiResponse(
                        "CRB_099",
                        "Failed to process request: ",
                        new CRBDelinquencyData()));

            }

            CRBApiResponse response = new CRBApiResponse(
                    "00",
                    "Success",
                    new CRBDelinquencyData(Collections.singletonList(report)));
            return ResponseEntity.ok(response);


        } catch (Exception e)
        {
            logError("Error processing delinquency check", e);
            return ResponseEntity.ok(new CRBApiResponse(
                    "CRB_099",
                    "System error: " + e.getMessage(),
                    new CRBDelinquencyData()));
        }
    }

    @PostMapping("/crb/delinquency-check")
    public ResponseEntity<CRBApiResponse> checkDelinquency(
            @Valid @RequestBody CRBDelinquencyRequest crbDelinquencyRequest,
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "SYSTEM")
            String userId, HttpServletRequest httpServletRequest)
    {
        try
        {
            CRBDelinquencyReport report = delinquencyService.checkDelinquencyById(crbDelinquencyRequest, userId);

            if (report == null)
            {
                return ResponseEntity.ok(new CRBApiResponse(
                        "CRB_099",
                        "Failed to process request: ",
                        new CRBDelinquencyData()));

            }

            CRBApiResponse response = new CRBApiResponse(
                    "00",
                    "Success",
                    new CRBDelinquencyData(Collections.singletonList(report)));
            return ResponseEntity.ok(response);


        } catch (Exception e)
        {
            logError("Error processing delinquency check", e);
            return ResponseEntity.ok(new CRBApiResponse(
                    "CRB_099",
                    "System error: " + e.getMessage(),
                    new CRBDelinquencyData()));
        }
    }
    @PostMapping("/settings/theme")
    public ResponseEntity<ApiResponse<?>> saveTheme(@RequestBody String theme, HttpServletRequest request)
    {
        JSONObject themeJson = new JSONObject(theme);
        System.out.println("=================/settings/theme ==============");
        System.out.println(themeJson.toString());

        try
        {
            long startTime = System.currentTimeMillis();

            int rowsAffected = userService.saveTheme(themeJson.optString("theme"));

            if (rowsAffected > 0)
            {
                ApiResponse<?> response = new ApiResponse<>(
                        "00",
                        "Theme saved successfully",
                        theme,
                        request.getRequestURI()
                );

                long endTime = System.currentTimeMillis();
                long timeTakenMs = endTime - startTime;
                double timeTakenSec = timeTakenMs / 1000.0;

                System.out.println("Response sent.");
                System.out.println("Time taken: " + timeTakenMs + " ms (" + timeTakenSec + " seconds)");

                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            else
            {
                // User not created - return proper error response
                ApiResponse<?> errorResponse = new ApiResponse<>(
                        "CRB_081",
                        "Theme update failed",
                        "No rows affected during user creation",
                        request.getRequestURI()
                );
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e)
        {
            ApiResponse<Object> errorResponse = new ApiResponse<>(
                    "CRB_080",
                    "FAILED",
                    e.getMessage(),
                    request.getRequestURI()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/settings/theme")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fetchUsers(HttpServletRequest request)
    {
        try
        {
            Map<String, Object> usersData = userService.getTheme();

            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    "00",
                    "Theme fetched successfully",
                    usersData,
                    request.getRequestURI()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e)
        {
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    "99",
                    "Error fetching Theme: " + e.getMessage(),
                    null,
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
