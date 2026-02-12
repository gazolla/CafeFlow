package com.cafeflow.core.llm;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class LLMConfig {

    @Value("${llm.default-provider:gemini}")
    private String defaultProvider;

    @Value("${llm.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${llm.gemini.model:gemini-2.0-flash}")
    private String geminiModel;

    @Value("${llm.groq.api-key:}")
    private String groqApiKey;

    @Value("${llm.groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @PostConstruct
    public void init() {
        boolean hasGemini = geminiApiKey != null && !geminiApiKey.isBlank();
        boolean hasGroq = groqApiKey != null && !groqApiKey.isBlank();

        if (hasGemini) {
            log.info("LLM provider available: Google Gemini (model: {})", geminiModel);
        }
        if (hasGroq) {
            log.info("LLM provider available: Groq (model: {})", groqModel);
        }
        if (!hasGemini && !hasGroq) {
            log.warn("No LLM API keys configured. AI helpers will not be functional. "
                    + "Set GEMINI_API_KEY or GROQ_API_KEY environment variable.");
        } else {
            log.info("Default LLM provider: {}", defaultProvider);
        }
    }

    @Bean
    public LLMClient geminiClient() {
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            return new GeminiClient(geminiApiKey, geminiModel);
        }
        return null;
    }

    @Bean
    public LLMClient groqClient() {
        if (groqApiKey != null && !groqApiKey.isBlank()) {
            return new GroqClient(groqApiKey, groqModel);
        }
        return null;
    }

    @Bean
    @Primary
    public LLMClient llmClient() {
        boolean hasGemini = geminiApiKey != null && !geminiApiKey.isBlank();
        boolean hasGroq = groqApiKey != null && !groqApiKey.isBlank();

        if ("groq".equalsIgnoreCase(defaultProvider) && hasGroq) {
            log.info("Primary LLM client: Groq ({})", groqModel);
            return new GroqClient(groqApiKey, groqModel);
        }

        if ("gemini".equalsIgnoreCase(defaultProvider) && hasGemini) {
            log.info("Primary LLM client: Gemini ({})", geminiModel);
            return new GeminiClient(geminiApiKey, geminiModel);
        }

        // Fallback: use whichever is available
        if (hasGemini) {
            log.info("Primary LLM client (fallback): Gemini ({})", geminiModel);
            return new GeminiClient(geminiApiKey, geminiModel);
        }

        if (hasGroq) {
            log.info("Primary LLM client (fallback): Groq ({})", groqModel);
            return new GroqClient(groqApiKey, groqModel);
        }

        log.warn("No LLM client available. Returning null - AI helpers will throw on use.");
        return null;
    }
}
