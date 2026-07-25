package com.iqbal;

import atlantafx.base.theme.PrimerLight;
import com.iqbal.config.AppConfig;
import com.iqbal.config.DataSourceProvider;
import com.iqbal.config.FlywayRunner;
import com.iqbal.repository.impl.DokterRepositoryImpl;
import com.iqbal.repository.impl.ObatRepositoryImpl;
import com.iqbal.repository.impl.PasienRepositoryImpl;
import com.iqbal.repository.impl.RekamMedisObatRepositoryImpl;
import com.iqbal.repository.impl.RekamMedisRepositoryImpl;
import com.iqbal.repository.impl.ReservasiRepositoryImpl;
import com.iqbal.repository.impl.StaffRepositoryImpl;
import com.iqbal.service.AuthService;
import com.iqbal.service.DokterService;
import com.iqbal.service.ObatService;
import com.iqbal.service.PasienService;
import com.iqbal.service.RekamMedisService;
import com.iqbal.service.ReservasiService;
import com.iqbal.ui.AppContext;
import com.iqbal.ui.util.FxmlLoaderUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import javax.sql.DataSource;
import java.io.IOException;

public class App extends Application {

    private AppContext appContext;

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        DataSource dataSource = DataSourceProvider.get();
        try {
            FlywayRunner.migrate(dataSource);
        } catch (Exception e) {
            showFatalError("Gagal menyiapkan database: " + e.getMessage());
            Platform.exit();
            return;
        }

        appContext = buildAppContext(dataSource, stage);

        try {
            FxmlLoaderUtil.LoadResult<Object> login = FxmlLoaderUtil.load("/fxml/login.fxml", appContext);
            Scene scene = new Scene(login.root(), 420, 520);
            scene.getStylesheets().add(App.class.getResource("/css/app.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            showFatalError("Gagal memuat halaman login: " + e.getMessage());
            Platform.exit();
            return;
        }

        stage.setTitle(AppConfig.get().getAppName());
        stage.setOnCloseRequest(event -> stopResourceMonitor());
        stage.show();
    }

    @Override
    public void stop() {
        stopResourceMonitor();
        DataSourceProvider.close();
    }

    private AppContext buildAppContext(DataSource dataSource, Stage stage) {
        PasienRepositoryImpl pasienRepository = new PasienRepositoryImpl(dataSource);
        DokterRepositoryImpl dokterRepository = new DokterRepositoryImpl(dataSource);
        ObatRepositoryImpl obatRepository = new ObatRepositoryImpl(dataSource);
        ReservasiRepositoryImpl reservasiRepository = new ReservasiRepositoryImpl(dataSource);
        RekamMedisRepositoryImpl rekamMedisRepository = new RekamMedisRepositoryImpl(dataSource);
        RekamMedisObatRepositoryImpl rekamMedisObatRepository = new RekamMedisObatRepositoryImpl(dataSource);
        StaffRepositoryImpl staffRepository = new StaffRepositoryImpl(dataSource);

        PasienService pasienService = new PasienService(pasienRepository);
        DokterService dokterService = new DokterService(dokterRepository);
        ObatService obatService = new ObatService(obatRepository);
        ReservasiService reservasiService = new ReservasiService(reservasiRepository, pasienRepository, dokterRepository);
        RekamMedisService rekamMedisService = new RekamMedisService(
                dataSource, rekamMedisRepository, rekamMedisObatRepository, obatRepository, reservasiRepository);
        AuthService authService = new AuthService(staffRepository);

        return new AppContext(pasienService, dokterService, obatService, reservasiService,
                rekamMedisService, authService, stage);
    }

    private void stopResourceMonitor() {
        if (appContext != null && appContext.getResourceMonitorTimeline() != null) {
            appContext.getResourceMonitorTimeline().stop();
        }
    }

    private void showFatalError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Kesalahan Fatal");
        alert.showAndWait();
    }
}
