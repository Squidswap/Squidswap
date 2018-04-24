package com.kinghorn.squidswap.squidswap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SquidswapSaveSwap extends AppCompatActivity {

    private FileService fileServ;
    private ImageView previewImage;
    private Button SaveBtn,CancelBtn;
    private TextView SaveImage;
    private int SWAP_PREV_REQ = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squidswap_save_swap);
        //Actionbar stuff goes here
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        LayoutInflater flate = getLayoutInflater();
        RelativeLayout r = (RelativeLayout) flate.inflate(R.layout.actionbar_layout,null);
        TextView title = (TextView) r.findViewById(R.id.SquidswapTitle);
        ImageButton set = (ImageButton) r.findViewById(R.id.SquidSwapSettings);
        ImageButton lay = (ImageButton) r.findViewById(R.id.LayersToggle);
        Typeface fac = Typeface.createFromAsset(getAssets(),"fonts/AdmiralCAT.ttf");
        title.setTypeface(fac);
        set.setVisibility(View.GONE);
        lay.setVisibility(View.GONE);
        bar.setDisplayShowTitleEnabled(false);
        bar.setCustomView(r);
        bar.setDisplayShowCustomEnabled(true);
        SaveImage = (TextView) findViewById(R.id.SaveImageText);

        fileServ = new FileService();
        Intent i = getIntent();

        if(i.hasExtra("SquidStampPreview")){
            previewImage = (ImageView) findViewById(R.id.FinalSwapPreview);
            final Bitmap fin = fileServ.LoadTemp("");
            previewImage.setImageBitmap(fin);
            CancelBtn = (Button) findViewById(R.id.CancelSwapSave);
            SaveBtn = (Button) findViewById(R.id.SaveSwap);

            SaveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder b = new AlertDialog.Builder(SquidswapSaveSwap.this);
                    LayoutInflater infl = getLayoutInflater();
                    RelativeLayout r = (RelativeLayout) infl.inflate(R.layout.save_dialog,null);
                    final EditText t = r.findViewById(R.id.FileNameText);

                    //Generate a randomized squidswap string.
                    int rand = (int) Math.floor(Math.random() * 10000);
                    t.setText("squidswap-"+rand);
                    b.setTitle("Save Image");
                    b.setView(r);
                    b.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fileServ.SaveToGallery(fin,t.getText().toString());
                            Intent r = new Intent();
                            r.putExtra("requestCode",SWAP_PREV_REQ);
                            setResult(RESULT_OK,r);
                            finish();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();

                }
            });

            CancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    //Class that will handle the saving and loading on temporary files that are being
    //edited.
    public class FileService{
        //Save the currently chosen file to the temporary directory.
        //We are also going to want to have a context variable that will determine the temp file
        //for foreground as well as background image.
        public void SaveTemp(Uri FileUri, Boolean FirstImg, String cont){
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
            File CachedPath = new File(getCacheDir(),"InkStampTemp");
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
                //Toast.makeText(getApplicationContext(),"Image saved to gallery.",Toast.LENGTH_SHORT).show();
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
