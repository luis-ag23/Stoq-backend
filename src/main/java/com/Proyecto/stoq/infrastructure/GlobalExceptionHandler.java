package com.Proyecto.stoq.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        HttpStatus status = resolveStatus(ex.getMessage());
        logger.warn("Error de negocio | status={} | path={} | message={}", status.value(), request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(status, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex,
                                                                         HttpServletRequest request) {
        Map<String, String> validationErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        }

        logger.warn("Error de validación | path={} | details={}", request.getRequestURI(), validationErrors, ex);

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Datos de entrada inválidos",
                request.getRequestURI(),
                validationErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex,
                                                                          HttpServletRequest request) {
        logger.warn("Violación de restricción | path={} | message={}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Error interno no controlado | path={}", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor",
                request.getRequestURI(),
                null
        );
    }

    private HttpStatus resolveStatus(String message) {
        if (message == null || message.isBlank()) {
            return HttpStatus.BAD_REQUEST;
        }

        String msg = message.toLowerCase();

        if (msg.contains("no encontrado")) {
            return HttpStatus.NOT_FOUND;
        }
        if (msg.contains("no tiene autorización") || msg.contains("no tiene permiso")
                || msg.contains("autorización") || msg.contains("autorizacion")
                || msg.contains("no autorizado") || msg.contains("forbidden")) {
            return HttpStatus.FORBIDDEN;
        }
        if (msg.contains("credenciales inválidas") || msg.contains("credenciales invalidas")
                || msg.contains("contraseña incorrecta") || msg.contains("contrasena incorrecta")) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (msg.contains("ya existe") || msg.contains("ya está") || msg.contains("ya esta")
                || msg.contains("registrado") || msg.contains("en uso")) {
            return HttpStatus.CONFLICT;
        }

        return HttpStatus.BAD_REQUEST;
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status,
                                                              String message,
                                                              String path,
                                                              Map<String, String> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        if (details != null && !details.isEmpty()) {
            body.put("details", details);
        }
        return ResponseEntity.status(status).body(body);
    }
}