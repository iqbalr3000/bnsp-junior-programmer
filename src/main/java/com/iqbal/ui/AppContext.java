package com.iqbal.ui;

import com.iqbal.service.AiAssistantService;
import com.iqbal.service.AuthService;
import com.iqbal.service.DokterService;
import com.iqbal.service.ObatService;
import com.iqbal.service.PasienService;
import com.iqbal.service.RekamMedisService;
import com.iqbal.service.ReservasiService;
import javafx.animation.Timeline;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;

/**
 * Composition root sederhana: memegang semua instance service dan Stage utama,
 * dipakai untuk dependency injection manual ke controller FXML lewat ContextAware.
 */
public class AppContext {

    private final PasienService pasienService;
    private final DokterService dokterService;
    private final ObatService obatService;
    private final ReservasiService reservasiService;
    private final RekamMedisService rekamMedisService;
    private final AuthService authService;
    private final AiAssistantService aiAssistantService;
    private final ExecutorService aiExecutor;
    private final Stage primaryStage;

    private Timeline resourceMonitorTimeline;

    public AppContext(PasienService pasienService,
                       DokterService dokterService,
                       ObatService obatService,
                       ReservasiService reservasiService,
                       RekamMedisService rekamMedisService,
                       AuthService authService,
                       AiAssistantService aiAssistantService,
                       ExecutorService aiExecutor,
                       Stage primaryStage) {
        this.pasienService = pasienService;
        this.dokterService = dokterService;
        this.obatService = obatService;
        this.reservasiService = reservasiService;
        this.rekamMedisService = rekamMedisService;
        this.authService = authService;
        this.aiAssistantService = aiAssistantService;
        this.aiExecutor = aiExecutor;
        this.primaryStage = primaryStage;
    }

    public PasienService getPasienService() {
        return pasienService;
    }

    public DokterService getDokterService() {
        return dokterService;
    }

    public ObatService getObatService() {
        return obatService;
    }

    public ReservasiService getReservasiService() {
        return reservasiService;
    }

    public RekamMedisService getRekamMedisService() {
        return rekamMedisService;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public AiAssistantService getAiAssistantService() {
        return aiAssistantService;
    }

    public ExecutorService getAiExecutor() {
        return aiExecutor;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Timeline getResourceMonitorTimeline() {
        return resourceMonitorTimeline;
    }

    public void setResourceMonitorTimeline(Timeline resourceMonitorTimeline) {
        this.resourceMonitorTimeline = resourceMonitorTimeline;
    }
}
