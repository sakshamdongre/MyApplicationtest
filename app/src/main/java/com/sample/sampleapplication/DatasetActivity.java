package com.sample.sampleapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.sample.sampleapplication.upload.Common;
import com.sample.sampleapplication.upload.IUploadCallbacks;
import com.sample.sampleapplication.upload.ProgressRequestBody;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatasetActivity extends AppCompatActivity implements IUploadCallbacks {

    private static String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private static String GALLERY_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static int CAMERA_PERMISSION_CODE = 111;
    private static int GALLERY_PERMISSION_CODE = 222;
    private static final int REQUEST_CAMERA = 123;
    private static final int REQUEST_GALLERY = 456;

    private File photoFile = null;
    private String photoPath = "";

    private ProgressDialog progressDialog;
    private ProgressDialog progressDialogPercent;
    private DatasetAdapter datasetAdapter;
    private APIInterface apiInterface;
    private LinearLayout viewNoData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        RecyclerView rv = findViewById(R.id.rv);
        viewNoData = findViewById(R.id.view_nodata);

        apiInterface = APIClient.getApiInterface();
        rv.setLayoutManager(new GridLayoutManager(this, 1));
        datasetAdapter = new DatasetAdapter(this);
        rv.setAdapter(datasetAdapter);

        loadImages();

        findViewById(R.id.btn_upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadDataset();
            }
        });
    }

    private void uploadDataset() {
        final CharSequence[] choice = {"Choose from Gallery", "Capture a photo"};
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Upload Photo");
        alert.setSingleChoiceItems(choice, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        dialog.dismiss();
                        if (permissionAlreadyGranted(GALLERY_PERMISSION)){
                            openGalleryIntent();
                            return;
                        }
                        requestPermission(GALLERY_PERMISSION, GALLERY_PERMISSION_CODE);
                        break;
                    case 1:
                        dialog.dismiss();
                        if (permissionAlreadyGranted(CAMERA_PERMISSION)){
                            openCameraIntent();
                            return;
                        }
                        requestPermission(CAMERA_PERMISSION, CAMERA_PERMISSION_CODE);
                        break;
                }
            }
        });
        alert.show();
    }

    private void openCameraIntent() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Exception : "+ ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.sample.sampleapplication", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, REQUEST_CAMERA);
            }
        }
    }
    private void openGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY);
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg", storageDir);
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            photoPath = photoFile.getAbsolutePath();
            askForImageName();
        } else if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            Uri uri;
            if (data != null) {
                uri = data.getData();
                try {
                    photoPath = Common.getFilePath(DatasetActivity.this, uri);
                    askForImageName();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void askForImageName() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Image Name");
        alert.setMessage("Please provide name of the person in the photo");
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alert.setView(input);
        alert.setPositiveButton("UPLOAD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = input.getText().toString();
                if (!name.equals("")) {
                    dialogInterface.dismiss();
                    uploadToServer(name);
                } else {
                    Toast.makeText(DatasetActivity.this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }

    private void uploadToFirebase(final String imageName, final String name){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference().child(imageName);

        File imageFile = new File(photoPath);
        Uri file = Uri.fromFile(imageFile);
        UploadTask uploadTask = storageRef.putFile(file);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storageRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("known_images").push();
                    mRef.child("image").setValue("" + downloadUri);
                    mRef.child("tag").setValue(name);
                    mRef.child("name").setValue(imageName);
                    progressDialogPercent.dismiss();
                    loadImages();
                } else {

                }
            }
        });
    }

    private void uploadToServer(final String name) {
        progressDialogPercent = new ProgressDialog(this);
        progressDialogPercent.setMessage("Uploading...");
        progressDialogPercent.setCancelable(true);
        progressDialogPercent.show();

        final File imageFile = new File(photoPath);
        ProgressRequestBody requestBody = new ProgressRequestBody(imageFile, this);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBody);
        RequestBody nameBody = RequestBody.create(MultipartBody.FORM, name);
        Call<String> uploadImage = apiInterface.uploadDataset(body, nameBody);
        uploadImage.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()){
                    progressDialogPercent.dismiss();
                    String exactPath = response.body().replace("\"", "");
                    //String imagePath = "http://192.168.43.95:5000/static/known" + exactPath.split("known")[1];
                    //uploadToFirebase(imageFile.getName(), name);
                    loadImages();
                } else {
                    progressDialogPercent.dismiss();
                    Toast.makeText(DatasetActivity.this, "Error Here"+response.errorBody(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                progressDialogPercent.dismiss();
                Toast.makeText(DatasetActivity.this, "Error"+t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImages() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Dataset...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        Call<String> uploadImage = apiInterface.loadDataset();
        uploadImage.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()){
                    progressDialog.dismiss();
                    if (response.body() != null) {
                        if (response.body().equals("")){
                            viewNoData.setVisibility(View.VISIBLE);
                        } else {
                            final List<String> dataset = Arrays.asList(response.body().split(","));
                            final ArrayList<String> tagList = new ArrayList<>();
                            ArrayList<String> imageList = new ArrayList<>();

                            for (String image : dataset){
                                String split1 = image.split("known")[1];
                                String split2 = split1.replace(".jpg", "");
                                String split3 = split2.replace("/", "");
                                tagList.add(split3);
                                imageList.add("http://192.168.43.95:5000/static/known" + split1);
                            }
                            datasetAdapter.updateDataset(imageList, tagList);
                            viewNoData.setVisibility(View.GONE);
                        }
                    }
                } else {
                    Toast.makeText(DatasetActivity.this, "Error Here"+response.errorBody(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(DatasetActivity.this, "Error"+t.toString(), Toast.LENGTH_SHORT).show();
            }
        });


//        final ArrayList<String> tagList = new ArrayList<>();
//        final ArrayList<String> imageList = new ArrayList<>();
//        final ArrayList<String> nameList = new ArrayList<>();
//        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("known_images");
//        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    imageList.add(String.valueOf(snapshot.child("image").getValue()));
//                    tagList.add(String.valueOf(snapshot.child("tag").getValue()));
//                    nameList.add(String.valueOf(snapshot.child("name").getValue()));
//                }
//                viewNoData.setVisibility(View.GONE);
//                progressDialog.dismiss();
//                if (imageList.size() == 0){
//                    viewNoData.setVisibility(View.VISIBLE);
//                } else {
//                    datasetAdapter.updateDataset(imageList, tagList, nameList);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    //Permission Code
    private boolean permissionAlreadyGranted(String permission) {
        int result = ContextCompat.checkSelfPermission(this, permission);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        return false;
    }
    private void requestPermission(String permission, int permissionCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

        }
        ActivityCompat.requestPermissions(this, new String[]{permission}, permissionCode);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission is denied!", Toast.LENGTH_SHORT).show();
                boolean showRationale = shouldShowRequestPermissionRationale( Manifest.permission.CAMERA );
                if (! showRationale) {
                    openSettingsDialog();
                }
            }
        } else if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission gallery granted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission gallery is denied!", Toast.LENGTH_SHORT).show();
                boolean showRationale = shouldShowRequestPermissionRationale( Manifest.permission.WRITE_EXTERNAL_STORAGE );
                if (! showRationale) {
                    openSettingsDialog();
                }
            }
        }

    }
    private void openSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Required Permissions");
        builder.setMessage("This app require permission to use camera and gallery. Grant them in app settings.");
        builder.setPositiveButton("Take Me To SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public void onProgressUpdate(int percent) {
        progressDialogPercent.setProgress(percent);
    }
}
