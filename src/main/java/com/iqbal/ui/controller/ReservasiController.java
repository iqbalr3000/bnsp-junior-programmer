package com.iqbal.ui.controller;

import atlantafx.base.theme.Styles;
import com.iqbal.model.Dokter;
import com.iqbal.model.Pasien;
import com.iqbal.model.Reservasi;
import com.iqbal.model.ReservasiStatus;
import com.iqbal.service.exception.ServiceException;
import com.iqbal.ui.AppContext;
import com.iqbal.ui.util.ContextAware;
import com.iqbal.ui.util.DialogUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReservasiController implements ContextAware {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private ComboBox<Pasien> pasienCombo;
    @FXML private ComboBox<Dokter> dokterCombo;
    @FXML private DatePicker tanggalPicker;
    @FXML private ComboBox<LocalTime> jamCombo;
    @FXML private TextArea keluhanArea;
    @FXML private Button simpanButton;
    @FXML private Button batalkanButton;
    @FXML private TableView<Reservasi> tableView;
    @FXML private TableColumn<Reservasi, String> colPasien;
    @FXML private TableColumn<Reservasi, String> colDokter;
    @FXML private TableColumn<Reservasi, String> colTanggal;
    @FXML private TableColumn<Reservasi, String> colJam;
    @FXML private TableColumn<Reservasi, String> colKeluhan;
    @FXML private TableColumn<Reservasi, String> colStatus;

    private AppContext context;
    private Reservasi selected;

    @Override
    public void setContext(AppContext context) {
        this.context = context;
        pasienCombo.setItems(FXCollections.observableArrayList(context.getPasienService().findAll()));
        dokterCombo.setItems(FXCollections.observableArrayList(context.getDokterService().findAll()));
        loadData();
    }

    @FXML
    private void initialize() {
        simpanButton.getStyleClass().add(Styles.ACCENT);
        batalkanButton.getStyleClass().add(Styles.DANGER);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        pasienCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pasien pasien) {
                return pasien != null ? pasien.getNama() : "";
            }

            @Override
            public Pasien fromString(String string) {
                return null;
            }
        });
        dokterCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Dokter dokter) {
                return dokter != null ? dokter.getNama() : "";
            }

            @Override
            public Dokter fromString(String string) {
                return null;
            }
        });
        jamCombo.setItems(FXCollections.observableArrayList(buildTimeSlots()));
        jamCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalTime time) {
                return time != null ? time.format(TIME_FORMAT) : "";
            }

            @Override
            public LocalTime fromString(String string) {
                return null;
            }
        });

        colPasien.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("pasienNama"));
        colDokter.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dokterNama"));
        colTanggal.setCellValueFactory(data -> {
            var tanggal = data.getValue().getTanggalReservasi();
            return new SimpleStringProperty(tanggal != null ? tanggal.format(DATE_FORMAT) : "");
        });
        colJam.setCellValueFactory(data -> {
            var jam = data.getValue().getJamReservasi();
            return new SimpleStringProperty(jam != null ? jam.format(TIME_FORMAT) : "");
        });
        colKeluhan.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("keluhan"));
        colStatus.setCellValueFactory(data -> {
            ReservasiStatus status = data.getValue().getStatus();
            return new SimpleStringProperty(status != null ? status.name() : "");
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selected = newVal;
            batalkanButton.setDisable(newVal == null || newVal.getStatus() != ReservasiStatus.MENUNGGU);
        });
    }

    private List<LocalTime> buildTimeSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(16, 0);
        while (!current.isAfter(end)) {
            slots.add(current);
            current = current.plusMinutes(30);
        }
        return slots;
    }

    private void loadData() {
        ObservableList<Reservasi> data = FXCollections.observableArrayList(context.getReservasiService().findAll());
        tableView.setItems(data);
    }

    @FXML
    private void handleSimpan() {
        Pasien pasien = pasienCombo.getValue();
        Dokter dokter = dokterCombo.getValue();
        if (pasien == null || dokter == null) {
            DialogUtil.showError("Pasien dan dokter wajib dipilih");
            return;
        }

        Reservasi reservasi = new Reservasi();
        reservasi.setPasienId(pasien.getId());
        reservasi.setDokterId(dokter.getId());
        reservasi.setTanggalReservasi(tanggalPicker.getValue());
        reservasi.setJamReservasi(jamCombo.getValue());
        reservasi.setKeluhan(keluhanArea.getText());
        try {
            context.getReservasiService().simpan(reservasi);
            loadData();
            handleReset();
            DialogUtil.showInfo("Reservasi berhasil disimpan");
        } catch (ServiceException e) {
            DialogUtil.showError(e.getMessage());
        }
    }

    @FXML
    private void handleBatalkan() {
        if (selected == null) {
            return;
        }
        if (!DialogUtil.showConfirm("Batalkan reservasi ini?")) {
            return;
        }
        try {
            context.getReservasiService().batalkan(selected.getId());
            loadData();
            handleReset();
        } catch (ServiceException e) {
            DialogUtil.showError(e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        pasienCombo.setValue(null);
        dokterCombo.setValue(null);
        tanggalPicker.setValue(null);
        jamCombo.setValue(null);
        keluhanArea.clear();
        selected = null;
        batalkanButton.setDisable(true);
        tableView.getSelectionModel().clearSelection();
    }
}
