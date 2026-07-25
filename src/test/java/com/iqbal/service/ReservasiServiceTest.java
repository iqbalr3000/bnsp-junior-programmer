package com.iqbal.service;

import com.iqbal.model.Dokter;
import com.iqbal.model.Pasien;
import com.iqbal.model.Reservasi;
import com.iqbal.repository.DokterRepository;
import com.iqbal.repository.PasienRepository;
import com.iqbal.repository.ReservasiRepository;
import com.iqbal.service.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservasiServiceTest {

    @Mock
    private ReservasiRepository reservasiRepository;

    @Mock
    private PasienRepository pasienRepository;

    @Mock
    private DokterRepository dokterRepository;

    private ReservasiService reservasiService;

    @BeforeEach
    void setUp() {
        reservasiService = new ReservasiService(reservasiRepository, pasienRepository, dokterRepository);
    }

    @Test
    void simpan_shouldThrowValidationException_whenSlotDokterSudahDipakai() throws SQLException {
        Reservasi reservasi = new Reservasi();
        reservasi.setPasienId(1L);
        reservasi.setDokterId(2L);
        reservasi.setTanggalReservasi(LocalDate.now());
        reservasi.setJamReservasi(LocalTime.of(9, 0));

        when(pasienRepository.findById(1L)).thenReturn(Optional.of(new Pasien()));
        when(dokterRepository.findById(2L)).thenReturn(Optional.of(new Dokter()));

        SQLException uniqueViolation = new SQLException("duplicate key value violates unique constraint", "23505");
        when(reservasiRepository.insert(any(Reservasi.class))).thenThrow(uniqueViolation);

        ValidationException ex = assertThrows(ValidationException.class, () -> reservasiService.simpan(reservasi));
        assertEquals("Dokter sudah memiliki reservasi pada tanggal dan jam tersebut", ex.getMessage());
    }
}
