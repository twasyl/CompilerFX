package com.twasyl.compilerfx.control;

import com.twasyl.compilerfx.app.CompilerFXApp;
import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.beans.Workspace;
import com.twasyl.compilerfx.controllers.CompilerFXController;
import com.twasyl.compilerfx.controllers.EditRepositoryController;
import com.twasyl.compilerfx.enums.Status;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import com.twasyl.compilerfx.utils.MavenExecutor;
import com.twasyl.compilerfx.utils.WorkspaceStringConverter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.IOException;
import java.util.Map;

public class MavenRepositoryTableRowFactory implements Callback<TableView<MavenRepository>, TableRow<MavenRepository>> {

    @Override
    public TableRow<MavenRepository> call(TableView<MavenRepository> mavenRepositoryTableView) {
        final TableRow<MavenRepository> row = new TableRow<>();
        final ContextMenu menu = new ContextMenu();

        final CustomMenuItem moveTo = new CustomMenuItem();

        final Label moveToLabel = new Label("Move to");

        final ChoiceBox<Workspace> moveToChoiceBox = new ChoiceBox<>();
        moveToChoiceBox.setConverter(new WorkspaceStringConverter());
        moveToChoiceBox.valueProperty().addListener(new ChangeListener<Workspace>() {
            @Override
            public void changed(ObservableValue<? extends Workspace> observableValue, Workspace workspace, Workspace workspace2) {
                if(row.getItem() != null) {
                    // Remove repository from current workspace
                    row.getItem().getWorkspace().getRepositories().remove(row.getItem());

                    // Add the repository to the new workspace
                    if(workspace2 != null) workspace2.getRepositories().add(row.getItem());

                    ConfigurationWorker.save();
                }

                menu.hide();
            }
        });

        final HBox moveToBox = new HBox(10);
        moveToBox.getChildren().addAll(moveToLabel, moveToChoiceBox);

        moveTo.setContent(moveToBox);

        final CustomMenuItem copyIn = new CustomMenuItem();

        final Label copyInLabel = new Label("Copy in");

        final ChoiceBox<Workspace> copyInChoiceBox = new ChoiceBox<>();
        copyInChoiceBox.setConverter(new WorkspaceStringConverter());
        copyInChoiceBox.valueProperty().addListener(new ChangeListener<Workspace>() {
            @Override
            public void changed(ObservableValue<? extends Workspace> observableValue, Workspace workspace, Workspace workspace2) {
                if(row.getItem() != null && workspace2 != null) {
                    MavenRepository copy = new MavenRepository();
                    copy.setId(System.currentTimeMillis());
                    copy.setPriority(ConfigurationWorker.getNextAvailablePriority());
                    copy.setRepositoryName(row.getItem().getRepositoryName());
                    copy.setPath(row.getItem().getPath());
                    copy.setOptions(row.getItem().getOptions());
                    copy.setPostBuildCommands(row.getItem().getPostBuildCommands());
                    copy.setStatus(Status.READY);
                    copy.setSelected(false);

                    for(Map.Entry<StringProperty, BooleanProperty> entry : row.getItem().getGoals().entrySet()) {
                        copy.getGoals().put(new SimpleStringProperty(entry.getKey().get()), new SimpleBooleanProperty(entry.getValue().get()));
                    }

                    workspace2.getRepositories().add(copy);

                    Configuration.getInstance().getRepositories().add(copy);
                    ConfigurationWorker.save();
                }

                menu.hide();
            }
        });

        final HBox copyInBox = new HBox(10);
        copyInBox.getChildren().addAll(copyInLabel, copyInChoiceBox);

        copyIn.setContent(copyInBox);

        final MenuItem edit = new MenuItem("Edit");
        edit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(row.getItem() != null) {

                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/twasyl/compilerfx/fxml/EditRepository.fxml"));

                        Parent parent = (Parent) loader.load();
                        EditRepositoryController controller = loader.getController();
                        controller.setRepository(row.getItem());

                        CompilerFXController.getCurrentInstance().switchScreen(parent);
                    } catch (IOException e) {
                    }
                }
            }
        });

        final MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(row.getItem() != null) {
                    Dialog.Response response = Dialog.showConfirmDialog(null,
                            "Delete repository?",
                            String.format("Are you sure you want to delete the repository '%1$s'?", row.getItem().getRepositoryName()));

                    if(response == Dialog.Response.YES) {
                        final MavenRepository repository =  row.getItem();
                        repository.getWorkspace().getRepositories().remove(repository);
                        Configuration.getInstance().getRepositories().remove(repository);
                        ConfigurationWorker.save();
                    }
                }
            }
        });

        final MenuItem compile = new MenuItem("Compile");
        compile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(row.getItem() != null) {
                    final MavenRepository repository = row.getItem();
                    MavenExecutor.execute(repository);
                }
            }
        });

        final MenuItem compileWithPostBuildCommands = new MenuItem("Compile & execute post build commands");
        compileWithPostBuildCommands.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(row.getItem() != null) {
                    final MavenRepository repository = row.getItem();
                    MavenExecutor.execute(repository, true);
                }
            }
        });

        final MenuItem abort = new MenuItem("Abort");
        abort.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(row.getItem() != null) {
                    final MavenRepository repository = row.getItem();

                    if(repository.getActiveProcess() != null) {
                        repository.setStatus(Status.ABORTED);
                        repository.getActiveProcess().destroy();
                    }
                }
            }
        });

        final MenuItem executePostBuildCommands = new MenuItem("Execute post build commands");
        executePostBuildCommands.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(row.getItem() != null) {
                    final MavenRepository repository =  row.getItem();
                    MavenExecutor.executePostBuildCommands(repository);
                }
            }
        });

        final MenuItem clearExecutionStack = new MenuItem("Clear execution result");
        clearExecutionStack.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(row.getItem() != null) {
                    final MavenRepository repository = row.getItem();
                    repository.setLastExecutionStack(null);
                }
            }
        });
        menu.getItems().addAll(compile, compileWithPostBuildCommands,
                executePostBuildCommands, abort,
                clearExecutionStack, new SeparatorMenuItem(),
                moveTo, copyIn, edit, delete);

        row.emptyProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                if(aBoolean2) row.setContextMenu(null);
                else row.setContextMenu(menu);
            }
        });

        row.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2
                        && mouseEvent.getButton().equals(MouseButton.PRIMARY)
                        && row.getItem() != null) {
                    final MavenRepository repository = row.getItem();
                    MavenExecutor.execute(repository);
                }
            }
        });

        row.itemProperty().addListener(new ChangeListener<MavenRepository>() {
            @Override
            public void changed(ObservableValue<? extends MavenRepository> observableValue, MavenRepository repository, MavenRepository repository2) {

                if(repository2 != null) {

                    if(executePostBuildCommands.disableProperty().isBound()) executePostBuildCommands.disableProperty().unbind();
                    executePostBuildCommands.disableProperty().bind(repository2.statusProperty().isNull()
                                                                .or(repository2.statusProperty().isEqualTo(Status.COMPILING))
                                                                .or(repository2.postBuildCommandsProperty().isNull())
                                                                .or(repository2.postBuildCommandsProperty().isEqualTo("")));

                    if(edit.disableProperty().isBound()) edit.disableProperty().unbind();
                    edit.disableProperty().bind(repository2.statusProperty().isNull().or(repository2.statusProperty().isEqualTo(Status.COMPILING)));

                    if(delete.disableProperty().isBound()) delete.disableProperty().unbind();
                    delete.disableProperty().bind(repository2.statusProperty().isNull().or(repository2.statusProperty().isEqualTo(Status.COMPILING)));

                    if(clearExecutionStack.disableProperty().isBound()) clearExecutionStack.disableProperty().unbind();
                    clearExecutionStack.disableProperty().bind(repository2.lastExecutionStackProperty().isNull().or(repository2.lastExecutionStackProperty().isEqualTo("")));

                    if(abort.disableProperty().isBound()) abort.disableProperty().unbind();
                    abort.disableProperty().bind(repository2.statusProperty().isNotEqualTo(Status.COMPILING).or(repository2.activeProcessProperty().isNull()));

                    moveToChoiceBox.getItems().clear();
                    copyInChoiceBox.getItems().clear();

                    for(Workspace workspace : Configuration.getInstance().getWorkspaces()) {
                        if(repository2.getWorkspace() != null && workspace.getId() != repository2.getWorkspace().getId()) {
                            moveToChoiceBox.getItems().add(workspace);
                            copyInChoiceBox.getItems().add(workspace);
                        }
                    }
                }
            }
        });

        return row;
    }
}
