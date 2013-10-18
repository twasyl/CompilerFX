package com.twasyl.compilerfx.app;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.control.Dialog;
import com.twasyl.compilerfx.controllers.CompilerFXController;
import com.twasyl.compilerfx.enums.Status;
import com.twasyl.compilerfx.exceptions.MissingConfigurationFileException;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import com.twasyl.compilerfx.utils.FXMLLoader;
import com.twasyl.compilerfx.utils.UIUtils;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Iterator;

public class CompilerFXApp extends Application {

    public static String version = "0.4.0";
    private static final ReadOnlyObjectProperty<CompilerFXApp> current = new SimpleObjectProperty<>();
    private final ReadOnlyObjectProperty<Stage> currentStage = new SimpleObjectProperty<>();

    @Override
    public void start(final Stage stage) throws Exception {
        ((SimpleObjectProperty<CompilerFXApp>) CompilerFXApp.current).set(this);
        ((SimpleObjectProperty<Stage>) currentStage).set(stage);

        final Scene scene = loadFullUI((Parent) FXMLLoader.load(getClass().getResource("/com/twasyl/compilerfx/fxml/MavenRepositories.fxml")));
        stage.setTitle("CompilerFX");
        stage.setScene(scene);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                // Only directly close the app if there are no current processes, otherwise ask what to do
                if (!Configuration.getInstance().getCurrentBuilds().isEmpty()) {
                    final String message = Configuration.getInstance().getCurrentBuilds().size() > 1 ?
                            FXMLLoader.getResourceBundle().getString("application.message.info.activeProcesses") :
                            FXMLLoader.getResourceBundle().getString("application.message.info.activeProcess");

                    if (Dialog.showConfirmDialog(stage, FXMLLoader.getResourceBundle().getString("dialog.title.quit"), message) == Dialog.Response.YES) {
                        final Iterator<MavenRepository> processIterator = Configuration.getInstance().getCurrentBuilds().iterator();
                        MavenRepository repository;

                        while (processIterator.hasNext()) {
                            repository = processIterator.next();

                            synchronized (repository) {
                                repository.setStatus(Status.ABORTED);

                                if (repository.getActiveProcess() != null) repository.getActiveProcess().destroy();
                            }
                        }
                    } else {
                        windowEvent.consume();
                    }
                }
            }
        });
        stage.show();
    }

    @Override
    public void init() throws Exception {
        try {
            ConfigurationWorker.load();
        } catch (MissingConfigurationFileException ex) {
            Configuration.getInstance().getWorkspaces().add(ConfigurationWorker.createDefaultWorkpace());
        }
    }

    @Override
    public void stop() throws Exception {
        ConfigurationWorker.save();
    }

    public void closeApplication() {
        final WindowEvent event = new WindowEvent(currentStage.get().getOwner(), WindowEvent.WINDOW_CLOSE_REQUEST);
        currentStage.get().fireEvent(event);
    }

    public Scene loadFullUI(Parent screenContent) {
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/twasyl/compilerfx/fxml/CompilerFX.fxml"));

        try {
            Parent root = (Parent) loader.load();
            CompilerFXController controller = loader.getController();

            controller.switchScreen(screenContent);

            return UIUtils.createScene(root);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static final ReadOnlyObjectProperty<CompilerFXApp> currentProperty() { return current; }
    public static final CompilerFXApp getCurrent() { return currentProperty().get(); }

    public ReadOnlyObjectProperty<Stage> currentStageProperty() { return this.currentStage; }
    public Stage getCurrentStage() { return this.currentStageProperty().get(); }

    public static void main(String[] args) {
        Application.launch(CompilerFXApp.class, args);
    }
}
