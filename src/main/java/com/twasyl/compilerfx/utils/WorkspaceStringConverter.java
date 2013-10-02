package com.twasyl.compilerfx.utils;

import com.twasyl.compilerfx.beans.Workspace;
import javafx.util.StringConverter;

public class WorkspaceStringConverter extends StringConverter<Workspace> {

    @Override
    public String toString(Workspace workspace) {
        return workspace == null ? "trash" : workspace.getName();
    }

    @Override
    public Workspace fromString(String s) {
        return null;  // Only needed for editable combo box
    }
}
