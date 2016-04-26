package com.example.mibu.encryptedsmsapp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mibu on 27-Apr-16.
 */
public class SMS_Adapter extends ArrayAdapter implements View.OnClickListener {

    private Activity context;
    //private String[] msgs;
    //private final String[] addresses;
    //private final String[] threads;
    Conversation tempValues = null;
    private static LayoutInflater inflater=null;

    static class ViewHolder {
        public TextView address;
        public TextView text;
        //public TextView thread;
        public ImageView image;
    }

    private ArrayList conversations;

    public SMS_Adapter (Activity context, ArrayList conversations ) {
        super(context, R.layout.sms_row, conversations);
        this.context = context;
        this.conversations = conversations;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.sms_row, null);

            holder = new ViewHolder();
            holder.address = (TextView) rowView.findViewById(R.id.addressv);
            holder.text = (TextView) rowView.findViewById(R.id.messagev);
            //holder.thread = (TextView) rowView.findViewById(R.id.threadv);
            holder.image = (ImageView) rowView.findViewById(R.id.conpic);
            rowView.setTag(holder);
        }
        else{
            holder = (ViewHolder) rowView.getTag();
        }

        if(conversations.size()<=0)
        {
            holder.address.setText("No Data");

        }
        else {

            tempValues = null;
            tempValues = (Conversation) conversations.get(position);
            holder.address.setText(tempValues.getAddress());
            holder.text.setText(tempValues.getMessage());
            //holder.thread.setText(tempValues.getThread());
            holder.image.setImageBitmap(tempValues.getImage());

            rowView.setOnClickListener(new OnItemClickListener(position));
        }

        return rowView;
    }
    @Override
    public void onClick(View v) {
        Log.v("SMS_Adapter", "Row button clicked");
    }

    private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {


            Conversation_Activity sct = (Conversation_Activity)context;

            /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/

            sct.onItemClick(mPosition);
        }
    }

}
