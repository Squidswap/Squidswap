package com.kinghorn.squidswap.squidswap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kinghorn.inksplat.inksplat.InkSplatActivity;
import com.kinghorn.inkstamp.inkstamp.InkStampActivity;
import com.squidswap.inkslice.inkslice.InkSliceActivity;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SquidswapActivity extends AppCompatActivity {

    private boolean FOREGROUND_CONTEXT = true,FOCUSED_FOREGROUND = false,FOCUSED_BACKGROUND = false,FOREGROUND_CHANGED = false,BACKGROUND_CHANGE = false;
    private ImageButton ImageButton,CameraButton,ImageRight,ImageLeft;
    private RelativeLayout CropCard,PaintCard,SwapCard,SaveCard,ForegroundLayout,BackgroundLayout;
    private ImageView ForegroundView,BackgroundView;
    private Uri ForegroundImage,BackgroundImage;
    private FileService FileServ;
    private TextView ContextText;

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
                    }else{
                        BackgroundView.setImageURI(data.getData());
                        BackgroundImage = data.getData();
                        FileServ.SaveTemp(data.getData(),true,"back");
                    }

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
                        System.out.println(cont);
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

                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squidswap);

        FileServ = new FileService();

        ForegroundView = (ImageView) findViewById(R.id.ForegroundImage);
        BackgroundView = (ImageView) findViewById(R.id.BackgroundImage);
        ForegroundLayout = (RelativeLayout) findViewById(R.id.ForegroundLayout);
        BackgroundLayout = (RelativeLayout) findViewById(R.id.BackgroundLayout);
        ContextText = (TextView) findViewById(R.id.LayerContextText);

        InitializeBottomButtons();
        InitializeCards();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.squidswap_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_settings:
                Intent i = new Intent(this,SquidSettings.class);
                startActivity(i);
                break;
            default:
                break;
        }

        return true;
    }

    //Shows or hides the option cards.
    private void ToggleCards(Boolean tog){
        if(tog){
            CropCard.setAlpha(1);
            PaintCard.setAlpha(1);
            SwapCard.setAlpha(1);
            SaveCard.setAlpha(1);
        }else{
            CropCard.setAlpha(.5f);
            PaintCard.setAlpha(.5f);
            SwapCard.setAlpha(.5f);
            SaveCard.setAlpha(.5f);
        }
    }

    //Grab and set click events for the bottom tab buttons
    private void InitializeBottomButtons(){
        ImageButton = (ImageButton) findViewById(R.id.OpenImage);
        CameraButton = (ImageButton) findViewById(R.id.CameraButton);
        ImageRight = (ImageButton) findViewById(R.id.MoveRight);
        ImageLeft = (ImageButton) findViewById(R.id.MoveLeft);

        ImageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackgroundLayout.setVisibility(View.VISIBLE);
                ForegroundLayout.setVisibility(View.GONE);
                ImageRight.setVisibility(View.GONE);
                ImageLeft.setVisibility(View.VISIBLE);
                FOREGROUND_CONTEXT = false;
                ContextText.setText("Background");

                if(BackgroundImage != null){
                    ToggleCards(true);
                }else{
                    ToggleCards(false);
                }
            }
        });

        ImageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackgroundLayout.setVisibility(View.GONE);
                ForegroundLayout.setVisibility(View.VISIBLE);
                ImageLeft.setVisibility(View.GONE);
                ImageRight.setVisibility(View.VISIBLE);
                FOREGROUND_CONTEXT = true;
                ContextText.setText("Foreground");

                if(ForegroundImage != null){
                    ToggleCards(true);
                }else{
                    ToggleCards(false);
                }
            }
        });

        ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Here we are going to want to know what the context of opening an image is, the user
                //can either be focused on the foreground image or the background image.
                if(FOREGROUND_CONTEXT){
                    //We are going to be opening an image for the foreground.
                    if(!FOREGROUND_CHANGED){
                        if(!FOCUSED_FOREGROUND){
                            // in onCreate or any event where your want the user to
                            // select a file
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent,
                                    "Select Picture"), 1);
                        }else{
                            Toast.makeText(getApplicationContext(),"Foreground image has already been chosen.",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        AlertDialog.Builder dia = new AlertDialog.Builder(SquidswapActivity.this);

                        
                    }
                }else{
                    //Opening image for the background;
                    if(!FOCUSED_BACKGROUND){
                        // in onCreate or any event where your want the user to
                        // select a file
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,
                                "Select Picture"), 1);
                    }else{
                        Toast.makeText(getApplicationContext(),"Background image has already been chosen.",Toast.LENGTH_SHORT).show();
                    }
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
        SaveCard = (RelativeLayout) findViewById(R.id.SaveCard);

        SaveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder build = new AlertDialog.Builder(SquidswapActivity.this);
                LayoutInflater inflate = getLayoutInflater();
                RelativeLayout lay = (RelativeLayout) inflate.inflate(R.layout.save_dialog,null);

                if(FOREGROUND_CONTEXT){
                    if(FOREGROUND_CHANGED){
                        if(ForegroundImage != null){
                            EditText t = lay.findViewById(R.id.FileNameText);

                            //Generate a randomized squidswap string.
                            int rand = (int) Math.floor(Math.random() * 10000);
                            t.setText("squidswap-"+rand+".png");

                            build.setView(lay);
                            build.setTitle("Save image to gallery?").setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

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
                            EditText t = lay.findViewById(R.id.FileNameText);

                            //Generate a randomized squidswap string.
                            int rand = (int) Math.floor(Math.random() * 10000);
                            t.setText("squidswap-"+rand+".png");

                            build.setView(lay);
                            build.setTitle("Save image to gallery?").setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

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

            }
        });

        CropCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //For these cards we are going to want to first check the context of
                //the tool so far, i.e foreground or background image.
                if(FOREGROUND_CONTEXT){
                    if(ForegroundImage != null){
                        Intent i = new InkSliceActivity.SliceBuilder(getApplicationContext(),"squidswap_tmp.png").start();
                        i.putExtra("InkSliceImg",FileServ.TempUriPath("fore"));
                        i.putExtra("SquidSwapContext","fore");
                        startActivityForResult(i,5);
                    }else{
                        Toast.makeText(getApplicationContext(),"Foreground image not selected...",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(BackgroundImage != null){
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
                        Intent i = new InkSplatActivity.InksplatBuilder(getApplicationContext(),"squidswap_tmp.png").build();
                        i.putExtra("InkImgChoice",FileServ.TempUriPath("fore"));
                        i.putExtra("SquidSwapContext","fore");
                        startActivityForResult(i,2);
                    }else{
                        Toast.makeText(getApplicationContext(),"Foreground image not selected...",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if(BackgroundImage != null){
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
                if(ForegroundImage != null){
                    Intent i = new InkStampActivity.InkStampBuilder(getApplicationContext(),"squidswap_tmp.png").start();
                    startActivity(i);
                }else{
                    Toast.makeText(getApplicationContext(),"Image not chosen.",Toast.LENGTH_SHORT).show();
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

                FileBmp = BitmapFactory.decodeStream(FileStream);
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

        public Bitmap LoadTemp(){
            Bitmap CachedFile;
            File CachedPath = new File(getCacheDir(),"squidswap_tmp.png");
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
    }
}
