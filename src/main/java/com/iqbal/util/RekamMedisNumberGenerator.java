package com.iqbal.util;

import com.iqbal.repository.PasienRepository;

import java.time.LocalDate;

public class RekamMedisNumberGenerator {

    private final PasienRepository pasienRepository;

    public RekamMedisNumberGenerator(PasienRepository pasienRepository) {
        this.pasienRepository = pasienRepository;
    }

    public String generate() {
        int year = LocalDate.now().getYear();
        int count = pasienRepository.countByYear(year);
        return "RM-%d-%06d".formatted(year, count + 1);
    }
}
