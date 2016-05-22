package com.example.mibu.encryptedsmsapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Mibu on 30-Apr-16.
 */
public class Conversation_Activity extends AppCompatActivity {

    private static final String TAG = Conversation_Activity.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;
    private static Conversation_Activity inst;
    public Conversation_Activity(){}
    EditText numText;
    Button pickcontact;
    Button compose;
    TextView header;
    List<Conversation> conversationlist = new ArrayList<>();
    Conversation_Adapter adapter;
    RecyclerView recyclerView;

    public static Conversation_Activity getInstance() {
        if (inst == null) {
            inst = new Conversation_Activity();
        }
        return inst;
    }

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

        Typeface courier = Typeface.createFromAsset(getAssets(), "cour.ttf");

        header = (TextView) findViewById(R.id.convheader);
        header.setTypeface(courier);
        numText = (EditText) findViewById(R.id.numberTxt);
        numText.setTypeface(courier);
        pickcontact = (Button) findViewById(R.id.pickContact);
        compose = (Button) findViewById(R.id.buttonCompose);

        final Bitmap conpic = BitmapFactory.decodeResource(getResources(), R.drawable.default_conpic);

        pickcontact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_PICK_CONTACTS /**PICK_CONTACT**/);
            }
        });

        compose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
            if (PhoneNumberUtils.isWellFormedSmsAddress(numText.getText().toString())) {

                    if (adapter!=null){
                        boolean found = false;
                        for ( int i = 0; i < adapter.getItemCount(); i++) {
                            Conversation tempValue = conversationlist.get(i);
                            String phoneno = tempValue.getPhonenumber();
                            String thread = tempValue.getThread();
                            if (numText.getText().toString().equals(phoneno) || ("+" + GetCountryZipCode() + numText.getText().toString().replaceAll("[^0-9\\+]", "")).equals(phoneno) || (numText.getText().toString().substring(GetCountryZipCode().length()+1)).equals(phoneno) ){
                                found = true;

                                Bitmap contactpic = tempValue.getImage();
                                ByteArrayOutputStream contactstream = new ByteArrayOutputStream();
                                contactpic.compress(Bitmap.CompressFormat.PNG, 50, contactstream);

                                Intent chat = new Intent(getApplicationContext(), Chat_Activity.class);
                                chat.putExtra("phoneno", phoneno);
                                chat.putExtra("threadid", thread);
                                chat.putExtra("name",tempValue.getAddress());
                                chat.putExtra("contactpic", contactstream.toByteArray());
                                startActivity(chat);
                                break;
                            }
                        }
                        if (!found) {

                            ByteArrayOutputStream contactstream = new ByteArrayOutputStream();
                            conpic.compress(Bitmap.CompressFormat.PNG, 50, contactstream);
                            if (!numText.getText().toString().substring(0,GetCountryZipCode().length()+1).equals("+"+ GetCountryZipCode())) {

                                Intent chat = new Intent(getApplicationContext(), Chat_Activity.class);
                                chat.putExtra("phoneno", "+" + GetCountryZipCode() + numText.getText().toString().replaceAll("[^0-9\\+]", ""));
                                chat.putExtra("name","+" + GetCountryZipCode() + numText.getText().toString().replaceAll("[^0-9\\+]", ""));
                                chat.putExtra("contactpic", contactstream.toByteArray() );
                                startActivity(chat);
                            }
                            else {

                                Intent chat = new Intent(getApplicationContext(), Chat_Activity.class);

                                chat.putExtra("phoneno", numText.getText().toString());
                                chat.putExtra("name", numText.getText().toString());
                                chat.putExtra("contactpic", contactstream.toByteArray() );
                                startActivity(chat);
                            }
                        }
                    }
                }
            else{
                Toast.makeText(getApplicationContext(), "Not a valid phone number", Toast.LENGTH_SHORT).show();
            }

            }
        });

        adapter = new Conversation_Adapter(this, conversationlist);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, OrientationHelper.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView = (RecyclerView) findViewById(R.id.convList);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            setConvDataAPI19();
        }else{

        setConvData();}
    }


    public void setConvDataAPI19() {
        ContentResolver contentResolver = getContentResolver();

        Cursor smsConvCursor19 = contentResolver.query(Telephony.Sms.CONTENT_URI, // Official CONTENT_URI from docs
                new String[] { /**"DISTINCT"+ Telephony.TextBasedSmsColumns.THREAD_ID, Telephony.TextBasedSmsColumns.ADDRESS,  Telephony.TextBasedSmsColumns.DATE,  Telephony.TextBasedSmsColumns.BODY,  Telephony.TextBasedSmsColumns.TYPE  }, // Select body text
                Telephony.TextBasedSmsColumns.THREAD_ID+" IS NOT NULL) GROUP BY ("+Telephony.TextBasedSmsColumns.THREAD_ID,
                null**/"DISTINCT thread_id","body","address", "type", "date"},"thread_id IS NOT NULL) GROUP BY (thread_id", null,
                Telephony.Sms.Inbox.DEFAULT_SORT_ORDER);

        int indexBody = smsConvCursor19.getColumnIndex("body");
        int indexAddress = smsConvCursor19.getColumnIndex("address");
        int indexThread = smsConvCursor19.getColumnIndex("thread_id");
        int indexDate = smsConvCursor19.getColumnIndex("date");
        int indexType = smsConvCursor19.getColumnIndex("type");

        if (indexBody < 0 || !smsConvCursor19.moveToLast()) return;
        do {
            Bitmap conpic = BitmapFactory.decodeResource(getResources(), R.drawable.default_conpic);
            String address = smsConvCursor19.getString(indexAddress);
            ContentResolver cr = getContentResolver();
            Uri curi = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(address));
            Cursor contactCursor = cr.query(curi,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);

            Date date = new Date(smsConvCursor19.getLong(indexDate));
            String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(date);

            if (contactCursor.getCount() > 0) {
                contactCursor.moveToFirst();

                String contactId = contactCursor.getString(contactCursor
                        .getColumnIndex(ContactsContract.PhoneLookup._ID));

                InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));


                String name = contactCursor.getString(contactCursor
                        .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                if ( input != null){
                    conversationlist.add(new Conversation(name,smsConvCursor19.getString(indexBody),smsConvCursor19.getString(indexThread), smsConvCursor19.getString(indexAddress),timestamp, BitmapFactory.decodeStream(input) ));}

                else {
                    conversationlist.add(new Conversation(name,smsConvCursor19.getString(indexBody),smsConvCursor19.getString(indexThread), smsConvCursor19.getString(indexAddress),timestamp, conpic ));
                }
            } else {
                conversationlist.add(new Conversation(smsConvCursor19.getString(indexAddress),smsConvCursor19.getString(indexBody),smsConvCursor19.getString(indexThread), smsConvCursor19.getString(indexAddress),timestamp, conpic));
            }
            contactCursor.close();
        } while (smsConvCursor19.moveToPrevious());
        smsConvCursor19.close();

    }


    public void setConvData() {

        ContentResolver contentResolver = getContentResolver();
        //final String[] projection = new String[]{"*"};

            Uri uri = Uri.parse("content://sms/");

            Cursor smsConvCursor = contentResolver.query(uri, new String[]{"DISTINCT thread_id","body","address", "type", "date"},"thread_id IS NOT NULL) GROUP BY (thread_id", null,"DATE desc");

            int indexBody = smsConvCursor.getColumnIndex("body");
            int indexAddress = smsConvCursor.getColumnIndex("address");
            int indexThread = smsConvCursor.getColumnIndex("thread_id");
            int indexDate = smsConvCursor.getColumnIndex("date");
            int indexPerson = smsConvCursor.getColumnIndex("person");
            int indexType = smsConvCursor.getColumnIndex("type");

        if (indexBody < 0 || !smsConvCursor.moveToLast()) return;
        do {
            Bitmap conpic = BitmapFactory.decodeResource(getResources(), R.drawable.default_conpic);
            String address = smsConvCursor.getString(indexAddress);
            ContentResolver cr = getContentResolver();
            Uri curi = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(address));
            Cursor contactCursor = cr.query(curi,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);

            Date date = new Date(smsConvCursor.getLong(indexDate));
            String timestamp = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(date);

            if (contactCursor.getCount() > 0) {
                contactCursor.moveToFirst();

                String contactId = contactCursor.getString(contactCursor
                        .getColumnIndex(ContactsContract.PhoneLookup._ID));

                InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));


                String name = contactCursor.getString(contactCursor
                        .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                if ( input != null){
                conversationlist.add(new Conversation(name,smsConvCursor.getString(indexBody),smsConvCursor.getString(indexThread), smsConvCursor.getString(indexAddress),timestamp, BitmapFactory.decodeStream(input) ));}

                else {
                    conversationlist.add(new Conversation(name,smsConvCursor.getString(indexBody),smsConvCursor.getString(indexThread), smsConvCursor.getString(indexAddress),timestamp, conpic ));
                }
            } else {
                conversationlist.add(new Conversation(smsConvCursor.getString(indexAddress),smsConvCursor.getString(indexBody),smsConvCursor.getString(indexThread), smsConvCursor.getString(indexAddress),timestamp, conpic));
            }
            contactCursor.close();
        } while (smsConvCursor.moveToPrevious());
        smsConvCursor.close();
        //}
    }

    public String GetCountryZipCode(){
        String CountryID="";
        String CountryZipCode="";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
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


    public void updateList(String smsAddress, String smsMessageStr) {
        if (adapter!=null){
            boolean found = false;
            for ( int i = 0; i < adapter.getItemCount(); i++){
                Conversation tempValue = conversationlist.get(i);
                String name = tempValue.getAddress();
                String thread = tempValue.getThread();
                String phoneno = tempValue.getPhonenumber();
                Bitmap image = tempValue.getImage();

                if (smsAddress.equals(phoneno) || (smsAddress.replaceAll("[^0-9\\+]", "")).equals("+" + GetCountryZipCode() + phoneno.replaceAll("[^0-9\\+]", "")) || (smsAddress.equals(phoneno.substring(GetCountryZipCode().length()+1)))){
                    found = true;
                    conversationlist.remove(i);
                    conversationlist.add(new Conversation(name, smsMessageStr,thread, phoneno,"Just now", image));
                    adapter.notifyItemRemoved(i);
                    adapter.notifyItemInserted(adapter.getItemCount());
                    recyclerView.smoothScrollToPosition(adapter.getItemCount());
                    break;
                }
            }
            if (!found) {
                Bitmap conpic = BitmapFactory.decodeResource(getResources(), R.drawable.default_conpic);
                conversationlist.add(new Conversation(smsAddress, smsMessageStr, null, smsAddress,"Just now", conpic));
                adapter.notifyItemInserted(adapter.getItemCount());
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        }
    }

    public void onItemClick(int mPosition) {
        Conversation tempValues = conversationlist.get(mPosition);

        try {
            Intent chat = new Intent(getApplicationContext(), Chat_Activity.class);

            Bitmap contactpic = tempValues.getImage();
            ByteArrayOutputStream contactstream = new ByteArrayOutputStream();
            contactpic.compress(Bitmap.CompressFormat.PNG, 50, contactstream);
            chat.putExtra("phoneno", tempValues.getPhonenumber());

            chat.putExtra("threadid", tempValues.getThread());
            chat.putExtra("name",tempValues.getAddress());
            chat.putExtra("contactpic", contactstream.toByteArray());
            startActivity(chat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d(TAG, "Response: " + data.toString());
            uriContact = data.getData();

            retrieveContactName();
            retrieveContactNumber();

        }
    }


    private void retrieveContactNumber() {

        String cNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            cNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (!cNumber.substring(0,GetCountryZipCode().length()+1).equals("+"+GetCountryZipCode())){
                numText.setText("+"+GetCountryZipCode()+cNumber.replaceAll("[^0-9\\+]",""));
            }else{
                numText.setText(cNumber.replaceAll("[^0-9\\+]",""));
            }

        }

        cursorPhone.close();

        Log.d(TAG, "Contact Phone Number: " + cNumber);
    }

    private void retrieveContactName() {

        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        Log.d(TAG, "Contact Name: " + contactName);

    }
}
