package com.iqbal.ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Helper Alert sederhana. Dipakai di seluruh controller supaya ServiceException
 * (dan turunannya) selalu tampil ke user, tidak pernah silent-fail atau crash.
 */
public final class DialogUtil {

    private DialogUtil() {
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Kesalahan");
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Informasi");
        alert.showAndWait();
    }

    public static boolean showConfirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Konfirmasi");
        return alert.showAndWait().filter(bt -> bt == ButtonType.YES).isPresent();
    }
}
