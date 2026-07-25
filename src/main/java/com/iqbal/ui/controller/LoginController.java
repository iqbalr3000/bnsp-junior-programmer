package com.iqbal.ui.controller;

import atlantafx.base.theme.Styles;
import com.iqbal.model.Staff;
import com.iqbal.service.exception.ServiceException;
import com.iqbal.ui.AppContext;
import com.iqbal.ui.SessionContext;
import com.iqbal.ui.util.ContextAware;
import com.iqbal.ui.util.DialogUtil;
import com.iqbal.ui.util.FxmlLoaderUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController implements ContextAware {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private AppContext context;

    @Override
    public void setContext(AppContext context) {
        this.context = context;
    }

    @FXML
    private void initialize() {
        loginButton.getStyleClass().add(Styles.ACCENT);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML
    private void handleLogin() {
        if (loginButton.isDisable()) {
            return;
        }
        loginButton.setDisable(true);
        String username = usernameField.getText();
        String password = passwordField.getText();
        try {
            Staff staff = context.getAuthService().login(username, password);
            SessionContext.setCurrentStaff(staff);
            openMainLayout();
        } catch (ServiceException e) {
            showInlineError(e.getMessage());
            loginButton.setDisable(false);
        }
    }

    private void showInlineError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void openMainLayout() {
        try {
            FxmlLoaderUtil.LoadResult<Object> mainLayout = FxmlLoaderUtil.load("/fxml/main-layout.fxml", context);
            Stage stage = context.getPrimaryStage();
            stage.getScene().setRoot(mainLayout.root());
            stage.setWidth(1280);
            stage.setHeight(800);
            stage.centerOnScreen();
        } catch (IOException e) {
            DialogUtil.showError("Gagal memuat halaman utama: " + e.getMessage());
        }
    }
}
