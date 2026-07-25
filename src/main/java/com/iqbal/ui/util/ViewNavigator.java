package com.iqbal.ui.util;

import com.iqbal.ui.AppContext;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

/**
 * Dipegang oleh MainLayoutController untuk mengganti node "center" dari
 * BorderPane root sesuai menu sidebar yang diklik.
 */
public class ViewNavigator {

    private final BorderPane root;
    private final AppContext context;

    public ViewNavigator(BorderPane root, AppContext context) {
        this.root = root;
        this.context = context;
    }

    public void navigateTo(String fxmlPath) {
        try {
            FxmlLoaderUtil.LoadResult<Object> result = FxmlLoaderUtil.load(fxmlPath, context);
            root.setCenter(result.root());
        } catch (IOException e) {
            DialogUtil.showError("Gagal memuat halaman: " + e.getMessage());
        }
    }
}
