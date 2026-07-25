package com.iqbal.repository;

import com.iqbal.model.ChatMessage;

import java.util.List;

public interface ChatMessageRepository {

    ChatMessage insert(ChatMessage message);

    List<ChatMessage> findAllByStaffId(long staffId);

    List<ChatMessage> findRecentByStaffId(long staffId, int limit);
}
