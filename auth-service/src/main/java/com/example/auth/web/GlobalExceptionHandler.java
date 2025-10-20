package com.example.auth.web;

import com.example.auth.service.DuplicateUserException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice(basePackages = "com.example.auth")
public class GlobalExceptionHandler {

    private Map<String, Object> body(HttpStatus status, String error, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status.value());
        map.put("error", error);
        map.put("message", message);
        map.put("timestamp", Instant.now().toString());
        return map;
    }

    // 409: username or email already taken
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateUserException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body(HttpStatus.CONFLICT, "Duplicate Entry", ex.getMessage()));
    }

    // 400: @Valid on @RequestBody failed
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage(),
                        (a, b) -> a // first wins if duplicate keys
                ));

        Map<String, Object> map = body(HttpStatus.BAD_REQUEST, "Validation Failed", "One or more fields are invalid.");
        map.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(map);
    }

    // 400: @Valid on query/path params, or programmatic validator throws ConstraintViolationException
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        var violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (a, b) -> a
                ));
        Map<String, Object> map = body(HttpStatus.BAD_REQUEST, "Validation Failed", "Constraint violations.");
        map.put("violations", violations);
        return ResponseEntity.badRequest().body(map);
    }

    // 400: Binding errors on non-request body (e.g., form/data, query params)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBind(BindException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage(),
                        (a, b) -> a
                ));
        Map<String, Object> map = body(HttpStatus.BAD_REQUEST, "Validation Failed", "Binding failed.");
        map.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(map);
    }

    // 400: Missing required request parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        String msg = "Missing required parameter: " + ex.getParameterName();
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Bad Request", msg));
    }

    // 400: Bad/malformed JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Bad Request", "Malformed JSON request."));
    }

    // 400: Generic client error signaling bad input
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage()));
    }

    // 409: DB unique constraint or other integrity violation
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body(HttpStatus.CONFLICT, "Data Integrity Violation", "Duplicate or invalid data."));
    }

    // 500: Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAny(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage()));
    }
}
