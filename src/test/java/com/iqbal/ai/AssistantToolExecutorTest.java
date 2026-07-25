package com.iqbal.ai;

import com.iqbal.model.Obat;
import com.iqbal.model.Pasien;
import com.iqbal.service.DokterService;
import com.iqbal.service.ObatService;
import com.iqbal.service.PasienService;
import com.iqbal.service.RekamMedisService;
import com.iqbal.service.ReservasiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistantToolExecutorTest {

    @Mock
    private PasienService pasienService;

    @Mock
    private DokterService dokterService;

    @Mock
    private ObatService obatService;

    @Mock
    private ReservasiService reservasiService;

    @Mock
    private RekamMedisService rekamMedisService;

    private AssistantToolExecutor toolExecutor;

    @BeforeEach
    void setUp() {
        toolExecutor = new AssistantToolExecutor(
                pasienService, dokterService, obatService, reservasiService, rekamMedisService);
    }

    @Test
    void dispatch_cekStokObat_shouldCallObatServiceSearch() {
        Obat obat = new Obat();
        obat.setNama("Paracetamol");
        obat.setKodeObat("OB-1");
        obat.setSatuan("tablet");
        obat.setStok(20);
        obat.setHarga(BigDecimal.valueOf(500));
        when(obatService.search("paracetamol")).thenReturn(List.of(obat));

        String result = toolExecutor.dispatch("cek_stok_obat", Map.of("keyword", "paracetamol"));

        verify(obatService).search("paracetamol");
        assertTrue(result.contains("Paracetamol"));
        assertTrue(result.contains("20"));
    }

    @Test
    void dispatch_obatStokMenipis_shouldFilterByThreshold() {
        Obat obatMenipis = new Obat();
        obatMenipis.setNama("Amoxicillin");
        obatMenipis.setSatuan("kapsul");
        obatMenipis.setStok(3);

        Obat obatCukup = new Obat();
        obatCukup.setNama("Vitamin C");
        obatCukup.setSatuan("tablet");
        obatCukup.setStok(50);

        when(obatService.findAll()).thenReturn(List.of(obatMenipis, obatCukup));

        String result = toolExecutor.dispatch("obat_stok_menipis", Map.of("threshold", 10));

        assertTrue(result.contains("Amoxicillin"));
        assertFalse(result.contains("Vitamin C"));
    }

    @Test
    void dispatch_cariPasien_shouldCallPasienServiceSearch() {
        Pasien pasien = new Pasien();
        pasien.setId(1L);
        pasien.setNama("Budi");
        pasien.setNik("1234567890123456");
        when(pasienService.search("budi")).thenReturn(List.of(pasien));

        String result = toolExecutor.dispatch("cari_pasien", Map.of("keyword", "budi"));

        verify(pasienService).search("budi");
        assertTrue(result.contains("Budi"));
    }

    @Test
    void dispatch_unknownTool_shouldNotThrow() {
        String result = toolExecutor.dispatch("halo", Map.of());

        assertEquals("Tool tidak dikenal: halo", result);
    }

    @Test
    void dispatch_shouldNotThrow_whenUnderlyingServiceFails() {
        when(obatService.search(anyString())).thenThrow(new RuntimeException("DB down"));

        String result = toolExecutor.dispatch("cek_stok_obat", Map.of("keyword", "x"));

        assertTrue(result.startsWith("Gagal mengambil data"));
    }
}
