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
    private static final String MONITOR_TAG = "[STOQ-MONITOR]";

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) && within(com.Proyecto.stoq.adapters.controllers..*)")
    public void apiControllerMethods() {}

    @Around("apiControllerMethods()")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String timestamp = LocalDateTime.now().format(formatter);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        // Obtener usuario autenticado del SecurityContext
        String user = obtenerUsuarioAutenticado();

        logger.info("{} ============================================================", MONITOR_TAG);
        logger.info("{} IN  {} | {} {} | user={} | ip={} | ua={}",
            MONITOR_TAG,
                timestamp, method, uri, user, remoteAddr, userAgent);

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            logger.info("{} OUT {} {} | status=OK | time={} ms | endpoint={}",
                MONITOR_TAG, method, uri, executionTime, joinPoint.getSignature().toShortString());
            logger.info("{} ============================================================", MONITOR_TAG);

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            logger.error("{} OUT {} {} | status=ERROR | time={} ms | error={} | user={} | ip={}",
                MONITOR_TAG, method, uri, executionTime, e.getMessage(), user, remoteAddr, e);
            logger.info("{} ============================================================", MONITOR_TAG);

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
            logger.debug("{} No se encontro autenticacion en el contexto", MONITOR_TAG);
        }
        return "ANONYMOUS";
    }
}