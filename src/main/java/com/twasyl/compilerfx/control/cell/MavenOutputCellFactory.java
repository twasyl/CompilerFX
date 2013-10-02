package com.twasyl.compilerfx.control.cell;

import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.enums.Status;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

public class MavenOutputCellFactory implements Callback<TableColumn<MavenRepository, String>, TableCell<MavenRepository, String>> {

    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>();

    public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() { return this.onAction; }
    public EventHandler<ActionEvent> getOnAction() { return this.onActionProperty().get(); }
    public void setOnAction(EventHandler<ActionEvent> onAction) { this.onActionProperty().set(onAction); }

    @Override
    public TableCell<MavenRepository, String> call(TableColumn<MavenRepository, String> mavenRepositoryBooleanTableColumn) {
        TableCell<MavenRepository, String>  cell = new TableCell<MavenRepository, String>() {
            private final Button button = new Button();

            {
                button.getStyleClass().add("output-result-button");
                button.setGraphic(new ImageView(new Image(getClass().getResource("/com/twasyl/compilerfx/images/terminal.png").toExternalForm())));
            }

            @Override
            protected void updateItem(String value, boolean empty) {
                if(empty) {
                    setGraphic(null);
                } else {
                    button.setUserData(getTableRow().getItem());

                    if(button.onActionProperty().isBound()) button.onActionProperty().unbind();

                    button.onActionProperty().bind(onAction);

                    setGraphic(button);
                }
            }
        };

        cell.getStyleClass().add("center-cell-content");

        return cell;
    }
}
