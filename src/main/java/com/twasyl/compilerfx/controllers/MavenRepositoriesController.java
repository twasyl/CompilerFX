package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.beans.Workspace;
import com.twasyl.compilerfx.control.Dialog;
import com.twasyl.compilerfx.control.WorkspaceTab;
import com.twasyl.compilerfx.enums.Status;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import com.twasyl.compilerfx.utils.MavenExecutor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import com.twasyl.compilerfx.utils.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

public class MavenRepositoriesController implements Initializable {

    @FXML private TabPane workspaces;
    @FXML private SplitMenuButton abortAll;

    @FXML private void compileSelection(ActionEvent event) {
        compileSelection(false);
    }

    @FXML private void compileAndExecutePostBuildCommandsOnSelection(ActionEvent event) {
        compileSelection(true);
    }

    @FXML private void reloadRepositories(ActionEvent event) {
        ConfigurationWorker.load();

        initUI();
    }

    @FXML private void deleteRepositories(ActionEvent event) {

        Dialog.Response response = Dialog.showConfirmDialog(null, "Delete selection?", "Are you sure you want to delete the selection?");

        if(response == Dialog.Response.YES) {
            WorkspaceTab selectedTab = (WorkspaceTab) this.workspaces.getSelectionModel().getSelectedItem();

            final List<MavenRepository> repositoriesToRemove = new ArrayList<>();
            boolean hasDoneModifications = false;

            for(MavenRepository repository : selectedTab.getSelectedRepositories()) {
                if(repository.getStatus() != Status.COMPILING) {
                    repositoriesToRemove.add(repository);
                }
            }

            selectedTab.getWorkspace().getRepositories().removeAll(repositoriesToRemove);
            hasDoneModifications = Configuration.getInstance().getRepositories().removeAll(repositoriesToRemove);

            if(hasDoneModifications) ConfigurationWorker.save();
        }
    }

    @FXML private void displayAddRepositoryScreen(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/com/twasyl/compilerfx/fxml/AddRepository.fxml"));
            CompilerFXController.getCurrentInstance().switchScreen(parent);
        } catch (IOException e) {
        }
    }

    @FXML private void abortWorkspaceBuilds(ActionEvent event) {
        Workspace workspace = ((WorkspaceTab) this.workspaces.getSelectionModel().getSelectedItem()).getWorkspace();
        Iterator<MavenRepository> builds = Configuration.getInstance().getCurrentBuilds().iterator();
        MavenRepository repository;

        while(builds.hasNext()) {
            repository = builds.next();

            synchronized (repository) {
                if(repository.getWorkspace().getId() == workspace.getId() && repository.getActiveProcess() != null) {
                    repository.setStatus(Status.ABORTED);
                    repository.getActiveProcess().destroy();
                }
            }
        }
    }

    @FXML private void abortAllWorkspacesBuilds(ActionEvent event) {
        Iterator<MavenRepository> builds = Configuration.getInstance().getCurrentBuilds().iterator();
        MavenRepository repository;

        while(builds.hasNext()) {
            repository = builds.next();

            synchronized (repository) {
                if(repository.getActiveProcess() != null) {
                    repository.setStatus(Status.ABORTED);
                    repository.getActiveProcess().destroy();
                }
            }
        }
    }

    private void compileSelection(final boolean executePostBuildCommands) {
        WorkspaceTab selectedTab = (WorkspaceTab) this.workspaces.getSelectionModel().getSelectedItem();
        ObservableList<MavenRepository> repos = FXCollections.observableArrayList();

        for(MavenRepository repository : selectedTab.getWorkspace().getRepositories()) {
            if(repository.isSelected()) {
                repos.add(repository);
            }
        }

        MavenExecutor.execute(repos, executePostBuildCommands);
    }

    private void initUI() {
        FXMLLoader loader = null;
        WorkspaceController workspaceController;
        Parent root;
        WorkspaceTab tab;

        this.workspaces.getTabs().clear();

        for(Workspace workspace : Configuration.getInstance().getWorkspaces()) {
            loader = new FXMLLoader(getClass().getResource("/com/twasyl/compilerfx/fxml/Workspace.fxml"));
            try {
                root = (Parent) loader.load();
                workspaceController = loader.getController();

                workspaceController.setWorkspace(workspace);

                tab = new WorkspaceTab(workspace.getName(), workspaceController);
                tab.setContent(root);
                tab.setClosable(false);

                this.workspaces.getTabs().add(tab);

                if(workspace.getActive()) workspaces.getSelectionModel().select(tab);

                workspace.activeProperty().bind(tab.selectedProperty());

            } catch (IOException e) {
            }
        }

        Configuration.getInstance().workspacesProperty().addListener(new SetChangeListener<Workspace>() {
            @Override
            public void onChanged(Change<? extends Workspace> change) {
                if(change.wasAdded()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/twasyl/compilerfx/fxml/Workspace.fxml"));;
                    try {
                        Parent root = (Parent) loader.load();

                        WorkspaceController workspaceController = loader.getController();
                        workspaceController.setWorkspace(change.getElementAdded());

                        WorkspaceTab tab = new WorkspaceTab(change.getElementAdded().getName(), workspaceController);
                        tab.setContent(root);
                        tab.setClosable(false);

                        workspaces.getTabs().add(tab);
                    } catch (IOException e) {
                    }
                } else if(change.wasRemoved()) {

                    Tab tabToRemove = null;
                    for(Tab  tab : workspaces.getTabs()) {
                        if(((WorkspaceTab) tab).getWorkspace().getId() == change.getElementRemoved().getId()) {
                            tabToRemove = tab;
                            break;
                        }
                    }

                    if(tabToRemove != null) workspaces.getTabs().remove(tabToRemove);
                }
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initUI();

        abortAll.disableProperty().bind(Configuration.getInstance().currentBuildsProperty().emptyProperty());
    }
}
