package com.example.mibu.encryptedsmsapp;

import android.graphics.Bitmap;

/**
 * Created by Mibu on 27-Apr-16.
 */
public class Chat_Message {

    public String message= "";
    public String timestamp= "";
    public Bitmap image = null;
    public int left;

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

    public void setImage(Bitmap image)
    {
        this.image = image;
    }

    public Bitmap getImage()
    {
        return image;
    }

    public int getLeft(){
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }
}
