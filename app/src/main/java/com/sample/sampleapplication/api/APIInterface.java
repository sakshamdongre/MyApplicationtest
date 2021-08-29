package com.sample.sampleapplication.api;

import com.sample.sampleapplication.UploadPOJO;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface APIInterface {
    @Multipart
    @POST("upload")
    Call<UploadPOJO> uploadPhoto(@Part MultipartBody.Part image);

    @Multipart
    @POST("uploadDataset")
    Call<String> uploadDataset(@Part MultipartBody.Part image, @Part("name") RequestBody name);

    @FormUrlEncoded
    @POST("recognise")
    Call<String> compareImages(@Field("img_unknown") String image_unknown);

    @GET("loadDataset")
    Call<String> loadDataset();
}
