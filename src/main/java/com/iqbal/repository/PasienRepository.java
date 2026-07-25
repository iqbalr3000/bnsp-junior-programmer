package com.iqbal.repository;

import com.iqbal.model.Pasien;

import java.util.List;
import java.util.Optional;

public interface PasienRepository {

    List<Pasien> findAll();

    Optional<Pasien> findById(long id);

    Optional<Pasien> findByNik(String nik);

    List<Pasien> search(String keyword);

    int countByYear(int year);

    Pasien insert(Pasien pasien);

    void update(Pasien pasien);

    void deleteById(long id);
}
