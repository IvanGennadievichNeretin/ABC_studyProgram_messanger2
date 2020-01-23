package com.example.abc1_4.Classes;

public class UserMessage {
    private String message;
    private String author;
    public UserMessage(String yourMessage, String authorName){
        message=yourMessage;
        author=authorName;
    }
    public UserMessage(){
        message="empty message";
        author="unknown author";
    }
    public String getMessage(){
        return this.message;
    }
    public String getAuthor(){
        return this.author;
    }
}
