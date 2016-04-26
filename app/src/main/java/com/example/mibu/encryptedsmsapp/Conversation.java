package com.example.mibu.encryptedsmsapp;

import android.graphics.Bitmap;

/**
 * Created by Mibu on 27-Apr-16.
 */
public class Conversation {

    public String address= "";
    public String message= "";
    public String thread= "";
    public String phonenumber= "";
    public Bitmap image = null;

    public String getAddress() {
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

    public String getThread() {

        return thread;
    }

    public void setThread(String thread) {

        this.thread = thread;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {

        this.phonenumber = phonenumber;
    }

    public void setImage(Bitmap image)
    {
        this.image = image;
    }

    public Bitmap getImage()
    {
        return image;
    }


}
