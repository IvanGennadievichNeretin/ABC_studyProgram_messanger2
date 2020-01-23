package com.example.abc1_4.Classes;

public class RoomHeader {
    private String RoomName;
    private boolean password;
    private String passwordValue;
    public RoomHeader(){
        RoomName = "RoomName";
        password = false;
    }
    public RoomHeader(String name, boolean Password){
        RoomName = name;
        password = Password;
        passwordValue = "noPassword";
    }
    public String getRoomName(){
        return this.RoomName;
    }
    public boolean isPassword(){
        return password;
    }
    public String getPasswordValue() {
        return passwordValue;
    }
    public void setRoomName(String roomName){
        this.RoomName=roomName;
    }

    public void setPassword(boolean password) {
        this.password = password;
    }
    public void setPasswordValue(String password) {
        this.passwordValue = password;
    }
    public boolean enteredPasswordIsCorrect(String enteredPassword){
        return this.passwordValue.equals(enteredPassword);
    }
}
