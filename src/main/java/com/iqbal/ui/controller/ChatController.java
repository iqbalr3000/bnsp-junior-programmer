package com.iqbal.ui.controller;

import com.iqbal.model.ChatMessage;
import com.iqbal.model.ChatRole;
import com.iqbal.model.Staff;
import com.iqbal.service.AiAssistantService;
import com.iqbal.ui.AppContext;
import com.iqbal.ui.SessionContext;
import com.iqbal.ui.util.ContextAware;
import com.iqbal.ui.util.DialogUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChatController implements ContextAware {

    @FXML private Label statusLabel;
    @FXML private ScrollPane historyScrollPane;
    @FXML private VBox messageContainer;
    @FXML private TextField inputField;
    @FXML private Button kirimButton;

    private AppContext context;
    private Long staffId;

    @Override
    public void setContext(AppContext context) {
        this.context = context;
        Staff staff = SessionContext.getCurrentStaff();
        this.staffId = staff != null ? staff.getId() : null;

        AiAssistantService aiAssistantService = context.getAiAssistantService();
        if (aiAssistantService == null) {
            statusLabel.setText("AI Assistant belum dikonfigurasi (GEMINI_API_KEY belum diset).");
            inputField.setDisable(true);
            kirimButton.setDisable(true);
            return;
        }

        statusLabel.setText("Tanya seputar data pasien, dokter, obat, reservasi, atau rekam medis.");
        List<ChatMessage> riwayat = aiAssistantService.riwayat(staffId);
        for (ChatMessage message : riwayat) {
            appendBubble(message.getRole(), message.getContent());
        }
        scrollToBottom();
    }

    @FXML
    private void handleKirim() {
        String text = inputField.getText();
        if (text == null || text.isBlank()) {
            return;
        }
        AiAssistantService aiAssistantService = context.getAiAssistantService();
        if (aiAssistantService == null || staffId == null) {
            return;
        }

        appendBubble(ChatRole.USER, text);
        inputField.clear();
        inputField.setDisable(true);
        kirimButton.setDisable(true);
        scrollToBottom();

        Task<ChatMessage> task = new Task<>() {
            @Override
            protected ChatMessage call() {
                return aiAssistantService.kirimPesan(staffId, text);
            }
        };
        task.setOnSucceeded(event -> {
            appendBubble(ChatRole.MODEL, task.getValue().getContent());
            inputField.setDisable(false);
            kirimButton.setDisable(false);
            inputField.requestFocus();
            scrollToBottom();
        });
        task.setOnFailed(event -> {
            DialogUtil.showError("Gagal mendapatkan jawaban dari AI assistant: " + task.getException().getMessage());
            inputField.setDisable(false);
            kirimButton.setDisable(false);
        });
        context.getAiExecutor().submit(task);
    }

    private void appendBubble(ChatRole role, String text) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(420);
        bubble.getStyleClass().add(role == ChatRole.USER ? "chat-bubble-user" : "chat-bubble-model");

        HBox row = new HBox(bubble);
        row.setAlignment(role == ChatRole.USER ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageContainer.getChildren().add(row);
    }

    private void scrollToBottom() {
        Platform.runLater(() -> historyScrollPane.setVvalue(1.0));
    }
}
