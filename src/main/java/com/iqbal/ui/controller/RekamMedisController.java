package com.iqbal.ui.controller;

import atlantafx.base.theme.Styles;
import com.iqbal.model.Dokter;
import com.iqbal.model.Obat;
import com.iqbal.model.Pasien;
import com.iqbal.model.RekamMedis;
import com.iqbal.model.RekamMedisObat;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RekamMedisController implements ContextAware {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML private ComboBox<Pasien> pasienCombo;
    @FXML private ComboBox<Dokter> dokterCombo;
    @FXML private ComboBox<Reservasi> reservasiCombo;
    @FXML private DatePicker tanggalPeriksaPicker;
    @FXML private TextArea diagnosisArea;
    @FXML private TextArea catatanArea;

    @FXML private ComboBox<Obat> obatCombo;
    @FXML private TextField jumlahField;
    @FXML private TextField dosisField;
    @FXML private TableView<RekamMedisObat> resepTable;
    @FXML private TableColumn<RekamMedisObat, String> colObatNama;
    @FXML private TableColumn<RekamMedisObat, String> colJumlah;
    @FXML private TableColumn<RekamMedisObat, String> colDosis;
    @FXML private TableColumn<RekamMedisObat, Void> colAksi;

    @FXML private Button simpanButton;

    @FXML private TableView<RekamMedis> tableView;
    @FXML private TableColumn<RekamMedis, String> colTanggal;
    @FXML private TableColumn<RekamMedis, String> colPasien;
    @FXML private TableColumn<RekamMedis, String> colDokter;
    @FXML private TableColumn<RekamMedis, String> colDiagnosis;

    private AppContext context;
    private final ObservableList<RekamMedisObat> resepList = FXCollections.observableArrayList();

    @Override
    public void setContext(AppContext context) {
        this.context = context;
        pasienCombo.setItems(FXCollections.observableArrayList(context.getPasienService().findAll()));
        dokterCombo.setItems(FXCollections.observableArrayList(context.getDokterService().findAll()));
        obatCombo.setItems(FXCollections.observableArrayList(context.getObatService().findAll()));
        loadData();
    }

    @FXML
    private void initialize() {
        simpanButton.getStyleClass().add(Styles.ACCENT);
        tanggalPeriksaPicker.setValue(LocalDate.now());
        resepTable.setItems(resepList);
        resepTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colAksi.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Hapus");
            {
                deleteButton.getStyleClass().add(Styles.DANGER);
                deleteButton.setOnAction(event -> resepList.remove(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        pasienCombo.setConverter(nameConverter(Pasien::getNama));
        dokterCombo.setConverter(nameConverter(Dokter::getNama));
        obatCombo.setConverter(nameConverter(Obat::getNama));
        reservasiCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Reservasi reservasi) {
                if (reservasi == null) {
                    return "";
                }
                String tanggal = reservasi.getTanggalReservasi() != null
                        ? reservasi.getTanggalReservasi().atTime(reservasi.getJamReservasi() != null
                                ? reservasi.getJamReservasi() : java.time.LocalTime.MIDNIGHT).format(DATETIME_FORMAT)
                        : "";
                return tanggal + " - " + reservasi.getKeluhan();
            }

            @Override
            public Reservasi fromString(String string) {
                return null;
            }
        });

        pasienCombo.valueProperty().addListener((obs, oldVal, newVal) -> refreshReservasiCombo(newVal));

        colObatNama.setCellValueFactory(new PropertyValueFactory<>("obatNama"));
        colJumlah.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getJumlah())));
        colDosis.setCellValueFactory(new PropertyValueFactory<>("dosis"));

        colTanggal.setCellValueFactory(data -> {
            LocalDate tanggal = data.getValue().getTanggalPeriksa();
            return new SimpleStringProperty(tanggal != null ? tanggal.format(DATE_FORMAT) : "");
        });
        colPasien.setCellValueFactory(new PropertyValueFactory<>("pasienNama"));
        colDokter.setCellValueFactory(new PropertyValueFactory<>("dokterNama"));
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
    }

    private <T> StringConverter<T> nameConverter(java.util.function.Function<T, String> nameFn) {
        return new StringConverter<>() {
            @Override
            public String toString(T item) {
                return item != null ? nameFn.apply(item) : "";
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        };
    }

    private void refreshReservasiCombo(Pasien pasien) {
        reservasiCombo.setValue(null);
        if (pasien == null) {
            reservasiCombo.setItems(FXCollections.observableArrayList());
            return;
        }
        List<Reservasi> menunggu = context.getReservasiService().findByPasienId(pasien.getId()).stream()
                .filter(r -> r.getStatus() == ReservasiStatus.MENUNGGU)
                .toList();
        reservasiCombo.setItems(FXCollections.observableArrayList(menunggu));
    }

    private void loadData() {
        tableView.setItems(FXCollections.observableArrayList(context.getRekamMedisService().findAll()));
    }

    @FXML
    private void handleTambahResep() {
        Obat obat = obatCombo.getValue();
        if (obat == null) {
            DialogUtil.showError("Pilih obat terlebih dahulu");
            return;
        }
        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahField.getText().trim());
        } catch (NumberFormatException e) {
            DialogUtil.showError("Jumlah harus berupa angka");
            return;
        }
        if (jumlah <= 0) {
            DialogUtil.showError("Jumlah harus lebih besar dari 0");
            return;
        }

        RekamMedisObat item = new RekamMedisObat();
        item.setObatId(obat.getId());
        item.setObatNama(obat.getNama());
        item.setJumlah(jumlah);
        item.setDosis(dosisField.getText());
        resepList.add(item);

        obatCombo.setValue(null);
        jumlahField.clear();
        dosisField.clear();
    }

    @FXML
    private void handleSimpan() {
        Pasien pasien = pasienCombo.getValue();
        Dokter dokter = dokterCombo.getValue();
        if (pasien == null || dokter == null) {
            DialogUtil.showError("Pasien dan dokter wajib dipilih");
            return;
        }
        if (resepList.isEmpty() && !DialogUtil.showConfirm("Belum ada resep obat ditambahkan. Simpan tanpa resep?")) {
            return;
        }

        RekamMedis rm = new RekamMedis();
        rm.setPasienId(pasien.getId());
        rm.setDokterId(dokter.getId());
        Reservasi reservasi = reservasiCombo.getValue();
        rm.setReservasiId(reservasi != null ? reservasi.getId() : null);
        rm.setTanggalPeriksa(tanggalPeriksaPicker.getValue());
        rm.setDiagnosis(diagnosisArea.getText());
        rm.setCatatan(catatanArea.getText());
        rm.setResepObat(new ArrayList<>(resepList));

        try {
            context.getRekamMedisService().simpanDenganResep(rm);
            loadData();
            handleReset();
            DialogUtil.showInfo("Rekam medis berhasil disimpan");
        } catch (ServiceException e) {
            // sengaja TIDAK reset form/resep list supaya user tinggal koreksi lalu simpan ulang
            DialogUtil.showError(e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        pasienCombo.setValue(null);
        dokterCombo.setValue(null);
        reservasiCombo.setValue(null);
        reservasiCombo.setItems(FXCollections.observableArrayList());
        tanggalPeriksaPicker.setValue(LocalDate.now());
        diagnosisArea.clear();
        catatanArea.clear();
        obatCombo.setValue(null);
        jumlahField.clear();
        dosisField.clear();
        resepList.clear();
    }
}
