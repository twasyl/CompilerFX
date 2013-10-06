package com.twasyl.compilerfx.beans;

import com.twasyl.compilerfx.enums.Status;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MavenRepository implements PropertyBean {

    public static enum Goal {
        CLEAN("clean"), INSTALL("install");

        private String goalName;

        private Goal(String goalName) {
            this.goalName = goalName;
        }

        public String getGoalName() {
            return goalName;
        }
    }

    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty path = new SimpleStringProperty();
    private final StringProperty repositoryName = new SimpleStringProperty();
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final MapProperty<StringProperty, BooleanProperty> goals = new SimpleMapProperty<>(FXCollections.observableMap(new LinkedHashMap<StringProperty, BooleanProperty>()));
    private final StringProperty lastExecutionStack = new SimpleStringProperty();
    private final IntegerProperty priority = new SimpleIntegerProperty();
    private final StringProperty options = new SimpleStringProperty();
    private final StringProperty postBuildCommands = new SimpleStringProperty();
    private final ObjectProperty<Workspace> workspace = new SimpleObjectProperty<>();
    private final ObjectProperty<Process> activeProcess = new SimpleObjectProperty<>();

    public long getId() { return this.idProperty().get(); }
    public LongProperty idProperty() { return this.id; }
    public void setId(long id) { this.idProperty().set(id); }

    public final StringProperty pathProperty() { return this.path; }
    public final String getPath() { return this.pathProperty().get(); }
    public final void setPath(String path) { this.pathProperty().set(path); }

    public StringProperty repositoryNameProperty() { return this.repositoryName; }
    public String getRepositoryName() { return this.repositoryNameProperty().get(); }
    public void setRepositoryName(String repositoryName) { this.repositoryNameProperty().set(repositoryName); }

    public ObjectProperty<Status> statusProperty() { return this.status; }
    public Status getStatus() { return this.statusProperty().get(); }
    public void setStatus(Status status) { this.statusProperty().set(status); }

    public BooleanProperty selectedProperty() { return this.selected; }
    public boolean isSelected() { return this.selectedProperty().get(); }
    public void setSelected(boolean selected) { this.selectedProperty().set(selected); }

    public MapProperty<StringProperty, BooleanProperty> goalsProperty() { return this.goals; }
    public ObservableMap<StringProperty,BooleanProperty> getGoals() { return this.goalsProperty().get(); }
    public void setGoals(ObservableMap<StringProperty,BooleanProperty> goals) { this.goalsProperty().set(goals); }

    public StringProperty lastExecutionStackProperty() { return this.lastExecutionStack; }
    public String getLastExecutionStack() { return this.lastExecutionStackProperty().get(); }
    public void setLastExecutionStack(String lastExecutionStack) { this.lastExecutionStackProperty().set(lastExecutionStack); }

    public IntegerProperty priorityProperty() { return this.priority; }
    public int getPriority() { return this.priorityProperty().get(); }
    public void setPriority(int priority) { this.priorityProperty().set(priority); }

    public StringProperty optionsProperty() { return this.options; }
    public String getOptions() { return this.optionsProperty().get(); }
    public void setOptions(String options) { this.optionsProperty().set(options); }

    public StringProperty postBuildCommandsProperty() { return this.postBuildCommands; }
    public String getPostBuildCommands() { return this.postBuildCommandsProperty().get(); }
    public void setPostBuildCommands(String postBuildCommands) { this.postBuildCommandsProperty().set(postBuildCommands); }

    public ObjectProperty<Workspace> workspaceProperty() { return this.workspace; }
    public Workspace getWorkspace() { return this.workspaceProperty().get(); }
    public void setWorkspace(Workspace workspace) { this.workspaceProperty().set(workspace); }

    public ObjectProperty<Process> activeProcessProperty() { return this.activeProcess; }
    public Process getActiveProcess() { return this.activeProcessProperty().get(); }
    public void setActiveProcess(Process activeProcess) { this.activeProcessProperty().set(activeProcess); }

    public Boolean isGoalActive(Goal goal) {
        Boolean result = null;


        for(Map.Entry<StringProperty, BooleanProperty> entry : getGoals().entrySet()) {
            if(entry.getKey().get().equals(goal.getGoalName())) {
                result = entry.getValue().getValue();
                break;
            }
        }

        return result;
    }

    @Override
    public void unbindAll() {
        if(idProperty().isBound()) idProperty().unbind();
        if(pathProperty().isBound()) pathProperty().unbind();
        if(repositoryNameProperty().isBound()) repositoryNameProperty().unbind();
        if(statusProperty().isBound()) statusProperty().unbind();
        if(selectedProperty().isBound()) selectedProperty().unbind();

        for(Map.Entry<StringProperty, BooleanProperty> entry : goalsProperty().entrySet()) {
            if(entry.getKey().isBound()) entry.getKey().unbind();
            if(entry.getValue().isBound()) entry.getValue().unbind();
        }

        if(lastExecutionStackProperty().isBound()) lastExecutionStackProperty().unbind();
        if(priorityProperty().isBound()) priorityProperty().unbind();
        if(optionsProperty().isBound()) optionsProperty().unbind();
        if(postBuildCommandsProperty().isBound()) postBuildCommandsProperty().unbind();
        if(workspaceProperty().isBound()) workspaceProperty().unbind();
        if(activeProcessProperty().isBound()) activeProcessProperty().unbind();
    }
}
