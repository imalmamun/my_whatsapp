package com.almamun.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private StorageReference UserProfileImagesRef;
    private static final int GalleryPick = 1;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
//        MARK: initialize section here...
        InitializeFields();

//        userName.setVisibility(View.INVISIBLE);
        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 UpdateSettings();
            }
        });
        RetrieveUserInfo();
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent().setAction(Intent.ACTION_GET_CONTENT).setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }




    private void InitializeFields() {
        UpdateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image );
        loadingBar = new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GalleryPick && resultCode == RESULT_OK && data!=null){
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);


        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultlUri = result.getUri();


                final StorageReference filePath = UserProfileImagesRef.child(currentUserId + ".jpg");
//                MARK: storing data to firebase storage...
                filePath.putFile(resultlUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile Image uploaded successfully...", Toast.LENGTH_SHORT).show();

//                          MARK: Retrieving Image Uri from firebase storage...
                           filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                               @Override
                               public void onSuccess(Uri uri) {

                                   Toast.makeText(SettingsActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();
//                                   MARK: Storing information to firebase database...
                                   RootRef.child("Users").child(currentUserId).child("image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if(task.isSuccessful()){
                                               loadingBar.dismiss();
                                               Toast.makeText(SettingsActivity.this, "Image Saved in database, Successfully", Toast.LENGTH_SHORT).show();
                                           }
                                           else{
                                               loadingBar.dismiss();
                                               String message = task.getException().toString();
                                               Toast.makeText(SettingsActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                                           }

                                       }
                                   });



                               }
                           }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   Toast.makeText(SettingsActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

                               }
                           });
                        }else{
                            loadingBar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
//                String downloadUrl = filePath.getDownloadUrl().toString();

            }

        }
    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "Please enter your current active status", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
            RootRef.child("Users").child(currentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(SettingsActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                        SendUserToMainActivity();
                    }
                    else{
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);



                }else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);

                }
                else{
                    Toast.makeText(SettingsActivity.this, "Please add and update your profile information", Toast.LENGTH_SHORT).show();
                    userName.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}