package coat.view.graphic;

import coat.Coat;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class MemoryPane extends StackPane {

    private final ProgressBar bar = new ProgressBar();
    private final Label info = new Label();

    public MemoryPane(){
        getChildren().setAll(bar, info);
        setPadding(new Insets(5));
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateRamUsage();
                if (Coat.getStage() != null && !Coat.getStage().isShowing()) {
                    timer.cancel();
                }
            }
        }, 1000, 2000);
        setOnMouseClicked(event -> Runtime.getRuntime().gc());
    }

    private void updateRamUsage() {
        final double freeMegas = Runtime.getRuntime().freeMemory() * 0.000000954;
        final double maxMegas = Runtime.getRuntime().maxMemory() * 0.000000954;
        final double progress = freeMegas / maxMegas;
        Platform.runLater(() -> {
            bar.setProgress(progress);
            info.setText(String.format("%.0f/%.0f(%.0f%%)", freeMegas, maxMegas, progress * 100.0));
        });

    }
}
