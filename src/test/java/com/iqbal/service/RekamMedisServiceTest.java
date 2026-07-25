package com.iqbal.service;

import com.iqbal.model.Obat;
import com.iqbal.model.RekamMedis;
import com.iqbal.model.RekamMedisObat;
import com.iqbal.repository.ObatRepository;
import com.iqbal.repository.RekamMedisObatRepository;
import com.iqbal.repository.RekamMedisRepository;
import com.iqbal.repository.ReservasiRepository;
import com.iqbal.service.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RekamMedisServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private RekamMedisRepository rekamMedisRepository;

    @Mock
    private RekamMedisObatRepository rekamMedisObatRepository;

    @Mock
    private ObatRepository obatRepository;

    @Mock
    private ReservasiRepository reservasiRepository;

    private RekamMedisService rekamMedisService;

    @BeforeEach
    void setUp() throws SQLException {
        rekamMedisService = new RekamMedisService(
                dataSource, rekamMedisRepository, rekamMedisObatRepository, obatRepository, reservasiRepository);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    private RekamMedis rekamMedisDenganResep(int jumlahDiminta) {
        RekamMedis rm = new RekamMedis();
        rm.setPasienId(1L);
        rm.setDokterId(2L);
        rm.setDiagnosis("Flu");

        RekamMedisObat resep = new RekamMedisObat();
        resep.setObatId(10L);
        resep.setJumlah(jumlahDiminta);
        resep.setDosis("3x1");
        rm.getResepObat().add(resep);
        return rm;
    }

    @Test
    void simpanDenganResep_shouldCommit_whenStokMencukupi() throws SQLException {
        RekamMedis rm = rekamMedisDenganResep(2);

        Obat obat = new Obat();
        obat.setId(10L);
        obat.setNama("Paracetamol");
        obat.setStok(5);

        when(rekamMedisRepository.insert(eq(connection), eq(rm))).thenReturn(100L);
        when(obatRepository.findByIdForUpdate(connection, 10L)).thenReturn(Optional.of(obat));

        rekamMedisService.simpanDenganResep(rm);

        verify(obatRepository, times(1)).kurangiStok(connection, 10L, 2);
        verify(connection, times(1)).commit();
        verify(connection, never()).rollback();
        verify(connection, times(1)).setAutoCommit(true);
        verify(connection, times(1)).close();
    }

    @Test
    void simpanDenganResep_shouldRollback_whenStokTidakMencukupi() throws SQLException {
        RekamMedis rm = rekamMedisDenganResep(10);

        Obat obat = new Obat();
        obat.setId(10L);
        obat.setNama("Paracetamol");
        obat.setStok(2); // stok kurang dari jumlah diminta

        when(rekamMedisRepository.insert(eq(connection), eq(rm))).thenReturn(100L);
        when(obatRepository.findByIdForUpdate(connection, 10L)).thenReturn(Optional.of(obat));

        assertThrows(InsufficientStockException.class, () -> rekamMedisService.simpanDenganResep(rm));

        verify(obatRepository, never()).kurangiStok(any(), anyLong(), anyInt());
        verify(connection, never()).commit();
        verify(connection, times(1)).rollback();
        verify(connection, times(1)).setAutoCommit(true);
        verify(connection, times(1)).close();
    }
}
