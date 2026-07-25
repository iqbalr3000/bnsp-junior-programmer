package com.iqbal.service;

import com.iqbal.model.Obat;
import com.iqbal.model.RekamMedis;
import com.iqbal.model.RekamMedisObat;
import com.iqbal.model.ReservasiStatus;
import com.iqbal.repository.ObatRepository;
import com.iqbal.repository.RekamMedisObatRepository;
import com.iqbal.repository.RekamMedisRepository;
import com.iqbal.repository.ReservasiRepository;
import com.iqbal.service.exception.InsufficientStockException;
import com.iqbal.service.exception.ServiceException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RekamMedisService {

    private final DataSource dataSource;
    private final RekamMedisRepository rekamMedisRepository;
    private final RekamMedisObatRepository rekamMedisObatRepository;
    private final ObatRepository obatRepository;
    private final ReservasiRepository reservasiRepository;

    public RekamMedisService(DataSource dataSource,
                              RekamMedisRepository rekamMedisRepository,
                              RekamMedisObatRepository rekamMedisObatRepository,
                              ObatRepository obatRepository,
                              ReservasiRepository reservasiRepository) {
        this.dataSource = dataSource;
        this.rekamMedisRepository = rekamMedisRepository;
        this.rekamMedisObatRepository = rekamMedisObatRepository;
        this.obatRepository = obatRepository;
        this.reservasiRepository = reservasiRepository;
    }

    public List<RekamMedis> findAll() {
        return rekamMedisRepository.findAll();
    }

    public Optional<RekamMedis> findById(long id) {
        return rekamMedisRepository.findById(id);
    }

    /**
     * Simpan rekam medis beserta resep obat dalam satu transaksi manual:
     * kurangi stok obat (row-locked via SELECT ... FOR UPDATE) untuk setiap resep,
     * dan jika rekam medis berasal dari sebuah reservasi, tandai reservasi tsb SELESAI.
     * Semua atau tidak sama sekali (rollback saat gagal di tengah jalan).
     */
    public RekamMedis simpanDenganResep(RekamMedis rm) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            long rekamMedisId = rekamMedisRepository.insert(conn, rm);
            rm.setId(rekamMedisId);

            for (RekamMedisObat resep : rm.getResepObat()) {
                Obat obat = obatRepository.findByIdForUpdate(conn, resep.getObatId())
                        .orElseThrow(() -> new ServiceException("Obat tidak ditemukan"));
                validateStok(obat, resep.getJumlah());
                obatRepository.kurangiStok(conn, obat.getId(), resep.getJumlah());
                rekamMedisObatRepository.insert(conn, rekamMedisId, resep);
            }

            if (rm.getReservasiId() != null) {
                reservasiRepository.updateStatus(conn, rm.getReservasiId(), ReservasiStatus.SELESAI);
            }

            conn.commit();
            return rm;
        } catch (InsufficientStockException e) {
            rollback(conn);
            throw e;
        } catch (Exception e) {
            rollback(conn);
            throw new ServiceException("Gagal menyimpan rekam medis", e);
        } finally {
            closeQuietly(conn);
        }
    }

    private void validateStok(Obat obat, int jumlahDiminta) {
        if (obat.getStok() < jumlahDiminta) {
            throw new InsufficientStockException(
                    "Stok obat '" + obat.getNama() + "' tidak mencukupi");
        }
    }

    private void rollback(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // koneksi mungkin sudah tidak valid, tidak ada tindakan lanjutan yang bisa diambil
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException ignored) {
            // koneksi akan dibuang oleh pool jika gagal ditutup dengan bersih
        }
    }
}
