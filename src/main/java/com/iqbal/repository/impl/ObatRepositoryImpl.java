package com.iqbal.repository.impl;

import com.iqbal.model.Obat;
import com.iqbal.repository.ObatRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ObatRepositoryImpl implements ObatRepository {

    private final DataSource dataSource;

    public ObatRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Obat> findAll() {
        String sql = "SELECT * FROM obat ORDER BY nama";
        List<Obat> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data obat", e);
        }
    }

    @Override
    public Optional<Obat> findById(long id) {
        String sql = "SELECT * FROM obat WHERE id = ?";
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
            throw new RuntimeException("Gagal mengambil data obat", e);
        }
    }

    @Override
    public List<Obat> search(String keyword) {
        String sql = "SELECT * FROM obat WHERE nama ILIKE ? OR kode_obat ILIKE ? ORDER BY nama";
        String pattern = "%" + keyword + "%";
        List<Obat> result = new ArrayList<>();
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
            throw new RuntimeException("Gagal mencari data obat", e);
        }
    }

    @Override
    public Obat insert(Obat o) {
        String sql = "INSERT INTO obat (kode_obat, nama, satuan, harga, stok, deskripsi) VALUES (?, ?, ?, ?, ?, ?) RETURNING *";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, o.getKodeObat());
            ps.setString(2, o.getNama());
            ps.setString(3, o.getSatuan());
            ps.setBigDecimal(4, o.getHarga());
            ps.setInt(5, o.getStok());
            ps.setString(6, o.getDeskripsi());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan data obat", e);
        }
    }

    @Override
    public void update(Obat o) {
        String sql = "UPDATE obat SET kode_obat = ?, nama = ?, satuan = ?, harga = ?, stok = ?, deskripsi = ?, updated_at = now() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, o.getKodeObat());
            ps.setString(2, o.getNama());
            ps.setString(3, o.getSatuan());
            ps.setBigDecimal(4, o.getHarga());
            ps.setInt(5, o.getStok());
            ps.setString(6, o.getDeskripsi());
            ps.setLong(7, o.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memperbarui data obat", e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM obat WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus data obat", e);
        }
    }

    @Override
    public Optional<Obat> findByIdForUpdate(Connection conn, long id) {
        String sql = "SELECT * FROM obat WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data obat untuk update", e);
        }
    }

    @Override
    public void kurangiStok(Connection conn, long obatId, int jumlah) {
        String sql = "UPDATE obat SET stok = stok - ?, updated_at = now() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jumlah);
            ps.setLong(2, obatId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengurangi stok obat", e);
        }
    }

    private Obat mapRow(ResultSet rs) throws SQLException {
        Obat o = new Obat();
        o.setId(rs.getLong("id"));
        o.setKodeObat(rs.getString("kode_obat"));
        o.setNama(rs.getString("nama"));
        o.setSatuan(rs.getString("satuan"));
        o.setHarga(rs.getBigDecimal("harga"));
        o.setStok(rs.getInt("stok"));
        o.setDeskripsi(rs.getString("deskripsi"));
        o.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        o.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        return o;
    }
}
