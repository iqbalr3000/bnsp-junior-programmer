package com.iqbal.ai;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Tool;

import java.util.List;

public class GeminiClientImpl implements GeminiClient, AutoCloseable {

    private final Client client;
    private final String model;

    public GeminiClientImpl(String apiKey, String model) {
        this.client = Client.builder().apiKey(apiKey).build();
        this.model = model;
    }

    @Override
    public GenerateContentResponse send(List<Content> contents, Content systemInstruction, List<Tool> tools) {
        GenerateContentConfig.Builder configBuilder = GenerateContentConfig.builder()
                .systemInstruction(systemInstruction);
        if (tools != null && !tools.isEmpty()) {
            configBuilder.tools(tools);
        }
        return client.models.generateContent(model, contents, configBuilder.build());
    }

    @Override
    public void close() {
        client.close();
    }
}
