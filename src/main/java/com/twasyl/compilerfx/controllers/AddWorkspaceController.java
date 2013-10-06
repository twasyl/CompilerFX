package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.Workspace;
import com.twasyl.compilerfx.control.Dialog;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import com.twasyl.compilerfx.utils.UIUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AddWorkspaceController implements Initializable {

    private final ObjectProperty<Stage> stage = new SimpleObjectProperty<>();
    private Workspace workspace;

    @FXML private TextField name;

    @FXML private void add(ActionEvent event) {
        addWorkspace();
    }

    @FXML private void keyPressedOnName(KeyEvent event) {
        if(event.getCode().equals(KeyCode.ENTER)) addWorkspace();
    }

    @FXML private void cancel(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/com/twasyl/compilerfx/fxml/MavenRepositories.fxml"));
            CompilerFXController.getCurrentInstance().switchScreen(parent);
        } catch (IOException e) {
        }
    }

    private void addWorkspace() {
        if(this.workspace.getName().trim().isEmpty()) {
            Dialog.showErrorDialog(null, "Error", "The name of the workspace can not be empty");
        } else {
            this.workspace.setId(System.currentTimeMillis());
            this.workspace.setActive(false);

            this.workspace.unbindAll();

            Configuration.getInstance().getWorkspaces().add(this.workspace);
            ConfigurationWorker.save();

            try {
                Parent parent = FXMLLoader.load(getClass().getResource("/com/twasyl/compilerfx/fxml/MavenRepositories.fxml"));
                CompilerFXController.getCurrentInstance().switchScreen(parent);
            } catch (IOException e) {
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.workspace = new Workspace();

        this.workspace.nameProperty().bind(this.name.textProperty());
    }
}
