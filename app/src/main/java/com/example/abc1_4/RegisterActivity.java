package com.example.abc1_4;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.abc1_4.Classes.ListOfUsers;
import com.example.abc1_4.Classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference refDataBase;
    private String DATABASE_URL;
    private ValueEventListener postListener;
    private ListOfUsers listOfUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity_main);
        mAuth = FirebaseAuth.getInstance();
        DATABASE_URL = "https://client-server-9bcdf.firebaseio.com";
        refDataBase = FirebaseDatabase.getInstance().getReference();
        listOfUsers = new ListOfUsers();
        //добавить слушателя с обновлением листа информации о пользователях
        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listOfUsers = dataSnapshot.child("users_info").getValue(ListOfUsers.class);
                if (listOfUsers == null){
                    listOfUsers = new ListOfUsers();
                }
                else{

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Toast.makeText(RegisterActivity.this, "Changes cancelled. " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        refDataBase.addValueEventListener(postListener);
    }

    @Override
    public void onStart(){
        super.onStart();
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    //регистрация пользователя
    public void onRegister(View view){
        EditText editEmail = findViewById(R.id.loginEditReg);
        final String email = editEmail.getText().toString();
        EditText editPassword = findViewById(R.id.passwordEditReg);
        final String password = editPassword.getText().toString();
        EditText editName = findViewById(R.id.usernameEditReg);
        final String name = editName.getText().toString();

        if (loginIsCorrect(email) && passwordIsCorrect(password) && nameIsCorrect(name) && passwordsAreEqual() && !userIsExist(name, email)){
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                //добавить пользователя в пул
                                mAuth.signInWithEmailAndPassword(email, password);
                                User newUser = new User(name, email);
                                addUserToDataBase(newUser);
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name).build();
                                mAuth.getCurrentUser().updateProfile(profileUpdates);
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else{
            if (!loginIsCorrect(email)){
                Toast.makeText(RegisterActivity.this, "Некорректный логин. Поле содержит некорректные " +
                        "символы или пользователь с таким логином уже существует", Toast.LENGTH_SHORT).show();
            }
            if (!passwordIsCorrect(password)){
                Toast.makeText(RegisterActivity.this, "Некорректный пароль", Toast.LENGTH_SHORT).show();
            }
            if(!passwordsAreEqual()){
                Toast.makeText(RegisterActivity.this, "Пароли должны совпадать", Toast.LENGTH_SHORT).show();
            }
            if(!nameIsCorrect(name)){
                Toast.makeText(RegisterActivity.this, "Некорректное имя пользователя", Toast.LENGTH_SHORT).show();
            }
            if (userIsExist(name, email)){
                Toast.makeText(RegisterActivity.this, "Пользователь с таким именем или email уже существует", Toast.LENGTH_SHORT).show();
            }
        }


    }

    public void addUserToDataBase(User user){
        listOfUsers.add(user);
        refDataBase.child("users_info").setValue(listOfUsers);
    }

    public void onBackButton(View view){
        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(intent);
    }

    //проверка на присутствие только символов латинского алфавита
    private boolean loginIsCorrect(String login){
        boolean check = true;
        int i;
        if ((login.length() >= 3)){
            for (i = 0; i < login.length(); i++){
                //отказ, если содержится пробел
                if (login.codePointAt(i) == 32)
                {
                    check = false;
                }

            }
        }
        else {check = false;}


        return check;
    }

    //проверка пароля
    private boolean passwordIsCorrect(String password){
        boolean check = true;
        int i;
        if ((password.length() >= 6)){
            for (i = 0; i < password.length(); i++){
                //отказ, если содержится некорректный символ
                if ((password.codePointAt(i) < 33)||(password.codePointAt(i) > 126))
                {
                    check = false;
                }

            }
        }
        else {check = false;}
        return check;
    }

    private boolean nameIsCorrect(String name){
        boolean check = true;
        int i;
        if ((name.length() >= 3)){
            for (i = 0; i < name.length(); i++){
                //отказ, если содержится символ не латинского алфавита или не цифра
                if ((name.codePointAt(i) < 48)||(name.codePointAt(i) > 122)
                        ||((name.codePointAt(i) >= 58)&&(name.codePointAt(i) <= 64))
                        ||((name.codePointAt(i) >= 91)&&(name.codePointAt(i) <= 96))
                )
                {
                    check = false;
                }

            }
        }
        else {check = false;}

        return check;
    }

    private boolean passwordsAreEqual(){
        EditText editPassword = findViewById(R.id.passwordEditReg);
        EditText editRepeatPassword = findViewById(R.id.password_repeat_EditReg);
        String a = editPassword.getText().toString();
        String b = editRepeatPassword.getText().toString();
        return (a.equals(b));
    }

    private boolean userIsExist(String name, String Email){
        boolean check = false;
        int i;
        for (i = 0; i < listOfUsers.size(); i++){
            if (name.equals(listOfUsers.get(i).username)) check = true;
            if (Email.equals(listOfUsers.get(i).email)) check = true;
        }
        return check;
    }
}
