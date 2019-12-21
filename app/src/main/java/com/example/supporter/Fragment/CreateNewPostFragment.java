package com.example.supporter.Fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreateNewPostFragment extends Fragment implements View.OnClickListener {

    //storage
    private StorageReference mStorageRef;
    private StorageTask uploadTask;
    private static final int READ_STORAGE = 1000;
    private static final int IMAGE_REQUEST_CODE = 100;
    private EditText contentPost;
    private ImageView picturePost;
    private Button btCreate;

    private View rootView;

    private Uri imageUri;

    private NavController navController;

    private LinearLayout llProgressBar;

    //FireBase
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    private String stringUserName;
    private String stringImageURL;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_create_new_post, container, false);
        initView(rootView);
        initFireBase();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                stringUserName = user.getUserName();
                stringImageURL = user.getAvatarURL();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        contentPost.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
    }

    private void initFireBase() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Posts").child(String.valueOf(System.currentTimeMillis()));
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads").child(firebaseUser.getUid());
    }

    private void initView(View rootView) {
        llProgressBar = rootView.findViewById(R.id.llProgressBar);
        contentPost = rootView.findViewById(R.id.new_post_content);
        picturePost = rootView.findViewById(R.id.new_post_picture);
        btCreate = rootView.findViewById(R.id.new_post_create);
        btCreate.setOnClickListener(this);
        picturePost.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_post_picture:
                requestPermission();
                break;
            case R.id.new_post_create:
                if (!contentPost.getText().toString().isEmpty()){
                    uploadPost();
                }
                break;
            default:
                break;
        }
    }

    private void uploadPost() {

        final HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userName",stringUserName);
        hashMap.put("imageURL",stringImageURL);
        hashMap.put("id",firebaseUser.getUid());
        hashMap.put("picturePost","default");
        hashMap.put("time", getCurrentTime());
        hashMap.put("contentPost",contentPost.getText().toString());

        llProgressBar.setVisibility(View.VISIBLE);

        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    if (imageUri!=null){
                        if (uploadTask != null && uploadTask.isInProgress()) {
                            Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_LONG).show();
                        } else
                            fileUploader();
                    }

                    navController.navigate(R.id.action_createNewPostFragment_to_menu_news);

                }else {
                    Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                    llProgressBar.setVisibility(View.GONE);
                }
            }
        });




    }

    private void fileUploader() {
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

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("picturePost", mUri);
                    reference.updateChildren(hashMap);

                } else {
                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
                llProgressBar.setVisibility(View.GONE);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCurrentTime() {

        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatDay = new SimpleDateFormat("dd/MM/yyyy");
        String day = formatDay.format(calendar.getTime());
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatHours = new SimpleDateFormat("hh:mm:ss");
        String hours = formatHours.format(calendar.getTime());

        return hours+" "+day;
    }

    private void requestPermission() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE);
                } else {
                    readDataExternal();
                }
            } else {
                readDataExternal();
            }

    }

    private void readDataExternal(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data !=null && data.getData() != null){

            if (requestCode == IMAGE_REQUEST_CODE){
                imageUri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),imageUri);
                    picturePost.setImageBitmap(bitmap);
                } catch (IOException e) {
                    Log.e("Error",e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

}
