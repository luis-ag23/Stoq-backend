package com.Proyecto.stoq.application.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class ApiLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingAspect.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllerMethods() {}

    @Around("restControllerMethods()")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String timestamp = LocalDateTime.now().format(formatter);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Obtener usuario autenticado del SecurityContext
        String user = obtenerUsuarioAutenticado();

        logger.info("[API AUDIT] {} | {} {} | User: {} | IP: {} | User-Agent: {}",
                timestamp, method, uri, user, remoteAddr, userAgent);

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            logger.info("[API PERFORMANCE] {} {} completed in {} ms", method, uri, executionTime);

            // Loguear respuesta exitosa
            logger.debug("[API RESPONSE] {} {} | Status: 200 | Response: {}", method, uri, result != null ? "Success" : "No Content");

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            logger.error("[API ERROR] {} {} failed after {} ms | Error: {}", method, uri, executionTime, e.getMessage(), e);

            // Loguear error para auditoría
            logger.warn("[API SECURITY] Potential issue detected on {} {} | User: {} | IP: {}", method, uri, user, remoteAddr);

            throw e; // Re-lanzar la excepción
        }
    }

    /**
     * Extrae el usuario autenticado del SecurityContext
     * @return email del usuario o "ANONYMOUS" si no está autenticado
     */
    private String obtenerUsuarioAutenticado() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                return authentication.getName();
            }
        } catch (Exception e) {
            logger.debug("No authentication found in context");
        }
        return "ANONYMOUS";
    }
}