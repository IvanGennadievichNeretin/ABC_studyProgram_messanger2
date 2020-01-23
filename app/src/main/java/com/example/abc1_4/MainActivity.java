package com.example.abc1_4;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vk.sdk.VKSdk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        //VKSdk.initialize(this.getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.signOut();
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);

    }

    public void onSignUp(View view){
        EditText editEmail = findViewById(R.id.loginEdit);
        String email = editEmail.getText().toString();
        EditText editPassword = findViewById(R.id.passwordEdit);
        String password = editPassword.getText().toString();
        mAuth.signOut();
        if ((!email.equals(""))&&(!password.equals(""))){
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                //Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(MainActivity.this, "Authentication successeful!.",Toast.LENGTH_SHORT).show();
                                //updateUI(user);
                                Intent intent = new Intent(MainActivity.this,ContentActivity.class);
                                //intent.putExtra(FirebaseAuth.class.getSimpleName(), mAuth);
                                startActivity(intent);
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(MainActivity.this, "Authentication failed. Invalid login or password",Toast.LENGTH_SHORT).show();
                                //Log.w(TAG, "signInWithEmail:failure", task.getException());
                                //Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                                //        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    });
        }
        else{
            Toast.makeText(MainActivity.this, "Нужно заполнить все поля",Toast.LENGTH_SHORT).show();
        }
    }

    public void getCurrentUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();
        }
    }

    public void onRegButtonClick(View view){
        Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
        startActivity(intent);
    }

    public void onVKButtonClick(View view){

    }

}
