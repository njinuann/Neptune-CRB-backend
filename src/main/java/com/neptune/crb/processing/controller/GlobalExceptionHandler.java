//package com.neptune.crb.processing.controller;
//
//import com.neptune.crb.processing.model.ApiErrorResponse;
//import jakarta.validation.ConstraintViolationException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.stream.Collectors;
//
//@Slf4j
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiErrorResponse> handleValidationException(
//            MethodArgumentNotValidException ex) {
//
//        String errors = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(FieldError::getDefaultMessage)
//                .collect(Collectors.joining(", "));
//
//        return ResponseEntity.badRequest()
//                .body(new ApiErrorResponse(
//                        "CRB_001",
//                        errors,
//                        null
//                ));
//    }
//
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
//            IllegalArgumentException ex) {
//
//        return ResponseEntity.badRequest()
//                .body(new ApiErrorResponse(
//                        "CRB_002",
//                        ex.getMessage(),
//                        null
//                ));
//    }
//
//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
//            ConstraintViolationException ex) {
//
//        return ResponseEntity.badRequest()
//                .body(new ApiErrorResponse(
//                        "CRB_003",
//                        ex.getMessage(),
//                        null
//                ));
//    }
//
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<ApiErrorResponse> handleRuntimeException(
//            RuntimeException ex) {
//
//        log.error("Runtime exception", ex);
//
//        return ResponseEntity.internalServerError()
//                .body(new ApiErrorResponse(
//                        "CRB_099",
//                        ex.getMessage(),
//                        null
//                ));
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiErrorResponse> handleGeneralException(
//            Exception ex) {
//
//        log.error("Unhandled exception", ex);
//
//        return ResponseEntity.internalServerError()
//                .body(new ApiErrorResponse(
//                        "CRB_999",
//                        "Unexpected system error",
//                        null
//                ));
//    }
//}