package com.iqbal.repository.impl;

import com.iqbal.model.RekamMedisObat;
import com.iqbal.repository.RekamMedisObatRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RekamMedisObatRepositoryImpl implements RekamMedisObatRepository {

    private final DataSource dataSource;

    public RekamMedisObatRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void insert(Connection conn, long rekamMedisId, RekamMedisObat item) {
        String sql = "INSERT INTO rekam_medis_obat (rekam_medis_id, obat_id, jumlah, dosis) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, rekamMedisId);
            ps.setLong(2, item.getObatId());
            ps.setInt(3, item.getJumlah());
            ps.setString(4, item.getDosis());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan resep obat", e);
        }
    }

    @Override
    public List<RekamMedisObat> findByRekamMedisId(long rekamMedisId) {
        String sql = "SELECT rmo.*, o.nama AS obat_nama FROM rekam_medis_obat rmo "
                + "JOIN obat o ON o.id = rmo.obat_id WHERE rmo.rekam_medis_id = ?";
        List<RekamMedisObat> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, rekamMedisId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil resep obat", e);
        }
    }

    private RekamMedisObat mapRow(ResultSet rs) throws SQLException {
        RekamMedisObat item = new RekamMedisObat();
        item.setId(rs.getLong("id"));
        item.setRekamMedisId(rs.getLong("rekam_medis_id"));
        item.setObatId(rs.getLong("obat_id"));
        item.setJumlah(rs.getInt("jumlah"));
        item.setDosis(rs.getString("dosis"));
        item.setObatNama(rs.getString("obat_nama"));
        return item;
    }
}
