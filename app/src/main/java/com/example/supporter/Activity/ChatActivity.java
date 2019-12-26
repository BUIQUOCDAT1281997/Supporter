package com.example.supporter.Activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.supporter.Adapter.MessageAdapter;
import com.example.supporter.Other.Chat;
import com.example.supporter.Other.User;
import com.example.supporter.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int IMAGE_REQUEST_CODE = 20199;
    private static final int READ_STORAGE = 2018;
    private CircleImageView imgUser;
    private TextView useName;

    //storage
    private StorageReference mStorageRef;
    private StorageTask uploadTask;

    //evenLister
    private ValueEventListener eventListener;

    private Uri imageUri;

    //FireBase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private DatabaseReference referenceFromChats;

    private RecyclerView recyclerView;
    private List<Chat> listData;
    private MessageAdapter messageAdapter;

    private ImageView btn_send;
    private EditText texSend;
    private ImageView picture_selected;

    private String userIDFriend;

    private boolean withPicture = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get data
        Intent intent = getIntent();
        userIDFriend = intent.getStringExtra("userID");

        //initView
        initView();

        //Toolbar
        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recyclerView
        recyclerView = findViewById(R.id.recycler_view_message);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        // set Even click
        btn_send.setOnClickListener(this);
        findViewById(R.id.tv_add_picture).setOnClickListener(this);

        // work with toolbar and read message
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                useName.setText(user.getUserName());
                if (!user.getAvatarURL().equals("default")) {
                    Glide.with(getApplicationContext()).load(user.getAvatarURL()).into(imgUser);
                }

                readMessage(firebaseUser.getUid(), userIDFriend, user.getAvatarURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userIDFriend);

    }

    private void initView() {
        picture_selected = findViewById(R.id.picture_selected);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userIDFriend);
        referenceFromChats = FirebaseDatabase.getInstance().getReference("Chats");
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads").child(firebaseUser.getUid());
        btn_send = findViewById(R.id.button_send);
        texSend = findViewById(R.id.text_send);
        imgUser = findViewById(R.id.circle_view_chat);
        useName = findViewById(R.id.chat_user_name);

    }

    private void sendMessage(String sender, final String receiver, String message, boolean textIsEmpty ) {

            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            final HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", sender);
            hashMap.put("receiver", receiver);
            if (textIsEmpty){
                hashMap.put("message", "Photo");
            }else {
                hashMap.put("message", message);
            }
            hashMap.put("isSeen", "false");


        if (withPicture){
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        hashMap.put("picture", mUri);
                        reference.child("Chats").push().setValue(hashMap);

                    } else {
                        Toast.makeText(ChatActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            hashMap.put("picture","default");
            reference.child("Chats").push().setValue(hashMap);
        }




            final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList");
        final DatabaseReference chat1 = chatRef
                    .child(firebaseUser.getUid())
                    .child(userIDFriend);
            chat1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        chat1.child("id").setValue(userIDFriend);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        final DatabaseReference chat2 = chatRef
                .child(userIDFriend)
                .child(firebaseUser.getUid());
        chat2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chat2.child("id").setValue(firebaseUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        withPicture = false;
        picture_selected.setVisibility(View.GONE);

    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    private void readMessage(final String myId, final String userID, final String imageUrl) {
        listData = new ArrayList<>();

        referenceFromChats = FirebaseDatabase.getInstance().getReference("Chats");
        referenceFromChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listData.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    if ((chat.getSender().equals(myId) && chat.getReceiver().equals(userID))
                            || (chat.getSender().equals(userID) && chat.getReceiver().equals(myId))) {
                        listData.add(chat);
                    }
                }
                messageAdapter = new MessageAdapter(listData, imageUrl, getApplicationContext());
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setOnOff(String onOff) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", onOff);

        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("Status")
                .child(firebaseUser.getUid());

        databaseReference.updateChildren(hashMap);
    }

    private void seenMessage(final String userIDFriends) {
        eventListener = referenceFromChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    assert chat != null;
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userIDFriends)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isSeen", "true");
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        setOnOff("online");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        seenMessage(userIDFriend);
    }

    @Override
    protected void onPause() {
        super.onPause();
        referenceFromChats.removeEventListener(eventListener);
        setOnOff("offline");

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_send){
            String currentText = texSend.getText().toString();

            if (!TextUtils.isEmpty(currentText)){
                sendMessage(firebaseUser.getUid(), userIDFriend, currentText, false);
            } else if (withPicture){
                sendMessage(firebaseUser.getUid(), userIDFriend, currentText, true);
            } else {
                Toast.makeText(this, "Message is Empty", Toast.LENGTH_LONG).show();
            }

            texSend.setText("");
        }

        if (view.getId() == R.id.tv_add_picture){
            requestPermission();
        }
    }

    private void requestPermission(){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE);
                }else {
                    readDataExternal();
                }
            }else {
                readDataExternal();
            }
    }

    private void readDataExternal(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == IMAGE_REQUEST_CODE) {
                imageUri = data.getData();
                withPicture = true;
                showPicture();
            }
        }
    }

    private void showPicture() {
        picture_selected.setVisibility(View.VISIBLE);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
            picture_selected.setImageBitmap(bitmap);
        } catch (IOException e) {
            Log.e("Error",e.getMessage());
            e.printStackTrace();
        }
    }
}
