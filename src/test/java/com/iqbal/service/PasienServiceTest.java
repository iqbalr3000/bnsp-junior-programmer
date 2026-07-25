package com.iqbal.service;

import com.iqbal.model.Pasien;
import com.iqbal.repository.PasienRepository;
import com.iqbal.service.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasienServiceTest {

    @Mock
    private PasienRepository pasienRepository;

    private PasienService pasienService;

    @BeforeEach
    void setUp() {
        pasienService = new PasienService(pasienRepository);
    }

    @Test
    void simpan_shouldThrowValidationException_whenNikIsNot16Digits() {
        Pasien pasien = new Pasien();
        pasien.setNama("Budi");
        pasien.setNik("12345"); // kurang dari 16 digit

        assertThrows(ValidationException.class, () -> pasienService.simpan(pasien));
    }

    @Test
    void simpan_shouldThrowValidationException_whenNikContainsNonDigit() {
        Pasien pasien = new Pasien();
        pasien.setNama("Budi");
        pasien.setNik("123456789012345A");

        assertThrows(ValidationException.class, () -> pasienService.simpan(pasien));
    }

    @Test
    void simpan_shouldGenerateNoRekamMedisAndInsert_whenPasienBaru() {
        Pasien pasien = new Pasien();
        pasien.setNama("Budi Santoso");
        pasien.setNik("1234567890123456");
        pasien.setTanggalLahir(LocalDate.of(1990, 1, 1));

        when(pasienRepository.countByYear(any(Integer.class))).thenReturn(4);
        when(pasienRepository.insert(any(Pasien.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pasien hasil = pasienService.simpan(pasien);

        int year = LocalDate.now().getYear();
        String expectedNoRekamMedis = "RM-%d-%06d".formatted(year, 5);
        assertEquals(expectedNoRekamMedis, hasil.getNoRekamMedis());
        verify(pasienRepository, times(1)).insert(eq(pasien));
    }
}
