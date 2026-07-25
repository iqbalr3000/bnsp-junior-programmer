package com.iqbal.service;

import com.iqbal.model.Dokter;
import com.iqbal.repository.DokterRepository;
import com.iqbal.service.exception.ValidationException;

import java.util.List;
import java.util.Optional;

public class DokterService {

    private final DokterRepository dokterRepository;

    public DokterService(DokterRepository dokterRepository) {
        this.dokterRepository = dokterRepository;
    }

    public List<Dokter> findAll() {
        return dokterRepository.findAll();
    }

    public Optional<Dokter> findById(long id) {
        return dokterRepository.findById(id);
    }

    public List<Dokter> search(String keyword) {
        return dokterRepository.search(keyword);
    }

    public Dokter simpan(Dokter dokter) {
        validate(dokter);
        if (dokter.getId() == null) {
            return dokterRepository.insert(dokter);
        }
        dokterRepository.update(dokter);
        return dokter;
    }

    public void hapus(long id) {
        dokterRepository.deleteById(id);
    }

    private void validate(Dokter dokter) {
        if (dokter.getNama() == null || dokter.getNama().isBlank()) {
            throw new ValidationException("Nama dokter wajib diisi");
        }
        if (dokter.getSpesialisasi() == null || dokter.getSpesialisasi().isBlank()) {
            throw new ValidationException("Spesialisasi dokter wajib diisi");
        }
        if (dokter.getNoStr() == null || dokter.getNoStr().isBlank()) {
            throw new ValidationException("Nomor STR dokter wajib diisi");
        }
    }
}
