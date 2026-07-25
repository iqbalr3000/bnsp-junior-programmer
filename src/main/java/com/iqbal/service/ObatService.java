package com.iqbal.service;

import com.iqbal.model.Obat;
import com.iqbal.repository.ObatRepository;
import com.iqbal.service.exception.ValidationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ObatService {

    private final ObatRepository obatRepository;

    public ObatService(ObatRepository obatRepository) {
        this.obatRepository = obatRepository;
    }

    public List<Obat> findAll() {
        return obatRepository.findAll();
    }

    public Optional<Obat> findById(long id) {
        return obatRepository.findById(id);
    }

    public List<Obat> search(String keyword) {
        return obatRepository.search(keyword);
    }

    public Obat simpan(Obat obat) {
        validate(obat);
        if (obat.getId() == null) {
            return obatRepository.insert(obat);
        }
        obatRepository.update(obat);
        return obat;
    }

    public void hapus(long id) {
        obatRepository.deleteById(id);
    }

    private void validate(Obat obat) {
        if (obat.getNama() == null || obat.getNama().isBlank()) {
            throw new ValidationException("Nama obat wajib diisi");
        }
        if (obat.getKodeObat() == null || obat.getKodeObat().isBlank()) {
            throw new ValidationException("Kode obat wajib diisi");
        }
        if (obat.getSatuan() == null || obat.getSatuan().isBlank()) {
            throw new ValidationException("Satuan obat wajib diisi");
        }
        if (obat.getHarga() == null || obat.getHarga().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Harga obat tidak boleh negatif");
        }
        if (obat.getStok() < 0) {
            throw new ValidationException("Stok obat tidak boleh negatif");
        }
    }
}
