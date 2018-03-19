package com.kinghorn.inksplat.inksplat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class InkSplatActivity extends AppCompatActivity {

    private Paint color,size_indicator;
    private Bitmap chosenImage,outputImage;
    private ImageButton check_btn,can_btn,stroke_back,ac_check,ac_cancel;
    private Intent intent;
    private InkCanvas canvas;
    private float size = 25f;
    private SeekBar brush_size_seeker;
    private Animation slide_up,slide_down;
    private ImageView eraser_toggle;
    private boolean ERASER_MODE = false;
    private TextView back_counter;
    private int INKSPLAT_GET_RESULT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ink_splat);

        //Grab the intent information from activity this was started from.
        intent = getIntent();

        if(intent.getExtras().get("InkImgChoice") != null){
            this.chosenImage = BitmapFactory.decodeFile(intent.getExtras().get("InkImgChoice").toString());
        }

        //Initialize the brush size indication paint.
        size_indicator = new Paint();
        size_indicator.setColor(Color.BLACK);
        size_indicator.setStyle(Paint.Style.STROKE);
        size_indicator.setAntiAlias(true);
        size_indicator.setStrokeWidth(10);


        //Initialize the color object.
        color = new Paint();
        color.setColor(Color.BLACK);
        color.setStyle(Paint.Style.STROKE);
        color.setAntiAlias(true);
        color.setStrokeCap(Paint.Cap.ROUND);
        color.setStrokeWidth(size);
        color.setStrokeJoin(Paint.Join.ROUND);

        canvas = new InkCanvas(getApplicationContext());

        RelativeLayout l = findViewById(R.id.paint_layout);
        l.addView(canvas);

        ImplementColorSwatches();
        ImplementClickEvents();
    }

    //Loops through the color swatch list and adds click events for each color swatch.
    private void ImplementColorSwatches(){
        final ViewGroup swatchList = findViewById(R.id.swatch_list);

        for(int i = 0;i < swatchList.getChildCount();i++){
            final RelativeLayout v = (RelativeLayout) swatchList.getChildAt(i);
            final View arraow = v.getChildAt(1);

            v.getChildAt(0).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for(int k = 0;k < swatchList.getChildCount();k++){
                        final RelativeLayout d = (RelativeLayout) swatchList.getChildAt(k);
                        d.getChildAt(1).setVisibility(View.INVISIBLE);
                    }

                    color.setColor(v.getChildAt(0).getBackgroundTintList().getDefaultColor());
                    canvas.invalidate();

                    arraow.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    //Saves the painting as a temporary file that will be opened after the activity if finished.
    private String SavePainting(){
        String file_name = "inksplat_tmp.jpg";
        Bitmap temp_img = canvas.getDrawingCache();
        File fil = new File(getApplicationContext().getCacheDir(),file_name);
        try {
            fil.createNewFile();
            FileOutputStream fos = new FileOutputStream(fil);
            temp_img.compress(Bitmap.CompressFormat.JPEG,100,fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Check for the image and then return it.
        return fil.getAbsolutePath();
    }

    //Initializes the click events for all our buttons.
    private void ImplementClickEvents(){
        brush_size_seeker = (SeekBar) findViewById(R.id.brush_seekbar);
        stroke_back = (ImageButton) findViewById(R.id.paintbrush_back);
        slide_up = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_up);
        slide_down = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_down);
        check_btn = (ImageButton) findViewById(R.id.activityCheck);
        can_btn = (ImageButton) findViewById(R.id.activityCancel);
        eraser_toggle = (ImageView) findViewById(R.id.EraserModeToggle);
        ac_check = (ImageButton) findViewById(R.id.activityCheck);
        ac_cancel = (ImageButton) findViewById(R.id.activityCancel);
        back_counter = (TextView) findViewById(R.id.backCounter);

        ac_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Here we will want to go back to the last activity that started this one.
                finish();
            }
        });
        ac_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Apply the paint to the actual image and send it back to the original activity.

                //Start the intent and save the temp file to the given directory.
                Intent r = new Intent();
                r.putExtra("InksplatFile",SavePainting());
                r.putExtra("requestCode",INKSPLAT_GET_RESULT);
                setResult(RESULT_OK,r);
                finish();
            }
        });

        eraser_toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ERASER_MODE){
                    ERASER_MODE = false;
                    eraser_toggle.setAlpha(.2f);
                    canvas.invalidate();
                }else{
                    ERASER_MODE = true;
                    eraser_toggle.setAlpha(.7f);
                    canvas.invalidate();
                }
            }
        });

        stroke_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(canvas.paths.size() == 1){
                    stroke_back.setClickable(false);
                    back_counter.setText("0");
                    stroke_back.setAlpha(1f);
                }

                if(canvas.paths.size() > 0){
                    canvas.paths.remove(canvas.paths.size() - 1);
                    back_counter.setText(String.valueOf(canvas.paths.size()));
                    canvas.invalidate();
                }
            }
        });

        brush_size_seeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                size = brush_size_seeker.getProgress();
                color.setStrokeWidth(brush_size_seeker.getProgress());
                canvas.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public class InkPath extends Path{
        private Paint p;

        public InkPath(int color){
            p = new Paint();
            p.setColor(color);
            p.setStyle(Paint.Style.STROKE);
            p.setAntiAlias(true);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(size);
            p.setStrokeJoin(Paint.Join.ROUND);
        }

        public Paint getPaint(){
            return this.p;
        }
    }

    //Canvas that we will use to draw the image onto.
    public class InkCanvas extends View{

        private Paint ep;
        private float eraserx = 0,erasery = 0;
        private InkPath p = new InkPath(Color.BLACK);
        private ArrayList<InkPath> paths;

        public InkCanvas(Context c){
            super(c);

            ep = new Paint();
            ep.setColor(Color.WHITE);
            ep.setStyle(Paint.Style.STROKE);
            ep.setStrokeWidth(5);
            ep.setAlpha(75);

            paths = new ArrayList<InkPath>();

            setDrawingCacheEnabled(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //Draw the chosen image onto the canvas.
            if(chosenImage != null){

                canvas.drawBitmap(GetScaledImage(),(getWidth() - GetScaledImage().getWidth()) / 2,(getHeight() - GetScaledImage().getHeight()) / 2,null);
            }

            for(InkPath path:paths){
                canvas.drawPath(path,path.getPaint());
            }
            canvas.drawPath(p,color);

            if(ERASER_MODE){
                canvas.drawCircle(eraserx,erasery,brush_size_seeker.getProgress(),ep);
            }

            //Draw the brush size indicator over everything else.
            //canvas.drawOval(new RectF((getWidth() - size) / 2,(getHeight() - size) / 2,((getWidth() - 100) / 2) + size,((getHeight() - 100) / 2) + size),size_indicator);
        }

        private Bitmap GetScaledImage(){
            //Center and scale up the chosen image.
            float scaledWidth = (float) getWidth() / (float) chosenImage.getWidth();
            Matrix m = new Matrix();
            m.setScale(scaledWidth,scaledWidth);
            Bitmap scaled = Bitmap.createBitmap(chosenImage,0,0,chosenImage.getWidth(),chosenImage.getHeight(),m,true);
            return scaled;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if(ERASER_MODE){
                        eraserx = event.getX();
                        erasery = event.getY();
                    }else{
                        p.moveTo(event.getX(),event.getY());
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(ERASER_MODE){
                        eraserx = event.getX();
                        erasery = event.getY();
                    }else{
                        p.moveTo(event.getX(),event.getY());

                        InkPath newPath = new InkPath(color.getColor());
                        newPath.set(p);
                        paths.add(newPath);

                        if(paths.size() == 1){
                            stroke_back.setClickable(true);
                            stroke_back.setAlpha(1f);
                        }

                        back_counter.setText(String.valueOf(canvas.paths.size()));

                        p.reset();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(ERASER_MODE){
                        eraserx = event.getX();
                        erasery = event.getY();
                    }else{
                        p.lineTo(event.getX(),event.getY());
                    }

                    break;
            }
            invalidate();
            return true;
        }

        //So when the painting has finished, we want to get the size of the painted image and crop the drawing cache to that size after applying the paint
        //to the image.
        public Bitmap CropPaintedImage(){
            return Bitmap.createBitmap(getDrawingCache(),(getWidth() - GetScaledImage().getWidth()) / 2,(getHeight() - GetScaledImage().getHeight()) / 2,((getWidth() - GetScaledImage().getWidth()) / 2) + GetScaledImage().getWidth(),((getHeight() - GetScaledImage().getHeight()) / 2)+GetScaledImage().getHeight());
        }

        private Bitmap ConvertPaintToImage(){
            for(InkPath p:paths){

            }
            return null;
        }
    }

    //Builder class that we will use to set up our instance of inksplat
    public static class InksplatBuilder extends Intent{
        private final Uri InkStartImg,InkTargetImg;
        private Context ctx;
        private Class<?> ac_class;
        private Activity ReturnAc;

        public InksplatBuilder(Context ctx, Uri InkStartImg, Uri InkTargetImg){
            super(ctx,InkSplatActivity.class);
            this.InkStartImg = InkStartImg;
            this.InkTargetImg = InkTargetImg;
            this.ctx = ctx;
        }

        //Starts the new intent and launches the activity.
        //Requires the activity we want to go back to.
        public Intent build(){
            this.putExtra("InkImgChoice",this.InkStartImg);
            return this;
        }
    }
}
