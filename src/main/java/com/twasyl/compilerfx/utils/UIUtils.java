package com.twasyl.compilerfx.utils;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UIUtils {

    private final static StringProperty styleSheet = new SimpleStringProperty(UIUtils.class.getResource("/com/twasyl/compilerfx/css/default.css").toExternalForm());

    public static StringProperty styleSheetProperty() { return styleSheet; }
    public static String getStyleSheet() { return styleSheetProperty().get(); }
    public static void setStyleSheet(String styleSheet) { styleSheetProperty().set(styleSheet); }

    public static Scene createScene(final Parent parent) {
        Scene scene = null;
        if(Platform.isFxApplicationThread()) {
            scene = new Scene(parent);
            scene.getStylesheets().add(getStyleSheet());
        } else {
            FutureTask<Scene> future = new FutureTask<Scene>(new Callable<Scene>() {
                @Override
                public Scene call() throws Exception {
                    Scene scene = new Scene(parent);
                    scene.getStylesheets().add(getStyleSheet());

                    return scene;
                }
            });

            Platform.runLater(future);
            try {
                scene = future.get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }

        return scene;
    }
}
