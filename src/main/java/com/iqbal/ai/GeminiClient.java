package com.iqbal.ai;

import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Tool;

import java.util.List;

/**
 * Seam tipis ke Gemini API supaya AiAssistantService bisa di-unit-test tanpa panggilan jaringan nyata.
 */
public interface GeminiClient {

    GenerateContentResponse send(List<Content> contents, Content systemInstruction, List<Tool> tools);
}
