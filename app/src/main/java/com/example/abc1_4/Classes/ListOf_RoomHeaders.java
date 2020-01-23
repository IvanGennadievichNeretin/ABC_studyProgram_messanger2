package com.example.abc1_4.Classes;

import java.util.ArrayList;
import java.util.List;

public class ListOf_RoomHeaders {
    public List<RoomHeader> HeadersList;
    public ListOf_RoomHeaders(){
        HeadersList = new ArrayList<RoomHeader>();
        RoomHeader newRoomHeader = new RoomHeader("unknown",false);
        HeadersList.add(newRoomHeader);
    }

    public void add(RoomHeader roomHeader){
        HeadersList.add(roomHeader);
    }
    public void clear(){
        HeadersList.clear();
    }
    public int GetSize(){
        return HeadersList.size();
    }
    public RoomHeader getHeader(int num){
        return HeadersList.get(num);
    }
    public RoomHeader findHeaderByName(String name){
        int i;
        int size = HeadersList.size();
        for (i = 0; i < size; i++){
            if (HeadersList.get(i) != null){
                if (name.equals(HeadersList.get(i).getRoomName()))
                    return HeadersList.get(i);
            }
        }
        return null;
    }
    public int findNumberByName(String name){
        int i;
        int size = HeadersList.size();
        for (i = 0; i < size; i++){
            if (HeadersList.get(i) != null){
                if (name.equals(HeadersList.get(i).getRoomName()))
                    return i;
            }
        }
        return -1;
    }
}
