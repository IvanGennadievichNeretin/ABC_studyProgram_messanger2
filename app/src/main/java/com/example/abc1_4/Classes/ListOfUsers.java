package com.example.abc1_4.Classes;

import java.util.ArrayList;
import java.util.List;

public class ListOfUsers {
    public List<User> listOfUsers;
    private int id;
    public ListOfUsers(){
        listOfUsers = new ArrayList<User>();
        id = 1;
        User newUser = new User("unknown","unknown");
        listOfUsers.add(newUser);
    }
    public void add(User user){
        listOfUsers.add(user);
    }
    public User get(int i){
        return listOfUsers.get(i);
    }
    public int size(){
        return listOfUsers.size();
    }
    public void clear(){
        listOfUsers.clear();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public User getUser(int i){
        return listOfUsers.get(i);
    }
}
