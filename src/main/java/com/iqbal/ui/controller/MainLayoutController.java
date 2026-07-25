package com.iqbal.ui.controller;

import com.iqbal.model.Staff;
import com.iqbal.ui.AppContext;
import com.iqbal.ui.SessionContext;
import com.iqbal.ui.util.ContextAware;
import com.iqbal.ui.util.FxmlLoaderUtil;
import com.iqbal.ui.util.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainLayoutController implements ContextAware {

    @FXML private BorderPane root;
    @FXML private Label staffNameLabel;
    @FXML private Button dashboardButton;
    @FXML private Button pasienButton;
    @FXML private Button dokterButton;
    @FXML private Button obatButton;
    @FXML private Button reservasiButton;
    @FXML private Button rekamMedisButton;
    @FXML private Button aiAssistantButton;
    @FXML private StatusBarController statusBarController;

    private AppContext context;
    private ViewNavigator navigator;
    private Button activeMenuButton;

    @Override
    public void setContext(AppContext context) {
        this.context = context;
        this.navigator = new ViewNavigator(root, context);

        if (statusBarController != null) {
            context.setResourceMonitorTimeline(statusBarController.getTimeline());
        }

        Staff staff = SessionContext.getCurrentStaff();
        staffNameLabel.setText(staff != null ? staff.getNama() : "");

        showDashboard();
    }

    @FXML
    private void showDashboard() {
        navigator.navigateTo("/fxml/dashboard-view.fxml");
        highlight(dashboardButton);
    }

    @FXML
    private void showPasien() {
        navigator.navigateTo("/fxml/pasien-view.fxml");
        highlight(pasienButton);
    }

    @FXML
    private void showDokter() {
        navigator.navigateTo("/fxml/dokter-view.fxml");
        highlight(dokterButton);
    }

    @FXML
    private void showObat() {
        navigator.navigateTo("/fxml/obat-view.fxml");
        highlight(obatButton);
    }

    @FXML
    private void showReservasi() {
        navigator.navigateTo("/fxml/reservasi-view.fxml");
        highlight(reservasiButton);
    }

    @FXML
    private void showRekamMedis() {
        navigator.navigateTo("/fxml/rekam-medis-view.fxml");
        highlight(rekamMedisButton);
    }

    @FXML
    private void showAiAssistant() {
        navigator.navigateTo("/fxml/chat-view.fxml");
        highlight(aiAssistantButton);
    }

    @FXML
    private void handleLogout() {
        if (statusBarController != null && statusBarController.getTimeline() != null) {
            statusBarController.getTimeline().stop();
        }
        SessionContext.setCurrentStaff(null);
        try {
            FxmlLoaderUtil.LoadResult<Object> login = FxmlLoaderUtil.load("/fxml/login.fxml", context);
            Stage stage = context.getPrimaryStage();
            stage.getScene().setRoot(login.root());
            stage.setWidth(420);
            stage.setHeight(520);
            stage.centerOnScreen();
        } catch (IOException e) {
            throw new IllegalStateException("Gagal memuat halaman login", e);
        }
    }

    private void highlight(Button selected) {
        if (activeMenuButton != null) {
            activeMenuButton.getStyleClass().remove("menu-active");
        }
        selected.getStyleClass().add("menu-active");
        activeMenuButton = selected;
    }
}
