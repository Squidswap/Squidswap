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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
    private ImageButton ImageButton,CameraButton;
    private RelativeLayout CropCard,PaintCard,SwapCard,SaveCard;
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
                    SaveCard.setAlpha(1);
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
        SaveCard = (RelativeLayout) findViewById(R.id.SaveCard);

        SaveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder build = new AlertDialog.Builder(SquidswapActivity.this);
                if(ChosenImage != null){
                    build.setTitle("Save image to gallery?").setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
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

            }
        });

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
