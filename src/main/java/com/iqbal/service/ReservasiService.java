package com.iqbal.service;

import com.iqbal.model.Reservasi;
import com.iqbal.model.ReservasiStatus;
import com.iqbal.repository.DokterRepository;
import com.iqbal.repository.PasienRepository;
import com.iqbal.repository.ReservasiRepository;
import com.iqbal.service.exception.ServiceException;
import com.iqbal.service.exception.ValidationException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ReservasiService {

    private static final String SQLSTATE_UNIQUE_VIOLATION = "23505";

    private final ReservasiRepository reservasiRepository;
    private final PasienRepository pasienRepository;
    private final DokterRepository dokterRepository;

    public ReservasiService(ReservasiRepository reservasiRepository,
                             PasienRepository pasienRepository,
                             DokterRepository dokterRepository) {
        this.reservasiRepository = reservasiRepository;
        this.pasienRepository = pasienRepository;
        this.dokterRepository = dokterRepository;
    }

    public List<Reservasi> findAll() {
        return reservasiRepository.findAll();
    }

    public Optional<Reservasi> findById(long id) {
        return reservasiRepository.findById(id);
    }

    public List<Reservasi> findByPasienId(long pasienId) {
        return reservasiRepository.findByPasienId(pasienId);
    }

    public Reservasi simpan(Reservasi reservasi) {
        validate(reservasi);
        try {
            return reservasiRepository.insert(reservasi);
        } catch (SQLException e) {
            if (SQLSTATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new ValidationException("Dokter sudah memiliki reservasi pada tanggal dan jam tersebut");
            }
            throw new ServiceException("Gagal menyimpan data reservasi", e);
        }
    }

    public void batalkan(long id) {
        reservasiRepository.updateStatus(id, ReservasiStatus.BATAL);
    }

    private void validate(Reservasi reservasi) {
        if (reservasi.getPasienId() == null) {
            throw new ValidationException("Pasien wajib dipilih");
        }
        if (reservasi.getDokterId() == null) {
            throw new ValidationException("Dokter wajib dipilih");
        }
        if (reservasi.getTanggalReservasi() == null) {
            throw new ValidationException("Tanggal reservasi wajib diisi");
        }
        if (reservasi.getJamReservasi() == null) {
            throw new ValidationException("Jam reservasi wajib diisi");
        }
        if (pasienRepository.findById(reservasi.getPasienId()).isEmpty()) {
            throw new ValidationException("Pasien tidak ditemukan");
        }
        if (dokterRepository.findById(reservasi.getDokterId()).isEmpty()) {
            throw new ValidationException("Dokter tidak ditemukan");
        }
    }
}
