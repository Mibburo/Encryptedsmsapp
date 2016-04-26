package com.example.mibu.encryptedsmsapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Mibu on 27-Apr-16.
 */
public class Chat_Activity extends Activity {

    private static Chat_Activity inst;

    private EditText chatText;
    private EditText encPass;
    Button buttonSend;
    IntentFilter intentFilter;
    Button decButton;
    EditText decPass;
    TextView decView;

    ListView list;
    Chat_Adapter adapter;
    public  Chat_Activity ChatListView = null;
    public ArrayList<Chat_Message> chatarray = new ArrayList<Chat_Message>();


    public static Chat_Activity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        // Generates the Array that holds the messages
        ChatListView = this;
        list = (ListView) findViewById(R.id.SMSChatList);

        // Intent to filter received sms messages
        intentFilter = new IntentFilter();
        intentFilter.addAction("SMS_RECEIVED_ACTION");
        buttonSend = (Button) findViewById(R.id.buttonSend);
        chatText = (EditText) findViewById(R.id.chatText);
        encPass = (EditText) findViewById(R.id.encPass);

        Intent compose = getIntent();

        final String conaddress = compose.getStringExtra("conaddress");

        final String phoneno = compose.getStringExtra("phoneno");

        final String threadid = compose.getStringExtra("threadid");

        // Calls the sendChatMessage method when the button is clicked
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                sendChatMessage(phoneno);
            }
        });

        //list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        refreshSMSChat(threadid, phoneno);
        //adapter = new Chatadapter(getApplicationContext(), R.layout.row_right);
        adapter = new Chat_Adapter(ChatListView, chatarray);
        list.setAdapter(adapter);
        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
    }

    // Method that sends the SMS
    private void sendChatMessage(String phoneno) {

        // Converts the typed Message to String
        String myMsg = chatText.getText().toString();
        // Converts the typed Password to String
        String myPass = encPass.getText().toString();

        final Chat_Message chmsg = new Chat_Message();

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
                chmsg.setMessage(encryptedMsg);
                chmsg.setLeft(2);
                chatarray.add(chmsg);
                adapter.notifyDataSetChanged();
                scroll();
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
                chmsg.setMessage(myMsg);
                chmsg.setLeft(2);
                chatarray.add(chmsg);
                adapter.notifyDataSetChanged();
                scroll();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "Sending SMS failed.",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void refreshSMSChat(String threadid, String phoneno) {

        ContentResolver contentResolver = getContentResolver();
        Cursor smsChatCursor = contentResolver.query(Uri.parse("content://sms/"), null, null, null, "DATE desc");
        int indexBody = smsChatCursor.getColumnIndex("body");
        int indexAddress = smsChatCursor.getColumnIndex("address");
        int indexThread = smsChatCursor.getColumnIndex("thread_id");
        int indexId = smsChatCursor.getColumnIndex("_id");
        int indexPerson = smsChatCursor.getColumnIndex("person");
        int indexType = smsChatCursor.getColumnIndex("type");
        int indexCreator = smsChatCursor.getColumnIndex("creator");
        if (indexBody < 0 || !smsChatCursor.moveToLast()) return;
        do {
            String address = smsChatCursor.getString(indexAddress);
            String inout = smsChatCursor.getString(indexType);
            ContentResolver cr = getContentResolver();
            Uri curi = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(address));
            Cursor contactCursor = cr.query(curi,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
            final Chat_Message chmsg = new Chat_Message();


            String thread = smsChatCursor.getString(indexThread);

            if (contactCursor.getCount() > 0) {
                contactCursor.moveToFirst();

                String contactId = contactCursor.getString(contactCursor
                        .getColumnIndex(ContactsContract.PhoneLookup._ID));

                InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));


                String name = contactCursor.getString(contactCursor
                        .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));

                if (thread.equals(threadid)  && inout.equals("1")) {

                    chmsg.setLeft(1);
                    chmsg.setMessage(smsChatCursor.getString(indexBody));
                    chmsg.setImage(BitmapFactory.decodeStream(input));
                    chatarray.add(chmsg);
                }
                if (thread.equals(threadid) && inout.equals("2")) {

                    chmsg.setLeft(2);
                    chmsg.setMessage(smsChatCursor.getString(indexBody));
                    chmsg.setImage(BitmapFactory.decodeStream(input));
                    chatarray.add(chmsg);
                }
            } else {
                if (thread.equals(threadid)&& inout.equals("1")) {
                    chmsg.setLeft(1);
                    chmsg.setMessage(smsChatCursor.getString(indexBody));
                    chatarray.add(chmsg);

                }
                if (thread.equals(threadid) && inout.equals("2")) {
                    chmsg.setLeft(2);
                    chmsg.setMessage(smsChatCursor.getString(indexBody));
                    chatarray.add(chmsg);

                }

            }
            contactCursor.close();
        } while (smsChatCursor.moveToPrevious());

        smsChatCursor.close();
    }

    public void updateList(final String smsMessage) {
        final Chat_Message chmsg = new Chat_Message();
        chmsg.setMessage(smsMessage);
        chmsg.setLeft(1);
        chatarray.add(chmsg);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        list.setSelection(list.getCount() - 1);
    }

    public void onItemClick(int mPosition) {

        final Chat_Message tempValues = (Chat_Message) chatarray.get(mPosition);

        setContentView(R.layout.passprompt);
        decPass = (EditText) findViewById(R.id.decpass);
        decButton = (Button) findViewById(R.id.decbutton);
        decView = (TextView) findViewById(R.id.decview);


        decButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String pass = decPass.getText().toString();
                String encMsg = tempValues.getMessage();
                String messageAfterDecrypt = null;
                try {
                    messageAfterDecrypt = AESEnc.decrypt(encMsg, pass);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                decView.setText(messageAfterDecrypt);
            }
        });
    }
}