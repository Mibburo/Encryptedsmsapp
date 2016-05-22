package com.example.mibu.encryptedsmsapp;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Mibu on 14-May-16.
 */
public class PassWindow extends AppCompatActivity{

    Button decButton;
    Button replaceButton;
    EditText decPass;

    TextView decView;
    TextView header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Typeface courier = Typeface.createFromAsset(getAssets(), "cour.ttf");

        setContentView(R.layout.passprompt);
        header = (TextView) findViewById(R.id.passheader);
        header.setTypeface(courier);

        DisplayMetrics dmetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dmetrics);

        int width = dmetrics.widthPixels;
        int height = dmetrics.heightPixels;

        getWindow().setLayout((int)(width*.8), (int)(height*.6));

        Intent passprompt = getIntent();

        final String address = passprompt.getStringExtra("address");
        final String msg = passprompt.getStringExtra("msg");
        final String timestamp = passprompt.getStringExtra("timestamp");
        final int type = passprompt.getExtras().getInt("type");

        decPass = (EditText) findViewById(R.id.decpass);
        decPass.setTypeface(courier);
        decButton = (Button) findViewById(R.id.decbutton);
        decButton.setTypeface(courier);
        decView = (TextView) findViewById(R.id.decview);
        decView.setTypeface(courier);
        replaceButton = (Button) findViewById(R.id.replacebutton);
        replaceButton.setTypeface(courier);

        decButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String pass = decPass.getText().toString();
                //String encMsg = msg;
                String messageAfterDecrypt = null;
                try {
                    messageAfterDecrypt = AESEnc.decrypt(msg, pass);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                decView.setText(messageAfterDecrypt);
            }
        });

        replaceButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!decView.getText().toString().equals("")) {
                    long millidate = 0;

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm:ss");
                    try {
                        Date date = sdf.parse(timestamp);
                        millidate = date.getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (type == Chat_Message.TYPE_RIGHT) {
                        ContentValues values = new ContentValues();
                        values.put("address", address); // phone number to send
                        if (millidate != 0) {
                            values.put("date", millidate);
                        }
                        values.put("read", "1"); // if you want to mark is as unread set to 0
                        values.put("type", "2"); // 2 means sent message
                        values.put("body", decView.getText().toString());

                        Uri uri = Uri.parse("content://sms/");
                        Uri rowUri = getApplicationContext().getContentResolver().insert(uri, values);
                        Toast.makeText(getApplicationContext(), "Message will be stored above the chosen encrypted one", Toast.LENGTH_LONG).show();
                    } else {
                        ContentValues values = new ContentValues();
                        values.put("address", address); // phone number to send
                        if (millidate != 0) {
                            values.put("date", millidate);
                        }
                        values.put("read", "1"); // if you want to mark is as unread set to 0
                        values.put("type", "1"); // 2 means sent message
                        values.put("body", decView.getText().toString());

                        Uri uri = Uri.parse("content://sms/");
                        Uri rowUri = getApplicationContext().getContentResolver().insert(uri, values);
                        Toast.makeText(getApplicationContext(), "Message will be stored above the chosen encrypted one", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), "No Decrypted Message to Store", Toast.LENGTH_LONG).show();
                }
            }

        });

    }
}
