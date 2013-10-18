package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.app.CompilerFXApp;
import com.twasyl.compilerfx.control.Dialog;
import com.twasyl.compilerfx.utils.FXMLLoader;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CompilerFXController implements Initializable {

    private static ObjectProperty<CompilerFXController> currentInstance = new SimpleObjectProperty<>();

    @FXML private BorderPane rootElement;
    @FXML private AnchorPane screenContent;

    public static ObjectProperty<CompilerFXController> currentInstanceProperty() { return currentInstance; }
    public static CompilerFXController getCurrentInstance() { return currentInstanceProperty().get(); }

    @FXML private void quitApplication(ActionEvent event) {
        CompilerFXApp.getCurrent().closeApplication();
    }

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

        final WebView webView = new WebView();
        webView.getEngine().load(getClass().getResource("/com/twasyl/compilerfx/help/help.html").toExternalForm());

        final Scene scene = new Scene(webView);
        final Stage stage = new Stage();
        stage.setTitle(FXMLLoader.getResourceBundle().getString("screen.help.dialog.title"));
        stage.setScene(scene);
        stage.show();
    }

    @FXML private void showAbout(ActionEvent event) {
        final StringBuilder changeLog = new StringBuilder();
        final String versionText = FXMLLoader.getResourceBundle().getString("screen.about.label.version");
        changeLog.append("Change logs:\n\n");
        changeLog.append(versionText + " 0.4.0:\n\n");
        changeLog.append("  - Fix verification of repository when editing a repository\n");
        changeLog.append("  - Help is in HTML, no more in markdown\n");
        changeLog.append("  - Allow to change UI language\n");
        changeLog.append("  - German and french languages added");
        changeLog.append("\n\n");
        changeLog.append(versionText + " 0.3.1:\n\n");
        changeLog.append("  - Fix commands execution on Windows platforms\n");
        changeLog.append("  - Edit button in edition screen for a repository is renamed to Save");
        changeLog.append("\n\n");
        changeLog.append(versionText + " 0.3.0:\n\n");
        changeLog.append("  - Abort feature for the current workspace and for all\n");
        changeLog.append("  - Feature for creating custom maven options\n");
        changeLog.append("  - Help button in the add/edit repository screen for adding maven options\n");
        changeLog.append("  - Add a File menu with a Quit item\n");
        changeLog.append("  - Add confirmation message for edition and deletion of repositories\n");
        changeLog.append("  - Add confirmation message for deletion of custom maven options\n");
        changeLog.append("  - Add confirmation message for deletion of workspaces\n");
        changeLog.append("  - Add confirmation message for renaming of workspaces\n");
        changeLog.append("  - Add error message for renaming workspaces with empty names");
        changeLog.append("\n\n");
        changeLog.append(versionText + " 0.2.0:\n\n");
        changeLog.append("  - Abort feature for the current workspace and for all\n");
        changeLog.append("  - Fix bug that blocked workspace's renaming when the workspace was freshly added\n");
        changeLog.append("  - Use dialogs in the whole application, even for error messages\n");
        changeLog.append("  - Ensure properties of workspaces and repositories are unbind when adding/editing them\n");
        changeLog.append("  - Correction of the label of the button for adding a repository (present in the toolbar)\n");
        changeLog.append("  - Buttons in dialogs are now positioned on the right of the dialog\n");
        changeLog.append("  - Possibility to use ENTER to add a workspace in the add screen\n");
        changeLog.append("  - Add change logs in About screen");

        final TextArea changeLogText = new TextArea(changeLog.toString());
        changeLogText.setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
        changeLogText.setWrapText(true);
        changeLogText.setPrefSize(500, 300);
        changeLogText.setMaxSize(500, 300);
        changeLogText.setMinSize(500, 300);


        final VBox helpContent = new VBox(10);
        helpContent.setAlignment(Pos.CENTER);
        helpContent.getChildren().addAll(
                new Label("CompilerFX"),
                new Label(versionText + " " + CompilerFXApp.version),
                new Label(FXMLLoader.getResourceBundle().getString("screen.about.label.author") + " Thierry Wasylczenko"),
                changeLogText
        );

        Dialog.showDialog(null, FXMLLoader.getResourceBundle().getString("dialog.title.about"), helpContent);
    }

    public void switchScreen(final Node to) {

        final Node currentScreen = screenContent.getChildren().isEmpty() ? null : screenContent.getChildren().get(0);

        to.setOpacity(0);

        final FadeTransition fadeIn = new FadeTransition(new Duration(500), to);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = null;

        if(currentScreen != null) {
            fadeOut = new FadeTransition(new Duration(500), currentScreen);
            fadeOut.setToValue(0);
        }

        screenContent.getChildren().add(to);

        ParallelTransition effect = new ParallelTransition();
        if(currentScreen != null) {
            effect.getChildren().addAll(fadeIn, fadeOut);
        } else {
            effect.getChildren().add(fadeIn);
        }

        effect.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(currentScreen != null) screenContent.getChildren().remove(currentScreen);
            }
        });

        effect.play();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentInstance.set(this);
    }
}
