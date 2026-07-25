package com.iqbal.repository;

import com.iqbal.model.RekamMedis;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface RekamMedisRepository {

    List<RekamMedis> findAll();

    Optional<RekamMedis> findById(long id);

    long insert(Connection conn, RekamMedis rekamMedis);

    void deleteById(long id);
}
