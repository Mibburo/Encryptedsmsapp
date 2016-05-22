package com.example.mibu.encryptedsmsapp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Mibu on 01-May-16.
 */
public class Chat_Activity extends AppCompatActivity {

    private static Chat_Activity inst;
    public Chat_Activity(){}
    private EditText chatText;
    private EditText encPass;
    Button buttonSend;
    IntentFilter intentFilter;
    ImageView contactPic;
    TextView headerName;
    TextView chatCounter;
    public static Chat_Activity getInstance() {
        if (inst == null) {
            inst = new Chat_Activity();
        }
        return inst;
    }
    List<Chat_Message> chatlist = new ArrayList<>();
    Chat_Adapter adapter;
    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

    }


    @Override
    public void onStart() {
        super.onStart();
        inst = this;

        Typeface courier = Typeface.createFromAsset(getAssets(), "cour.ttf");
        // Intent to filter received sms messages
        intentFilter = new IntentFilter();
        intentFilter.addAction("SMS_RECEIVED_ACTION");
        buttonSend = (Button) findViewById(R.id.buttonSend);
        buttonSend.setTypeface(courier);
        chatText = (EditText) findViewById(R.id.chatText);
        chatText.setTypeface(courier);

        chatCounter = (TextView) findViewById(R.id.counterview);

        chatText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int aft)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (!encPass.getText().toString().equals("")){
                // this will show characters remaining
                chatCounter.setText(92 - s.toString().length() + "/92");
                }else{
                    chatCounter.setText(160 - s.toString().length() + "/160");
                }
            }
        });


        encPass = (EditText) findViewById(R.id.encPass);
        encPass.setTypeface(courier);
        contactPic = (ImageView) findViewById(R.id.contactpic);
        headerName = (TextView) findViewById(R.id.chatheader);

        Intent compose = getIntent();

        final String conaddress = compose.getStringExtra("conaddress");

        final String phoneno = compose.getStringExtra("phoneno");

        final String threadid = compose.getStringExtra("threadid");
        final String name = compose.getStringExtra("name");

        Bitmap contactpic = BitmapFactory.decodeByteArray(getIntent().getByteArrayExtra("contactpic"),0,getIntent().getByteArrayExtra("contactpic").length);
        contactPic.setImageBitmap(contactpic);
        headerName.setText(name);
        headerName.setTypeface(courier);

        // Calls the sendChatMessage method when the button is clicked
        buttonSend.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View arg0) {

        sendChatMessage(phoneno);
        }
        });


        adapter = new Chat_Adapter(this, chatlist);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, OrientationHelper.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView = (RecyclerView) findViewById(R.id.SMSChatList);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        getSMSChatData(threadid, phoneno);

    }

    private void sendChatMessage(String phoneno) {

        // Converts the typed Message to String
        String myMsg = chatText.getText().toString();
        // Converts the typed Password to String
        String myPass = encPass.getText().toString();


        if (!TextUtils.isEmpty(myPass)){

            if (BuildConfig.DEBUG) {
                AESEnc.DEBUG_LOG_ENABLED = true;
            }

            // Performs Encryption of the typed Message using the typed Password (calls the encryption method from AESEnc class)
            String encryptedMsg = null;
            try {
                encryptedMsg = AESEnc.encrypt(myMsg, myPass);
            } catch (Exception e) {
                e.printStackTrace();
            }
            chatText.setText("");

            // Sends the Encrypted Message via SMS
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneno, null, encryptedMsg, null, null);
                Toast.makeText(getApplicationContext(), "SMS sent.",
                        Toast.LENGTH_LONG).show();
                chatlist.add(new Chat_Message(null, encryptedMsg, "Just Now", Chat_Message.TYPE_RIGHT));
                adapter.notifyItemInserted(adapter.getItemCount());
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
                Conversation_Activity conv_inst = Conversation_Activity.instance();
                conv_inst.updateList(phoneno, encryptedMsg);

                ContentValues values = new ContentValues();
                values.put("address",phoneno); // phone number to send
                values.put("date", System.currentTimeMillis()+"");
                values.put("read", "1"); // if you want to mark is as unread set to 0
                values.put("type", "2"); // 2 means sent message
                values.put("body", encryptedMsg);

                Uri uri = Uri.parse("content://sms/");
                Uri rowUri = getApplicationContext().getContentResolver().insert(uri,values);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Sending SMS failed.",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        else {
            chatText.setText("");
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneno, null, myMsg, null, null);
                Toast.makeText(getApplicationContext(), "SMS sent.",
                        Toast.LENGTH_LONG).show();
                chatlist.add(new Chat_Message(null, myMsg, "Just Now", Chat_Message.TYPE_RIGHT));
                adapter.notifyItemInserted(adapter.getItemCount());
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
                Conversation_Activity conv_inst = Conversation_Activity.instance();
                conv_inst.updateList(phoneno, myMsg);

                ContentValues values = new ContentValues();
                values.put("address",phoneno); // phone number to send
                values.put("date", System.currentTimeMillis()+"");
                values.put("read", "1"); // if you want to mark is as unread set to 0
                values.put("type", "2"); // 2 means sent message
                values.put("body", myMsg);

                Uri uri = Uri.parse("content://sms/");
                Uri rowUri = getApplicationContext().getContentResolver().insert(uri,values);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Sending SMS failed.",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void getSMSChatData(String threadid, String phoneno) {

            ContentResolver contentResolver = getContentResolver();
            Cursor smsChatCursor = contentResolver.query(Uri.parse("content://sms/"), null, null, null, "DATE desc");
            int indexBody = smsChatCursor.getColumnIndex("body");
            int indexAddress = smsChatCursor.getColumnIndex("address");
            int indexThread = smsChatCursor.getColumnIndex("thread_id");
            int indexDate = smsChatCursor.getColumnIndex("date");
            int indexType = smsChatCursor.getColumnIndex("type");

            if (indexBody < 0 || !smsChatCursor.moveToLast()) {
                return;
            }
            //adapter.clear();
            do {
                String address = smsChatCursor.getString(indexAddress);
                String inout = smsChatCursor.getString(indexType);
                Date date = new Date(smsChatCursor.getLong(indexDate));
                ContentResolver cr = getContentResolver();
                String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(date);

                String thread = smsChatCursor.getString(indexThread);

                    if ((thread.equals(threadid)|| phoneno.equals(smsChatCursor.getString(indexAddress))) && inout.equals("1")) {
                        chatlist.add(new Chat_Message(smsChatCursor.getString(indexAddress), smsChatCursor.getString(indexBody), timestamp, Chat_Message.TYPE_LEFT));
                    }

                    if ((thread.equals(threadid)|| phoneno.equals(smsChatCursor.getString(indexAddress))) && inout.equals("2")) {
                        chatlist.add(new Chat_Message(smsChatCursor.getString(indexAddress),smsChatCursor.getString(indexBody), timestamp, Chat_Message.TYPE_RIGHT));
                    }

            } while (smsChatCursor.moveToPrevious());

            smsChatCursor.close();


        }

    public String GetCountryZipCode(){
        String CountryID="";
        String CountryZipCode="";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        CountryID= manager.getSimCountryIso().toUpperCase();
        String[] rl=this.getResources().getStringArray(R.array.CountryCodes);
        for(int i=0;i<rl.length;i++){
            String[] g=rl[i].split(",");
            if(g[1].trim().equals(CountryID.trim())){
                CountryZipCode=g[0];
                break;
            }
        }
        return CountryZipCode;
    }

    public void updateList(String smsAddress , String smsMessageStr) {
        if (adapter != null) {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                Chat_Message tempValue = chatlist.get(i);
                String phoneno = tempValue.getAddress();
                int type = tempValue.getType();
                if (type == Chat_Message.TYPE_LEFT) {
                    if (smsAddress.equals(phoneno) || (smsAddress.replaceAll("[^0-9\\+]", "")).equals("+" + GetCountryZipCode() + phoneno.replaceAll("[^0-9\\+]", "")) || (smsAddress.equals(phoneno.substring(GetCountryZipCode().length()+1)))) {
                        chatlist.add(new Chat_Message(smsAddress, smsMessageStr, "Just Now", Chat_Message.TYPE_LEFT));
                        adapter.notifyItemInserted(adapter.getItemCount());
                        recyclerView.smoothScrollToPosition(adapter.getItemCount());
                        break;
                    }
                }
            }
        }
    }

    public void onItemClick(int mPosition) {

        final Chat_Message tempValues =  chatlist.get(mPosition);

        try{
            Intent passprompt = new Intent(getApplicationContext(),PassWindow.class);
            String address = tempValues.getAddress();
            String msg = tempValues.getMessage();
            String timestamp = tempValues.getTimestamp();
            int type = tempValues.getType();
            passprompt.putExtra("address", address);
            passprompt.putExtra("msg", msg);
            passprompt.putExtra("timestamp", timestamp);
            passprompt.putExtra("type", type);
            startActivity(passprompt);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
