package com.iqbal.repository.impl;

import com.iqbal.model.Pasien;
import com.iqbal.repository.PasienRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PasienRepositoryImpl implements PasienRepository {

    private final DataSource dataSource;

    public PasienRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Pasien> findAll() {
        String sql = "SELECT * FROM pasien ORDER BY nama";
        List<Pasien> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data pasien", e);
        }
    }

    @Override
    public Optional<Pasien> findById(long id) {
        String sql = "SELECT * FROM pasien WHERE id = ?";
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
            throw new RuntimeException("Gagal mengambil data pasien", e);
        }
    }

    @Override
    public Optional<Pasien> findByNik(String nik) {
        String sql = "SELECT * FROM pasien WHERE nik = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nik);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data pasien", e);
        }
    }

    @Override
    public List<Pasien> search(String keyword) {
        String sql = "SELECT * FROM pasien WHERE nama ILIKE ? OR nik ILIKE ? OR no_rekam_medis ILIKE ? ORDER BY nama";
        String pattern = "%" + keyword + "%";
        List<Pasien> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mencari data pasien", e);
        }
    }

    @Override
    public int countByYear(int year) {
        String sql = "SELECT COUNT(*) FROM pasien WHERE no_rekam_medis LIKE ?";
        String pattern = "RM-" + year + "-%";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghitung data pasien", e);
        }
    }

    @Override
    public Pasien insert(Pasien p) {
        String sql = "INSERT INTO pasien (no_rekam_medis, nama, nik, tanggal_lahir, jenis_kelamin, alamat, no_telepon) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING *";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNoRekamMedis());
            ps.setString(2, p.getNama());
            ps.setString(3, p.getNik());
            ps.setObject(4, p.getTanggalLahir());
            ps.setString(5, p.getJenisKelamin());
            ps.setString(6, p.getAlamat());
            ps.setString(7, p.getNoTelepon());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan data pasien", e);
        }
    }

    @Override
    public void update(Pasien p) {
        String sql = "UPDATE pasien SET nama = ?, nik = ?, tanggal_lahir = ?, jenis_kelamin = ?, "
                + "alamat = ?, no_telepon = ?, updated_at = now() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNama());
            ps.setString(2, p.getNik());
            ps.setObject(3, p.getTanggalLahir());
            ps.setString(4, p.getJenisKelamin());
            ps.setString(5, p.getAlamat());
            ps.setString(6, p.getNoTelepon());
            ps.setLong(7, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memperbarui data pasien", e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM pasien WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus data pasien", e);
        }
    }

    private Pasien mapRow(ResultSet rs) throws SQLException {
        Pasien p = new Pasien();
        p.setId(rs.getLong("id"));
        p.setNoRekamMedis(rs.getString("no_rekam_medis"));
        p.setNama(rs.getString("nama"));
        p.setNik(rs.getString("nik"));
        p.setTanggalLahir(rs.getObject("tanggal_lahir", java.time.LocalDate.class));
        p.setJenisKelamin(rs.getString("jenis_kelamin"));
        p.setAlamat(rs.getString("alamat"));
        p.setNoTelepon(rs.getString("no_telepon"));
        p.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        p.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        return p;
    }
}
