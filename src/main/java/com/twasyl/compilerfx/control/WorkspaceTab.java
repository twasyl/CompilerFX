package com.twasyl.compilerfx.control;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.beans.Workspace;
import com.twasyl.compilerfx.controllers.DeleteWorkspaceController;
import com.twasyl.compilerfx.controllers.WorkspaceController;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import com.twasyl.compilerfx.utils.FXMLLoader;
import com.twasyl.compilerfx.utils.UIUtils;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

        final MenuItem delete = new MenuItem(FXMLLoader.getResourceBundle().getString("control.workspacetab.contextmenu.item.delete"));
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                if (getWorkspace() != null && getWorkspace().getRepositories().isEmpty()) {
                    Dialog.Response response = Dialog.showConfirmDialog(null,
                            FXMLLoader.getResourceBundle().getString("control.workspacetab.dialog.title.deleteWorkspace"),
                            String.format(
                                    FXMLLoader.getResourceBundle().getString("control.workspacetab.message.info.confirmDeletion"),
                                    getWorkspace().getName()));

                    if(response == Dialog.Response.YES) {
                        Configuration.getInstance().getWorkspaces().remove(getWorkspace());
                        ConfigurationWorker.save();
                    }
                } else if (getWorkspace() != null && !getWorkspace().getRepositories().isEmpty()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/twasyl/compilerfx/fxml/DeleteWorkspace.fxml"));

                    try {
                        final Parent root = (Parent) loader.load();
                        final DeleteWorkspaceController controller = loader.getController();
                        controller.setWorkspaceToDelete(getWorkspace());

                        final Scene scene = UIUtils.createScene(root);
                        final Stage stage = new Stage();
                        stage.setTitle(FXMLLoader.getResourceBundle().getString("control.workspacetab.dialog.title.deleteWorkspace"));
                        stage.setScene(scene);
                        stage.show();
                        controller.setStage(stage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        final MenuItem rename = new MenuItem(FXMLLoader.getResourceBundle().getString("control.workspacetab.contextmenu.item.rename"));
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
                                Dialog.Response response = Dialog.showConfirmDialog(null,
                                        FXMLLoader.getResourceBundle().getString("control.workspacetab.dialog.title.renameWorkspace"),
                                        String.format(
                                                FXMLLoader.getResourceBundle().getString("control.workspacetab.message.info.confirmRenaming"),
                                                getWorkspace().getName(), field.getText()));

                                if(response == Dialog.Response.YES) {
                                    getWorkspace().setName(field.getText());
                                    ConfigurationWorker.save();
                                    WorkspaceTab.this.setGraphic(null);
                                    WorkspaceTab.this.setText(getWorkspace().getName());
                                }
                            } else {
                                Dialog.showErrorDialog(null,
                                        FXMLLoader.getResourceBundle().getString("control.workspacetab.dialog.title.renameWorkspace"),
                                        FXMLLoader.getResourceBundle().getString("control.workspacetab.message.error.emptyWorkspaceName"));
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
