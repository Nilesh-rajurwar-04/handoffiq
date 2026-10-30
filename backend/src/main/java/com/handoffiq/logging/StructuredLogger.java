package com.handoffiq.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class StructuredLogger {

    private static final Logger log = LoggerFactory.getLogger(StructuredLogger.class);

    public void logDomainEvent(String event, Long userId, String outcome, long durationMs, String message) {
        String originalEvent = MDC.get(LoggingMdcFilter.MDC_EVENT);
        String originalUserId = MDC.get(LoggingMdcFilter.MDC_USER_ID);
        String originalOutcome = MDC.get(LoggingMdcFilter.MDC_OUTCOME);
        String originalDuration = MDC.get(LoggingMdcFilter.MDC_DURATION_MS);

        try {
            if (event != null) MDC.put(LoggingMdcFilter.MDC_EVENT, event);
            if (userId != null) MDC.put(LoggingMdcFilter.MDC_USER_ID, String.valueOf(userId));
            if (outcome != null) MDC.put(LoggingMdcFilter.MDC_OUTCOME, outcome);
            MDC.put(LoggingMdcFilter.MDC_DURATION_MS, String.valueOf(durationMs));

            log.info("{}", message);
        } finally {
            if (originalEvent != null) MDC.put(LoggingMdcFilter.MDC_EVENT, originalEvent);
            if (originalUserId != null) MDC.put(LoggingMdcFilter.MDC_USER_ID, originalUserId);
            if (originalOutcome != null) MDC.put(LoggingMdcFilter.MDC_OUTCOME, originalOutcome);
            if (originalDuration != null) MDC.put(LoggingMdcFilter.MDC_DURATION_MS, originalDuration);
        }
    }
}
