package com.kinghorn.squidswap.squidswap;

import android.Manifest;
import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.kinghorn.inksplat.inksplat.InkSplatActivity;
import com.kinghorn.inkstamp.inkstamp.InkStampActivity;
import com.squidswap.inkslice.inkslice.InkSliceActivity;
import com.squidswap.inktag.inktag.InktagActivity;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SquidswapActivity extends AppCompatActivity {

    private boolean FOREGROUND_CONTEXT = true,FOCUSED_FOREGROUND = false,FOCUSED_BACKGROUND = false,FOREGROUND_CHANGED = false,BACKGROUND_CHANGE = false,APP_UNLOCKED = false;
    private ImageButton ImageButton,CameraButton,ImageRight,ImageLeft,RemoveContextImage,RateUsButton,SaveImgButton;
    private RelativeLayout CropCard,PaintCard,SwapCard,SaveCard,MemeCard,ForegroundLayout,BackgroundLayout,ImageLayout;
    private ImageView ForegroundView,BackgroundView;
    private Uri ForegroundImage,BackgroundImage;
    private FileService FileServ;
    private TextView ContextText,NotSelected;
    private SquidSettingsManager setManage;
    private FirebaseAnalytics SquidAnalytics;
    private AdView mainAd;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch(requestCode){
                case 1:
                    if(FOREGROUND_CONTEXT){
                        ForegroundView.setImageURI(data.getData());
                        ForegroundImage = data.getData();
                        FileServ.SaveTemp(data.getData(),true,"fore");
                        ForegroundLayout.setVisibility(View.VISIBLE);
                    }else{
                        BackgroundView.setImageURI(data.getData());
                        BackgroundImage = data.getData();
                        FileServ.SaveTemp(data.getData(),true,"back");
                        BackgroundLayout.setVisibility(View.VISIBLE);
                    }

                    NotSelected.setVisibility(View.GONE);
                    ToggleCards(true);
                    break;
                case 2:
                    if(data.hasExtra("InksplatFile") && data.hasExtra("SquidSwapContext")){
                        String cont = data.getExtras().getString("SquidSwapContext");

                        String file_path = data.getStringExtra("InksplatFile");
                        FileServ.SaveTemp(Uri.parse(file_path),false,cont);

                        if(cont.equals("fore")){
                            ForegroundView.setImageBitmap(BitmapFactory.decodeFile(file_path));
                            FOREGROUND_CHANGED = true;
                        }else{
                            BackgroundView.setImageBitmap(BitmapFactory.decodeFile(file_path));
                            BACKGROUND_CHANGE = true;
                        }
                        //SelectedImage.setImageBitmap(BitmapFactory.decodeFile(file_path));
                    }
                    break;
                case 5:
                    if(data.hasExtra("InkSliceFile")){
                        String cont = data.getExtras().getString("SquidSwapContext");
                        String file_path = data.getStringExtra("InkSliceFile");
                        FileServ.SaveTemp(Uri.parse(file_path),false,cont);

                        if(cont.equals("fore")){
                            ForegroundView.setImageBitmap(BitmapFactory.decodeFile(file_path));
                            FOREGROUND_CHANGED = true;
                        }else{
                            BackgroundView.setImageBitmap(BitmapFactory.decodeFile(file_path));
                            BACKGROUND_CHANGE = true;
                        }
                    }
                    break;
                case 4:
                    //Move onto the preview activity and ask if they would like to save the swap.
                    Intent i = new Intent(getApplicationContext(),SquidswapSaveSwap.class);
                    i.putExtra("SquidStampPreview",data.getExtras().getString("InkStampFile"));
                    startActivityForResult(i,7);
                    break;
                case 7:
                    ForegroundImage = null;
                    BackgroundImage = null;
                    ForegroundView.setImageBitmap(null);
                    BackgroundView.setImageBitmap(null);
                    ForegroundLayout.setVisibility(View.GONE);
                    BackgroundLayout.setVisibility(View.GONE);
                    FOREGROUND_CHANGED = false;
                    BACKGROUND_CHANGE = false;
                    FOREGROUND_CONTEXT = true;
                    break;
                case 13:
                    if(data.hasExtra("InkTagFile")){
                        String cont = data.getExtras().getString("SquidSwapContext");
                        String file_path = data.getStringExtra("InkTagFile");
                        FileServ.SaveTemp(Uri.parse(file_path),false,cont);

                        if(cont.equals("fore")){
                            ForegroundView.setImageBitmap(BitmapFactory.decodeFile(file_path));
                            FOREGROUND_CHANGED = true;
                        }else{
                            BackgroundView.setImageBitmap(BitmapFactory.decodeFile(file_path));
                            BACKGROUND_CHANGE = true;
                        }
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SquidAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

        FileServ = new FileService();
        setManage = new SquidSettingsManager(getApplicationContext());

        if(setManage.HasSetting("SquidswapSmallCards") && setManage.LoadBoolSetting("SquidswapSmallCards")){
            setContentView(R.layout.activity_squidswap_short);
        }else {
            setContentView(R.layout.activity_squidswap);
        }

        MobileAds.initialize(getApplicationContext(),"ca-app-pub-7417016807781274~5442947578");
        mainAd = (AdView) findViewById(R.id.adView);
        AdRequest ad = new AdRequest.Builder().build();
        mainAd.loadAd(ad);

        ForegroundView = (ImageView) findViewById(R.id.ForegroundImage);
        BackgroundView = (ImageView) findViewById(R.id.BackgroundImage);
        ForegroundLayout = (RelativeLayout) findViewById(R.id.ForegroundLayout);
        BackgroundLayout = (RelativeLayout) findViewById(R.id.BackgroundLayout);
        ContextText = (TextView) findViewById(R.id.LayerText);
        NotSelected = (TextView) findViewById(R.id.NotSelected);
        RelativeLayout img = (RelativeLayout) findViewById(R.id.ImageLayout);

        InitializeBottomButtons();
        InitializeCards();

        //Actionbar stuff goes here
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        LayoutInflater flate = getLayoutInflater();
        RelativeLayout r = (RelativeLayout) flate.inflate(R.layout.actionbar_layout,null);
        TextView title = (TextView) r.findViewById(R.id.SquidswapTitle);
        ImageButton set = (ImageButton) r.findViewById(R.id.SquidSwapSettings);
        ImageButton lay = (ImageButton) r.findViewById(R.id.LayersToggle);
        RateUsButton = (ImageButton) r.findViewById(R.id.RateButton);
        SaveImgButton = (ImageButton) r.findViewById(R.id.SaveButton);
        Typeface fac = Typeface.createFromAsset(getAssets(),"fonts/AdmiralCAT.ttf");
        title.setTypeface(fac);
        bar.setDisplayShowTitleEnabled(false);
        bar.setCustomView(r);
        bar.setDisplayShowCustomEnabled(true);

        SaveImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder build = new AlertDialog.Builder(SquidswapActivity.this);
                LayoutInflater inflate = getLayoutInflater();
                RelativeLayout lay = (RelativeLayout) inflate.inflate(R.layout.save_dialog,null);

                if(FOREGROUND_CONTEXT){
                    if(FOREGROUND_CHANGED){
                        if(ForegroundImage != null){
                            final EditText t = lay.findViewById(R.id.FileNameText);

                            //Generate a randomized squidswap string.
                            int rand = (int) Math.floor(Math.random() * 10000);
                            t.setText("squidswap-"+rand);

                            build.setView(lay);
                            build.setTitle("Save image to gallery?").setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Bitmap b = FileServ.LoadTemp("fore");
                                    String filname = t.getText().toString();
                                    FileServ.SaveToGallery(b,filname);

                                    //After this we need to ask if they want to start from the beginning or not
                                    AlertDialog.Builder next = new AlertDialog.Builder(SquidswapActivity.this);
                                    LayoutInflater infl = getLayoutInflater();
                                    RelativeLayout lay = (RelativeLayout) infl.inflate(R.layout.new_project_dialog,null);
                                    next.setView(lay);
                                    next.setTitle("Image Saved").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //If yes then we want to reset the front image.
                                            FOREGROUND_CONTEXT = false;
                                            FOREGROUND_CHANGED = false;
                                            ForegroundImage = null;
                                            ForegroundView.setImageBitmap(null);
                                            BackgroundImage = null;
                                            BackgroundView.setImageBitmap(null);
                                            ToggleCards(false);
                                            ForegroundLayout.setVisibility(View.GONE);
                                            BackgroundLayout.setVisibility(View.GONE);
                                            NotSelected.setVisibility(View.VISIBLE);
                                        }
                                    }).show();

                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"Image not chosen.",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Image has not been edited.",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(BACKGROUND_CHANGE){
                        if(BackgroundImage != null){
                            final EditText t = lay.findViewById(R.id.FileNameText);

                            //Generate a randomized squidswap string.
                            int rand = (int) Math.floor(Math.random() * 10000);
                            t.setText("squidswap-"+rand+".png");

                            build.setView(lay);
                            build.setTitle("Save image to gallery?").setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Bitmap b = FileServ.LoadTemp("back");
                                    String filname = t.getText().toString();
                                    FileServ.SaveToGallery(b,filname);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"Image not chosen.",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Image has not been edited.",Toast.LENGTH_SHORT).show();
                    }
                }

                SendSquidEvent("Saving Image");
            }
        });

        RateUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendSquidEvent("Rating App");
                AlertDialog.Builder b = new AlertDialog.Builder(SquidswapActivity.this);
                LayoutInflater infl = getLayoutInflater();
                LinearLayout l = (LinearLayout) infl.inflate(R.layout.rate_dialog,null);
                b.setView(l);

                b.setTitle("Rate Us").setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri r = Uri.parse("market://details?id=" + getPackageName());
                        Intent toMarket = new Intent(Intent.ACTION_VIEW,r);

                        try{
                            startActivity(toMarket);
                        }catch(ActivityNotFoundException e){
                            Toast.makeText(getApplicationContext(),"Unable to open App Market",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
            }
        });

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),SquidSettings.class);
                startActivity(i);
            }
        });

        lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendSquidEvent("Changing Layers");
                FOREGROUND_CONTEXT = !FOREGROUND_CONTEXT;
                String msg = "";

                if(FOREGROUND_CONTEXT){
                    msg = "Switched to foreground layer.";
                    BackgroundLayout.setVisibility(View.GONE);

                    if(ForegroundImage != null){
                        ToggleCards(true);
                        ForegroundLayout.setVisibility(View.VISIBLE);
                    }else{
                        ToggleCards(false);
                    }

                    ContextText.setText("Foreground Layer");
                }else{
                    msg = "Switched to background layer.";
                    ForegroundLayout.setVisibility(View.GONE);

                    if(BackgroundImage != null){
                        ToggleCards(true);
                        BackgroundLayout.setVisibility(View.VISIBLE);
                    }else{
                       ToggleCards(false);
                    }

                    ContextText.setText("Background Layer");
                }
            }
        });

        NotSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Here we are going to want to know what the context of opening an image is, the user
                //can either be focused on the foreground image or the background image.
                if(FOREGROUND_CONTEXT){
                    //We are going to be opening an image for the foreground.
                    if(!FOREGROUND_CHANGED){
                        if(!FOCUSED_FOREGROUND){
                            OpenFileFromMedia();
                        }else{
                            Toast.makeText(getApplicationContext(),"Foreground image has already been chosen.",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        AlertDialog.Builder dia = new AlertDialog.Builder(SquidswapActivity.this);
                        LayoutInflater inf = getLayoutInflater();
                        RelativeLayout l = (RelativeLayout) inf.inflate(R.layout.discard_image,null);

                        dia.setView(l);
                        dia.setTitle("Discard Image").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }
                }else{
                    //Opening image for the background;
                    if(!BACKGROUND_CHANGE){
                        if(!FOCUSED_BACKGROUND){
                            OpenFileFromMedia();
                        }else{
                            Toast.makeText(getApplicationContext(),"Background image has already been chosen.",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        AlertDialog.Builder dia = new AlertDialog.Builder(SquidswapActivity.this);

                        dia.setTitle("Image has been edited, proceeding will discard any changes. Do you wish to continue?").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }
                }
            }
        });

        RequestPermission();
    }

    private void SendSquidEvent(String eventName){
        Bundle bun = new Bundle();
        bun.putString("Action",eventName);
        SquidAnalytics.logEvent(eventName ,bun);
    }

    private void RequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,android.Manifest.permission.INTERNET,android.Manifest.permission.ACCESS_NETWORK_STATE}, 786);
        }
    }

    //Shows or hides the option cards.
    private void ToggleCards(Boolean tog){
        if(tog){
            CropCard.setAlpha(1);
            PaintCard.setAlpha(1);
            SwapCard.setAlpha(1);
            SaveCard.setAlpha(1);
            MemeCard.setAlpha(1);
        }else{
            CropCard.setAlpha(.5f);
            PaintCard.setAlpha(.5f);
            SwapCard.setAlpha(.5f);
            SaveCard.setAlpha(.5f);
            MemeCard.setAlpha(.5f);
        }
    }

    //Grab and set click events for the bottom tab buttons
    private void InitializeBottomButtons(){
        ImageButton = (ImageButton) findViewById(R.id.OpenImage);
        CameraButton = (ImageButton) findViewById(R.id.CameraButton);

        ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Here we are going to want to know what the context of opening an image is, the user
                //can either be focused on the foreground image or the background image.
                if(FOREGROUND_CONTEXT){
                    //We are going to be opening an image for the foreground.
                    if(!FOREGROUND_CHANGED){
                        if(!FOCUSED_FOREGROUND){
                           OpenFileFromMedia();
                        }else{
                            Toast.makeText(getApplicationContext(),"Foreground image has already been chosen.",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        AlertDialog.Builder dia = new AlertDialog.Builder(SquidswapActivity.this);
                        LayoutInflater inf = getLayoutInflater();
                        RelativeLayout l = (RelativeLayout) inf.inflate(R.layout.discard_image,null);

                        dia.setView(l);
                        dia.setTitle("Discard Image").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FOREGROUND_CONTEXT = false;
                                FOREGROUND_CHANGED = false;
                                ForegroundImage = null;
                                ForegroundView.setImageBitmap(null);
                                BackgroundImage = null;
                                BackgroundView.setImageBitmap(null);
                                ToggleCards(false);
                                ForegroundLayout.setVisibility(View.GONE);
                                BackgroundLayout.setVisibility(View.GONE);
                                NotSelected.setVisibility(View.VISIBLE);

                                OpenFileFromMedia();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }
                }else{
                    //Opening image for the background;
                    if(!BACKGROUND_CHANGE){
                        if(!FOCUSED_BACKGROUND){
                           OpenFileFromMedia();
                        }else{
                            Toast.makeText(getApplicationContext(),"Background image has already been chosen.",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        AlertDialog.Builder dia = new AlertDialog.Builder(SquidswapActivity.this);

                        dia.setTitle("Image has been edited, proceeding will discard any changes. Do you wish to continue?").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FOREGROUND_CONTEXT = false;
                                FOREGROUND_CHANGED = false;
                                ForegroundImage = null;
                                ForegroundView.setImageBitmap(null);
                                BackgroundImage = null;
                                BackgroundView.setImageBitmap(null);
                                ToggleCards(false);
                                ForegroundLayout.setVisibility(View.GONE);
                                BackgroundLayout.setVisibility(View.GONE);
                                NotSelected.setVisibility(View.VISIBLE);

                                OpenFileFromMedia();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }
                }

                SendSquidEvent("Opening Image");
            }
        });

        CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Here we are going to want to open the camera application for the user
                //to take an image.
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i,9);
            }
        });
    }

    //Opends a file from the media library and starts an activity for a
    //result that will then open the file.
    private void OpenFileFromMedia(){
        // in onCreate or any event where your want the user to
        // select a file
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), 1);
    }

    //Grab and set click events for the choice cards.
    private void InitializeCards(){
        CropCard = (RelativeLayout) findViewById(R.id.CropCard);
        PaintCard = (RelativeLayout) findViewById(R.id.PaintCard);
        SwapCard = (RelativeLayout) findViewById(R.id.SwapCard);
        SaveCard = (RelativeLayout) findViewById(R.id.SaveCard);
        MemeCard = (RelativeLayout) findViewById(R.id.MemeCard);

        MemeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FOREGROUND_CONTEXT){
                    SendSquidEvent("Meme Image");
                    Intent i = new InktagActivity.TagBuilder(getApplicationContext(),"squidswap_tmp.png").start();
                    i.putExtra("InkTagFile",FileServ.TempUriPath("fore"));
                    i.putExtra("SquidSwapContext","fore");
                    startActivityForResult(i,13);
                }else{
                    SendSquidEvent("Meme Image");
                    Intent i = new InktagActivity.TagBuilder(getApplicationContext(),"squidswap_tmp.png").start();
                    i.putExtra("InkTagFile",FileServ.TempUriPath("back"));
                    i.putExtra("SquidSwapContext","back");
                    startActivityForResult(i,13);
                }
            }
        });

        SaveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder build = new AlertDialog.Builder(SquidswapActivity.this);
                LayoutInflater inflate = getLayoutInflater();
                RelativeLayout lay = (RelativeLayout) inflate.inflate(R.layout.save_dialog,null);

                if(FOREGROUND_CONTEXT){
                    if(FOREGROUND_CHANGED){
                        if(ForegroundImage != null){
                            final EditText t = lay.findViewById(R.id.FileNameText);

                            //Generate a randomized squidswap string.
                            int rand = (int) Math.floor(Math.random() * 10000);
                            t.setText("squidswap-"+rand);

                            build.setView(lay);
                            build.setTitle("Save image to gallery?").setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Bitmap b = FileServ.LoadTemp("fore");
                                    String filname = t.getText().toString();
                                    FileServ.SaveToGallery(b,filname);

                                    //After this we need to ask if they want to start from the beginning or not
                                    AlertDialog.Builder next = new AlertDialog.Builder(SquidswapActivity.this);
                                    LayoutInflater infl = getLayoutInflater();
                                    RelativeLayout lay = (RelativeLayout) infl.inflate(R.layout.new_project_dialog,null);
                                    next.setView(lay);
                                    next.setTitle("Image Saved").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //If yes then we want to reset the front image.
                                            FOREGROUND_CONTEXT = false;
                                            FOREGROUND_CHANGED = false;
                                            ForegroundImage = null;
                                            ForegroundView.setImageBitmap(null);
                                            BackgroundImage = null;
                                            BackgroundView.setImageBitmap(null);
                                            ToggleCards(false);
                                            ForegroundLayout.setVisibility(View.GONE);
                                            BackgroundLayout.setVisibility(View.GONE);
                                            NotSelected.setVisibility(View.VISIBLE);
                                        }
                                    }).show();

                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"Image not chosen.",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Image has not been edited.",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(BACKGROUND_CHANGE){
                        if(BackgroundImage != null){
                            final EditText t = lay.findViewById(R.id.FileNameText);

                            //Generate a randomized squidswap string.
                            int rand = (int) Math.floor(Math.random() * 10000);
                            t.setText("squidswap-"+rand+".png");

                            build.setView(lay);
                            build.setTitle("Save image to gallery?").setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Bitmap b = FileServ.LoadTemp("back");
                                    String filname = t.getText().toString();
                                    FileServ.SaveToGallery(b,filname);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"Image not chosen.",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Image has not been edited.",Toast.LENGTH_SHORT).show();
                    }
                }

                SendSquidEvent("Saving Image");
            }
        });

        CropCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //For these cards we are going to want to first check the context of
                //the tool so far, i.e foreground or background image.
                if(FOREGROUND_CONTEXT){
                    if(ForegroundImage != null){
                        SendSquidEvent("Cropping Image");
                        Intent i = new InkSliceActivity.SliceBuilder(getApplicationContext(),"squidswap_tmp.png").start();
                        i.putExtra("InkSliceImg",FileServ.TempUriPath("fore"));
                        i.putExtra("SquidSwapContext","fore");
                        startActivityForResult(i,5);
                    }else{
                        Toast.makeText(getApplicationContext(),"Foreground image not selected...",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(BackgroundImage != null){
                        SendSquidEvent("Cropping Image");
                        Intent i = new InkSliceActivity.SliceBuilder(getApplicationContext(),"squidswap_tmp.png").start();
                        i.putExtra("InkSliceImg",FileServ.TempUriPath("back"));
                        i.putExtra("SquidSwapContext","back");
                        startActivityForResult(i,5);
                    }else{
                        Toast.makeText(getApplicationContext(),"Background image not selected...",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        PaintCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FOREGROUND_CONTEXT){
                    if(ForegroundImage != null){
                        SendSquidEvent("Painting Image");
                        Intent i = new InkSplatActivity.InksplatBuilder(getApplicationContext(),"squidswap_tmp.png").build();
                        i.putExtra("InkImgChoice",FileServ.TempUriPath("fore"));
                        i.putExtra("SquidSwapContext","fore");
                        startActivityForResult(i,2);
                    }else{
                        Toast.makeText(getApplicationContext(),"Foreground image not selected...",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(BackgroundImage != null){
                        SendSquidEvent("Painting Image");
                        Intent i = new InkSplatActivity.InksplatBuilder(getApplicationContext(),"squidswap_tmp.png").build();
                        i.putExtra("InkImgChoice",FileServ.TempUriPath("back"));
                        i.putExtra("SquidSwapContext","back");
                        startActivityForResult(i,2);
                    }else{
                        Toast.makeText(getApplicationContext(),"Background image not selected...",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        SwapCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ForegroundImage != null && BackgroundImage != null){
                    SendSquidEvent("Swapping Image");
                    //If we have more than one image then we just want to load the
                    Intent i = new InkStampActivity.InkStampBuilder(getApplicationContext(),"");
                    i.putExtra("InkForeground",FileServ.TempUriPath("fore"));
                    i.putExtra("InkBackground",FileServ.TempUriPath("back"));
                    startActivityForResult(i,4);
                }else{
                    //Check which one is missing and then prompt the user to open an image for the given image.
                    AlertDialog.Builder d = new AlertDialog.Builder(SquidswapActivity.this);
                    LayoutInflater inf = getLayoutInflater();
                    RelativeLayout r = (RelativeLayout) inf.inflate(R.layout.swap_dialog_layout,null);
                    d.setView(r);
                    d.setTitle("Missing Image").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setPositiveButton("Open", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //If they agree we need to determine which image is missing and then open from the gallery.  Once they choose the image we will

                        }
                    });

                    if(ForegroundImage == null){
                        d.show();
                    }else{
                        d.show();
                    }
                }
            }
        });
    }

    //Class that will handle the saving and loading on temporary files that are being
    //edited.
    private class FileService{
        //Save the currently chosen file to the temporary directory.
        //We are also going to want to have a context variable that will determine the temp file
        //for foreground as well as background image.
        public void SaveTemp(Uri FileUri,Boolean FirstImg,String cont){
            File fil = new File(getApplicationContext().getCacheDir(),"squidswap_tmp_"+cont+".png");
            OutputStream out = null;
            InputStream FileStream;
            Bitmap FileBmp;

            try{
                if(FirstImg){
                    FileStream = getApplicationContext().getContentResolver().openInputStream(FileUri);
                }else{
                    FileStream = new FileInputStream(FileUri.getPath());
                }

                if(FirstImg){
                    FileBmp = EfficientLoad(FileUri);
                }else{
                    FileBmp = BitmapFactory.decodeStream(FileStream);
                }

                fil.createNewFile();
                out = new FileOutputStream(fil);
                FileBmp.compress(Bitmap.CompressFormat.PNG,100,out);
                out.flush();
                out.close();
            }catch(IOException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Error saving temporary file...",Toast.LENGTH_SHORT).show();
            }
        }

        //Checks the size of bitmap that is going to be loaded and makes sure it is
        //not to large so we do not cause an out of memory exception.
        //
        //Returns a boolean if the image needs to be loaded with a lower resolution.
        private Bitmap EfficientLoad(Uri i){
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inJustDecodeBounds = true;

            Bitmap b;

            try {
                Bitmap checked = BitmapFactory.decodeStream(getApplicationContext().getContentResolver().openInputStream(i),null,op);

                //If the Image had a bigger width than 1500 we are going to want to scale it down
                //to return it.
                if(op.outWidth > 1500){
                    op.inSampleSize = 4;
                    op.inJustDecodeBounds = false;
                    b = BitmapFactory.decodeStream(getApplicationContext().getContentResolver().openInputStream(i),null,op);
                }else{
                    b = BitmapFactory.decodeStream(getApplicationContext().getContentResolver().openInputStream(i));
                }
            } catch (FileNotFoundException e) {
                b = null;
                e.printStackTrace();
            }

            return b;
        }

        public Bitmap LoadTemp(String cont){
            Bitmap CachedFile;
            File CachedPath = new File(getCacheDir(),"squidswap_tmp_"+cont+".png");
            FileInputStream CachedInput;

            try{
                //Make sure the file exists.
                CachedInput = new FileInputStream(CachedPath);
                CachedFile = BitmapFactory.decodeStream(CachedInput);
                return CachedFile;
            }catch(IOException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Error opening cached file...",Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        public Uri TempUriPath(String cont){
            return Uri.parse(getCacheDir().toString() + "/squidswap_tmp_"+cont+".png");
        }

        //Saves the temporary file to the gallery as well as adds the watermark based on whether or not
        //the tool has been unlocked or not.
        public void SaveToGallery(Bitmap finalImg,String filename){
            if(!APP_UNLOCKED){
                //Add the
            }

            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File rootDir = new File(root);
            rootDir.mkdirs();

            String name = filename+".jpeg";
            File saved = new File(rootDir,name);
            System.out.println(saved.getAbsolutePath());
            try {
                FileOutputStream out = new FileOutputStream(saved);
                finalImg.compress(Bitmap.CompressFormat.JPEG,100,out);
                out.flush();
                out.close();
                this.ScanMediaFiles(saved.getPath());
                Toast.makeText(getApplicationContext(),"Image saved to gallery.",Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(),"Error saving file to gallery...",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void ScanMediaFiles(String path){
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(path);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        }
    }
}
