package com.example.self;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;


import com.example.self.databinding.ActivityPostJournalBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

public class PostJournalActivity extends AppCompatActivity {
    private ActivityPostJournalBinding bi;
    private static final int GALLERY_CODE = 1;

    private String currentUserId;
    private String currentUserNane;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private Uri imageUri;

    private CollectionReference collectionReference = db.collection("Journal");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_journal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        bi = DataBindingUtil.setContentView(this, R.layout.activity_post_journal);
        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        bi.postProgressbar.setVisibility(View.INVISIBLE);

        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserid();
            currentUserNane = JournalApi.getInstance().getUsername();


        }
        authStateListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                } else {
                }
            }
        };
        bi.postCameraButton.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_CODE);
        });
        bi.myJournalButton.setOnClickListener(v -> {
            startActivity(new Intent(PostJournalActivity.this,JournalListActivity.class));
        });

        bi.postButton.setOnClickListener(v -> {
            saveJournal();
        });

    }

    private void saveJournal() {
        String title=bi.postTitleEt.getText().toString().trim();
        String thoughts=bi.postDescriptionEt.getText().toString().trim();

        if(!TextUtils.isEmpty(title)&& !TextUtils.isEmpty(thoughts)&& imageUri!=null){

            StorageReference filepath=storageReference
                    .child("journal_images")
                    .child("my_image_"+ Timestamp.now().getSeconds());

            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String imageUrl=uri.toString();
                                    //Todo: create a journal object
                                    Journal journal= new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thoughts);
                                    journal.setImageUrl(imageUrl);
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUserId(currentUserId);
                                    journal.setUserName(currentUserNane);

                                    //Todo: invoke our collectionReference
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    bi.postProgressbar.setVisibility(View.INVISIBLE);
                                                    startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });
                                    //Todo:and save a Journal instance
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                          bi.postProgressbar.setVisibility(View.INVISIBLE);

                        }
                    });

        }else{
            bi.postProgressbar.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE && resultCode==RESULT_OK) {
            if (data != null) {
                imageUri = data.getData();
                bi.postImageView.setImageURI(imageUri);
            }
        }
    }
}