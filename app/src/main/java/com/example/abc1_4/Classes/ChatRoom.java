package com.example.abc1_4.Classes;

import java.util.HashMap;
import java.util.Map;

public class ChatRoom {
    private ChatHistory chatHistory;
    //private RoomHeader roomHeader;
    private int id;
    private String owner;
    public ChatRoom(){
        this.chatHistory = new ChatHistory();
        //this.roomHeader = new RoomHeader("roomHeader", false);
        this.owner = "unknown";
    }
    public ChatRoom(String name, boolean password){
        this.chatHistory = new ChatHistory();
        //this.roomHeader = new RoomHeader(name, password);
        this.owner = "unknown";
    }

    public ChatHistory getChatHistory() {
        return this.chatHistory;
    }

    //public RoomHeader getRoomHeader() {
    //    return this.roomHeader;
    //}
    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return this.id;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("ChatHistory", chatHistory.toMap());
        result.put("id", id);
        result.put("owner", owner);
        return result;
    }
}
