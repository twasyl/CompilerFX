package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.Workspace;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DeleteWorkspaceController implements Initializable {

    @FXML private ChoiceBox<Workspace> workspaces;

    private final ObjectProperty<Stage> stage = new SimpleObjectProperty<>();
    private final ObjectProperty<Workspace> workspaceToDelete = new SimpleObjectProperty<>();

    public ObjectProperty<Stage> stageProperty() { return this.stage; }
    public Stage getStage() { return this.stageProperty().get(); }
    public void setStage(Stage stage) { this.stageProperty().set(stage); }

    public ObjectProperty<Workspace> workspaceToDeleteProperty() { return this.workspaceToDelete; }
    public Workspace getWorkspaceToDelete() { return this.workspaceToDelete.get(); }
    public void setWorkspaceToDelete(Workspace workspaceToDelete) { this.workspaceToDeleteProperty().set(workspaceToDelete); }

    @FXML private void delete(ActionEvent event) {
        Configuration.getInstance().getWorkspaces().remove(getWorkspaceToDelete());

        if(workspaces.getValue() == null) {
            Configuration.getInstance().getRepositories().removeAll(getWorkspaceToDelete().getRepositories());
        } else {
            workspaces.getValue().getRepositories().addAll(getWorkspaceToDelete().getRepositories());
        }

        ConfigurationWorker.save();

        if(getStage() != null) getStage().close();
    }

    @FXML private void cancel(ActionEvent event) {
        if(getStage() != null) getStage().close();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        workspaceToDelete.addListener(new ChangeListener<Workspace>() {
            @Override
            public void changed(ObservableValue<? extends Workspace> observableValue, Workspace oldWorkspace, Workspace newWorkspace) {
                if(newWorkspace != null) {

                    ObservableList<Workspace> items = FXCollections.observableArrayList();
                    items.addAll(null);

                    for(Workspace workspace : Configuration.getInstance().getWorkspaces()) {
                        if(workspace.getId() != newWorkspace.getId()) {
                            items.add(workspace);
                        }
                    }

                    workspaces.setItems(items);
                }
            }
        });
    }
}
