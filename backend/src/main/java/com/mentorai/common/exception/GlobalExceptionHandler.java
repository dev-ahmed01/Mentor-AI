package com.mentorai.common.exception;

import com.mentorai.common.config.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "One or more fields are invalid.", fieldErrors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "One or more values are invalid.", Map.of(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiError> handleBadCredentials(HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "The email or password is incorrect.", Map.of(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> handleAccessDenied(HttpServletRequest request) {
        return response(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                "You do not have permission to access this resource.", Map.of(), request);
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> handleConflict(ConflictException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "RESOURCE_CONFLICT",
                exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND",
                exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleDataIntegrity(HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "RESOURCE_CONFLICT",
                "The requested change conflicts with existing data.", Map.of(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unhandled request failure", exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "The request could not be completed.", Map.of(), request);
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status,
            String code,
            String message,
            Map<String, String> fieldErrors,
            HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestIdFilter.ATTRIBUTE);
        ApiError error = new ApiError(
                Instant.now(), status.value(), code, message, request.getRequestURI(),
                requestId == null ? "unavailable" : requestId.toString(), fieldErrors);
        return ResponseEntity.status(status).body(error);
    }
}
