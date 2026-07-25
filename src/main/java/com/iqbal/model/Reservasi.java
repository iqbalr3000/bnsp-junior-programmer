package com.iqbal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Reservasi {

    private Long id;
    private Long pasienId;
    private Long dokterId;
    private LocalDate tanggalReservasi;
    private LocalTime jamReservasi;
    private String keluhan;
    private ReservasiStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // transient, diisi manual dari JOIN query untuk kebutuhan tampilan list
    private String pasienNama;
    private String dokterNama;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getTanggalReservasi() {
        return tanggalReservasi;
    }

    public void setTanggalReservasi(LocalDate tanggalReservasi) {
        this.tanggalReservasi = tanggalReservasi;
    }

    public LocalTime getJamReservasi() {
        return jamReservasi;
    }

    public void setJamReservasi(LocalTime jamReservasi) {
        this.jamReservasi = jamReservasi;
    }

    public String getKeluhan() {
        return keluhan;
    }

    public void setKeluhan(String keluhan) {
        this.keluhan = keluhan;
    }

    public ReservasiStatus getStatus() {
        return status;
    }

    public void setStatus(ReservasiStatus status) {
        this.status = status;
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
}
