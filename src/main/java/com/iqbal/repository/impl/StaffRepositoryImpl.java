package com.iqbal.repository.impl;

import com.iqbal.model.Role;
import com.iqbal.model.Staff;
import com.iqbal.repository.StaffRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class StaffRepositoryImpl implements StaffRepository {

    private final DataSource dataSource;

    public StaffRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Staff> findByUsername(String username) {
        String sql = "SELECT * FROM staff WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data staff", e);
        }
    }

    private Staff mapRow(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setId(rs.getLong("id"));
        s.setUsername(rs.getString("username"));
        s.setPasswordHash(rs.getString("password_hash"));
        s.setNama(rs.getString("nama"));
        s.setRole(Role.valueOf(rs.getString("role")));
        s.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        s.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        return s;
    }
}
