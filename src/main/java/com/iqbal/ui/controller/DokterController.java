package com.iqbal.ui.controller;

import atlantafx.base.theme.Styles;
import com.iqbal.model.Dokter;
import com.iqbal.service.exception.ServiceException;
import com.iqbal.ui.AppContext;
import com.iqbal.ui.util.ContextAware;
import com.iqbal.ui.util.DialogUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class DokterController implements ContextAware {

    @FXML private Label formTitleLabel;
    @FXML private TextField namaField;
    @FXML private TextField spesialisasiField;
    @FXML private TextField noStrField;
    @FXML private TextField noTeleponField;
    @FXML private Button simpanButton;
    @FXML private Button hapusButton;
    @FXML private TextField searchField;
    @FXML private TableView<Dokter> tableView;
    @FXML private TableColumn<Dokter, String> colNama;
    @FXML private TableColumn<Dokter, String> colSpesialisasi;
    @FXML private TableColumn<Dokter, String> colNoStr;
    @FXML private TableColumn<Dokter, String> colNoTelepon;

    private AppContext context;
    private Dokter selected;

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

        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colSpesialisasi.setCellValueFactory(new PropertyValueFactory<>("spesialisasi"));
        colNoStr.setCellValueFactory(new PropertyValueFactory<>("noStr"));
        colNoTelepon.setCellValueFactory(new PropertyValueFactory<>("noTelepon"));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> populateForm(newVal));
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadData(newVal));
    }

    private void loadData(String keyword) {
        ObservableList<Dokter> data = FXCollections.observableArrayList(
                (keyword == null || keyword.isBlank())
                        ? context.getDokterService().findAll()
                        : context.getDokterService().search(keyword));
        tableView.setItems(data);
    }

    private void populateForm(Dokter dokter) {
        selected = dokter;
        if (dokter == null) {
            clearForm();
            return;
        }
        formTitleLabel.setText("Ubah Data Dokter");
        namaField.setText(dokter.getNama());
        spesialisasiField.setText(dokter.getSpesialisasi());
        noStrField.setText(dokter.getNoStr());
        noTeleponField.setText(dokter.getNoTelepon());
        hapusButton.setDisable(false);
    }

    @FXML
    private void handleSimpan() {
        Dokter dokter = (selected != null) ? selected : new Dokter();
        dokter.setNama(namaField.getText());
        dokter.setSpesialisasi(spesialisasiField.getText());
        dokter.setNoStr(noStrField.getText());
        dokter.setNoTelepon(noTeleponField.getText());
        try {
            context.getDokterService().simpan(dokter);
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
        if (!DialogUtil.showConfirm("Hapus data dokter '" + selected.getNama() + "'?")) {
            return;
        }
        try {
            context.getDokterService().hapus(selected.getId());
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
        formTitleLabel.setText("Tambah Data Dokter");
        namaField.clear();
        spesialisasiField.clear();
        noStrField.clear();
        noTeleponField.clear();
        hapusButton.setDisable(true);
    }
}
