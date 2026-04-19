//package com.ryleq.crb.crb.processing.controller;
//
//import model.com.neptune.crb.processing.ApiResponse;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import org.springframework.validation.FieldError;
//
//import java.util.stream.Collectors;
//
//import static util.com.neptune.crb.processing.Logger.logError;
//
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<Object>> handleException(Exception e, HttpServletRequest request) {
//        logError("Exception occurred at "+request.getRequestURI()  +": "+ e.getMessage(),  e); // Logs full stack trace
//        ApiResponse<Object> response = new ApiResponse<>(
//                "CRB_091",
//                "FAILED",
//                e.getMessage(),
//                request.getRequestURI()
//        );
//        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex,HttpServletRequest request) {
//        String errorMessage = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(FieldError::getDefaultMessage)
//                .collect(Collectors.joining(", "));
//        ApiResponse<Object> response = new ApiResponse<>(
//                "CRB_400",
//                "VALIDATION_FAILED",
//                errorMessage,
//                request.getRequestURI()
//        );
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//}
