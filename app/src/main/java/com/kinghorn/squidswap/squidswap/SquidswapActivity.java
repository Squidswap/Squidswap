package com.kinghorn.squidswap.squidswap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kinghorn.inksplat.inksplat.InkSplatActivity;
import com.squidswap.inkslice.inkslice.InkSliceActivity;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SquidswapActivity extends AppCompatActivity {

    private boolean FOCUSED_IMAGE = false;
    private ImageButton ImageButton,CameraButton,PreviewButton,SaveButton;
    private RelativeLayout CropCard,PaintCard,SwapCard;
    private LinearLayout TopOptions;
    private ImageView SelectedImage;
    private Uri ChosenImage;
    private FileService FileServ;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch(requestCode){
                case 1:
                    SelectedImage.setImageURI(data.getData());
                    ChosenImage = data.getData();
                    FileServ.SaveTemp(data.getData(),true);
                    //Make all of the options visible.
                    CropCard.setAlpha(1);
                    PaintCard.setAlpha(1);
                    SwapCard.setAlpha(1);
                    TopOptions.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    if(data.hasExtra("InksplatFile")){
                        String file_path = data.getStringExtra("InksplatFile");
                        FileServ.SaveTemp(Uri.parse(file_path),false);
                        SelectedImage.setImageBitmap(BitmapFactory.decodeFile(file_path));
                    }
                    break;
                case 5:
                    if(data.hasExtra("InkSliceFile")){
                        String file_path = data.getStringExtra("InkSliceFile");
                        FileServ.SaveTemp(Uri.parse(file_path),false);
                        SelectedImage.setImageBitmap(BitmapFactory.decodeFile(file_path));
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squidswap);

        FileServ = new FileService();
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
                    Intent i = new InkSliceActivity.SliceBuilder(getApplicationContext(),"squidswap_tmp.png").start();
                    i.putExtra("InkSliceImg",FileServ.TempUriPath());
                    startActivityForResult(i,5);
                }else{
                    Toast.makeText(getApplicationContext(),"Image not chosen.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        PaintCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ChosenImage != null){
                    Intent i = new InkSplatActivity.InksplatBuilder(getApplicationContext(),"squidswap_tmp.png").build();
                    i.putExtra("InkImgChoice",FileServ.TempUriPath());
                    startActivityForResult(i,2);
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

    //Class that will handle the saving and loading on temporary files that are being
    //edited.
    private class FileService{
        //Save the currently chosen file to the temporary directory.
        public void SaveTemp(Uri FileUri,Boolean FirstImg){
            File fil = new File(getApplicationContext().getCacheDir(),"squidswap_tmp.png");
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

        public Uri TempUriPath(){
            return Uri.parse(getCacheDir().toString() + "/squidswap_tmp.png");
        }
    }
}
