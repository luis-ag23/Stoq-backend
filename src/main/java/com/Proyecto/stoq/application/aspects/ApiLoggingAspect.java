package com.Proyecto.stoq.application.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
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

    @Pointcut("within(com.Proyecto.stoq.adapters.controllers..*)")
    public void apiControllerMethods() {}

    @Around("apiControllerMethods()")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        String timestamp = LocalDateTime.now().format(formatter);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String handler = ((MethodSignature) joinPoint.getSignature()).getDeclaringType().getSimpleName()
                + "."
                + joinPoint.getSignature().getName();

        // Obtener usuario autenticado del SecurityContext
        String user = obtenerUsuarioAutenticado();

        logger.info("[API-AUDIT][START] ts={} method={} uri={} handler={} user={} ip={} ua={}",
                timestamp, method, uri, handler, user, remoteAddr, userAgent);

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            logger.info("[API-AUDIT][END] method={} uri={} handler={} user={} durationMs={} result={}",
                    method, uri, handler, user, executionTime, result != null ? "Success" : "NoContent");

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            logger.error("[API-AUDIT][ERROR] method={} uri={} handler={} user={} ip={} durationMs={} error={}",
                    method, uri, handler, user, remoteAddr, executionTime, e.getMessage(), e);

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