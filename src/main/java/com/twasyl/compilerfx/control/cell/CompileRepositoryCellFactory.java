package com.twasyl.compilerfx.control.cell;

import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class CompileRepositoryCellFactory implements Callback<TableColumn<MavenRepository, Boolean>, TableCell<MavenRepository, Boolean>> {

    @Override
    public TableCell<MavenRepository, Boolean> call(TableColumn<MavenRepository, Boolean> mavenRepositoryBooleanTableColumn) {
        TableCell<MavenRepository, Boolean> cell = new TableCell<MavenRepository, Boolean>() {

            private final CheckBox checkBox = new CheckBox();

            @Override
            protected void updateItem(Boolean value, boolean empty) {
                if(empty) {
                    setGraphic(null);
                } else {
                    if(getTableRow() != null && getTableRow().getItem() != null && getTableRow().getItem() instanceof MavenRepository) {
                        MavenRepository repo = (MavenRepository) getTableRow().getItem();
                        this.checkBox.selectedProperty().unbindBidirectional(repo.selectedProperty());
                        this.checkBox.selectedProperty().bindBidirectional(repo.selectedProperty());
                        this.checkBox.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                ConfigurationWorker.save();
                            }
                        });
                    } else {
                        this.checkBox.setSelected(value);
                    }

                    setGraphic(this.checkBox);
                }
            }
        };

        cell.getStyleClass().add("center-cell-content");

        return cell;
    }
}
