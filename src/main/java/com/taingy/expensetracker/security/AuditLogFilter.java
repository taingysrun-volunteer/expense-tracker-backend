package com.taingy.expensetracker.security;

import com.taingy.expensetracker.model.AuditLog;
import com.taingy.expensetracker.repository.AuditLogRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class AuditLogFilter extends OncePerRequestFilter {

    private final AuditLogRepository auditLogRepository;

    public AuditLogFilter(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper(response);

        CachedBodyHttpServletRequest wrappedRequest =
                new CachedBodyHttpServletRequest(request);

        long startTime = System.currentTimeMillis();
        Exception exception = null;

        try {
            filterChain.doFilter(wrappedRequest, response);
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            AuditLog log = new AuditLog();
            log.setMethod(request.getMethod());
            log.setEndpoint(request.getRequestURI());
            log.setStatus(response.getStatus());
            log.setExecutionTimeMs(duration);
            log.setIpAddress(request.getRemoteAddr());
            log.setCreatedAt(LocalDateTime.now());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.setUsername(auth != null ? auth.getName() : "ANONYMOUS");

            if (!request.getMethod().equals("GET") && !request.getRequestURI().startsWith("/api/auth")) {
                log.setRequestBody(new String(wrappedRequest.getInputStream().readAllBytes()));
            }

            String responseBody = new String(
                    wrappedResponse.getContentAsByteArray(),
                    StandardCharsets.UTF_8
            );

            if (wrappedResponse.getStatus() >= 400) {
                log.setErrorMessage(
                        exception != null
                                ? exception.getMessage()
                                : responseBody
                );
            }

            auditLogRepository.save(log);

            wrappedResponse.copyBodyToResponse();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api/audit-logs");
    }

}

