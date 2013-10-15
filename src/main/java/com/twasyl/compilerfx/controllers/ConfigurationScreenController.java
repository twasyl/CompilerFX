package com.twasyl.compilerfx.controllers;

import com.twasyl.compilerfx.beans.Configuration;
import com.twasyl.compilerfx.beans.MavenRepository;
import com.twasyl.compilerfx.utils.ConfigurationWorker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ConfigurationScreenController implements Initializable {

    @FXML private TextField mvnCommand;
    @FXML private Button browseMvnCommand;
    @FXML private TableView<MavenRepository.MavenOption> customOptions;
    @FXML private TextField customOptionOption;
    @FXML private TextField customOptionDescription;

    @FXML private void browse(ActionEvent event) {

        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(null);

        if(file != null) {
            this.mvnCommand.setText(file.getAbsolutePath());
        }
    }

    @FXML private void addCustomOption(ActionEvent event) {
        if(!this.customOptionOption.getText().trim().isEmpty()) {
            MavenRepository.MavenOption option = new MavenRepository.MavenOption();
            option.setId(System.currentTimeMillis());
            option.setOption(this.customOptionOption.getText());
            option.setDescription(this.customOptionDescription.getText());

            this.customOptions.getItems().add(option);

            this.customOptionOption.clear();
            this.customOptionDescription.clear();
        }
    }

    @FXML private void save(ActionEvent event) {
        Configuration.getInstance().setMavenCommand(this.mvnCommand.getText());
        Configuration.getInstance().getCustomMavenOptions().clear();
        Configuration.getInstance().getCustomMavenOptions().addAll(this.customOptions.getItems());
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
        if(Configuration.getInstance().getMavenCommand() != null) {
            this.mvnCommand.setText(Configuration.getInstance().getMavenCommand());
        }

        for(MavenRepository.MavenOption option : Configuration.getInstance().getCustomMavenOptions()) {
            this.customOptions.getItems().add(option);
        }
    }
}
