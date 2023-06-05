package com.example.beta;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

/**
 * @author		Ilai Shimoni ilaigithub@gmail.com
 * @version	    3.0
 * @since		12/10/22
 * this class performs the action of uploading a profile picture for the
 * user to the database
 */
public class storage extends AppCompatActivity {
    ImageView img;
    private static final int PICK_IMAGE_REQUEST = 1;
    Uri mImageUri;
    StorageReference storageReference;
    ProgressDialog progressDialog;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        img = (ImageView) findViewById(R.id.imageView);
    }

    /**
     * opens the gallery upon "select image" button click
     */
    public void SelectImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * loads the image selected from the gallery
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Picasso.with(this).load(mImageUri).into(img);
        }
    }

    /**
     * opens the camera
     */
    public void takepicture(View view) {
        Intent open_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     startActivityForResult(open_camera, 100);
    }

    /**
     * upload the image found on the preview and assign it to the current user
     */
    public void uploadImage(View view) {

        if(img.getDrawable() == null){
            Toast.makeText(this, "please select an image before proceeding", Toast.LENGTH_SHORT).show();
        }
        else{

            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading File...");
            progressDialog.show();

            storageReference = FirebaseStorage.getInstance().getReference("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() );

            storageReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            img.setImageBitmap(null);
                            Toast.makeText(storage.this, "successfully uploaded", Toast.LENGTH_SHORT).show();
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                            Intent intent = new Intent(storage.this,AquariumPicker.class);
                            startActivity(intent);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                            Toast.makeText(storage.this, "there was something wrong with your upload...", Toast.LENGTH_SHORT).show();

                        }
                    });

        }
    }
}