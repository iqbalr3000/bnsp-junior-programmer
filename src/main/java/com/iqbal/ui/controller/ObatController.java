package com.iqbal.ui.controller;

import atlantafx.base.theme.Styles;
import com.iqbal.model.Obat;
import com.iqbal.service.exception.ServiceException;
import com.iqbal.ui.AppContext;
import com.iqbal.ui.util.ContextAware;
import com.iqbal.ui.util.DialogUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;

public class ObatController implements ContextAware {

    @FXML private Label formTitleLabel;
    @FXML private TextField kodeObatField;
    @FXML private TextField namaField;
    @FXML private TextField satuanField;
    @FXML private TextField hargaField;
    @FXML private TextField stokField;
    @FXML private TextArea deskripsiArea;
    @FXML private Button simpanButton;
    @FXML private Button hapusButton;
    @FXML private TextField searchField;
    @FXML private TableView<Obat> tableView;
    @FXML private TableColumn<Obat, String> colKodeObat;
    @FXML private TableColumn<Obat, String> colNama;
    @FXML private TableColumn<Obat, String> colSatuan;
    @FXML private TableColumn<Obat, String> colHarga;
    @FXML private TableColumn<Obat, String> colStok;

    private AppContext context;
    private Obat selected;

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

        colKodeObat.setCellValueFactory(new PropertyValueFactory<>("kodeObat"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colSatuan.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        colHarga.setCellValueFactory(data -> {
            BigDecimal harga = data.getValue().getHarga();
            return new SimpleStringProperty(harga != null ? harga.toPlainString() : "");
        });
        colStok.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getStok())));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> populateForm(newVal));
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadData(newVal));
    }

    private void loadData(String keyword) {
        ObservableList<Obat> data = FXCollections.observableArrayList(
                (keyword == null || keyword.isBlank())
                        ? context.getObatService().findAll()
                        : context.getObatService().search(keyword));
        tableView.setItems(data);
    }

    private void populateForm(Obat obat) {
        selected = obat;
        if (obat == null) {
            clearForm();
            return;
        }
        formTitleLabel.setText("Ubah Data Obat");
        kodeObatField.setText(obat.getKodeObat());
        namaField.setText(obat.getNama());
        satuanField.setText(obat.getSatuan());
        hargaField.setText(obat.getHarga() != null ? obat.getHarga().toPlainString() : "");
        stokField.setText(String.valueOf(obat.getStok()));
        deskripsiArea.setText(obat.getDeskripsi());
        hapusButton.setDisable(false);
    }

    @FXML
    private void handleSimpan() {
        BigDecimal harga;
        int stok;
        try {
            harga = new BigDecimal(hargaField.getText().trim());
        } catch (NumberFormatException e) {
            DialogUtil.showError("Harga harus berupa angka");
            return;
        }
        try {
            stok = Integer.parseInt(stokField.getText().trim());
        } catch (NumberFormatException e) {
            DialogUtil.showError("Stok harus berupa angka");
            return;
        }

        Obat obat = (selected != null) ? selected : new Obat();
        obat.setKodeObat(kodeObatField.getText());
        obat.setNama(namaField.getText());
        obat.setSatuan(satuanField.getText());
        obat.setHarga(harga);
        obat.setStok(stok);
        obat.setDeskripsi(deskripsiArea.getText());
        try {
            context.getObatService().simpan(obat);
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
        if (!DialogUtil.showConfirm("Hapus data obat '" + selected.getNama() + "'?")) {
            return;
        }
        try {
            context.getObatService().hapus(selected.getId());
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
        formTitleLabel.setText("Tambah Data Obat");
        kodeObatField.clear();
        namaField.clear();
        satuanField.clear();
        hargaField.clear();
        stokField.clear();
        deskripsiArea.clear();
        hapusButton.setDisable(true);
    }
}
