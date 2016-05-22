package com.example.mibu.encryptedsmsapp;


/**
 * Created by Mibu on 30-Apr-16.
 */
public class Chat_Message {

    public static final int TYPE_LEFT = 0;
    public static final int TYPE_RIGHT = 1;

    public String address;
    public String message;
    public String timestamp;
    public int type;

    public Chat_Message (String address,String message, String timestamp, int type){
        this.address = address;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getAddress(){

        return address;
    }

    public void setAddress(String address) {

        this.address = address;
    }

    public String getMessage(){

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getTimestamp() {

        return timestamp;
    }

    public void setTimestamp(String timestamp) {

        this.timestamp = timestamp;
    }


    public int getType(){
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
