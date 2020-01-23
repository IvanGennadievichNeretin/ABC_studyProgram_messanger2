package com.example.abc1_4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.abc1_4.Classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ContentActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference refDataBase;
    private User userdata;
    private String DATABASE_URL;
    private ValueEventListener postListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        mAuth = FirebaseAuth.getInstance();
        DATABASE_URL = "https://client-server-9bcdf.firebaseio.com";
        refDataBase = FirebaseDatabase.getInstance().getReference();


        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Toast.makeText(ContentActivity.this, "Changes cancelled. " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        refDataBase.addValueEventListener(postListener);

    }

    @Override
    public void onStart(){
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        TextView textview1 = findViewById(R.id.WelcomeText);
        String name = currentUser.getDisplayName();
        if (name == null) name = currentUser.getEmail();
        String newText = "Welcome, " + name;
        textview1.setText(newText);
    }

    @Override
    public void onStop(){
        refDataBase.removeEventListener(postListener);
        super.onStop();
    }

    public void onBackButton_Data(View view){
        mAuth.signOut();
        refDataBase.removeEventListener(postListener);
        Intent intent = new Intent(ContentActivity.this,MainActivity.class);
        startActivity(intent);
    }

    public void onStartMessagingButton(View view){
        refDataBase.removeEventListener(postListener);
        Intent intent = new Intent(ContentActivity.this,RoomsListActivity.class);
        startActivity(intent);
    }
}
