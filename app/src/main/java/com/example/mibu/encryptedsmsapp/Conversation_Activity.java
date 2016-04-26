package com.example.mibu.encryptedsmsapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Mibu on 27-Apr-16.
 */
public class Conversation_Activity extends Activity {

    private static Conversation_Activity inst;
    EditText numText;
    Button pickcontact;
    Button compose;
    private static String contactid;
    static final int PICK_CONTACT=1;

    ListView list;
    SMS_Adapter adapter;
    public  Conversation_Activity CustomListView = null;
    public ArrayList<Conversation> convarray = new ArrayList<Conversation>();

    public static Conversation_Activity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversations);

        numText = (EditText) findViewById(R.id.numberTxt);
        pickcontact = (Button) findViewById(R.id.pickContact);
        compose = (Button) findViewById(R.id.buttonCompose);


        pickcontact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
            }
        });

        compose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent chat = new Intent(getApplicationContext(), Chat_Activity.class);

                chat.putExtra("phoneno", numText.getText().toString());
                chat.putExtra("conId", contactid);
                startActivity(chat);
            }
        });

        CustomListView = this;
        list = (ListView) findViewById(R.id.convList);

        setConvData();

        adapter = new SMS_Adapter(CustomListView, convarray);
        list.setAdapter(adapter);


    }

    public void setConvData() {

        ContentResolver contentResolver = getContentResolver();
        final String[] projection = new String[]{"*"};
        Uri uri = Uri.parse("content://mms-sms/conversations/");
        Cursor smsConvCursor = contentResolver.query(uri, projection, null, null, null);
        int indexBody = smsConvCursor.getColumnIndex("body");
        int indexAddress = smsConvCursor.getColumnIndex("address");
        int indexThread = smsConvCursor.getColumnIndex("thread_id");
        int indexPerson = smsConvCursor.getColumnIndex("person");
        int indexType = smsConvCursor.getColumnIndex("type");
        if (indexBody < 0 || !smsConvCursor.moveToFirst()) return;
        do {
            String address = smsConvCursor.getString(indexAddress);
            ContentResolver cr = getContentResolver();
            Uri curi = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(address));
            Cursor contactCursor = cr.query(curi,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);


            final Conversation sched = new Conversation();

            if (contactCursor.getCount() > 0) {
                contactCursor.moveToFirst();

                String contactId = contactCursor.getString(contactCursor
                        .getColumnIndex(ContactsContract.PhoneLookup._ID));

                InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));


                String name = contactCursor.getString(contactCursor
                        .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                sched.setAddress(name);
                sched.setMessage(smsConvCursor.getString(indexBody));
                sched.setThread(smsConvCursor.getString(indexThread));
                sched.setPhonenumber(smsConvCursor.getString(indexAddress));
                sched.setImage(BitmapFactory.decodeStream(input));
                convarray.add(sched);
            } else {
                sched.setAddress(smsConvCursor.getString(indexAddress));
                sched.setMessage(smsConvCursor.getString(indexBody));
                sched.setThread(smsConvCursor.getString(indexThread));
                sched.setPhonenumber(smsConvCursor.getString(indexAddress));
                convarray.add(sched);
            }
            contactCursor.close();
        } while (smsConvCursor.moveToNext());

    }

    public void updateList() {

        adapter.clear();
        setConvData();
        adapter.notifyDataSetChanged();
    }

    public void onItemClick(int mPosition) {
        Conversation tempValues = (Conversation) convarray.get(mPosition);

        try {
            Intent chat = new Intent(getApplicationContext(), Chat_Activity.class);

            chat.putExtra("phoneno", tempValues.getPhonenumber());

            chat.putExtra("threadid", tempValues.getThread());
            startActivity(chat);
        } catch (Exception e) {
            e.printStackTrace();


            // SHOW ALERT

            //Toast.makeText(CustomListView, "" + tempValues.getAddress() + "" + tempValues.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data){ super.onActivityResult(reqCode, resultCode, data);

        switch(reqCode)
        {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri contactData = data.getData();
                    Cursor c = getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst())
                    {
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        String hasPhone =
                                c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1"))
                        {
                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            TelephonyManager tManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                            numText.setText(cNumber);
                            String cID = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                            Toast.makeText(getApplicationContext(), cID, Toast.LENGTH_SHORT).show();
                            contactid = cID;
                        }
                    }
                }
        }
    }


}