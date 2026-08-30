package com.sameer.job.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.sameer.job.config.GeminiProperties;
import com.sameer.job.exception.ErrorCodes;
import com.sameer.job.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final Client genAiClient;
    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    public String generateText(String systemInstruction, String prompt) throws Exception {
        return generateText(systemInstruction, prompt, geminiProperties.getTemperature(), geminiProperties.getMaxOutputTokens());
    }

    public <T> T generateJson(String systemInstruction, String prompt, Class<T> responseType) throws Exception {
        return callJson(systemInstruction, prompt, responseType);
    }

    public String generateText(String systemInstruction, String prompt, double temperature, int maxTokens) throws Exception {
        return callText(systemInstruction, prompt, (float) geminiProperties.getTemperature(), geminiProperties.getMaxOutputTokens());
    }


    // system_prompt = system_instruction
    private <T> T callJson(String systemInstruction,
                           String prompt,
                           Class<T> responseType
    ) throws Exception {
        try {
            GenerateContentConfig config = buildConfig(systemInstruction, 0.3f, geminiProperties.getMaxOutputTokens(), true);

            GenerateContentResponse response = genAiClient.models.generateContent(geminiProperties.getModel(), prompt, config);
            return objectMapper.readValue(response.text(), responseType);
        } catch (Exception e) {
            log.warn("Gemini JSON generation failed", e);
            throw new ServiceUnavailableException(ErrorCodes.AI_UNAVAILABLE, "AI service is unavailable");
        }
    }

    // system_prompt = system_instruction
    private String callText(String systemInstruction,
                            String prompt,
                            float temperature,
                            int maxTokens
    ) throws Exception {
        try {
            GenerateContentConfig config = buildConfig(systemInstruction, temperature, maxTokens, false);

            GenerateContentResponse response = genAiClient.models.generateContent(geminiProperties.getModel(), prompt, config);
            return response.text();
        } catch (Exception e) {
            log.warn("Gemini text generation failed", e);
            throw new ServiceUnavailableException(ErrorCodes.AI_UNAVAILABLE, "AI service is unavailable");
        }
    }

    private GenerateContentConfig buildConfig(String systemInstruction, float temperature, int maxTokens, boolean jsonMode) {

        GenerateContentConfig.Builder builder = GenerateContentConfig.builder()
                                                                     .temperature(temperature)
                                                                     .maxOutputTokens(maxTokens);

        if (systemInstruction != null && !systemInstruction.isBlank()) {
            builder.systemInstruction(Content.fromParts(Part.fromText(systemInstruction)));
        }

        if (jsonMode) {
            builder.responseMimeType("application/json");
        }

        return builder.build();
    }
}
