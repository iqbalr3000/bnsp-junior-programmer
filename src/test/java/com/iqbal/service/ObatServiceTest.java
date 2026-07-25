package com.iqbal.service;

import com.iqbal.model.Obat;
import com.iqbal.repository.ObatRepository;
import com.iqbal.service.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ObatServiceTest {

    @Mock
    private ObatRepository obatRepository;

    private ObatService obatService;

    @BeforeEach
    void setUp() {
        obatService = new ObatService(obatRepository);
    }

    @Test
    void simpan_shouldThrowValidationException_whenHargaNegatif() {
        Obat obat = new Obat();
        obat.setNama("Paracetamol");
        obat.setKodeObat("OBT-001");
        obat.setSatuan("Tablet");
        obat.setHarga(new BigDecimal("-100"));
        obat.setStok(10);

        assertThrows(ValidationException.class, () -> obatService.simpan(obat));
    }

    @Test
    void simpan_shouldThrowValidationException_whenStokNegatif() {
        Obat obat = new Obat();
        obat.setNama("Paracetamol");
        obat.setKodeObat("OBT-001");
        obat.setSatuan("Tablet");
        obat.setHarga(new BigDecimal("1000"));
        obat.setStok(-5);

        assertThrows(ValidationException.class, () -> obatService.simpan(obat));
    }
}
