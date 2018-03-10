package com.kinghorn.squidswap.squidswap;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SquidswapActivity extends AppCompatActivity {

    private ImageButton ImageButton,CameraButton;
    private RelativeLayout CropCard,PaintCard,SwapCard;
    private ImageView SelectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squidswap);
    }

    //Grab and set click events for the bottom tab buttons
    private void InitializeBottomButtons(){
        ImageButton = (ImageButton) findViewById(R.id.OpenImage);
        CameraButton = (ImageButton) findViewById(R.id.CameraButton);

        ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    //Grab and set click events for the choice cards.
    private void InitializeCards(){
        CropCard = (RelativeLayout) findViewById(R.id.CropCard);
        PaintCard = (RelativeLayout) findViewById(R.id.PaintCard);
        SwapCard = (RelativeLayout) findViewById(R.id.SwapCard);

        CropCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        PaintCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        SwapCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });
    }
}
