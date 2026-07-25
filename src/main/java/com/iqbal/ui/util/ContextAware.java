package com.iqbal.ui.util;

import com.iqbal.ui.AppContext;

/**
 * Diimplementasikan oleh controller FXML yang butuh akses ke service layer.
 * FXMLLoader selalu instantiate controller dengan no-arg constructor, jadi
 * context di-inject belakangan lewat method ini (lihat FxmlLoaderUtil).
 */
public interface ContextAware {

    void setContext(AppContext context);
}
