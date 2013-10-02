package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.app.CompilerFXApp;
import com.twasyl.compilerfx.control.Dialog;
import com.twasyl.compilerfx.utils.UIUtils;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.markdown4j.Markdown4jProcessor;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CompilerFXController implements Initializable {

    private static ObjectProperty<CompilerFXController> currentInstance = new SimpleObjectProperty<>();

    @FXML private BorderPane rootElement;
    @FXML private AnchorPane screenContent;

    public static ObjectProperty<CompilerFXController> currentInstanceProperty() { return currentInstance; }
    public static CompilerFXController getCurrentInstance() { return currentInstanceProperty().get(); }

    @FXML private void showConfigurationScreen(ActionEvent event) {

        try {
            final Parent parent = FXMLLoader.load(getClass().getResource("/com/twasyl/compilerfx/fxml/ConfigurationScreen.fxml"));
            switchScreen(parent);
        } catch (IOException e) {
        }

    }

    @FXML private void showAddWorkspaceScreen(ActionEvent event) {
        try {
            final Parent root = FXMLLoader.load(getClass().getResource("/com/twasyl/compilerfx/fxml/AddWorkspace.fxml"));
            switchScreen(root);
        } catch (IOException e) {
        }
    }

    @FXML private void showHelp(ActionEvent event) {
        try {
            final String help = new Markdown4jProcessor().process(getClass().getResourceAsStream("/com/twasyl/compilerfx/help/README.md"));

            final WebView webView = new WebView();
            webView.getEngine().loadContent(help);

            final Scene scene = new Scene(webView);
            final Stage stage = new Stage();
            stage.setTitle("Help");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
        }
    }

    @FXML private void showAbout(ActionEvent event) {
        final VBox helpContent = new VBox(10);
        helpContent.setAlignment(Pos.CENTER);
        helpContent.getChildren().addAll(
                new Label("CompilerFX"),
                new Label("version " + CompilerFXApp.version),
                new Label("Author: Thierry Wasylczenko")
        );

        Dialog.showDialog(null, "About", helpContent);
    }

    public void switchScreen(final Node to) {

        final Node currentScreen = screenContent.getChildren().get(0);

        to.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(new Duration(500), to);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(new Duration(500), currentScreen);
        fadeOut.setToValue(0);

        screenContent.getChildren().add(to);

        ParallelTransition effect = new ParallelTransition();
        effect.getChildren().addAll(fadeIn, fadeOut);
        effect.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                screenContent.getChildren().remove(currentScreen);
            }
        });

        effect.play();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentInstance.set(this);
    }
}
