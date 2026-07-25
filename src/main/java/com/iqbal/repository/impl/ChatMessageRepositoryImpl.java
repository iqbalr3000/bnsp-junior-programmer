package com.iqbal.repository.impl;

import com.iqbal.model.ChatMessage;
import com.iqbal.model.ChatRole;
import com.iqbal.repository.ChatMessageRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    private final DataSource dataSource;

    public ChatMessageRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public ChatMessage insert(ChatMessage message) {
        String sql = "INSERT INTO chat_message (staff_id, role, content, tool_trace) VALUES (?, ?, ?, ?) RETURNING *";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, message.getStaffId());
            ps.setString(2, message.getRole().name());
            ps.setString(3, message.getContent());
            ps.setString(4, message.getToolTrace());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan pesan chat", e);
        }
    }

    @Override
    public List<ChatMessage> findAllByStaffId(long staffId) {
        String sql = "SELECT * FROM chat_message WHERE staff_id = ? ORDER BY created_at ASC";
        List<ChatMessage> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil riwayat chat", e);
        }
    }

    @Override
    public List<ChatMessage> findRecentByStaffId(long staffId, int limit) {
        String sql = "SELECT * FROM chat_message WHERE staff_id = ? ORDER BY created_at DESC LIMIT ?";
        List<ChatMessage> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, staffId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil riwayat chat terbaru", e);
        }
    }

    private ChatMessage mapRow(ResultSet rs) throws SQLException {
        ChatMessage message = new ChatMessage();
        message.setId(rs.getLong("id"));
        message.setStaffId(rs.getLong("staff_id"));
        message.setRole(ChatRole.valueOf(rs.getString("role")));
        message.setContent(rs.getString("content"));
        message.setToolTrace(rs.getString("tool_trace"));
        message.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        return message;
    }
}
