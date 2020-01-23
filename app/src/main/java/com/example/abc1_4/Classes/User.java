package com.example.abc1_4.Classes;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String username;
    public String email;

    public User(){
        this.username = "";
        this.email = "";
    }

    public User(String username, String email){
        this.username = username;
        this.email = email;
    }

    public String getUsername(){
        return this.username;
    }
    public String getEmail(){return this.email;}

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
