package com.example.mibu.encryptedsmsapp;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


/**
 * Created by Mibu on 05-May-16.
 */
public class Conversation_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Conversation> conversationlist;
    private Context context;

    public Conversation_Adapter(Context context, List<Conversation>list){
        this.context=context;
        this.conversationlist=list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view;

        view = LayoutInflater.from(context).inflate(R.layout.conversation_row, parent, false);
        return new ConvViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Conversation object = conversationlist.get(position);
        if (object !=null) {

            ((ConvViewHolder)holder).address.setText(object.getAddress());
            ((ConvViewHolder)holder).message.setText(object.getMessage());
            ((ConvViewHolder)holder).timestamp.setText(object.getTimestamp());
            ((ConvViewHolder)holder).image.setImageBitmap(object.getImage());

        }
    }

    public long getItemId(int position) {
        return position;
    }

    public Object getItem(int position) {
        return conversationlist.get(position);
    }

    @Override
    public int getItemCount() {
        if (conversationlist==null)
            return 0;
        return conversationlist.size();
    }

    public class ConvViewHolder extends RecyclerView.ViewHolder {
        private TextView address;
        private TextView message;
        private TextView timestamp;
        private ImageView image;

        public ConvViewHolder(View itemView){
            super(itemView);
            Typeface courier = Typeface.createFromAsset(itemView.getContext().getAssets(), "cour.ttf");
            address = (TextView) itemView.findViewById(R.id.addressv);
            address.setTypeface(courier);
            message = (TextView) itemView.findViewById(R.id.messagev);
            message.setTypeface(courier);
            timestamp = (TextView) itemView.findViewById(R.id.convtime);
            timestamp.setTypeface(courier);
            image = (ImageView) itemView.findViewById(R.id.conpic);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int mPosition = getAdapterPosition();
                    Conversation_Activity sct = (Conversation_Activity)context;
                    sct.onItemClick(mPosition);
                }
            });
        }

    }

}
