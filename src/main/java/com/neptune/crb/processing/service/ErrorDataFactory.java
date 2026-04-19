package com.neptune.crb.processing.service;

import com.neptune.crb.processing.model.ErrorData;
import org.springframework.stereotype.Service;

@Service
public class ErrorDataFactory {

    public <T> ErrorData<T> createErrorData(String code, String error, String message) {
        return ErrorData.<T>builder()
                .code(code)
                .error(error)
                .message(message)
                .build();
    }
}

