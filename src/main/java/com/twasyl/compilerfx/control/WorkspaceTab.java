package com.twasyl.compilerfx.control;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.beans.Workspace;
import com.twasyl.compilerfx.controllers.DeleteWorkspaceController;
import com.twasyl.compilerfx.controllers.WorkspaceController;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import com.twasyl.compilerfx.utils.UIUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class WorkspaceTab extends Tab {

    public WorkspaceTab() {
        super();

        final ContextMenu menu = new ContextMenu();

        final MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                if (getWorkspace() != null && getWorkspace().getRepositories().isEmpty()) {
                    Configuration.getInstance().getWorkspaces().remove(getWorkspace());
                    ConfigurationWorker.save();
                } else if (getWorkspace() != null && !getWorkspace().getRepositories().isEmpty()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/twasyl/compilerfx/fxml/DeleteWorkspace.fxml"));

                    try {
                        final Parent root = (Parent) loader.load();
                        final DeleteWorkspaceController controller = loader.getController();
                        controller.setWorkspaceToDelete(getWorkspace());

                        final Scene scene = UIUtils.createScene(root);
                        final Stage stage = new Stage();
                        stage.setTitle("Delete workspace");
                        stage.setScene(scene);
                        stage.show();
                        controller.setStage(stage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        final MenuItem rename = new MenuItem("Rename");
        rename.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                final TextField field = new TextField(getWorkspace().getName());
                field.setPrefColumnCount(10);
                field.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent keyEvent) {
                        if(keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                            WorkspaceTab.this.setGraphic(null);
                            WorkspaceTab.this.setText(getWorkspace().getName());
                        } else if(keyEvent.getCode().equals(KeyCode.ENTER)) {
                            if(!field.getText().isEmpty()) {
                                getWorkspace().setName(field.getText());
                                ConfigurationWorker.save();
                                WorkspaceTab.this.setGraphic(null);
                                WorkspaceTab.this.setText(getWorkspace().getName());
                            }
                        }
                    }
                });

                WorkspaceTab.this.setGraphic(field);
                WorkspaceTab.this.setText(null);
            }
        });

        menu.getItems().addAll(rename, delete);

        setContextMenu(menu);
    }

    public WorkspaceTab(String s) {
        this();
        setText(s);
    }

    public WorkspaceTab(String s, WorkspaceController controller) {
        this(s);
        setUserData(controller);
    }

    public Workspace getWorkspace() {
        Workspace result = null;

        if(getUserData() != null && getUserData() instanceof WorkspaceController) {
            result = ((WorkspaceController) getUserData()).getWorkspace();
        }

        return result;
    }

    public ObservableList<MavenRepository> getSelectedRepositories() {
        ObservableList<MavenRepository> results = null;

        if(getUserData() != null && getUserData() instanceof WorkspaceController) {
            results = ((WorkspaceController) getUserData()).getSelectedRepositories();
        }

        return results;
    }
}
