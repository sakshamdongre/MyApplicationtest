package com.sample.sampleapplication.api;

import com.google.gson.annotations.SerializedName;

public class SamplePOJO {
    @SerializedName("image_path")
    private String imagePath;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
