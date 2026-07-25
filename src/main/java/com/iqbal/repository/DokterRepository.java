package com.iqbal.repository;

import com.iqbal.model.Dokter;

import java.util.List;
import java.util.Optional;

public interface DokterRepository {

    List<Dokter> findAll();

    Optional<Dokter> findById(long id);

    List<Dokter> search(String keyword);

    Dokter insert(Dokter dokter);

    void update(Dokter dokter);

    void deleteById(long id);
}
