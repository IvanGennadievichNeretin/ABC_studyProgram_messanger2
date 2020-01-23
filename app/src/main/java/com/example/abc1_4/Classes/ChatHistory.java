package com.example.abc1_4.Classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHistory {
    public List<UserMessage> userMessagesHistory;
    public ChatHistory(){
        userMessagesHistory = new ArrayList<UserMessage>();

    }
    public void Add(UserMessage _userMessage){
        this.userMessagesHistory.add(_userMessage);
    }
    public void Clear(){
        this.userMessagesHistory.clear();
    }
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        int i;
        for (i = 0; i < this.userMessagesHistory.size(); i++){
            result.put(Integer.toString(i), userMessagesHistory.get(i));
        }
        return result;
    }
}
