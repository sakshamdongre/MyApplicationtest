package com.sample.sampleapplication;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sample.sampleapplication.api.APIClient;
import com.sample.sampleapplication.api.APIInterface;
import com.sample.sampleapplication.upload.IUploadCallbacks;
import com.sample.sampleapplication.upload.ProgressRequestBody;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.MultipartBody;
 import retrofit2.Call;
 import retrofit2.Callback;
 import retrofit2.Response;

public class ProcessActivity extends AppCompatActivity implements IUploadCallbacks{

    private ImageView imageSelected, btnClose;
    private Button btnUpload;
    private APIInterface apiInterface;
    private File imageFile = null;
    private ProgressDialog progressDialog;
    private Boolean isBtnUpload = true;
    private String unknownImage = "";
    private ResultAdapter resultAdapter;
    private RelativeLayout viewProcess;
    private RecyclerView rvResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int mSec = calendar.get(Calendar.MILLISECOND);

        imageSelected = findViewById(R.id.img_select);
        btnClose = findViewById(R.id.btn_close);
        btnUpload = findViewById(R.id.btn_upload);
        rvResult = findViewById(R.id.rv_result);
        viewProcess = findViewById(R.id.view_process);

        String imagePath = getIntent().getStringExtra("image_path");
        if (imagePath != null) {
            imageFile = new File(imagePath);
            Uri imageUri = Uri.fromFile(imageFile);
            Glide.with(this).load(imageUri).into(imageSelected);
        }
        apiInterface = APIClient.getApiInterface();
        progressDialog = new ProgressDialog(this);
        rvResult.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        resultAdapter = new ResultAdapter(this);
        rvResult.setAdapter(resultAdapter);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if (isBtnUpload)
                    uploadImage();
               else
                   recogniseImage();
           }
        });
    }

    private void recogniseImage() {
        progressDialog.setMessage("Recognizing...");
        progressDialog.show();

//        final ArrayList<String> knownImages = new ArrayList<>();
//        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("known_images");
//        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot data : dataSnapshot.getChildren()){
//                    knownImages.add(String.valueOf(data.child("image").getValue()));
//                }
//
//                Log.e("Images", android.text.TextUtils.join(",", knownImages));
//                Log.e("ImagesUnknown", unknownImage);
//
//                Call<String> compareImage = apiInterface.compareImages(android.text.TextUtils.join(",", knownImages), unknownImage);
//                compareImage.enqueue(new Callback<String>() {
//                    @Override
//                    public void onResponse(Call<String> call, Response<String> response) {
//                        if (response.isSuccessful()){
//                            progressDialog.dismiss();
//                            Log.e("Result", response.body());
//                            updateResult(response.body().split(","));
//                        } else {
//                            progressDialog.dismiss();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<String> call, Throwable t) {
//                        progressDialog.dismiss();
//                        Toast.makeText(ProcessActivity.this, "Error : "+t, Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        Call<String> compareImage = apiInterface.compareImages(unknownImage);
        compareImage.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()){
                    progressDialog.dismiss();
                    Log.e("Result", response.body());
                    String imgBox = "http://192.168.43.95:5000/static/recognised" + response.body().replace("\"", "")
                            .split("recognised")[1];
                    Log.e("Result", "Here " + imgBox);
                    Glide.with(getApplicationContext()).load(imgBox).into(imageSelected);
                    //updateResult(response.body().split(","));
                } else {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(ProcessActivity.this, "Error : "+t, Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void updateResult(final String[] result) {
//        final ArrayList<String> resultList = new ArrayList<>(Arrays.asList(result));
//        final ArrayList<String> tagList = new ArrayList<>();
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("known_images");
//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot data : dataSnapshot.getChildren()){
//                    for (String imageResult : resultList) {
//                        if (String.valueOf(data.child("image").getValue()).equals(imageResult)){
//                            tagList.add(String.valueOf(data.child("tag").getValue()));
//                        }
//                    }
//                }
//
//                resultAdapter.updateDataset(resultList, tagList);
//                rvResult.setVisibility(View.VISIBLE);
//                viewProcess.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void uploadImage() {
         progressDialog.setMessage("Uploading...");
         progressDialog.setCancelable(false);
         progressDialog.show();

         ProgressRequestBody requestBody = new ProgressRequestBody(imageFile, this);
         MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBody);
         final Call<UploadPOJO> uploadImage = apiInterface.uploadPhoto(body);
         uploadImage.enqueue(new Callback<UploadPOJO>() {
             @Override
             public void onResponse(Call<UploadPOJO> call, Response<UploadPOJO> response) {
                 if (response.isSuccessful()){
                     UploadPOJO uploadPOJO = response.body();
                     String imgBox = "http://192.168.43.95:5000/static/detected" + uploadPOJO.getImgDetected().replace("\"", "")
                             .split("detected")[1];
                     unknownImage = uploadPOJO.getImgOriginal();
                     Log.e("Links", imgBox + "\n\n" + unknownImage);
                     Glide.with(ProcessActivity.this).load(imgBox).into(imageSelected);
                     progressDialog.dismiss();
                     btnUpload.setText("RECOGNIZE");
                     btnUpload.setBackgroundColor(ContextCompat.getColor(ProcessActivity.this, android.R.color.holo_blue_dark));
                     isBtnUpload = false;
                 } else {
                     Toast.makeText(ProcessActivity.this, "Error Here"+response.errorBody(), Toast.LENGTH_SHORT).show();
                 }
             }

             @Override
             public void onFailure(Call<UploadPOJO> call, Throwable t) {
                 progressDialog.dismiss();
                 Toast.makeText(ProcessActivity.this, "Error"+t.toString(), Toast.LENGTH_SHORT).show();
             }
         });
     }

//    private void uploadToFirebase(String name){
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        final StorageReference storageRef = storage.getReference().child(name);
//
//        Uri file = Uri.fromFile(imageFile);
//        UploadTask uploadTask = storageRef.putFile(file);
//        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//            @Override
//            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                if (!task.isSuccessful()) {
//                    throw task.getException();
//                }
//                return storageRef.getDownloadUrl();
//            }
//        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//            @Override
//            public void onComplete(@NonNull Task<Uri> task) {
//                if (task.isSuccessful()) {
//                    Uri downloadUri = task.getResult();
//                    unknownImage = "" + downloadUri;
//                    progressDialog.dismiss();
//                    btnUpload.setText("RECOGNIZE");
//                    btnUpload.setBackgroundColor(ContextCompat.getColor(ProcessActivity.this, android.R.color.holo_blue_dark));
//                    isBtnUpload = false;
//                }
//            }
//        });
//    }

     @Override
     public void onProgressUpdate(int percent) {
//         progressDialog.setProgress(percent);
     }
 }
