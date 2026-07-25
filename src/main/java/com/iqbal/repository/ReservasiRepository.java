package com.iqbal.repository;

import com.iqbal.model.Reservasi;
import com.iqbal.model.ReservasiStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ReservasiRepository {

    List<Reservasi> findAll();

    Optional<Reservasi> findById(long id);

    List<Reservasi> findByPasienId(long pasienId);

    Reservasi insert(Reservasi reservasi) throws SQLException;

    void updateStatus(long id, ReservasiStatus status);

    void updateStatus(Connection conn, long id, ReservasiStatus status);

    void deleteById(long id);
}
