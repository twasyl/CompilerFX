package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConfigurationScreenController implements Initializable {

    @FXML private TextField mvnCommand;
    @FXML private Button browseMvnCommand;

    @FXML private void browse(ActionEvent event) {

        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(null);

        if(file != null) {
            this.mvnCommand.setText(file.getAbsolutePath());
        }
    }

    @FXML private void save(ActionEvent event) {
        Configuration.getInstance().setMavenCommand(this.mvnCommand.getText());
        ConfigurationWorker.save();

        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/com/twasyl/compilerfx/fxml/MavenRepositories.fxml"));
            CompilerFXController.getCurrentInstance().switchScreen(parent);
        } catch (IOException e) {
        }
    }

    @FXML private void cancel(ActionEvent event) {
        if(Configuration.getInstance().getMavenCommand() != null) {
            this.mvnCommand.setText(Configuration.getInstance().getMavenCommand());
        } else {
            this.mvnCommand.setText("");
        }

        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/com/twasyl/compilerfx/fxml/MavenRepositories.fxml"));
            CompilerFXController.getCurrentInstance().switchScreen(parent);
        } catch (IOException e) {
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ConfigurationWorker.load();

        if(Configuration.getInstance().getMavenCommand() != null) {
            this.mvnCommand.setText(Configuration.getInstance().getMavenCommand());
        }
    }
}
