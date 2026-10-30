package com.handoffiq.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiServiceFactory {

    private static final Logger log = LoggerFactory.getLogger(AiServiceFactory.class);

    @Value("${handoffiq.ai.provider:gemini}")
    private String configuredProvider;

    private final AiServiceProvider geminiAiService;
    private final AiServiceProvider openAiService;
    private final AiServiceProvider fallbackAiService;

    public AiServiceFactory(
            @Qualifier("geminiAiService") AiServiceProvider geminiAiService,
            @Qualifier("openAiService") AiServiceProvider openAiService,
            @Qualifier("fallbackAiService") AiServiceProvider fallbackAiService) {
        this.geminiAiService = geminiAiService;
        this.openAiService = openAiService;
        this.fallbackAiService = fallbackAiService;
    }

    public AiServiceProvider getProvider() {
        String provider = configuredProvider != null ? configuredProvider.trim().toLowerCase() : "gemini";
        log.debug("Selecting active AI provider: {}", provider);

        return switch (provider) {
            case "openai" -> openAiService;
            case "fallback", "rules" -> fallbackAiService;
            default -> geminiAiService;
        };
    }
}
