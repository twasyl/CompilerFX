package com.twasyl.compilerfx.control.cell;

import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.control.Dialog;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class DeleteMavenOptionCellFactory implements Callback<TableColumn<MavenRepository.MavenOption, Long>, TableCell<MavenRepository.MavenOption, Long>> {

    @Override
    public TableCell<MavenRepository.MavenOption, Long> call(TableColumn<MavenRepository.MavenOption, Long> mavenOptionIdColumn) {
        final TableCell<MavenRepository.MavenOption, Long> cell = new TableCell<MavenRepository.MavenOption, Long>() {

            final Button delete = new Button("-");

            {
                delete.getStyleClass().add("center-cell-content");
                delete.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        if(getTableRow().getItem() != null) {
                            Dialog.Response response = Dialog.showConfirmDialog(null, "Maven custom option", "Do you really want to delete this option?");
                            if(response == Dialog.Response.YES) {
                                getTableView().getItems().remove(getTableRow().getItem());
                            }
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Long value, boolean empty) {
                if(empty) {
                    setGraphic(null);
                } else {
                    setGraphic(delete);
                }
            }
        };

        cell.getStyleClass().add("center-cell-content");

        return cell;
    }
}
