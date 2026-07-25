package com.iqbal.ui.util;

import com.iqbal.ui.AppContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Satu-satunya abstraksi generik yang diperbolehkan di layer UI: murni soal
 * loading FXML + wiring AppContext, bukan menyembunyikan SQL/business logic.
 */
public final class FxmlLoaderUtil {

    private FxmlLoaderUtil() {
    }

    public static <T> LoadResult<T> load(String fxmlPath, AppContext context) throws IOException {
        FXMLLoader loader = new FXMLLoader(FxmlLoaderUtil.class.getResource(fxmlPath));
        Parent root = loader.load();
        T controller = loader.getController();
        if (controller instanceof ContextAware contextAware) {
            contextAware.setContext(context);
        }
        return new LoadResult<>(root, controller);
    }

    public record LoadResult<T>(Parent root, T controller) {
    }
}
