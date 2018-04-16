package com.kinghorn.squidswap.squidswap;

import android.app.ActionBar;
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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.kinghorn.inksplat.inksplat.InkSplatActivity;
import com.kinghorn.inkstamp.inkstamp.InkStampActivity;
import com.squidswap.inkslice.inkslice.InkSliceActivity;

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
    private ImageButton ImageButton,CameraButton,ImageRight,ImageLeft,RemoveContextImage;
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

                    ToggleRemoveImage();
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
        RemoveContextImage = (ImageButton) findViewById(R.id.RemoveImage);

        InitializeBottomButtons();
        InitializeCards();

        //Actionbar stuff goes here
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        LayoutInflater flate = getLayoutInflater();
        RelativeLayout r = (RelativeLayout) flate.inflate(R.layout.actionbar_layout,null);
        TextView title = (TextView) r.findViewById(R.id.SquidswapTitle);
        ImageButton set = (ImageButton) r.findViewById(R.id.SquidSwapSettings);
        Typeface fac = Typeface.createFromAsset(getAssets(),"fonts/AdmiralCAT.ttf");
        title.setTypeface(fac);
        bar.setDisplayShowTitleEnabled(false);
        bar.setCustomView(r);
        bar.setDisplayShowCustomEnabled(true);

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),SquidSettings.class);
                startActivity(i);
            }
        });

        RequestPermission();

        RemoveContextImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(SquidswapActivity.this);
                LayoutInflater infl = getLayoutInflater();
                RelativeLayout r = (RelativeLayout) infl.inflate(R.layout.remove_layout,null);
                b.setView(r);

                String imgCont = "";

                if(FOREGROUND_CONTEXT){
                    imgCont = " foreground ";
                }else{
                    imgCont = " background ";
                }

                b.setTitle("Remove"+imgCont+"image?").setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
            }
        });
    }

    private void RequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 786);
        }
    }

    /*public boolean onCreateOptionsMenu(Menu menu) {
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
    }*/

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

                ToggleRemoveImage();
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

                ToggleRemoveImage();
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

        CameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

    //Shows or hides the remove image based on the context and if an image
    //has been opened within the context.
    private void ToggleRemoveImage(){
        if(FOREGROUND_CONTEXT){
            //Hide logic for the foreground.
            if(ForegroundImage != null){
                RemoveContextImage.setVisibility(View.VISIBLE);
            }else{
                RemoveContextImage.setVisibility(View.GONE);
            }
        }else{
            if(BackgroundImage != null){
                RemoveContextImage.setVisibility(View.VISIBLE);
            }else{
                RemoveContextImage.setVisibility(View.GONE);
            }
        }
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
                            final EditText t = lay.findViewById(R.id.FileNameText);

                            //Generate a randomized squidswap string.
                            int rand = (int) Math.floor(Math.random() * 10000);
                            t.setText("squidswap-"+rand+".png");

                            build.setView(lay);
                            build.setTitle("Save image to gallery?").setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Bitmap b = FileServ.LoadTemp("fore");
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

        //Erases the temp file in the local app cache based on the given uri.
        public void EraseTemporaryFile(Uri tempFile){

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
