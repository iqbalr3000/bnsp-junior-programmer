package com.iqbal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RekamMedis {

    private Long id;
    private Long reservasiId;
    private Long pasienId;
    private Long dokterId;
    private LocalDate tanggalPeriksa;
    private String diagnosis;
    private String catatan;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // transient, diisi manual dari JOIN query untuk kebutuhan tampilan list
    private String pasienNama;
    private String dokterNama;

    private List<RekamMedisObat> resepObat = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservasiId() {
        return reservasiId;
    }

    public void setReservasiId(Long reservasiId) {
        this.reservasiId = reservasiId;
    }

    public Long getPasienId() {
        return pasienId;
    }

    public void setPasienId(Long pasienId) {
        this.pasienId = pasienId;
    }

    public Long getDokterId() {
        return dokterId;
    }

    public void setDokterId(Long dokterId) {
        this.dokterId = dokterId;
    }

    public LocalDate getTanggalPeriksa() {
        return tanggalPeriksa;
    }

    public void setTanggalPeriksa(LocalDate tanggalPeriksa) {
        this.tanggalPeriksa = tanggalPeriksa;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPasienNama() {
        return pasienNama;
    }

    public void setPasienNama(String pasienNama) {
        this.pasienNama = pasienNama;
    }

    public String getDokterNama() {
        return dokterNama;
    }

    public void setDokterNama(String dokterNama) {
        this.dokterNama = dokterNama;
    }

    public List<RekamMedisObat> getResepObat() {
        return resepObat;
    }

    public void setResepObat(List<RekamMedisObat> resepObat) {
        this.resepObat = resepObat;
    }
}
