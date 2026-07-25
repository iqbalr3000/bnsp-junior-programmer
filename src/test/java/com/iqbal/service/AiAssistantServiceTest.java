package com.iqbal.service;

import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.iqbal.ai.AssistantToolExecutor;
import com.iqbal.ai.GeminiClient;
import com.iqbal.model.ChatRole;
import com.iqbal.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAssistantServiceTest {

    private static final long STAFF_ID = 1L;
    private static final int WINDOW_SIZE = 5;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private AssistantToolExecutor toolExecutor;

    private AiAssistantService aiAssistantService;

    @BeforeEach
    void setUp() {
        aiAssistantService = new AiAssistantService(chatMessageRepository, geminiClient, toolExecutor, WINDOW_SIZE);
        lenient().when(toolExecutor.declareTools()).thenReturn(List.of());
        lenient().when(chatMessageRepository.insert(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(chatMessageRepository.findRecentByStaffId(anyLong(), anyInt())).thenReturn(List.of());
    }

    @Test
    void kirimPesan_shouldPersistUserMessageBeforeCallingGemini() {
        when(geminiClient.send(any(), any(), any())).thenReturn(responseWithText("Halo"));

        aiAssistantService.kirimPesan(STAFF_ID, "Halo AI");

        InOrder order = inOrder(chatMessageRepository, geminiClient);
        order.verify(chatMessageRepository).insert(argThat(m ->
                m.getRole() == ChatRole.USER && "Halo AI".equals(m.getContent())));
        order.verify(geminiClient).send(any(), any(), any());
    }

    @Test
    void kirimPesan_shouldQuerySlidingWindowWithConfiguredSize() {
        when(geminiClient.send(any(), any(), any())).thenReturn(responseWithText("Halo"));

        aiAssistantService.kirimPesan(STAFF_ID, "Halo");

        verify(chatMessageRepository).findRecentByStaffId(STAFF_ID, WINDOW_SIZE);
    }

    @Test
    void kirimPesan_whenGeminiReturnsFunctionCall_shouldDispatchToolThenSendFollowUp() {
        Map<String, Object> args = Map.of("keyword", "paracetamol");
        when(geminiClient.send(any(), any(), any()))
                .thenReturn(responseWithFunctionCall("cek_stok_obat", args))
                .thenReturn(responseWithText("Stok paracetamol 20 tablet"));
        when(toolExecutor.dispatch("cek_stok_obat", args)).thenReturn("nama=Paracetamol, stok=20 tablet");

        aiAssistantService.kirimPesan(STAFF_ID, "cek stok paracetamol");

        verify(geminiClient, times(2)).send(any(), any(), any());
        verify(toolExecutor, times(1)).dispatch(eq("cek_stok_obat"), eq(args));
        verify(chatMessageRepository).insert(argThat(m ->
                m.getRole() == ChatRole.MODEL
                        && "Stok paracetamol 20 tablet".equals(m.getContent())
                        && m.getToolTrace() != null));
    }

    @Test
    void kirimPesan_shouldStopAfterMaxToolRounds() {
        when(geminiClient.send(any(), any(), any()))
                .thenReturn(responseWithFunctionCall("cek_stok_obat", Map.of("keyword", "x")));
        when(toolExecutor.dispatch(anyString(), anyMap())).thenReturn("hasil");

        assertDoesNotThrow(() -> aiAssistantService.kirimPesan(STAFF_ID, "tanya terus"));

        // 2 putaran tool call (MAX_TOOL_ROUNDS) + 1 percobaan ke-3 yang melebihi batas
        // + 1 permintaan terakhir tanpa tool = 4 panggilan, tidak infinite loop
        verify(geminiClient, times(4)).send(any(), any(), any());
        verify(chatMessageRepository).insert(argThat(m -> m.getRole() == ChatRole.MODEL));
    }

    @Test
    void riwayat_shouldDelegateToFindAllByStaffId() {
        when(chatMessageRepository.findAllByStaffId(STAFF_ID)).thenReturn(List.of());

        aiAssistantService.riwayat(STAFF_ID);

        verify(chatMessageRepository).findAllByStaffId(STAFF_ID);
    }

    private GenerateContentResponse responseWithFunctionCall(String name, Map<String, Object> args) {
        Content modelContent = Content.builder().role("model").parts(Part.fromFunctionCall(name, args)).build();
        Candidate candidate = Candidate.builder().content(modelContent).build();
        return GenerateContentResponse.builder().candidates(List.of(candidate)).build();
    }

    private GenerateContentResponse responseWithText(String text) {
        Content modelContent = Content.builder().role("model").parts(Part.fromText(text)).build();
        Candidate candidate = Candidate.builder().content(modelContent).build();
        return GenerateContentResponse.builder().candidates(List.of(candidate)).build();
    }
}
