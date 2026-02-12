package com.cafeflow.core.llm;

public class LLMClientFactory {

    public static LLMClient createGroqClient(String apiKey) {
        return new GroqClient(apiKey, "llama-3.3-70b-versatile");
    }

    public static LLMClient createGeminiClient(String apiKey) {
        return new GeminiClient(apiKey, "gemini-2.0-flash");
    }

}