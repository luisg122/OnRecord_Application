package com.example.onrecord3;


import android.widget.ImageButton;

import androidx.cardview.widget.CardView;

public class Card {

    private ImageButton transcribe;
    private CardView cv;


    public ImageButton getTranscribe(){
        return transcribe;
    }

    public void setTranscribe(ImageButton transcribe){
        this.transcribe = transcribe;
    }

    public CardView getCv(){
        return cv;}

    public void setCv(CardView cv) {
        this.cv = cv;
    }
}

