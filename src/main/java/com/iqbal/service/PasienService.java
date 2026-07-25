package com.iqbal.service;

import com.iqbal.model.Pasien;
import com.iqbal.repository.PasienRepository;
import com.iqbal.service.exception.ValidationException;
import com.iqbal.util.RekamMedisNumberGenerator;

import java.util.List;
import java.util.Optional;

public class PasienService {

    private final PasienRepository pasienRepository;
    private final RekamMedisNumberGenerator numberGenerator;

    public PasienService(PasienRepository pasienRepository) {
        this.pasienRepository = pasienRepository;
        this.numberGenerator = new RekamMedisNumberGenerator(pasienRepository);
    }

    public List<Pasien> findAll() {
        return pasienRepository.findAll();
    }

    public Optional<Pasien> findById(long id) {
        return pasienRepository.findById(id);
    }

    public List<Pasien> search(String keyword) {
        return pasienRepository.search(keyword);
    }

    public Pasien simpan(Pasien pasien) {
        validate(pasien);
        if (pasien.getId() == null) {
            pasien.setNoRekamMedis(numberGenerator.generate());
            return pasienRepository.insert(pasien);
        }
        pasienRepository.update(pasien);
        return pasien;
    }

    public void hapus(long id) {
        pasienRepository.deleteById(id);
    }

    private void validate(Pasien pasien) {
        if (pasien.getNama() == null || pasien.getNama().isBlank()) {
            throw new ValidationException("Nama pasien wajib diisi");
        }
        if (pasien.getNik() == null || !pasien.getNik().matches("\\d{16}")) {
            throw new ValidationException("NIK harus terdiri dari 16 digit angka");
        }
    }
}
