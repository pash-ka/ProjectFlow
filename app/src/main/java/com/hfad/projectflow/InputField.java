package com.hfad.projectflow;

public class InputField {
    private String label;
    private String value;

    public InputField(String label){
        this.label = label;
        this.value = "";
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
