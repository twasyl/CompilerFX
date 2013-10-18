package com.twasyl.compilerfx.control;

import com.twasyl.compilerfx.utils.FXMLLoader;
import com.twasyl.compilerfx.utils.UIUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Dialog extends Stage {

    public static enum Response {
        OK, CANCEL, YES, NO;
    }

    private Response userResponse;

    private Dialog(String title, Stage owner, Scene scene) {
        setTitle(title);
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UTILITY);
        setResizable(false);
        setScene(scene);
    }

    public void showDialog() {
        sizeToScene();
        centerOnScreen();

        if(Platform.isFxApplicationThread()) { showAndWait(); }
        else {
            FutureTask<Void> future = new FutureTask<Void>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    showAndWait();
                    return null;
                }
            });

            Platform.runLater(future);
            try {
                future.get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }
    }

    private Response getUserResponse() {
        return userResponse;
    }

    private void setUserResponse(Response userResponse) {
        this.userResponse = userResponse;
    }

    public static Response showDialog(Stage owner, String title, Node content) {
        final Button okButton = new Button(FXMLLoader.getResourceBundle().getString("button.ok"));

        final Dialog dialog = buildDialog(owner, title, content, okButton);

        setButtonAction(okButton, Response.OK, dialog);

        dialog.showDialog();

        return dialog.getUserResponse();
    }

    public static Response showConfirmDialog(Stage owner, String title, String message) {
        final Button yesButton = new Button(FXMLLoader.getResourceBundle().getString("button.yes"));
        yesButton.requestFocus();
        final Button noButton = new Button(FXMLLoader.getResourceBundle().getString("button.no"));

        final Text messageText = new Text(message);
        messageText.setWrappingWidth(300);
        messageText.setStyle("-fx-fill: white;");

        final Dialog dialog = buildDialog(owner, title, messageText, noButton, yesButton);

        setButtonAction(yesButton, Response.YES, dialog);
        setButtonAction(noButton, Response.NO, dialog);

        dialog.showDialog();

        return dialog.getUserResponse();
    }

    public static Response showErrorDialog(Stage owner, String title, String message) {
        final Button okButton = new Button(FXMLLoader.getResourceBundle().getString("button.ok"));

        final Text textMessage = new Text(message);
        textMessage.setStyle("-fx-fill: white; -fx-font-size: 15pt;");

        final ImageView image = new ImageView(new Image(UIUtils.class.getResource("/com/twasyl/compilerfx/images/error_white.png").toExternalForm()));

        final HBox root = new HBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().addAll(textMessage, image);

        final Dialog dialog = buildDialog(owner, title, root, okButton);

        setButtonAction(okButton, Response.OK, dialog);

        dialog.showDialog();

        return dialog.getUserResponse();
    }

    private static void setButtonAction(final Button button, final Response response, final Dialog dialog) {
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                dialog.setUserResponse(response);
                dialog.close();
            }
        });
    }

    private static Dialog buildDialog(final Stage owner, final String title, final Node content, final Button ... buttons) {
        final HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.BASELINE_RIGHT);
        buttonsBox.getChildren().addAll(buttons);

        final VBox dialogContent = new VBox(10);
        dialogContent.getChildren().addAll(content, buttonsBox);

        final Scene scene = UIUtils.createScene(dialogContent);

        Dialog dialog = null;
        if(Platform.isFxApplicationThread()) {
            dialog = new Dialog(title, owner, scene);
        } else {
            FutureTask<Dialog> future = new FutureTask<Dialog>(new Callable<Dialog>() {
                @Override
                public Dialog call() throws Exception {
                    return new Dialog(title, owner, scene);
                }
            });
            Platform.runLater(future);
            try {
                dialog = future.get();
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            }
        }

        return dialog;
    }
}
