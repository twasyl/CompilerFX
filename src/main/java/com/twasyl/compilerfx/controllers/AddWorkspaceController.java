package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.Workspace;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AddWorkspaceController implements Initializable {

    private final ObjectProperty<Stage> stage = new SimpleObjectProperty<>();
    private Workspace workspace;

    @FXML private TextField name;

    @FXML private void add(ActionEvent event) {
        if(this.workspace.getName().trim().isEmpty()) {
            UIUtils.showErrorScreen("The name of the workspace can not be empty");
        } else {
            this.workspace.setId(System.currentTimeMillis());
            this.workspace.setActive(false);

            Configuration.getInstance().getWorkspaces().add(this.workspace);
            ConfigurationWorker.save();

            try {
                Parent parent = FXMLLoader.load(getClass().getResource("/com/twasyl/compilerfx/fxml/MavenRepositories.fxml"));
                CompilerFXController.getCurrentInstance().switchScreen(parent);
            } catch (IOException e) {
            }
        }
    }

    @FXML private void cancel(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/com/twasyl/compilerfx/fxml/MavenRepositories.fxml"));
            CompilerFXController.getCurrentInstance().switchScreen(parent);
        } catch (IOException e) {
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.workspace = new Workspace();

        this.workspace.nameProperty().bind(this.name.textProperty());
    }
}