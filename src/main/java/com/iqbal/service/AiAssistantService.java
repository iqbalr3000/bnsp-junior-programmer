package com.iqbal.service;

import com.google.genai.types.Content;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Tool;
import com.iqbal.ai.AssistantToolExecutor;
import com.iqbal.ai.GeminiClient;
import com.iqbal.model.ChatMessage;
import com.iqbal.model.ChatRole;
import com.iqbal.repository.ChatMessageRepository;
import com.iqbal.service.exception.AiAssistantException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AiAssistantService {

    private static final int MAX_TOOL_ROUNDS = 2;
    private static final String SYSTEM_INSTRUCTION_TEXT = """
            Kamu adalah asisten AI untuk staff Sistem Informasi Manajemen Rumah Sakit.
            Tugasmu HANYA menjawab pertanyaan staff berdasarkan data yang tersedia lewat tool yang diberikan
            (data pasien, dokter, obat, reservasi, rekam medis). Kamu TIDAK BOLEH membuat/mengubah data apa pun,
            dan TIDAK BOLEH memberikan diagnosis, saran pengobatan, atau nasihat medis apa pun kepada pasien -
            itu bukan tugasmu. Kalau pertanyaan di luar cakupan data aplikasi, katakan dengan jujur kamu tidak tahu.
            Jawab singkat, jelas, dan dalam Bahasa Indonesia.
            """;

    private final ChatMessageRepository chatMessageRepository;
    private final GeminiClient geminiClient;
    private final AssistantToolExecutor toolExecutor;
    private final int contextWindowSize;

    public AiAssistantService(ChatMessageRepository chatMessageRepository,
                               GeminiClient geminiClient,
                               AssistantToolExecutor toolExecutor,
                               int contextWindowSize) {
        this.chatMessageRepository = chatMessageRepository;
        this.geminiClient = geminiClient;
        this.toolExecutor = toolExecutor;
        this.contextWindowSize = contextWindowSize;
    }

    public List<ChatMessage> riwayat(long staffId) {
        return chatMessageRepository.findAllByStaffId(staffId);
    }

    public ChatMessage kirimPesan(long staffId, String userText) {
        ChatMessage userMessage = new ChatMessage();
        userMessage.setStaffId(staffId);
        userMessage.setRole(ChatRole.USER);
        userMessage.setContent(userText);
        chatMessageRepository.insert(userMessage);

        List<Content> contents = assembleSlidingWindow(staffId);
        Content systemInstruction = Content.fromParts(Part.fromText(SYSTEM_INSTRUCTION_TEXT));
        List<Tool> tools = toolExecutor.declareTools();

        String finalText;
        StringBuilder toolTrace = new StringBuilder();
        int round = 0;

        try {
            while (true) {
                GenerateContentResponse response = geminiClient.send(contents, systemInstruction, tools);
                List<FunctionCall> functionCalls = response.functionCalls();

                if (functionCalls == null || functionCalls.isEmpty()) {
                    finalText = response.text();
                    break;
                }

                round++;
                if (round > MAX_TOOL_ROUNDS) {
                    // paksa jawaban teks tanpa tool supaya loop tidak berputar tak terbatas
                    response = geminiClient.send(contents, systemInstruction, List.of());
                    finalText = response.text();
                    break;
                }

                FunctionCall functionCall = functionCalls.get(0);
                String functionName = functionCall.name().orElse("");
                Map<String, Object> functionArgs = functionCall.args().orElse(Map.of());
                String result = toolExecutor.dispatch(functionName, functionArgs);
                toolTrace.append(functionName).append("(").append(functionArgs).append(") -> ").append(result).append('\n');

                Content modelTurn = response.candidates().orElseThrow().get(0).content().orElseThrow();
                Content functionResponseTurn = Content.builder()
                        .role("user")
                        .parts(Part.fromFunctionResponse(functionName, Map.of("result", result)))
                        .build();
                contents.add(modelTurn);
                contents.add(functionResponseTurn);
            }
        } catch (AiAssistantException e) {
            throw e;
        } catch (Exception e) {
            throw new AiAssistantException("Gagal mendapatkan jawaban dari AI assistant", e);
        }

        ChatMessage modelMessage = new ChatMessage();
        modelMessage.setStaffId(staffId);
        modelMessage.setRole(ChatRole.MODEL);
        modelMessage.setContent(finalText);
        modelMessage.setToolTrace(toolTrace.isEmpty() ? null : toolTrace.toString());
        return chatMessageRepository.insert(modelMessage);
    }

    private List<Content> assembleSlidingWindow(long staffId) {
        List<ChatMessage> recent = new ArrayList<>(chatMessageRepository.findRecentByStaffId(staffId, contextWindowSize));
        Collections.reverse(recent);
        List<Content> contents = new ArrayList<>();
        for (ChatMessage message : recent) {
            String role = message.getRole() == ChatRole.USER ? "user" : "model";
            contents.add(Content.builder().role(role).parts(Part.fromText(message.getContent())).build());
        }
        return contents;
    }
}
