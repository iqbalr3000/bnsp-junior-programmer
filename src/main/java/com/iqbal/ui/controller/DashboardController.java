package com.iqbal.ui.controller;

import com.iqbal.model.Obat;
import com.iqbal.model.ReservasiStatus;
import com.iqbal.model.Staff;
import com.iqbal.ui.AppContext;
import com.iqbal.ui.SessionContext;
import com.iqbal.ui.util.ContextAware;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDate;

public class DashboardController implements ContextAware {

    private static final int STOK_MENIPIS_THRESHOLD = 10;

    @FXML private Label welcomeLabel;
    @FXML private Label totalPasienLabel;
    @FXML private Label totalDokterLabel;
    @FXML private Label totalObatLabel;
    @FXML private Label reservasiHariIniLabel;
    @FXML private Label reservasiMenungguLabel;
    @FXML private Label totalRekamMedisLabel;
    @FXML private Label obatStokMenipisLabel;

    private AppContext context;

    @Override
    public void setContext(AppContext context) {
        this.context = context;
        loadStatistik();
    }

    private void loadStatistik() {
        Staff staff = SessionContext.getCurrentStaff();
        welcomeLabel.setText("Selamat datang, " + (staff != null ? staff.getNama() : "") + "!");

        totalPasienLabel.setText(String.valueOf(context.getPasienService().findAll().size()));
        totalDokterLabel.setText(String.valueOf(context.getDokterService().findAll().size()));
        totalRekamMedisLabel.setText(String.valueOf(context.getRekamMedisService().findAll().size()));

        LocalDate today = LocalDate.now();
        long reservasiHariIni = context.getReservasiService().findAll().stream()
                .filter(r -> today.equals(r.getTanggalReservasi()))
                .count();
        reservasiHariIniLabel.setText(String.valueOf(reservasiHariIni));

        long reservasiMenunggu = context.getReservasiService().findAll().stream()
                .filter(r -> r.getStatus() == ReservasiStatus.MENUNGGU)
                .count();
        reservasiMenungguLabel.setText(String.valueOf(reservasiMenunggu));

        var daftarObat = context.getObatService().findAll();
        totalObatLabel.setText(String.valueOf(daftarObat.size()));
        long stokMenipis = daftarObat.stream()
                .mapToInt(Obat::getStok)
                .filter(stok -> stok < STOK_MENIPIS_THRESHOLD)
                .count();
        obatStokMenipisLabel.setText(String.valueOf(stokMenipis));
    }
}
