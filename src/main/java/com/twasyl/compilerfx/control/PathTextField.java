package com.twasyl.compilerfx.control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class PathTextField extends TextField {

    {
        textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                if(!textProperty().isBound() && s2 != null) {
                    textProperty().set(s2.replaceAll("\\\\", "/"));
                }
            }
        });
    }
}
