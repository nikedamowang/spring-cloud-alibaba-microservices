package com.cloudDemo.userservice.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局校验异常处理器
 * 统一处理参数校验错误，提供友好的错误响应
 */
@Slf4j
@RestControllerAdvice
public class ValidationExceptionHandler {

    /**
     * 处理请求体参数校验异常 (@RequestBody 上的 @Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.warn("请求参数校验失败: {}", e.getMessage());

        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(this::buildFieldError)
                .collect(Collectors.toList());

        Map<String, Object> response = buildErrorResponse(
                "参数校验失败",
                "VALIDATION_FAILED",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理表单参数校验异常 (@ModelAttribute 上的 @Valid)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindException(BindException e) {
        log.warn("表单参数校验失败: {}", e.getMessage());

        List<Map<String, String>> errors = e.getFieldErrors().stream()
                .map(this::buildFieldError)
                .collect(Collectors.toList());

        Map<String, Object> response = buildErrorResponse(
                "表单参数校验失败",
                "FORM_VALIDATION_FAILED",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理单个参数校验异常 (@PathVariable, @RequestParam 上的校验)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("参数约束校验失败: {}", e.getMessage());

        List<Map<String, String>> errors = e.getConstraintViolations().stream()
                .map(this::buildConstraintError)
                .collect(Collectors.toList());

        Map<String, Object> response = buildErrorResponse(
                "参数约束校验失败",
                "CONSTRAINT_VIOLATION",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 构建字段错误信息
     */
    private Map<String, String> buildFieldError(FieldError fieldError) {
        Map<String, String> error = new HashMap<>();
        error.put("field", fieldError.getField());
        error.put("message", fieldError.getDefaultMessage());
        error.put("rejectedValue", String.valueOf(fieldError.getRejectedValue()));
        return error;
    }

    /**
     * 构建约束错误信息
     */
    private Map<String, String> buildConstraintError(ConstraintViolation<?> violation) {
        Map<String, String> error = new HashMap<>();
        error.put("field", violation.getPropertyPath().toString());
        error.put("message", violation.getMessage());
        error.put("rejectedValue", String.valueOf(violation.getInvalidValue()));
        return error;
    }

    /**
     * 构建统一的错误响应格式
     */
    private Map<String, Object> buildErrorResponse(String message, String errorCode, List<Map<String, String>> errors) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("errorCode", errorCode);
        response.put("timestamp", new Date().getTime());
        response.put("errors", errors);
        response.put("errorCount", errors.size());
        return response;
    }
}
