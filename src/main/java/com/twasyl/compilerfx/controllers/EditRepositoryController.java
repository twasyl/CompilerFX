package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.control.Dialog;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EditRepositoryController extends RepositoryController implements Initializable {

    private final ObjectProperty<MavenRepository> editedRepository = new SimpleObjectProperty<>(new MavenRepository());

    @FXML private void edit(ActionEvent event) {
        Dialog.Response response = Dialog.showConfirmDialog(null, "Edit repository", "Do you really want to apply modifications on this repository?");

        if(response == Dialog.Response.YES && checkRepositoryValidity()) {
            this.editedRepository.get().unbindAll();

            if(this.editedRepository.get().getPostBuildCommands() != null)
                this.editedRepository.get().setPostBuildCommands(this.editedRepository.get().getPostBuildCommands().replaceAll("\n", ""));


            Configuration.getInstance().getRepositories().remove(getRepository());
            Configuration.getInstance().getRepositories().add(this.editedRepository.get());

            getRepository().getWorkspace().getRepositories().remove(getRepository());
            this.editedRepository.get().getWorkspace().getRepositories().add(this.editedRepository.get());

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
        final BooleanProperty cleanProperty = new SimpleBooleanProperty();
        cleanProperty.bind(clean.selectedProperty());

        final BooleanProperty installProperty = new SimpleBooleanProperty();
        installProperty.bind(install.selectedProperty());

        this.editedRepository.get().repositoryNameProperty().bind(name.textProperty());
        this.editedRepository.get().pathProperty().bind(path.textProperty());
        this.editedRepository.get().optionsProperty().bind(options.textProperty());
        this.editedRepository.get().postBuildCommandsProperty().bind(postBuildCommands.textProperty());
        this.editedRepository.get().getGoals().put(new SimpleStringProperty(MavenRepository.Goal.CLEAN.getGoalName()), cleanProperty);
        this.editedRepository.get().getGoals().put(new SimpleStringProperty(MavenRepository.Goal.INSTALL.getGoalName()), installProperty);
        this.editedRepository.get().workspaceProperty().bind(workspace.valueProperty());

        this.repository.addListener(new ChangeListener<MavenRepository>() {
            @Override
            public void changed(final ObservableValue<? extends MavenRepository> observableValue, final MavenRepository oldRepository, final MavenRepository newRepository) {
                if (newRepository != null) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if(newRepository != null) {
                                editedRepository.get().setId(newRepository.getId());
                                editedRepository.get().setPriority(newRepository.getPriority());
                                editedRepository.get().setSelected(newRepository.isSelected());
                                editedRepository.get().setLastExecutionStack(newRepository.getLastExecutionStack());
                                editedRepository.get().setStatus(newRepository.getStatus());

                                name.setText(newRepository.getRepositoryName());
                                path.setText(newRepository.getPath());
                                options.setText(newRepository.getOptions());
                                postBuildCommands.setText(newRepository.getPostBuildCommands());

                                Boolean goalActive = newRepository.isGoalActive(MavenRepository.Goal.CLEAN);
                                clean.setSelected(goalActive == null ? false : goalActive);

                                goalActive = newRepository.isGoalActive(MavenRepository.Goal.INSTALL);
                                install.setSelected(goalActive == null ? false : goalActive);

                                workspace.setValue(newRepository.getWorkspace());
                            }
                        }
                    });
                }
            }
        });
    }
}
