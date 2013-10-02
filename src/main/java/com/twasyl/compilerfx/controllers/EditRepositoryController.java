package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.beans.Workspace;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import com.twasyl.compilerfx.utils.UIUtils;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class EditRepositoryController implements Initializable {

    private final ObjectProperty<MavenRepository> originalRepository = new SimpleObjectProperty<>();
    private final ObjectProperty<MavenRepository> editedRepository = new SimpleObjectProperty<>(new MavenRepository());

    @FXML private TextField name;
    @FXML private TextField path;
    @FXML private CheckBox clean;
    @FXML private CheckBox install;
    @FXML private TextField options;
    @FXML private TextArea postBuildCommands;
    @FXML private ChoiceBox<Workspace> workspace;

    public ObjectProperty<MavenRepository> originalRepositoryProperty() { return this.originalRepository; }
    public MavenRepository getOriginalRepository() { return this.originalRepositoryProperty().get(); }
    public void setOriginalRepository(MavenRepository originalRepository) { this.originalRepositoryProperty().set(originalRepository); }

    @FXML private void browse(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        File directory = chooser.showDialog(null);

        if(directory != null) {
            path.setText(directory.getAbsolutePath());

            if(getOriginalRepository().getRepositoryName() == null ||
                    getOriginalRepository().getRepositoryName().trim().isEmpty()) {
                name.setText(directory.getName());
            }
        }
    }

    @FXML private void edit(ActionEvent event) {
        File repositoryFolder = new File(this.editedRepository.get().getPath().replaceAll("\\\\", "/"));
        boolean repositoryValid = true;

        /** Perform some checks */
        if(!repositoryFolder.exists()) {
            repositoryValid = false;
            UIUtils.showErrorScreen(String.format("The originalRepository '%1$s' does not exist", repositoryFolder.getAbsolutePath()));
        }

        if(repositoryValid) {
            File pom = new File(repositoryFolder, "pom.xml");

            if(!pom.exists()) {
                repositoryValid = false;
                UIUtils.showErrorScreen("Can not find pom.xml file in the originalRepository");
            }
        }

        if(repositoryValid) {
            this.editedRepository.get().workspaceProperty().unbind();

            this.editedRepository.get().pathProperty().unbind();
            this.editedRepository.get().setPath(this.editedRepository.get().getPath().replaceAll("\\\\", "/"));

            this.editedRepository.get().postBuildCommandsProperty().unbind();
            if(this.editedRepository.get().getPostBuildCommands() != null)
                this.editedRepository.get().setPostBuildCommands(this.editedRepository.get().getPostBuildCommands().replaceAll("\n", ""));


            Configuration.getInstance().getRepositories().remove(getOriginalRepository());
            Configuration.getInstance().getRepositories().add(this.editedRepository.get());

            getOriginalRepository().getWorkspace().getRepositories().remove(getOriginalRepository());
            this.editedRepository.get().getWorkspace().getRepositories().add(this.editedRepository.get());

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

        this.originalRepository.addListener(new ChangeListener<MavenRepository>() {
            @Override
            public void changed(final ObservableValue<? extends MavenRepository> observableValue, final MavenRepository oldRepository, final MavenRepository newRepository) {
                if (newRepository != null) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            editedRepository.get().setId(newRepository.getId());
                            editedRepository.get().setPriority(newRepository.getPriority());
                            editedRepository.get().setSelected(newRepository.isSelected());
                            editedRepository.get().setLastExecutionStack(newRepository.getLastExecutionStack());
                            editedRepository.get().setStatus(newRepository.getStatus());

                            name.setText(newRepository.getRepositoryName());
                            path.setText(newRepository.getPath());
                            options.setText(newRepository.getOptions());
                            postBuildCommands.setText(newRepository.getPostBuildCommands());
                            clean.setSelected(newRepository.isGoalActive(MavenRepository.Goal.CLEAN));
                            install.setSelected(newRepository.isGoalActive(MavenRepository.Goal.INSTALL));
                            workspace.setValue(newRepository.getWorkspace());
                        }
                    });
                }
            }
        });
    }
}
