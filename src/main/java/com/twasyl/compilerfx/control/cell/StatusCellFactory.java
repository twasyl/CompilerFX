package com.twasyl.compilerfx.control.cell;

import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.enums.Status;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class StatusCellFactory implements Callback<TableColumn<MavenRepository, Status>, TableCell<MavenRepository, Status>> {

    @Override
    public TableCell<MavenRepository, Status> call(TableColumn<MavenRepository, Status> gitRepositoryStatusTableColumn) {
        TableCell<MavenRepository, Status> cell = new TableCell<MavenRepository, Status>() {

            @Override
            protected void updateItem(Status status, boolean empty) {

                for(Status st : Status.values()) {
                    this.getStyleClass().remove(st.getCssClass());
                }

                if(empty) {
                    setGraphic(null);
                    setText("");
                } else if(!empty && status != null) {
                    this.setText(status.getLabel());
                    this.getStyleClass().add(status.getCssClass());
                }
            }
        };

        cell.getStyleClass().add("center-cell-content");

        return cell;
    }
}
