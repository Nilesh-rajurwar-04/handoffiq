package com.handoffiq.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingMdcFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingMdcFilter.class);
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_EVENT = "event";
    public static final String MDC_DURATION_MS = "durationMs";
    public static final String MDC_OUTCOME = "outcome";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = "corr-" + UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put(MDC_CORRELATION_ID, correlationId);
        MDC.put(MDC_USER_ID, "anonymous");
        MDC.put(MDC_EVENT, request.getMethod() + " " + request.getRequestURI());
        MDC.put(MDC_OUTCOME, "PENDING");
        MDC.put(MDC_DURATION_MS, "0");

        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
            long duration = System.currentTimeMillis() - startTime;
            MDC.put(MDC_DURATION_MS, String.valueOf(duration));
            String outcome = response.getStatus() < 400 ? "SUCCESS" : "FAILED_" + response.getStatus();
            MDC.put(MDC_OUTCOME, outcome);

            if (!request.getRequestURI().contains("/actuator") && !request.getRequestURI().contains("/swagger")) {
                log.info("HTTP request completed with status {}", response.getStatus());
            }
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put(MDC_DURATION_MS, String.valueOf(duration));
            MDC.put(MDC_OUTCOME, "EXCEPTION");
            log.error("HTTP request failed with unhandled exception: {}", ex.getMessage(), ex);
            throw ex;
        } finally {
            MDC.clear();
        }
    }
}
