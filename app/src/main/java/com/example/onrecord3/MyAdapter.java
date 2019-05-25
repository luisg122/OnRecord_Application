package com.example.onrecord3;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;



import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private static ArrayList<Card> dataset;
    private LayoutInflater inflater;

    private static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemRecord(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public MyAdapter(Context context, ArrayList<Card> dataset) {
        inflater = LayoutInflater.from(context);
        this.dataset = dataset;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        return new MyViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //holder.transcribe.setText(dataset.get(position).getTranscribe());
        holder.cv.setVisibility(View.VISIBLE);

        // holder.bind(dataset.get(position), listener);
    }


    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        EditText transcribe;
        ImageButton icon;
        CardView cv;

        public MyViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card);
            transcribe = (EditText) itemView.findViewById(R.id.cardViewText);
            icon = (ImageButton) itemView.findViewById(R.id.mic);

            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    int position = getAdapterPosition();
                    if(listener != null) {
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemRecord(position);
                        }
                    }
                }
            });
        }
    }
}