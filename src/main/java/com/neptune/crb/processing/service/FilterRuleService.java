package com.neptune.crb.processing.service;

import com.neptune.crb.processing.model.FilterRule;
import com.neptune.crb.processing.repository.FilterRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author NJINU <alexnyiha@gmail.com>
 * @version 1.0
 * @since 15-Apr-26
 */
@Service
public class FilterRuleService
{

    private final FilterRuleRepository repository;

    public FilterRuleService(FilterRuleRepository repository)
    {
        this.repository = repository;
    }

    // -----------------------------------
    // 1. GET (with pagination)
    // -----------------------------------
    public Map<String, Object> getFilterRules(int page, int size)
    {
        int offset = page * size;

        List<FilterRule> rules = repository.findAll(offset, size);
        int total = repository.countAll();

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("total", total);

        Map<String, Object> data = new HashMap<>();
        data.put("rules", rules);
        data.put("pagination", pagination);

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("code", "FL00");

        return response;
    }

    // -----------------------------------
    // 2. CREATE
    // -----------------------------------
    public Map<String, Object> createFilterRule(FilterRule request)
    {
        // generate ID if not provided
        String id = request.getId() != null ? request.getId() : "r_" + System.currentTimeMillis();

        request.setId(id);
        request.setActive(Boolean.TRUE.equals(request.getActive()) ? "Y" : "N");
        request.setCreatedAt(LocalDateTime.now());

        repository.save(request);

        return buildSuccessResponse("Rule created successfully", request);
    }

    // -----------------------------------
    // 3. UPDATE
    // -----------------------------------
    public Map<String, Object> updateFilterRule(String id, FilterRule request)
    {
        Optional<FilterRule> existingOpt = repository.findById(id);
        System.out.println("reposit " + existingOpt);

        if (existingOpt.isEmpty())
        {
            return buildErrorResponse("Rule not found");
        }

        FilterRule existing = existingOpt.get();

        // update fields
        existing.setName(request.getName() == null ? existing.getName() : request.getName());
        existing.setCategory(request.getCategory() == null ? existing.getCategory() : request.getCategory());
        existing.setField(request.getField() == null ? existing.getField() : request.getField());
        existing.setOperator(request.getOperator() == null ? existing.getOperator() : request.getOperator());
        existing.setValue(request.getValue() == null ? existing.getValue() : request.getValue());
        existing.setActive(request.getActive() == null ? existing.getActive() : request.getActive());

        repository.update(id, existing);

        return buildSuccessResponse("Rule updated successfully", existing);
    }

    // -----------------------------------
    // 4. DELETE
    // -----------------------------------
    public Map<String, Object> deleteFilterRule(String id)
    {
        Optional<FilterRule> existingOpt = repository.findById(id);

        if (existingOpt.isEmpty())
        {
            return buildErrorResponse("Rule not found");
        }

        repository.delete(id);

        return buildSuccessResponse("Rule deleted successfully", null);
    }

    // -----------------------------------
    // 5. GET ACTIVE RULES (for submission use-case)
    // -----------------------------------
    public List<FilterRule> getActiveRules()
    {
        return repository.findActive();
    }

    // -----------------------------------
    // Helpers
    // -----------------------------------
    private Map<String, Object> buildSuccessResponse(String message, Object dataObj)
    {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "FL00");
        response.put("message", message);
        response.put("data", dataObj);
        return response;
    }

    private Map<String, Object> buildErrorResponse(String message)
    {
        Map<String, Object> response = new HashMap<>();
        response.put("code", "FL96");
        response.put("message", message);
        response.put("data", null);
        return response;
    }
}