package com.twasyl.compilerfx.utils;

import com.twasyl.compilerfx.beans.Configuration;
import javafx.util.BuilderFactory;
import javafx.util.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class FXMLLoader extends javafx.fxml.FXMLLoader {

    public FXMLLoader() {
        super();
    }

    public FXMLLoader(URL url) {
        super(url, getResourceBundle());
    }

    public FXMLLoader(URL url, ResourceBundle resourceBundle) {
        super(url, resourceBundle);
    }

    public FXMLLoader(URL url, ResourceBundle resourceBundle, BuilderFactory builderFactory) {
        super(url, resourceBundle, builderFactory);
    }

    public FXMLLoader(URL url, ResourceBundle resourceBundle, BuilderFactory builderFactory, Callback<Class<?>, Object> classObjectCallback) {
        super(url, resourceBundle, builderFactory, classObjectCallback);
    }

    public FXMLLoader(Charset charset) {
        super(charset);
    }

    public FXMLLoader(URL url, ResourceBundle resourceBundle, BuilderFactory builderFactory, Callback<Class<?>, Object> classObjectCallback, Charset charset) {
        super(url, resourceBundle, builderFactory, classObjectCallback, charset);
    }

    public FXMLLoader(URL url, ResourceBundle resourceBundle, BuilderFactory builderFactory, Callback<Class<?>, Object> classObjectCallback, Charset charset, LinkedList<javafx.fxml.FXMLLoader> fxmlLoaders) {
        super(url, resourceBundle, builderFactory, classObjectCallback, charset, fxmlLoaders);
    }

    @Override
    public Object load(InputStream inputStream) throws IOException {
        setResources(getResourceBundle());
        return super.load(inputStream);
    }

    @Override
    public Object load() throws IOException {
        setResources(getResourceBundle());
        return super.load();
    }

    public void reset() {
        setRoot(null);
        setController(null);
    }

    public static <T> T load(URL resource) throws IOException {
        return javafx.fxml.FXMLLoader.load(resource, getResourceBundle());
    }

    public static ResourceBundle getResourceBundle() {
        ResourceBundle bundle = null;

        if(Configuration.getInstance().getUiLocale() == null) {
            bundle = ResourceBundle.getBundle("com.twasyl.compilerfx.localization.compilerfx");
        } else {
            bundle = ResourceBundle.getBundle("com.twasyl.compilerfx.localization.compilerfx", Configuration.getInstance().getUiLocale());
        }

        return bundle;
    }
}
