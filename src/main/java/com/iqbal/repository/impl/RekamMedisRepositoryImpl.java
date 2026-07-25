package com.iqbal.repository.impl;

import com.iqbal.model.RekamMedis;
import com.iqbal.model.RekamMedisObat;
import com.iqbal.repository.RekamMedisRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RekamMedisRepositoryImpl implements RekamMedisRepository {

    private static final String SELECT_WITH_JOIN =
            "SELECT rm.*, p.nama AS pasien_nama, d.nama AS dokter_nama "
                    + "FROM rekam_medis rm "
                    + "JOIN pasien p ON p.id = rm.pasien_id "
                    + "JOIN dokter d ON d.id = rm.dokter_id ";

    private static final String SELECT_RESEP_OBAT =
            "SELECT rmo.*, o.nama AS obat_nama "
                    + "FROM rekam_medis_obat rmo "
                    + "JOIN obat o ON o.id = rmo.obat_id "
                    + "WHERE rmo.rekam_medis_id = ?";

    private final DataSource dataSource;

    public RekamMedisRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<RekamMedis> findAll() {
        String sql = SELECT_WITH_JOIN + "ORDER BY rm.tanggal_periksa DESC";
        List<RekamMedis> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data rekam medis", e);
        }
    }

    @Override
    public Optional<RekamMedis> findById(long id) {
        String sql = SELECT_WITH_JOIN + "WHERE rm.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                RekamMedis rm = mapRow(rs);
                rm.setResepObat(findResepObat(conn, id));
                return Optional.of(rm);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data rekam medis", e);
        }
    }

    @Override
    public long insert(Connection conn, RekamMedis rm) {
        String sql = "INSERT INTO rekam_medis (reservasi_id, pasien_id, dokter_id, tanggal_periksa, diagnosis, catatan) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (rm.getReservasiId() != null) {
                ps.setLong(1, rm.getReservasiId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setLong(2, rm.getPasienId());
            ps.setLong(3, rm.getDokterId());
            ps.setObject(4, rm.getTanggalPeriksa() != null ? rm.getTanggalPeriksa() : java.time.LocalDate.now());
            ps.setString(5, rm.getDiagnosis());
            ps.setString(6, rm.getCatatan());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan data rekam medis", e);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM rekam_medis WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus data rekam medis", e);
        }
    }

    private List<RekamMedisObat> findResepObat(Connection conn, long rekamMedisId) throws SQLException {
        List<RekamMedisObat> result = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_RESEP_OBAT)) {
            ps.setLong(1, rekamMedisId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RekamMedisObat item = new RekamMedisObat();
                    item.setId(rs.getLong("id"));
                    item.setRekamMedisId(rs.getLong("rekam_medis_id"));
                    item.setObatId(rs.getLong("obat_id"));
                    item.setJumlah(rs.getInt("jumlah"));
                    item.setDosis(rs.getString("dosis"));
                    item.setObatNama(rs.getString("obat_nama"));
                    result.add(item);
                }
            }
        }
        return result;
    }

    private RekamMedis mapRow(ResultSet rs) throws SQLException {
        RekamMedis rm = new RekamMedis();
        rm.setId(rs.getLong("id"));
        long reservasiId = rs.getLong("reservasi_id");
        rm.setReservasiId(rs.wasNull() ? null : reservasiId);
        rm.setPasienId(rs.getLong("pasien_id"));
        rm.setDokterId(rs.getLong("dokter_id"));
        rm.setTanggalPeriksa(rs.getObject("tanggal_periksa", java.time.LocalDate.class));
        rm.setDiagnosis(rs.getString("diagnosis"));
        rm.setCatatan(rs.getString("catatan"));
        rm.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        rm.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));
        rm.setPasienNama(rs.getString("pasien_nama"));
        rm.setDokterNama(rs.getString("dokter_nama"));
        return rm;
    }
}
