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
public class TextSummarizerHelper extends BaseHelper {

    private final LLMClient llmClient;

    @Override
    protected String getServiceName() {
        return "text_summarizer";
    }

    /**
     * Summarizes the given text in 2-3 concise sentences.
     */
    public String summarize(String text) {
        return executeWithProtection("summarize", () -> {
            applyRateLimit();
            String prompt = "Summarize the following text in 2-3 concise sentences:\n\n" + text;
            return llmClient.send(prompt);
        });
    }

    private void applyRateLimit() {
        try {
            log.debug("Applying 1s delay for rate limiting...");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Rate limit delay interrupted", e);
        }
    }

    /**
     * Summarizes the given text in exactly the specified number of sentences.
     */
    public String summarize(String text, int maxSentences) {
        return executeWithProtection("summarize(" + maxSentences + ")", () -> {
            applyRateLimit();
            String prompt = "Summarize the following text in exactly %d sentences:\n\n%s"
                    .formatted(maxSentences, text);
            return llmClient.send(prompt);
        });
    }

    /**
     * Summarizes each text in the list individually.
     * Returns a list of summaries in the same order.
     */
    public List<String> summarizeBatch(List<String> texts) {
        return executeWithProtection("summarizeBatch", () -> {
            List<String> summaries = new ArrayList<>();
            for (String text : texts) {
                applyRateLimit();
                String prompt = "Summarize the following text in 2-3 concise sentences:\n\n" + text;
                summaries.add(llmClient.send(prompt));
            }
            return summaries;
        });
    }

    /**
     * Summarizes the given text and writes the summary in the target language.
     */
    public String summarizeToLanguage(String text, String targetLanguage) {
        return executeWithProtection("summarizeToLanguage(" + targetLanguage + ")", () -> {
            applyRateLimit();
            String prompt = "Summarize the following text in 2-3 sentences. Write the summary in %s:\n\n%s"
                    .formatted(targetLanguage, text);
            return llmClient.send(prompt);
        });
    }
}
