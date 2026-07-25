package com.iqbal.ui.controller;

import com.iqbal.config.DataSourceProvider;
import com.zaxxer.hikari.HikariPoolMXBean;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Status bar pemantauan resource (memory JVM + Hikari connection pool).
 * Bukan controller ContextAware karena tidak butuh service layer sama sekali,
 * cukup baca langsung dari Runtime dan DataSourceProvider.
 */
public class StatusBarController {

    private static final long BYTES_PER_MB = 1024L * 1024L;

    @FXML private Label memoryLabel;
    @FXML private Label dbPoolLabel;

    private Timeline timeline;

    @FXML
    private void initialize() {
        refresh();
        timeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> refresh()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public Timeline getTimeline() {
        return timeline;
    }

    private void refresh() {
        Runtime runtime = Runtime.getRuntime();
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / BYTES_PER_MB;
        long maxMb = runtime.maxMemory() / BYTES_PER_MB;
        memoryLabel.setText("Memory: " + usedMb + "/" + maxMb + " MB");

        try {
            HikariPoolMXBean pool = DataSourceProvider.get().getHikariPoolMXBean();
            dbPoolLabel.setText("DB Pool: active " + pool.getActiveConnections()
                    + " / idle " + pool.getIdleConnections()
                    + " / total " + pool.getTotalConnections());
        } catch (Exception e) {
            dbPoolLabel.setText("DB Pool: n/a");
        }
    }
}
