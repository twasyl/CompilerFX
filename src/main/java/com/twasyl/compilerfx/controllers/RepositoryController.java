package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.beans.Workspace;
import com.twasyl.compilerfx.control.Dialog;
import com.twasyl.compilerfx.utils.MavenExecutor;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class RepositoryController {

    protected final ObjectProperty<MavenRepository> repository = new SimpleObjectProperty<>();
    @FXML protected TextField name;
    @FXML protected TextField path;
    @FXML protected CheckBox clean;
    @FXML protected CheckBox install;
    @FXML protected TextField options;
    @FXML protected TextArea postBuildCommands;
    @FXML protected ChoiceBox<Workspace> workspace;

    public ObjectProperty<MavenRepository> repositoryProperty() { return repository; }
    public MavenRepository getRepository() { return repository.get(); }

    public void setRepository(MavenRepository repository) {
        if (repository != null) {
            if(getRepository() == null) this.repositoryProperty().set(new MavenRepository());
            getRepository().duplicateFrom(repository);


            if ((repository.getRepositoryName() == null || repository.getRepositoryName().trim().isEmpty()) && repository.getPath() != null) {
                this.name.setText((new File(repository.getPath()).getName()));
            } else {
                this.name.setText(repository.getRepositoryName());
            }

            this.path.setText(repository.getPath());
            this.options.setText(repository.getOptions());
            this.postBuildCommands.setText(repository.getPostBuildCommands());
            this.workspace.setValue(repository.getWorkspace());

            BooleanProperty goal = repository.getGoals().get(new SimpleStringProperty(MavenRepository.Goal.CLEAN.getGoalName()));
            if (goal != null) clean.setSelected(goal.get());

            goal = repository.getGoals().get(new SimpleStringProperty(MavenRepository.Goal.INSTALL.getGoalName()));
            if (goal != null) install.setSelected(goal.get());
        }
    }



    @FXML protected void cancel(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/com/twasyl/compilerfx/fxml/MavenRepositories.fxml"));
            CompilerFXController.getCurrentInstance().switchScreen(parent);
        } catch (IOException e) {
        }
    }

    @FXML protected void browse(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        File directory = chooser.showDialog(null);

        if(directory != null) {
            path.setText(directory.getAbsolutePath());

            if(repository.get().getRepositoryName() == null ||
                    repository.get().getRepositoryName().trim().isEmpty()) {
                name.setText(directory.getName());
            }
        }
    }

    @FXML protected void showOptionsHelp(ActionEvent event) {
        final List<MavenRepository.MavenOption> options = MavenExecutor.getMavenOptions();

        final TableColumn<MavenRepository.MavenOption, String> option = new TableColumn<>("Option");
        option.setPrefWidth(100);
        option.setSortable(false);
        option.setCellValueFactory(new PropertyValueFactory<MavenRepository.MavenOption, String>("option"));

        final TableColumn<MavenRepository.MavenOption, String> optionName = new TableColumn<>("Option name");
        optionName.setPrefWidth(100);
        optionName.setSortable(false);
        optionName.setCellValueFactory(new PropertyValueFactory<MavenRepository.MavenOption, String>("optionName"));

        final TableColumn<MavenRepository.MavenOption, String> description = new TableColumn<>("Description");
        description.setPrefWidth(250);
        description.setSortable(false);
        description.setCellValueFactory(new PropertyValueFactory<MavenRepository.MavenOption, String>("description"));

        final TableView<MavenRepository.MavenOption> optionsTable = new TableView<>();
        optionsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        optionsTable.getColumns().addAll(option, optionName, description);
        optionsTable.getItems().addAll(options);
        optionsTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2 && mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                    MavenRepository.MavenOption selectedOption = optionsTable.getSelectionModel().getSelectedItem();

                    if(RepositoryController.this.options.getText() == null || RepositoryController.this.options.getText().isEmpty()) {
                        RepositoryController.this.options.setText(selectedOption.getOption());
                    } else {
                        RepositoryController.this.options.setText(
                                RepositoryController.this.options.getText() +
                                        " " + selectedOption.getOption());
                    }
                }
            }
        });

        Dialog.showDialog(null, "Options help", optionsTable);
    }

    protected final boolean checkRepositoryValidity() {
        File repositoryFolder = new File(this.repository.get().getPath().replaceAll("\\\\", "/"));
        boolean repositoryValid = true;

        /** Perform some checks */
        if(!repositoryFolder.exists()) {
            repositoryValid = false;
            Dialog.showErrorDialog(null, "Error", String.format("The repository '%1$s' does not exist", repositoryFolder.getAbsolutePath()));
        }

        if(repositoryValid) {
            File pom = new File(repositoryFolder, "pom.xml");

            if(!pom.exists()) {
                repositoryValid = false;
                Dialog.showErrorDialog(null, "Error", "Can not find pom.xml file in the repository");
            }
        }

        return repositoryValid;
    }
}
