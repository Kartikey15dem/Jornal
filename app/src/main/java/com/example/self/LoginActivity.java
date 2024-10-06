package com.example.self;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.example.self.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding bin;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference collectionReference=db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bin = DataBindingUtil.setContentView(this, R.layout.activity_login);
        firebaseAuth=FirebaseAuth.getInstance();

        bin.createAcctButton.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateAccountActivity.class));
        });

        bin.loginButton.setOnClickListener(v -> {
            String  emailAddress=bin.email.getText().toString().trim();
            String password=bin.password.getText().toString().trim();

            loginEmailPassword(emailAddress,password);
        });
    }

    private void loginEmailPassword(String eml, String pwd) {
        bin.loginProgress.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(eml) && !TextUtils.isEmpty(pwd)) {
            firebaseAuth.signInWithEmailAndPassword(eml,pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                           FirebaseUser user=firebaseAuth.getCurrentUser();
                            assert user != null;
                            String currentUserId=user.getUid();

                            collectionReference
                                    .whereEqualTo("userId",currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                            if(error!=null){}
                                            assert value != null;
                                            if(!value.isEmpty()){
                                                bin.loginProgress.setVisibility(View.INVISIBLE);
                                                for(QueryDocumentSnapshot snapshot:value){
                                                    JournalApi journalApi =JournalApi.getInstance();
                                                    journalApi.setUsername(snapshot.getString("username"));
                                                    journalApi.setUserid(snapshot.getString("userId"));
                                                }
                                                startActivity(new Intent(LoginActivity.this,PostJournalActivity.class));
                                                finish();
                                            }
                                        }

                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            bin.loginProgress.setVisibility(View.INVISIBLE);
                        }
                    });
        }else{
            bin.loginProgress.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Please enter the email and password", Toast.LENGTH_SHORT).show();
        }
    }
}