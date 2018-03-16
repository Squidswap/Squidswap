package com.kinghorn.squidswap.squidswap;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SquidswapActivity extends AppCompatActivity {

    private boolean FOCUSED_IMAGE = false;
    private ImageButton ImageButton,CameraButton,PreviewButton,SaveButton;
    private RelativeLayout CropCard,PaintCard,SwapCard;
    private LinearLayout TopOptions;
    private ImageView SelectedImage;
    private Uri ChosenImage;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch(requestCode){
                case 1:
                    SelectedImage.setImageURI(data.getData());
                    ChosenImage = data.getData();

                    //Make all of the options visible.
                    CropCard.setAlpha(1);
                    PaintCard.setAlpha(1);
                    SwapCard.setAlpha(1);
                    TopOptions.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squidswap);

        SelectedImage = (ImageView) findViewById(R.id.ImagePreview);

        InitializeBottomButtons();
        InitializeCards();
        InitializeTopOptions();
    }

    //Grab and set click events for the bottom tab buttons
    private void InitializeBottomButtons(){
        ImageButton = (ImageButton) findViewById(R.id.OpenImage);
        CameraButton = (ImageButton) findViewById(R.id.CameraButton);

        ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!FOCUSED_IMAGE){
                    // in onCreate or any event where your want the user to
                    // select a file
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            "Select Picture"), 1);
                }
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
                if(ChosenImage != null){
                    Toast.makeText(getApplicationContext(),"Opening Crop Tool",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Image not chosen.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        PaintCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ChosenImage != null){
                    Toast.makeText(getApplicationContext(),"Opening Crop Tool",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Image not chosen.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        SwapCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ChosenImage != null){
                    Toast.makeText(getApplicationContext(),"Opening Crop Tool",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Image not chosen.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Set click events for the top buttons.
    private void InitializeTopOptions(){
        TopOptions = (LinearLayout) findViewById(R.id.TopOptions);
        PreviewButton = (ImageButton) findViewById(R.id.PreviewButton);
        SaveButton = (ImageButton) findViewById(R.id.SaveButton);

        PreviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
