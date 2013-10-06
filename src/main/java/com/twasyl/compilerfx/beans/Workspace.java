package com.twasyl.compilerfx.beans;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import java.util.Comparator;
import java.util.TreeSet;

public class Workspace implements PropertyBean {

    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final SetProperty<MavenRepository> repositories = new SimpleSetProperty<>();
    private final BooleanProperty active = new SimpleBooleanProperty();

    public Workspace() {
        TreeSet<MavenRepository> repositoryTreeSet = new TreeSet<>(new Comparator<MavenRepository>() {
            @Override
            public int compare(MavenRepository o1, MavenRepository o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });

        repositories.set(FXCollections.observableSet(repositoryTreeSet));
        repositories.addListener(new SetChangeListener<MavenRepository>() {
            @Override
            public void onChanged(Change<? extends MavenRepository> change) {
                if(change.wasAdded()) change.getElementAdded().setWorkspace(Workspace.this);
                else if(change.wasRemoved()) change.getElementRemoved().setWorkspace(null);
            }
        });
    }

    public LongProperty idProperty() { return this.id; }
    public long getId() { return this.idProperty().get(); }
    public void setId(long id) { this.idProperty().set(id); }

    public StringProperty nameProperty() { return this.name; }
    public String getName() { return this.nameProperty().get(); }
    public void setName(String name) { this.nameProperty().set(name); }

    public SetProperty<MavenRepository> repositoriesProperty() { return this.repositories; }
    public ObservableSet<MavenRepository> getRepositories() { return this.repositoriesProperty().get(); }
    public void setRepositories(ObservableSet<MavenRepository> repositories) { this.repositoriesProperty().set(repositories); }

    public BooleanProperty activeProperty() { return this.active; }
    public boolean getActive() { return this.activeProperty().get(); }
    public void setActive(boolean active) { this.activeProperty().set(active); }

    @Override
    public void unbindAll() {
        if(idProperty().isBound()) idProperty().unbind();
        if(nameProperty().isBound()) nameProperty().unbind();
        if(repositoriesProperty().isBound()) repositoriesProperty().unbind();
        if(activeProperty().isBound()) activeProperty().unbind();
    }
}
