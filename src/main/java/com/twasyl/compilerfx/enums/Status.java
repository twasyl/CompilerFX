package com.twasyl.compilerfx.enums;

public enum Status {
    READY("readyStatus", "Ready"),
    COMPILING("compilingStatus", "Compiling"),
    POST_BUILD("postBuild", "Post build"),
    IN_ERROR("errorStatus", "Error"),
    ABORTED("abortedStatus", "Aborted"),
    DONE("doneStatus", "Done");

    private String cssClass;
    private String label;

    private Status(String cssClass, String label) {
        this.cssClass = cssClass;
        this.label = label;
    }

    public String getCssClass() {
        return cssClass;
    }

    public String getLabel() {
        return label;
    }
}
