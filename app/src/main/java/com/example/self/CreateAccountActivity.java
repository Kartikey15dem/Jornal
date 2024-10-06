package com.example.self;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.example.self.databinding.ActivityCreateAccountBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {
    private ActivityCreateAccountBinding b;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        b = DataBindingUtil.setContentView(this, R.layout.activity_create_account);

        firebaseAuth=FirebaseAuth.getInstance();
        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    //user already exists
                }
                else {
                    //user does not exist
                }
            }
        };
        b.acctButton.setOnClickListener(v -> {
            if(!TextUtils.isEmpty(b.emailAccount.getText().toString())&& !TextUtils.isEmpty(b.passwordAccount.getText().toString()) && !TextUtils.isEmpty(b.usernameAccount.getText().toString())) {
                String email = b.emailAccount.getText().toString().trim();
                String password = b.passwordAccount.getText().toString().trim();
                String username = b.usernameAccount.getText().toString().trim();
                createUserEmailAccount(email, password, username);
            }
            else{
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void createUserEmailAccount(String email, String password, String username) {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)) {
            b.acctProgress.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                           if (task.isSuccessful()) {
                               currentUser = firebaseAuth.getCurrentUser();
                               assert currentUser != null;
                               String currentUserId = currentUser.getUid();

                               Map<String, Object> userObj = new HashMap<>();
                               userObj.put("userId", currentUserId);
                               userObj.put("username", username);

                               collectionReference.add(userObj)
                                       .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                           @Override
                                           public void onSuccess(DocumentReference documentReference) {
                                               documentReference.get()
                                                       .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                           @Override
                                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                               if (task.getResult().exists()){
                                                                   b.acctProgress.setVisibility(View.INVISIBLE);
                                                                   String name = task.getResult().getString("username");

                                                                   JournalApi journalApi=JournalApi.getInstance();
                                                                   journalApi.setUsername(name);
                                                                   journalApi.setUserid(currentUserId);

                                                                   Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                                                   intent.putExtra("username", name);
                                                                   intent.putExtra("userId", currentUserId);
                                                                   startActivity(intent);
                                                                   finish();
                                                               }
                                                               else{
                                                                   b.acctProgress.setVisibility(View.INVISIBLE);
                                                               }
                                                           }
                                                       });
                                           }
                                       })
                                       .addOnFailureListener(new OnFailureListener() {
                                           @Override
                                           public void onFailure(@NonNull Exception e) {

                                           }
                                       });
                           }

                           else{

                           }
                        }

                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser=firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}