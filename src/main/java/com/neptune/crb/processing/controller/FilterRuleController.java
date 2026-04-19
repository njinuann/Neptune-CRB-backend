package com.neptune.crb.processing.controller;

import com.neptune.crb.processing.model.ApiResponse;
import com.neptune.crb.processing.model.FilterRule;
import com.neptune.crb.processing.service.FilterRuleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 15-Apr-26
 */
@RestController
@RequestMapping("/api/settings/filter-rules")
public class FilterRuleController
{

    private final FilterRuleService filterRuleService;

    public FilterRuleController(FilterRuleService filterRuleService)
    {
        this.filterRuleService = filterRuleService;
    }

    // -----------------------------------
    // 1. GET → fetch rules
    // -----------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getFilterRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request)
    {
        Map<String, Object> result = filterRuleService.getFilterRules(page, size);

        return buildResponse(result, request);
    }

    // -----------------------------------
    // 2. POST → create rule
    // -----------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createFilterRule(
            @RequestBody FilterRule filterRule,
            HttpServletRequest request)
    {
        Map<String, Object> result = filterRuleService.createFilterRule(filterRule);

        return buildResponse(result, request);
    }

    // -----------------------------------
    // 3. PUT → update rule
    // -----------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateFilterRule(
            @PathVariable String id,
            @RequestBody FilterRule requestBody,
            HttpServletRequest request)
    {
        System.out.println("received update "+id);
        System.out.println("received requestBody "+requestBody);
        Map<String, Object> result = filterRuleService.updateFilterRule(id, requestBody);

        return buildResponse(result, request);
    }

    // -----------------------------------
    // 4. DELETE → delete rule
    // -----------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteFilterRule(
            @PathVariable String id,
            HttpServletRequest request)
    {
        Map<String, Object> result = filterRuleService.deleteFilterRule(id);

        return buildResponse(result, request);
    }

    // -----------------------------------
    // 🔥 Central Response Builder (KEY IMPROVEMENT)
    // -----------------------------------
    private ResponseEntity<ApiResponse<?>> buildResponse(
            Map<String, Object> result,
            HttpServletRequest request)
    {
        String code = String.valueOf(result.get("code"));
        String message = result.get("message") != null
                ? result.get("message").toString()
                : "Request processed";

        Object data = result.get("data");

        boolean success = code != null && code.startsWith("00") || code.startsWith("FL00");

        ApiResponse<Object> response = new ApiResponse<>(
                code.equals("FL00") ? "00" : code,
                message,
                data,
                request.getRequestURI()
        );

        return success
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}