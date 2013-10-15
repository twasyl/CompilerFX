package com.twasyl.compilerfx.app;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.control.Dialog;
import com.twasyl.compilerfx.enums.Status;
import com.twasyl.compilerfx.exceptions.MissingConfigurationFileException;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import com.twasyl.compilerfx.utils.UIUtils;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Iterator;

public class CompilerFXApp extends Application {

    public static String version = "0.3.0";
    private static final ReadOnlyObjectProperty<CompilerFXApp> current = new SimpleObjectProperty<>();
    private final ReadOnlyObjectProperty<Stage> currentStage = new SimpleObjectProperty<>();

    @Override
    public void start(final Stage stage) throws Exception {
        ((SimpleObjectProperty<CompilerFXApp>) CompilerFXApp.current).set(this);
        ((SimpleObjectProperty<Stage>) currentStage).set(stage);

        Parent root = FXMLLoader.load(getClass().getResource("/com/twasyl/compilerfx/fxml/CompilerFX.fxml"));

        final Scene scene = UIUtils.createScene(root);
        stage.setTitle("CompilerFX");
        stage.setScene(scene);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                // Only directly close the app if there are no current processes, otherwise ask what to do
                if (!Configuration.getInstance().getCurrentBuilds().isEmpty()) {
                    final String message = String.format("%1$s currently active. Abort %2$s?",
                            Configuration.getInstance().getCurrentBuilds().size() > 1 ? "Processes are" : "A process is",
                            Configuration.getInstance().getCurrentBuilds().size() > 1 ? "them" : "it"
                    );

                    if (Dialog.showConfirmDialog(stage, "Quit?", message) == Dialog.Response.YES) {
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

    private static final ReadOnlyObjectProperty<CompilerFXApp> currentProperty() { return current; }
    public static final CompilerFXApp getCurrent() { return currentProperty().get(); }

    public static void main(String[] args) {
        Application.launch(CompilerFXApp.class, args);
    }
}
