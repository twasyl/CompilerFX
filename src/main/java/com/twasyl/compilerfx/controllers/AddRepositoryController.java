package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.control.Dialog;
import com.twasyl.compilerfx.enums.Status;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AddRepositoryController extends RepositoryController implements Initializable {

    @FXML private void add(ActionEvent event) {

        if(checkRepositoryValidity()) {
            getRepository().unbindAll();

            getRepository().setId((int) System.currentTimeMillis());
            getRepository().setStatus(Status.READY);
            getRepository().postBuildCommandsProperty().unbind();

            if(getRepository().getPostBuildCommands() != null)
                getRepository().setPostBuildCommands(getRepository().getPostBuildCommands().replaceAll("\n", ""));

            if(Configuration.getInstance().getRepositories().isEmpty()) {
                getRepository().setPriority(1);
            } else {
                getRepository().setPriority(ConfigurationWorker.getNextAvailablePriority());
            }

            getRepository().getWorkspace().getRepositories().add(getRepository());

            Configuration.getInstance().getRepositories().add(getRepository());

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
        this.workspace.getSelectionModel().selectFirst();

        if(getRepository() == null) repository.set(new MavenRepository());

        final BooleanProperty cleanProperty = new SimpleBooleanProperty();
        cleanProperty.bind(clean.selectedProperty());

        final BooleanProperty installProperty = new SimpleBooleanProperty();
        installProperty.bind(install.selectedProperty());

        getRepository().repositoryNameProperty().bind(name.textProperty());
        getRepository().pathProperty().bind(path.textProperty());
        getRepository().optionsProperty().bind(options.textProperty());
        getRepository().postBuildCommandsProperty().bind(postBuildCommands.textProperty());
        getRepository().workspaceProperty().bind(workspace.valueProperty());
        getRepository().getGoals().put(new SimpleStringProperty(MavenRepository.Goal.CLEAN.getGoalName()), cleanProperty);
        getRepository().getGoals().put(new SimpleStringProperty(MavenRepository.Goal.INSTALL.getGoalName()), installProperty);
    }
}
