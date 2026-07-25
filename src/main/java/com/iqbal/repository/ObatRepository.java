package com.iqbal.repository;

import com.iqbal.model.Obat;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface ObatRepository {

    List<Obat> findAll();

    Optional<Obat> findById(long id);

    List<Obat> search(String keyword);

    Obat insert(Obat obat);

    void update(Obat obat);

    void deleteById(long id);

    Optional<Obat> findByIdForUpdate(Connection conn, long id);

    void kurangiStok(Connection conn, long obatId, int jumlah);
}
