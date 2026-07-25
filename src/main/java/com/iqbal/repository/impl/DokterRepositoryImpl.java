package com.iqbal.repository.impl;

import com.iqbal.model.Dokter;
import com.iqbal.repository.DokterRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DokterRepositoryImpl implements DokterRepository {

    private final DataSource dataSource;

    public DokterRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Dokter> findAll() {
        String sql = "SELECT * FROM dokter ORDER BY nama";
        List<Dokter> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data dokter", e);
        }
    }

    @Override
    public Optional<Dokter> findById(long id) {
        String sql = "SELECT * FROM dokter WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data dokter", e);
        }
    }

    @Override
    public List<Dokter> search(String keyword) {
        String sql = "SELECT * FROM dokter WHERE nama ILIKE ? OR spesialisasi ILIKE ? ORDER BY nama";
        String pattern = "%" + keyword + "%";
        List<Dokter> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencari data dokter", e);
        }
    }

    @Override
    public Dokter insert(Dokter d) {
        String sql = "INSERT INTO dokter (nama, spesialisasi, no_str, no_telepon) VALUES (?, ?, ?, ?) RETURNING *";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getNama());
            ps.setString(2, d.getSpesialisasi());
            ps.setString(3, d.getNoStr());
            ps.setString(4, d.getNoTelepon());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan data dokter", e);
        }
    }

    @Override
    public void update(Dokter d) {
        String sql = "UPDATE dokter SET nama = ?, spesialisasi = ?, no_str = ?, no_telepon = ?, updated_at = now() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getNama());
            ps.setString(2, d.getSpesialisasi());
            ps.setString(3, d.getNoStr());
            ps.setString(4, d.getNoTelepon());
            ps.setLong(5, d.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memperbarui data dokter", e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM dokter WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus data dokter", e);
        }
    }

    private Dokter mapRow(ResultSet rs) throws SQLException {
        Dokter d = new Dokter();
        d.setId(rs.getLong("id"));
        d.setNama(rs.getString("nama"));
        d.setSpesialisasi(rs.getString("spesialisasi"));
        d.setNoStr(rs.getString("no_str"));
        d.setNoTelepon(rs.getString("no_telepon"));
        d.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        d.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        return d;
    }
}
