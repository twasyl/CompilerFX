package com.twasyl.compilerfx.control.cell;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.beans.Workspace;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class PriorityCellFactory implements Callback<TableColumn<MavenRepository, Integer>, TableCell<MavenRepository, Integer>> {

    private static enum Priority {
        INCREASE, DECREASE
    }

    @Override
    public TableCell<MavenRepository, Integer> call(TableColumn<MavenRepository, Integer> mavenRepositoryStatusTableColumn) {
        final TableCell<MavenRepository, Integer> cell = new TableCell<MavenRepository, Integer>() {

            final Button up = new Button("", new ImageView(new Image(getClass().getResource("/com/twasyl/compilerfx/images/up.png").toExternalForm())));
            final Button down = new Button("", new ImageView(new Image(getClass().getResource("/com/twasyl/compilerfx/images/down.png").toExternalForm())));
            final HBox content = new HBox(10);

            {
                content.getStyleClass().add("center-cell-content");
                up.getStyleClass().add("priority-button");
                down.getStyleClass().add("priority-button");
            }

            @Override
            protected void updateItem(Integer integer, boolean empty) {
                if(empty) {
                    setGraphic(null);
                } else {
                    up.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            updatePriority(Priority.INCREASE);
                        }
                    });

                    down.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            updatePriority(Priority.DECREASE);
                        }
                    });

                    content.getChildren().clear();
                    content.getChildren().addAll(down, up);

                    setGraphic(content);
                }
            }

            private void updatePriority(Priority increment) {
                if (getTableRow() != null) {
                    final MavenRepository repository = (MavenRepository) getTableRow().getItem();
                    final Workspace workspace = repository.getWorkspace();

                    workspace.getRepositories().remove(repository);

                    for(MavenRepository repo : Configuration.getInstance().getRepositories()) {
                        if(increment == Priority.DECREASE && repo.getPriority() == repository.getPriority() + 1) {
                            repo.setPriority(repository.getPriority());
                            repository.setPriority(repo.getPriority() + 1);
                            break;
                        } else if(increment == Priority.INCREASE && repo.getPriority() == repository.getPriority() - 1) {
                            repo.setPriority(repository.getPriority());
                            repository.setPriority(repo.getPriority() - 1);
                            break;
                        }
                    }

                    // To resort the table
                    workspace.getRepositories().add(repository);

                    ConfigurationWorker.save();
                }
            }
        };

        cell.getStyleClass().add("center-cell-content");

        return cell;
    }
}
