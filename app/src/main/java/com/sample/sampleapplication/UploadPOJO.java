package com.sample.sampleapplication;

import com.google.gson.annotations.SerializedName;

public class UploadPOJO {
    @SerializedName("img_original")
    private String imgOriginal;
    @SerializedName("img_detected")
    private String imgDetected;

    public String getImgOriginal() {
        return imgOriginal;
    }

    public void setImgOriginal(String imgOriginal) {
        this.imgOriginal = imgOriginal;
    }

    public String getImgDetected() {
        return imgDetected;
    }

    public void setImgDetected(String imgDetected) {
        this.imgDetected = imgDetected;
    }
}
