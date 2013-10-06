package com.twasyl.compilerfx.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class UIUtils {

    private final static StringProperty styleSheet = new SimpleStringProperty(UIUtils.class.getResource("/com/twasyl/compilerfx/css/default.css").toExternalForm());

    public static StringProperty styleSheetProperty() { return styleSheet; }
    public static String getStyleSheet() { return styleSheetProperty().get(); }
    public static void setStyleSheet(String styleSheet) { styleSheetProperty().set(styleSheet); }

    public static Scene createScene(Parent parent) {
        final Scene scene = new Scene(parent);
        scene.getStylesheets().add(getStyleSheet());

        return scene;
    }
}
