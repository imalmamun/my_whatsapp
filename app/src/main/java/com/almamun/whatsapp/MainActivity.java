package com.almamun.whatsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.almamun.whatsapp.UserAuthAll.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        MARK: mapping here...

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = findViewById(R.id.main_page_toolbar);
        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);
        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);



        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("WhatsApp");




    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser == null){
            SendUserToLonginActivity();

        }
        else{
            VarifyUserExistance();
        }
    }

    private void VarifyUserExistance() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else{
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToLonginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_option){
            mAuth.signOut();
            SendUserToLonginActivity();

        }
        if(item.getItemId()==R.id.main_settings_option){
            SendUserToSettingsActivity();

        }
        if(item.getItemId()==R.id.main_create_group_option){
            RequestNewGroup();
        }
        if(item.getItemId()==R.id.main_find_friends_option){
            SendUserToFindFriendsActivity();

        }
        return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g quick members");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "You need to create a group name for creating a group", Toast.LENGTH_SHORT).show();
                }else{
                    CreateNewGroup(groupName);

                }

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

    private void CreateNewGroup(final String groupName) {
        String currentUserId = mAuth.getCurrentUser().getUid();
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, groupName+" named group is created", Toast.LENGTH_SHORT).show();
                }
                else{
                    String message = task.getException().toString();
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }
    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
        finish();
    }

}


//tutorial sixteen completed of this video ok(of coding cafe 16 done)..
//tutorial seventeen is completed ...(coding cafe 17 done.(have to revise ok..has something new here...))
//tutorial eighteen is completed ...(coding cafe 18 done.)
//completed tutorial 19.....(moving to next one...)
//completed tutorial 20...(it would be better to revise this once again...)
//completed tutorial 21...(learn some beautiful stuff really awesome...now i can send message in group and see those messages...)
//completed tutorial 22...(learn to scroll to down but can not fix all problem I took a help of another video...when i call onstart of the app it d
//doesn't scroll to the bottom ...then i fixed it ...
//completed tutorial 23 successfully(designed phone verification layout...)
//completed tutorial 24 (phone verification)but it is not working in my phone...app is crashing ...don't know why (but one thing is clear that i have to learn a lot of thing from this video...)
//completed tutorial 25 learned a lot of thing regarding retrieving image from gallery and cropping it with the help of an third party library called (ArthurHub Android-Image-Cropper)a github library..
//completed tutorial 26 successfully learned(uploading images inside firebase storage)...
//completed tutorial 27 successfully, it was tough one...mohammad ali himself made mistake i i corrected working almost five four hour...learned(about picasso library, and it doesn't retrieve picture without https....and firebase doesn't return https as the video showed, so i c
// I converted it in https..with onsuccesslistener and onfailure listener instead of using oncomplitelistener as it has no url return...)
//completed tutorial 28 successfully(created find friends activity)...