package com.sample.sampleapplication;

public class SampleModel {
    private int position;
    private String text;
    private boolean isChecked;

    public SampleModel(int position, String text, boolean isChecked) {
        this.position = position;
        this.text = text;
        this.isChecked = isChecked;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
