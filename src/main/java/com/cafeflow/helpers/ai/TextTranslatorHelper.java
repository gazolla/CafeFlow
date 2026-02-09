package com.cafeflow.helpers.ai;

import com.cafeflow.core.base.BaseHelper;
import com.cafeflow.core.llm.LLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TextTranslatorHelper extends BaseHelper {

    private final LLMClient llmClient;

    @Override
    protected String getServiceName() {
        return "text_translator";
    }

    /**
     * Translates the given text to the target language.
     * Returns only the translated text, no explanations.
     */
    public String translate(String text, String targetLanguage) {
        return executeWithProtection("translate(" + targetLanguage + ")", () -> {
            String prompt = """
                    Translate the following text to %s.
                    Return ONLY the translated text, nothing else. No explanations, no notes.

                    Text:
                    %s""".formatted(targetLanguage, text);
            return llmClient.send(prompt);
        });
    }

    /**
     * Translates the given text from a specific source language to the target language.
     */
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        return executeWithProtection("translate(%s->%s)".formatted(sourceLanguage, targetLanguage), () -> {
            String prompt = """
                    Translate the following text from %s to %s.
                    Return ONLY the translated text, nothing else. No explanations, no notes.

                    Text:
                    %s""".formatted(sourceLanguage, targetLanguage, text);
            return llmClient.send(prompt);
        });
    }

    /**
     * Translates each text in the list to the target language.
     * Returns a list of translations in the same order.
     */
    public List<String> translateBatch(List<String> texts, String targetLanguage) {
        return executeWithProtection("translateBatch(" + targetLanguage + ")", () -> {
            List<String> translations = new ArrayList<>();
            for (String text : texts) {
                String prompt = """
                        Translate the following text to %s.
                        Return ONLY the translated text, nothing else.

                        Text:
                        %s""".formatted(targetLanguage, text);
                translations.add(llmClient.send(prompt));
            }
            return translations;
        });
    }

    /**
     * Detects the language of the given text.
     * Returns the language name in English (e.g., "Portuguese", "English", "Spanish").
     */
    public String detectLanguage(String text) {
        return executeWithProtection("detectLanguage", () -> {
            String prompt = """
                    Detect the language of the following text.
                    Return ONLY the language name in English (e.g., "English", "Portuguese", "Spanish").
                    Nothing else.

                    Text:
                    %s""".formatted(text);
            return llmClient.send(prompt).trim().replaceAll("\"", "");
        });
    }
}
