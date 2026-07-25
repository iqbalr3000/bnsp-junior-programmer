package com.iqbal.model;

public class RekamMedisObat {

    private Long id;
    private Long rekamMedisId;
    private Long obatId;
    private int jumlah;
    private String dosis;

    // transient, diisi manual dari JOIN query untuk kebutuhan tampilan
    private String obatNama;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRekamMedisId() {
        return rekamMedisId;
    }

    public void setRekamMedisId(Long rekamMedisId) {
        this.rekamMedisId = rekamMedisId;
    }

    public Long getObatId() {
        return obatId;
    }

    public void setObatId(Long obatId) {
        this.obatId = obatId;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getObatNama() {
        return obatNama;
    }

    public void setObatNama(String obatNama) {
        this.obatNama = obatNama;
    }
}
