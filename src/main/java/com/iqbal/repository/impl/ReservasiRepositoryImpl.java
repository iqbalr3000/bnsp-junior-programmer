package com.iqbal.repository.impl;

import com.iqbal.model.Reservasi;
import com.iqbal.model.ReservasiStatus;
import com.iqbal.repository.ReservasiRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservasiRepositoryImpl implements ReservasiRepository {

    private static final String SELECT_WITH_JOIN =
            "SELECT r.*, p.nama AS pasien_nama, d.nama AS dokter_nama "
                    + "FROM reservasi r "
                    + "JOIN pasien p ON p.id = r.pasien_id "
                    + "JOIN dokter d ON d.id = r.dokter_id ";

    private final DataSource dataSource;

    public ReservasiRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Reservasi> findAll() {
        String sql = SELECT_WITH_JOIN + "ORDER BY r.tanggal_reservasi, r.jam_reservasi";
        List<Reservasi> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data reservasi", e);
        }
    }

    @Override
    public Optional<Reservasi> findById(long id) {
        String sql = SELECT_WITH_JOIN + "WHERE r.id = ?";
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
            throw new RuntimeException("Gagal mengambil data reservasi", e);
        }
    }

    @Override
    public List<Reservasi> findByPasienId(long pasienId) {
        String sql = SELECT_WITH_JOIN + "WHERE r.pasien_id = ? ORDER BY r.tanggal_reservasi, r.jam_reservasi";
        List<Reservasi> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, pasienId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data reservasi", e);
        }
    }

    @Override
    public Reservasi insert(Reservasi r) throws SQLException {
        // SQLException (termasuk pelanggaran unique index uq_reservasi_slot_dokter, SQLState 23505)
        // sengaja dibiarkan menjalar apa adanya; translasi pesan dilakukan di service layer.
        String sql = "INSERT INTO reservasi (pasien_id, dokter_id, tanggal_reservasi, jam_reservasi, keluhan, status) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING *";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, r.getPasienId());
            ps.setLong(2, r.getDokterId());
            ps.setObject(3, r.getTanggalReservasi());
            ps.setObject(4, r.getJamReservasi());
            ps.setString(5, r.getKeluhan());
            ps.setString(6, (r.getStatus() != null ? r.getStatus() : ReservasiStatus.MENUNGGU).name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return mapRowNoJoin(rs);
            }
        }
    }

    @Override
    public void updateStatus(long id, ReservasiStatus status) {
        try (Connection conn = dataSource.getConnection()) {
            updateStatus(conn, id, status);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memperbarui status reservasi", e);
        }
    }

    @Override
    public void updateStatus(Connection conn, long id, ReservasiStatus status) {
        String sql = "UPDATE reservasi SET status = ?, updated_at = now() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memperbarui status reservasi", e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM reservasi WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus data reservasi", e);
        }
    }

    private Reservasi mapRow(ResultSet rs) throws SQLException {
        Reservasi r = mapRowNoJoin(rs);
        r.setPasienNama(rs.getString("pasien_nama"));
        r.setDokterNama(rs.getString("dokter_nama"));
        return r;
    }

    private Reservasi mapRowNoJoin(ResultSet rs) throws SQLException {
        Reservasi r = new Reservasi();
        r.setId(rs.getLong("id"));
        r.setPasienId(rs.getLong("pasien_id"));
        r.setDokterId(rs.getLong("dokter_id"));
        r.setTanggalReservasi(rs.getObject("tanggal_reservasi", java.time.LocalDate.class));
        r.setJamReservasi(rs.getObject("jam_reservasi", java.time.LocalTime.class));
        r.setKeluhan(rs.getString("keluhan"));
        r.setStatus(ReservasiStatus.valueOf(rs.getString("status")));
        r.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        r.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        return r;
    }
}
