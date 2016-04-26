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
public class Chat_Adapter extends ArrayAdapter implements View.OnClickListener {

    private Activity context;
    Chat_Message tempValues = null;
    private static LayoutInflater inflater=null;

    static class ViewHolder {
        public TextView text;
        public TextView timestamp;
        public ImageView image;
    }


    private ArrayList<Chat_Message> chat;

    public Chat_Adapter (Activity context, ArrayList chat ) {
        super(context, R.layout.chat_row, chat);
        this.context = context;
        this.chat = chat;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        tempValues = null;
        tempValues = chat.get(position);
        if (rowView == null) {
            holder = new ViewHolder();

            if (tempValues.getLeft()== 1 ){
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.chat_row, null);
                //holder = new ViewHolder();
                holder.text = (TextView) rowView.findViewById(R.id.chatview);
                holder.timestamp = (TextView) rowView.findViewById(R.id.timestamp);
                holder.image = (ImageView) rowView.findViewById(R.id.icon);
                rowView.setTag(holder);
            }
            else {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.chat_row_right, null);
                //holder = new ViewHolder();
                holder.text = (TextView) rowView.findViewById(R.id.chatview);
                holder.timestamp = (TextView) rowView.findViewById(R.id.timestamp);
                holder.image = (ImageView) rowView.findViewById(R.id.icon);
                rowView.setTag(holder);
            }
        }
        else{
            holder = (ViewHolder) rowView.getTag();
        }

        if(chat.size()<=0)
        {
            holder.text.setText("No Data");

        }
        else {


            holder.text.setText(tempValues.getMessage());
            holder.timestamp.setText(tempValues.getTimestamp());
            holder.image.setImageBitmap(tempValues.getImage());

            rowView.setOnClickListener(new OnItemClickListener(position));
        }

        return rowView;
    }

    @Override
    public void onClick(View v) {
        Log.v("Chat_Adapter", "Row button clicked");
    }

    private class OnItemClickListener  implements View.OnClickListener {
        private int mPosition;

        OnItemClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {


            Chat_Activity sct = (Chat_Activity)context;

            /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/

            sct.onItemClick(mPosition);
        }
    }

}
