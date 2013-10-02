package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.beans.Workspace;
import com.twasyl.compilerfx.enums.Status;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import com.twasyl.compilerfx.utils.UIUtils;
import javafx.beans.property.*;
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
import java.util.Iterator;
import java.util.ResourceBundle;

public class AddRepositoryController implements Initializable {

    private MavenRepository repository;
    @FXML private TextField name;
    @FXML private TextField path;
    @FXML private CheckBox clean;
    @FXML private CheckBox install;
    @FXML private TextField options;
    @FXML private TextArea postBuildCommands;
    @FXML private ChoiceBox<Workspace> workspace;

    @FXML private void browse(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        File directory = chooser.showDialog(null);

        if(directory != null) {
            path.setText(directory.getAbsolutePath());

            if(repository.getRepositoryName() == null ||
                    repository.getRepositoryName().trim().isEmpty()) {
                name.setText(directory.getName());
            }
        }
    }

    @FXML private void add(ActionEvent event) {

        File repositoryFolder = new File(this.repository.getPath().replaceAll("\\\\", "/"));
        boolean repositoryValid = true;

        /** Perform some checks */
        if(!repositoryFolder.exists()) {
            repositoryValid = false;
            UIUtils.showErrorScreen(String.format("The repository '%1$s' does not exist", repositoryFolder.getAbsolutePath()));
        }

        if(repositoryValid) {
            File pom = new File(repositoryFolder, "pom.xml");

            if(!pom.exists()) {
                repositoryValid = false;
                UIUtils.showErrorScreen("Can not find pom.xml file in the repository");
            }
        }

        if(repositoryValid) {
            this.repository.setId((int) System.currentTimeMillis());
            this.repository.setStatus(Status.READY);
            this.repository.postBuildCommandsProperty().unbind();

            if(this.repository.getPostBuildCommands() != null)
                this.repository.setPostBuildCommands(this.repository.getPostBuildCommands().replaceAll("\n", ""));

            if(Configuration.getInstance().getRepositories().isEmpty()) {
                this.repository.setPriority(1);
            } else {
                this.repository.setPriority(ConfigurationWorker.getNextAvailablePriority());
            }

            this.repository.pathProperty().unbind();
            this.repository.setPath(this.repository.getPath().replaceAll("\\\\", "/"));

            this.repository.workspaceProperty().unbind();
            this.repository.getWorkspace().getRepositories().add(this.repository);

            Configuration.getInstance().getRepositories().add(this.repository);

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

    public void setRepository(MavenRepository repository) {
        if(repository != null) {
            if(repository.getRepositoryName() == null || repository.getRepositoryName().trim().isEmpty()) {
                this.name.setText((new File(repository.getPath()).getName()));
            } else {
                this.name.setText(repository.getRepositoryName());
            }

            this.path.setText(repository.getPath());
            this.options.setText(repository.getOptions());
            this.postBuildCommands.setText(repository.getPostBuildCommands());
            this.workspace.setValue(repository.getWorkspace());

            BooleanProperty goal = repository.getGoals().get(new SimpleStringProperty(MavenRepository.Goal.CLEAN.getGoalName()));
            if(goal != null) clean.setSelected(goal.get());

            goal = repository.getGoals().get(new SimpleStringProperty(MavenRepository.Goal.INSTALL.getGoalName()));
            if(goal != null) install.setSelected(goal.get());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.repository = new MavenRepository();

        final BooleanProperty cleanProperty = new SimpleBooleanProperty();
        cleanProperty.bind(clean.selectedProperty());

        final BooleanProperty installProperty = new SimpleBooleanProperty();
        installProperty.bind(install.selectedProperty());

        this.repository.repositoryNameProperty().bind(name.textProperty());
        this.repository.pathProperty().bind(path.textProperty());
        this.repository.optionsProperty().bind(options.textProperty());
        this.repository.postBuildCommandsProperty().bind(postBuildCommands.textProperty());
        this.repository.workspaceProperty().bind(workspace.valueProperty());
        this.repository.getGoals().put(new SimpleStringProperty(MavenRepository.Goal.CLEAN.getGoalName()), cleanProperty);
        this.repository.getGoals().put(new SimpleStringProperty(MavenRepository.Goal.INSTALL.getGoalName()), installProperty);
    }
}
