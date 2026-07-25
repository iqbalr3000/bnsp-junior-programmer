package com.iqbal.repository;

import com.iqbal.model.RekamMedisObat;

import java.sql.Connection;
import java.util.List;

public interface RekamMedisObatRepository {

    void insert(Connection conn, long rekamMedisId, RekamMedisObat item);

    List<RekamMedisObat> findByRekamMedisId(long rekamMedisId);
}
