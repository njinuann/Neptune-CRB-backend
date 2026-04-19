package com.neptune.crb.processing.util;

import com.neptune.crb.processing.model.ErrorData;
import com.neptune.crb.processing.service.ErrorDataFactory;

public class CrbException extends RuntimeException {
    private static final long serialVersionUID = 7718828512143293558L;

    // Optionally store ErrorData if needed later
    private final ErrorData<?> errorData;

    public CrbException(ErrorDataFactory errorDataFactory, String code, String error, String message) {
        this.errorData = errorDataFactory.createErrorData(code, error, message);
    }

    // Alternative: Accept ErrorData directly (no factory dependency)
    public CrbException(ErrorData<?> errorData) {
        this.errorData = errorData;
    }
}