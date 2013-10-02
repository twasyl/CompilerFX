package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.beans.Workspace;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import com.twasyl.compilerfx.utils.UIUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class WorkspaceController implements Initializable {

    private final ObjectProperty<Workspace> workspace = new SimpleObjectProperty<>();
    @FXML private TableView<MavenRepository> repositoriesTable;
    @FXML private CheckBox compileAll;

    public ObjectProperty<Workspace> workspaceProperty() { return this.workspace; }
    public Workspace getWorkspace() { return this.workspaceProperty().get(); }
    public void setWorkspace(Workspace workspace) { this.workspaceProperty().set(workspace); }

    @FXML private void displayExecutionResult(ActionEvent event) {
        final Button button = (Button) event.getSource();
        final MavenRepository repository = (MavenRepository) button.getUserData();
        final TextArea stack = new TextArea();

        stack.setPrefSize(800, 600);
        stack.setEditable(false);
        stack.textProperty().bind(repository.lastExecutionStackProperty());
        Scene scene = new Scene(stack);

        Stage stage = new Stage();
        stage.setTitle(String.format("Output of %1$s", repository.getRepositoryName()));
        stage.setScene(scene);
        stage.show();

        stage.showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                stack.textProperty().unbind();
            }
        });
    }

    @FXML private void onDragOver(DragEvent event) {
        if(event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }

        event.consume();
    }

    @FXML private void onDragDropped(DragEvent event) {
        Dragboard board = event.getDragboard();

        if(board.hasFiles()) {

            boolean repositoryValid = true;
            File repositoryFolder = new File(board.getFiles().get(0).getAbsolutePath().replaceAll("%20", " "));

            /** Perform some checks */
            if(!repositoryFolder.exists()) {
                repositoryValid = false;
                UIUtils.showErrorScreen(String.format("The repository '%1$s' does not exist", repositoryFolder.getAbsolutePath()));
            }

            if(repositoryValid && repositoryFolder.isFile()) {
                repositoryValid = false;
                UIUtils.showErrorScreen("The repository must be a folder");
            }

            if(repositoryValid && !(new File(repositoryFolder, "pom.xml")).exists()) {
                repositoryValid = false;
                UIUtils.showErrorScreen("Can not find pom.xml file in the repository");
            }

            if(repositoryValid) {
                MavenRepository repository = new MavenRepository();
                repository.setPath(repositoryFolder.getAbsolutePath());
                repository.setRepositoryName(repositoryFolder.getName());
                repository.setWorkspace(getWorkspace());
                repository.getGoals().put(new SimpleStringProperty(MavenRepository.Goal.CLEAN.getGoalName()), new SimpleBooleanProperty(true));
                repository.getGoals().put(new SimpleStringProperty(MavenRepository.Goal.INSTALL.getGoalName()), new SimpleBooleanProperty(true));

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/twasyl/compilerfx/fxml/AddRepository.fxml"));

                try {
                    Parent root = (Parent) loader.load();
                    AddRepositoryController controller = loader.getController();
                    controller.setRepository(repository);

                    CompilerFXController.getCurrentInstance().switchScreen(root);
                } catch (IOException e) {
                }
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
        }

        event.consume();
    }

    public ObservableList<MavenRepository> getSelectedRepositories() {
        return this.repositoriesTable.getSelectionModel().getSelectedItems();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final SetChangeListener<MavenRepository> changeListener = new SetChangeListener<MavenRepository>() {
            @Override
            public void onChanged(Change<? extends MavenRepository> change) {
                if(change.wasAdded()) {
                    repositoriesTable.getItems().clear();
                    repositoriesTable.getItems().addAll(change.getSet());
                }
                else if(change.wasRemoved()) repositoriesTable.getItems().remove(change.getElementRemoved());
            }
        };

        this.workspaceProperty().addListener(new ChangeListener<Workspace>() {
            @Override
            public void changed(ObservableValue<? extends Workspace> observableValue, Workspace workspace, Workspace workspace2) {
                if(workspace != null) {
                    workspace.repositoriesProperty().removeListener(changeListener);
                }

                if(workspace2 != null) {
                    repositoriesTable.getItems().addAll(workspace2.repositoriesProperty().get());
                    workspace2.repositoriesProperty().addListener(changeListener);

                    boolean allSelected = true;
                    for(MavenRepository repository : workspace2.getRepositories()) {
                        if(!repository.isSelected()) {
                            allSelected = false;
                            break;
                        }
                    }

                    compileAll.setSelected(allSelected);
                }
            }
        });

        this.compileAll.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if(getWorkspace() != null) {
                    for(MavenRepository repository : getWorkspace().getRepositories()) {
                        repository.setSelected(newValue);
                    }

                    ConfigurationWorker.save();
                }
            }
        });

        this.repositoriesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
}
