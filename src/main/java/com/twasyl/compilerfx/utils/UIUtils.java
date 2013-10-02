package com.twasyl.compilerfx.utils;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class UIUtils {

    private final static StringProperty styleSheet = new SimpleStringProperty(UIUtils.class.getResource("/com/twasyl/compilerfx/css/default.css").toExternalForm());

    public static StringProperty styleSheetProperty() { return styleSheet; }
    public static String getStyleSheet() { return styleSheetProperty().get(); }
    public static void setStyleSheet(String styleSheet) { styleSheetProperty().set(styleSheet); }

    public static void showErrorScreen(String message) {

        final Text textMessage = new Text(message);
        textMessage.setStyle("-fx-fill: white; -fx-font-size: 15pt;");

        final ImageView image = new ImageView(new Image(UIUtils.class.getResource("/com/twasyl/compilerfx/images/error_white.png").toExternalForm()));

        final HBox root = new HBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(textMessage, image);

        if(!Platform.isFxApplicationThread()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    final Scene scene = createScene(root);

                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.setTitle("Error");
                    stage.show();
                }
            });
        } else {
            final Scene scene = createScene(root);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Error");
            stage.show();
        }
    }

    public static Scene createScene(Parent parent) {
        final Scene scene = new Scene(parent);
        scene.getStylesheets().add(getStyleSheet());

        return scene;
    }
}
