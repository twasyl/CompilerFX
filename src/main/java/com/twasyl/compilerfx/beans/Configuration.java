package com.twasyl.compilerfx.beans;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

public class Configuration {

    private static final ObjectProperty<Configuration> instance = new SimpleObjectProperty<>(new Configuration());
    private final StringProperty mavenCommand = new SimpleStringProperty();
    private final SetProperty<MavenRepository> repositories = new SimpleSetProperty<>();
    private final SetProperty<Workspace> workspaces = new SimpleSetProperty<>();
    private final SetProperty<MavenRepository.MavenOption> customMavenOptions = new SimpleSetProperty<>();
    private final ListProperty<MavenRepository> currentBuilds = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<MavenRepository>()));

    private Configuration() {
        TreeSet<MavenRepository> repositoryTreeSet = new TreeSet<>(new Comparator<MavenRepository>() {
            @Override
            public int compare(MavenRepository o1, MavenRepository o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });

        TreeSet<Workspace> workspaceTreeSet = new TreeSet<>(new Comparator<Workspace>() {
            @Override
            public int compare(Workspace o1, Workspace o2) {
                return (int) (o1.getId() - o2.getId());
            }
        });

        TreeSet<MavenRepository.MavenOption> optionsSet = new TreeSet<>(new Comparator<MavenRepository.MavenOption>() {
            @Override
            public int compare(MavenRepository.MavenOption o1, MavenRepository.MavenOption o2) {
                return (int) (o1.getId() - o2.getId());
            }
        });

        this.repositories.set(FXCollections.observableSet(repositoryTreeSet));
        this.workspaces.set(FXCollections.observableSet(workspaceTreeSet));
        this.customMavenOptions.set(FXCollections.observableSet(optionsSet));
    }

    public static ObjectProperty<Configuration> instanceProperty() { return instance; }
    public static Configuration getInstance() { return instanceProperty().get(); }

    public StringProperty mavenCommandProperty() { return this.mavenCommand; }
    public String getMavenCommand() { return this.mavenCommandProperty().get(); }
    public void setMavenCommand(String mavenCommand) { this.mavenCommandProperty().set(mavenCommand); }

    public SetProperty<MavenRepository> repositoriesProperty() { return this.repositories; }
    public ObservableSet<MavenRepository> getRepositories() { return this.repositoriesProperty().get(); }
    public void setRepositories(ObservableSet<MavenRepository> repositories) { this.repositoriesProperty().set(repositories); }

    public SetProperty<Workspace> workspacesProperty() { return this.workspaces; }
    public ObservableSet<Workspace> getWorkspaces() { return this.workspacesProperty().get(); }
    public void setWorkspaces(ObservableSet<Workspace> workspaces) { this.workspacesProperty().set(workspaces); }

    public ListProperty<MavenRepository> currentBuildsProperty() { return this.currentBuilds; }
    public ObservableList<MavenRepository> getCurrentBuilds() { return this.currentBuildsProperty().get(); }
    public void setCurrentBuilds(ObservableList<MavenRepository> currentBuilds) { this.currentBuildsProperty().set(currentBuilds); }

    public SetProperty<MavenRepository.MavenOption> customMavenOptionsProperty() { return this.customMavenOptions; }
    public ObservableSet<MavenRepository.MavenOption> getCustomMavenOptions() { return this.customMavenOptionsProperty().get(); }
    public void setCustomMavenOptions(ObservableSet<MavenRepository.MavenOption> customMavenOptions) { this.customMavenOptionsProperty().set(customMavenOptions); }

    public ObservableList<Workspace> getWorkspacesAsList() { return FXCollections.observableArrayList(getWorkspaces()); }
}
