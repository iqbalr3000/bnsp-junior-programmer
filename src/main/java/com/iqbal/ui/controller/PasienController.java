package com.iqbal.ui.controller;

import atlantafx.base.theme.Styles;
import com.iqbal.model.Pasien;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PasienController implements ContextAware {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @FXML private Label formTitleLabel;
    @FXML private Label noRekamMedisLabel;
    @FXML private TextField namaField;
    @FXML private TextField nikField;
    @FXML private DatePicker tanggalLahirPicker;
    @FXML private ComboBox<String> jenisKelaminCombo;
    @FXML private TextArea alamatArea;
    @FXML private TextField noTeleponField;
    @FXML private Button simpanButton;
    @FXML private Button hapusButton;
    @FXML private TextField searchField;
    @FXML private TableView<Pasien> tableView;
    @FXML private TableColumn<Pasien, String> colNoRekamMedis;
    @FXML private TableColumn<Pasien, String> colNama;
    @FXML private TableColumn<Pasien, String> colNik;
    @FXML private TableColumn<Pasien, String> colTanggalLahir;
    @FXML private TableColumn<Pasien, String> colJenisKelamin;
    @FXML private TableColumn<Pasien, String> colNoTelepon;

    private AppContext context;
    private Pasien selected;

    @Override
    public void setContext(AppContext context) {
        this.context = context;
        loadData(null);
    }

    @FXML
    private void initialize() {
        simpanButton.getStyleClass().add(Styles.ACCENT);
        hapusButton.getStyleClass().add(Styles.DANGER);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        jenisKelaminCombo.setItems(FXCollections.observableArrayList("L", "P"));

        nikField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty() || (newText.length() <= 16 && newText.chars().allMatch(Character::isDigit))) {
                return change;
            }
            return null;
        }));

        colNoRekamMedis.setCellValueFactory(new PropertyValueFactory<>("noRekamMedis"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNik.setCellValueFactory(new PropertyValueFactory<>("nik"));
        colTanggalLahir.setCellValueFactory(data -> {
            LocalDate tgl = data.getValue().getTanggalLahir();
            return new SimpleStringProperty(tgl != null ? tgl.format(DATE_FORMAT) : "");
        });
        colJenisKelamin.setCellValueFactory(new PropertyValueFactory<>("jenisKelamin"));
        colNoTelepon.setCellValueFactory(new PropertyValueFactory<>("noTelepon"));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> populateForm(newVal));
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadData(newVal));
    }

    private void loadData(String keyword) {
        ObservableList<Pasien> data = FXCollections.observableArrayList(
                (keyword == null || keyword.isBlank())
                        ? context.getPasienService().findAll()
                        : context.getPasienService().search(keyword));
        tableView.setItems(data);
    }

    private void populateForm(Pasien pasien) {
        selected = pasien;
        if (pasien == null) {
            clearForm();
            return;
        }
        formTitleLabel.setText("Ubah Data Pasien");
        noRekamMedisLabel.setText("No RM: " + pasien.getNoRekamMedis());
        namaField.setText(pasien.getNama());
        nikField.setText(pasien.getNik());
        tanggalLahirPicker.setValue(pasien.getTanggalLahir());
        jenisKelaminCombo.setValue(pasien.getJenisKelamin());
        alamatArea.setText(pasien.getAlamat());
        noTeleponField.setText(pasien.getNoTelepon());
        hapusButton.setDisable(false);
    }

    @FXML
    private void handleSimpan() {
        Pasien pasien = (selected != null) ? selected : new Pasien();
        pasien.setNama(namaField.getText());
        pasien.setNik(nikField.getText());
        pasien.setTanggalLahir(tanggalLahirPicker.getValue());
        pasien.setJenisKelamin(jenisKelaminCombo.getValue());
        pasien.setAlamat(alamatArea.getText());
        pasien.setNoTelepon(noTeleponField.getText());
        try {
            context.getPasienService().simpan(pasien);
            loadData(searchField.getText());
            handleReset();
            DialogUtil.showInfo("Data berhasil disimpan");
        } catch (ServiceException e) {
            DialogUtil.showError(e.getMessage());
        }
    }

    @FXML
    private void handleHapus() {
        if (selected == null) {
            return;
        }
        if (!DialogUtil.showConfirm("Hapus data pasien '" + selected.getNama() + "'?")) {
            return;
        }
        try {
            context.getPasienService().hapus(selected.getId());
            loadData(searchField.getText());
            handleReset();
        } catch (ServiceException e) {
            DialogUtil.showError(e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        clearForm();
        tableView.getSelectionModel().clearSelection();
    }

    private void clearForm() {
        selected = null;
        formTitleLabel.setText("Tambah Data Pasien");
        noRekamMedisLabel.setText("");
        namaField.clear();
        nikField.clear();
        tanggalLahirPicker.setValue(null);
        jenisKelaminCombo.setValue(null);
        alamatArea.clear();
        noTeleponField.clear();
        hapusButton.setDisable(true);
    }
}
