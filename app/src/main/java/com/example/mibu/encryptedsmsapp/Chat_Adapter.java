package com.example.mibu.encryptedsmsapp;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import static com.example.mibu.recyclertesttake3.Chat_Message.TYPE_LEFT;
import static com.example.mibu.recyclertesttake3.Chat_Message.TYPE_RIGHT;


/**
 * Created by Mibu on 30-Apr-16.
 */
public class Chat_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Chat_Message> chatmessagelist;
    private Context context;

    public Chat_Adapter(Context context, List<Chat_Message>list){
        this.context=context;
        this.chatmessagelist=list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case TYPE_LEFT:
                view = LayoutInflater.from(context).inflate(R.layout.chat_row, parent, false);
                return new LeftViewHolder(view);
            case TYPE_RIGHT:
                view = LayoutInflater.from(context).inflate(R.layout.chat_row_right, parent, false);
                return new RightViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Chat_Message object = chatmessagelist.get(position);
        if (object !=null) {
            switch (object.getType()) {
                case TYPE_LEFT:
                    ((LeftViewHolder)holder).message.setText(object.getMessage());
                    ((LeftViewHolder)holder).timestamp.setText(object.getTimestamp());
                    break;
                case TYPE_RIGHT:
                    ((RightViewHolder)holder).message.setText(object.getMessage());
                    ((RightViewHolder)holder).timestamp.setText(object.getTimestamp());
                    break;
            }
        }
    }

    public long getItemId(int position) {
        return position;
    }


    @Override
    public int getItemCount() {
        if (chatmessagelist==null)
            return 0;
        return chatmessagelist.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatmessagelist != null) {
            Chat_Message object = chatmessagelist.get(position);
            if (object != null) {
                return object.getType();
            }
        }
        return 0;
    }

    public class LeftViewHolder extends RecyclerView.ViewHolder {

        private TextView message;
        private TextView timestamp;

        public LeftViewHolder(View itemView){
            super(itemView);

            Typeface courier = Typeface.createFromAsset(itemView.getContext().getAssets(), "cour.ttf");
            message = (TextView) itemView.findViewById(R.id.chatview);
            message.setTypeface(courier);
            timestamp = (TextView) itemView.findViewById(R.id.timestamp);
            timestamp.setTypeface(courier);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int mPosition = getAdapterPosition();
                    Chat_Activity sct = (Chat_Activity)context;
                    sct.onItemClick(mPosition);
                }
            });
        }

    }

    public class RightViewHolder extends RecyclerView.ViewHolder {
        private TextView message;
        private TextView timestamp;

        public RightViewHolder(View itemView){
            super(itemView);
            Typeface courier = Typeface.createFromAsset(itemView.getContext().getAssets(), "cour.ttf");
            message = (TextView) itemView.findViewById(R.id.chatviewright);
            message.setTypeface(courier);
            timestamp = (TextView) itemView.findViewById(R.id.timestampright);
            timestamp.setTypeface(courier);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int mPosition = getAdapterPosition();
                    Chat_Activity sct = (Chat_Activity)context;
                    sct.onItemClick(mPosition);
                }
            });
        }
    }

}
