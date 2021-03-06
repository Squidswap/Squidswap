package com.squidswap.inkslice.inkslice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.media.Image;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

import javax.xml.transform.Source;

public class InkSliceActivity extends Activity {

    private static Uri FocusedImage;
    private FileService FilServ;
    private SliceCanvas SliceCan;
    private RelativeLayout CanvasLayout,BottomToggle;
    private LinearLayout ZoomSeekLay,TopCommands;
    private ImageButton SuccessBtn,CancelBtn,RotateNinety,MirrorBtn,ToggleFreeform,ResetFreeForm;
    private int SELECT_PICTURE = 1,INKSLICE_RETURN = 3;
    private float POINT_X,POINT_Y,PORT_WIDTH,PORT_HEIGHT,IMG_X,IMG_Y,CURRENT_SCALE = 1,IMAGE_ROTATION = 0;
    private static Bitmap SliceFile,BeforeCrop;
    private boolean RESIZING = false,CROPPING = false,MIRRORED = false,FREEFORM = false,HAS_CROPPED = false;
    private TextView ProgressInst;
    private String TempFileName,ParentContext;
    private SeekBar ZoomSeeker;
    private CropSlicer FreeformSlicer;
    private ToggleButton CropTog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ink_slice);

        this.POINT_X = 0;
        this.POINT_Y = 0;
        this.PORT_WIDTH = 500;
        this.PORT_HEIGHT = 500;
        POINT_X = getResources().getDisplayMetrics().widthPixels / 2;
        POINT_Y = getResources().getDisplayMetrics().heightPixels / 2;
        this.SliceCan = new SliceCanvas(getApplicationContext());
        ViewGroup.LayoutParams CanParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        this.SliceCan.setLayoutParams(CanParams);
        this.FilServ = new FileService();
        this.CanvasLayout = findViewById(R.id.CanvasLayout);
        this.ProgressInst = (TextView) findViewById(R.id.ProgressText);
        this.TopCommands = (LinearLayout) findViewById(R.id.TopCommands);
        FreeformSlicer = new CropSlicer(this.SliceCan);

        //Add the canvas to the layout that holds the canvasview.
        this.CanvasLayout.addView(this.SliceCan);

        Intent i = getIntent();

        if(i.hasExtra("InkSliceImg")){
            SliceFile = FilServ.LoadFile((Uri) i.getExtras().get("InkSliceImg"),false);

            if(i.hasExtra("SquidSwapContext")){
                ParentContext =  i.getExtras().getString("SquidSwapContext");
            }

            //We need to determine the minimum scale value that will always be the full width of the screen.
            //Minimum scale of the image.
            float scale = (float) getResources().getDisplayMetrics().widthPixels / SliceFile.getWidth();
            CURRENT_SCALE = scale;
            this.InitializeButton();
            SliceCan.invalidate();
        }

    }

    private void InitializeButton(){
        SuccessBtn = (ImageButton) findViewById(R.id.SuccessBtn);
        CancelBtn = (ImageButton) findViewById(R.id.CancelBtn);
        BottomToggle = (RelativeLayout) findViewById(R.id.ScaleSliderUp);
        ZoomSeekLay = (LinearLayout) findViewById(R.id.ZoomSeekLayout);
        ZoomSeeker = (SeekBar) findViewById(R.id.ZoomSeek);
        ZoomSeeker.setMax((int) (CURRENT_SCALE + 3) * 100);
        ZoomSeeker.setProgress((int) Math.ceil(CURRENT_SCALE * 100));
        RotateNinety = (ImageButton) findViewById(R.id.RotateNinety);
        MirrorBtn = (ImageButton) findViewById(R.id.MirrorButton);
        ResetFreeForm = (ImageButton) findViewById(R.id.ResetFreeForm);
        CropTog = (ToggleButton) findViewById(R.id.CropToggle);

        CropTog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FREEFORM = !FREEFORM;

                if(FREEFORM){
                    ResetFreeForm.setVisibility(View.VISIBLE);
                }else{
                    ResetFreeForm.setVisibility(View.GONE);
                }

                SliceCan.invalidate();
            }
        });

        ResetFreeForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FreeformSlicer.ResetPath();
                HAS_CROPPED = false;
                SliceCan.invalidate();
            }
        });

        MirrorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MIRRORED = !MIRRORED;
                SliceCan.invalidate();
            }
        });

        RotateNinety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMAGE_ROTATION += 90;
                SliceCan.invalidate();
            }
        });

        CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        SuccessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(FREEFORM){
                    AlertDialog.Builder b = new AlertDialog.Builder(new ContextThemeWrapper(InkSliceActivity.this,android.R.style.Theme_Material_Light));
                    LayoutInflater flate = getLayoutInflater();
                    RelativeLayout r = (RelativeLayout) flate.inflate(R.layout.inkslice_dialog, null);
                    b.setView(r);
                    b.setTitle("Crop Image?").setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent r = new Intent();
                            r.putExtra("InkSliceFile",FilServ.SaveFile(FreeformSlicer.CropOut(),"squidswap_tmp.png"));
                            r.putExtra("requestCode",INKSLICE_RETURN);
                            r.putExtra("SquidSwapContext",ParentContext);
                            setResult(RESULT_OK,r);
                            finish();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
                }else{
                    AlertDialog.Builder b = new AlertDialog.Builder(new ContextThemeWrapper(InkSliceActivity.this,android.R.style.Theme_Material_Light));
                    LayoutInflater flate = getLayoutInflater();
                    RelativeLayout r = (RelativeLayout) flate.inflate(R.layout.inkslice_dialog, null);
                    b.setView(r);
                    b.setTitle("Crop Image?").setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent r = new Intent();
                            r.putExtra("InkSliceFile",FilServ.SaveFile(SliceCan.ReturnCropPreview(),"squidswap_tmp.png"));
                            r.putExtra("requestCode",INKSLICE_RETURN);
                            r.putExtra("SquidSwapContext",ParentContext);
                            setResult(RESULT_OK,r);
                            finish();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
                }
            }
        });

        BottomToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ZoomSeekLay.getVisibility() == View.GONE){
                    ZoomSeekLay.setVisibility(View.VISIBLE);
                    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,75);
                    p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    p.setMargins(0,0,0,ZoomSeekLay.getHeight());
                    BottomToggle.setLayoutParams(p);
                }else{
                    ZoomSeekLay.setVisibility(View.GONE);
                    RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,75);
                    p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    p.setMargins(0,0,0,0);
                    BottomToggle.setLayoutParams(p);
                }
            }
        });

        ZoomSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i >= 100){
                    CURRENT_SCALE = (float) i / 100;
                }

                SliceCan.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //Class that will initialize creating an inkslice activity.
    public static class SliceBuilder extends Intent {
        private Uri First;
        private Context ctx;

        public SliceBuilder(Context ctx,String TempFileName){
            super(ctx,InkSliceActivity.class);
            TempFileName = TempFileName;
            this.First = Uri.parse(ctx.getApplicationContext().getCacheDir() + "/" + TempFileName);
            this.ctx = ctx;
        }

        //Starts the slicing activity.
        public Intent start(){
            this.putExtra("InkSliceFile",this.First);
            return this;
        }
    }

    //Class that will handle drawing to the actual canvas that will display stuff
    //over the chosen image.
    private class SliceCanvas extends View {
        private Paint ViewPortPaint,PorterPaint,BorderPaint,HandlePaint,CrosshairPaint;
        private int HANDLE_RADIUS = 30;
        private float DRAG_START_Y,DRAG_START_X,PAN_START_X,PAN_START_Y,PAN_END_X,PAN_END_Y,LAST_PINCH = 0;
        private boolean HANDLE_TOP = false,HANDLE_BOTTOM = false,HANDLE_LEFT = false,HANDLE_RIGHT = false,CROSSHAIRS = true,PINCHING = false;
        private Rect VIEWPORT_RECT;

        public SliceCanvas(Context context) {
            super(context);

            setDrawingCacheEnabled(true);
            setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

            ViewPortPaint = new Paint();
            ViewPortPaint.setColor(Color.BLACK);
            ViewPortPaint.setAlpha(160);
            ViewPortPaint.setStyle(Paint.Style.FILL);

            PorterPaint = new Paint();
            PorterPaint.setColor(Color.TRANSPARENT);
            PorterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

            BorderPaint = new Paint();
            BorderPaint.setColor(Color.WHITE);
            BorderPaint.setStyle(Paint.Style.STROKE);
            BorderPaint.setStrokeWidth(5);

            HandlePaint = new Paint();
            HandlePaint.setColor(Color.WHITE);
            HandlePaint.setStyle(Paint.Style.FILL);

            CrosshairPaint = new Paint();
            CrosshairPaint.setStyle(Paint.Style.STROKE);
            CrosshairPaint.setColor(Color.WHITE);
            CrosshairPaint.setStrokeWidth(3);
            CrosshairPaint.setAlpha(50);

            //Initialize the viewport rectangle.
            VIEWPORT_RECT = new Rect((int) Math.floor(POINT_X),(int) Math.floor(POINT_Y),(int) Math.floor(POINT_X + 500),(int) Math.floor(POINT_Y + 500));

            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(!CROPPING){
                        switch(motionEvent.getAction()){
                            case MotionEvent.ACTION_DOWN:
                                //Determine the type of cropping tool that is being used.
                                if(FREEFORM){
                                    //Start the painting here.
                                    if(!HAS_CROPPED){
                                        FreeformSlicer.StartShape(new SlicePoint(motionEvent.getX(),motionEvent.getY()));
                                    }
                                }else{
                                    //Determine if the touch is inside one of the circles or not.
                                    if(InsideHandle(POINT_X,POINT_Y + (PORT_HEIGHT/2),HANDLE_RADIUS,motionEvent.getX(),motionEvent.getY()) || InsideHandle(POINT_X,POINT_Y - (PORT_HEIGHT / 2),HANDLE_RADIUS,motionEvent.getX(),motionEvent.getY()) || InsideHandle(POINT_X - (PORT_WIDTH/2),POINT_Y,HANDLE_RADIUS,motionEvent.getX(),motionEvent.getY()) || InsideHandle(POINT_X + (PORT_WIDTH/2),POINT_Y,HANDLE_RADIUS,motionEvent.getX(),motionEvent.getY())){

                                        if(InsideHandle(POINT_X,POINT_Y + (PORT_HEIGHT/2),HANDLE_RADIUS,motionEvent.getX(),motionEvent.getY())){
                                            SetHandle(1);
                                        }else if(InsideHandle(POINT_X,POINT_Y - (PORT_HEIGHT / 2),HANDLE_RADIUS,motionEvent.getX(),motionEvent.getY())){
                                            SetHandle(2);
                                        }else if(InsideHandle(POINT_X - (PORT_WIDTH/2),POINT_Y,HANDLE_RADIUS,motionEvent.getX(),motionEvent.getY())){
                                            SetHandle(3);
                                        }else if(InsideHandle(POINT_X + (PORT_WIDTH/2),POINT_Y,HANDLE_RADIUS,motionEvent.getX(),motionEvent.getY())){
                                            SetHandle(4);
                                        }

                                        BorderPaint.setColor(getResources().getColor(R.color.colorAccent));
                                        HandlePaint.setColor(getResources().getColor(R.color.colorAccent));
                                        CrosshairPaint.setColor(getResources().getColor(R.color.colorAccent));
                                        RESIZING = true;
                                        DRAG_START_Y = motionEvent.getY();
                                        DRAG_START_X = motionEvent.getX();
                                    }else{
                                        //Here is where we have to check if they are clicking on the viewport or not.
                                        if(VIEWPORT_RECT.contains((int) motionEvent.getX(),(int) motionEvent.getY())){
                                            POINT_X = motionEvent.getX();
                                            POINT_Y = motionEvent.getY();
                                            VIEWPORT_RECT.set((int) Math.floor(POINT_X - (PORT_WIDTH/2)),(int) Math.floor(POINT_Y - (PORT_HEIGHT/2)),(int) Math.floor(POINT_X + (PORT_WIDTH/2)),(int) Math.floor(POINT_Y + (PORT_HEIGHT/2)));
                                        }
                                    }
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                if(FREEFORM){
                                    if(!HAS_CROPPED){
                                        FreeformSlicer.EndShape(new SlicePoint(motionEvent.getX(),motionEvent.getY()));
                                        HAS_CROPPED = true;
                                    }
                                }else{
                                    BorderPaint.setColor(Color.WHITE);
                                    HandlePaint.setColor(Color.WHITE);
                                    CrosshairPaint.setColor(Color.WHITE);
                                    PINCHING = false;
                                    RESIZING = false;
                                }
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if(FREEFORM){
                                    if(!HAS_CROPPED) {
                                        FreeformSlicer.AddPoint(new SlicePoint(motionEvent.getX(), motionEvent.getY()));
                                    }
                                }else{
                                    //Determine if the touch is inside one of the circles or not.
                                    if(!RESIZING && !PINCHING){
                                        BorderPaint.setColor(Color.WHITE);
                                        //Next we need to check if where they are clicking is actually inside the viewport rectangle, if so
                                        //then they can start moving the rectange if not then we want to pan the background image.

                                        if(VIEWPORT_RECT.contains((int) motionEvent.getX(),(int) motionEvent.getY())){
                                            //Check here if the viewport is off the screen or not.
                                            if(motionEvent.getY() + (PORT_HEIGHT/2) < getHeight() && motionEvent.getY() - (PORT_HEIGHT/2) > 0){
                                                POINT_Y = motionEvent.getY();
                                            }

                                            if(motionEvent.getX() + (PORT_WIDTH/2) < getWidth() && motionEvent.getX() - (PORT_WIDTH/2) > 0){
                                                POINT_X = motionEvent.getX();
                                            }

                                            VIEWPORT_RECT.set((int) Math.floor(POINT_X - (PORT_WIDTH/2)),(int) Math.floor(POINT_Y - (PORT_HEIGHT/2)),(int) Math.floor(POINT_X + (PORT_WIDTH/2)),(int) Math.floor(POINT_Y + (PORT_HEIGHT/2)));
                                        }
                                    }else if(!PINCHING){
                                        if(HANDLE_TOP){
                                            //Determine the movement based on
                                            float DragDiff = DRAG_START_Y - motionEvent.getY();

                                            if(PORT_HEIGHT < getHeight()){
                                                PORT_HEIGHT += DragDiff;
                                                DRAG_START_Y = motionEvent.getY();
                                            }else{
                                                PORT_HEIGHT = getHeight();
                                            }
                                        }

                                        if(HANDLE_BOTTOM){
                                            //Determine the movement based on
                                            float DragDiff = DRAG_START_Y - motionEvent.getY();

                                            if(PORT_HEIGHT < getHeight()){
                                                PORT_HEIGHT -= DragDiff;
                                                DRAG_START_Y = motionEvent.getY();
                                            }else{
                                                PORT_HEIGHT = getHeight();
                                            }
                                        }

                                        if(HANDLE_RIGHT){
                                            float DragDiff = DRAG_START_X - motionEvent.getX();

                                            if(PORT_WIDTH < getWidth()){
                                                PORT_WIDTH -= DragDiff;
                                                DRAG_START_X = motionEvent.getX();
                                            }else{
                                                PORT_WIDTH = getWidth();
                                            }
                                        }

                                        if(HANDLE_LEFT){
                                            float DragDiff = DRAG_START_X - motionEvent.getX();

                                            if(PORT_WIDTH < getWidth()){
                                                PORT_WIDTH += DragDiff;
                                                DRAG_START_X = motionEvent.getX();
                                            }else{
                                                PORT_WIDTH = getWidth();
                                            }
                                        }
                                    }

                                    //Now if we are pinching we want to determine the distance between the two points and
                                    //scale based on the distance.
                                    if(PINCHING){
                                        double distance = Math.sqrt(Math.pow(motionEvent.getX(0) - motionEvent.getX(1),2) + Math.pow(motionEvent.getY(0) - motionEvent.getY(1),2));

                                        if(distance > LAST_PINCH){
                                            if(CURRENT_SCALE < 3){
                                                CURRENT_SCALE += (float) distance / 10000;
                                            }
                                        }else{
                                            if(CURRENT_SCALE > .25){
                                                CURRENT_SCALE -= (float) distance / 10000;
                                            }
                                        }

                                        LAST_PINCH = (float) distance;
                                    }
                                }
                                break;
                        }
                    }
                    invalidate();
                    return true;
                }
            });
        }

        //Resets all the handles and sets one to true
        private void SetHandle(int handle){
            HANDLE_BOTTOM = false;
            HANDLE_TOP = false;
            HANDLE_LEFT = false;
            HANDLE_RIGHT = false;

            switch(handle){
                case 1:
                    HANDLE_BOTTOM = true;
                    break;
                case 2:
                    HANDLE_TOP = true;
                    break;
                case 3:
                    HANDLE_LEFT = true;
                    break;
                case 4:
                    HANDLE_RIGHT = true;
                    break;
            }

            RESIZING = true;
        }

        //Returns the preview of the cropped image and displays it.
        private Bitmap ReturnCropPreview(){
            CROPPING = true;
            invalidate();
            BeforeCrop = SliceFile;
            Bitmap cached = getDrawingCache();

            //Reset all our values.
            CURRENT_SCALE = 1;
            Bitmap b = Bitmap.createBitmap(cached,VIEWPORT_RECT.left,VIEWPORT_RECT.top,VIEWPORT_RECT.right - VIEWPORT_RECT.left,VIEWPORT_RECT.bottom - VIEWPORT_RECT.top);
            IMG_X = (getWidth() - b.getWidth()) / 2;
            IMG_Y = (getHeight() - b.getHeight()) / 2;

            return b;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if(SliceFile != null){
                Bitmap ScaledBmp = AutoScale(SliceFile);

                canvas.drawBitmap(MirrorImage(RotateImage(ScaledBmp)),IMG_X,(getHeight() - ScaledBmp.getHeight()) / 2,null);

                if(!CROPPING && !FREEFORM){
                    DrawViewport(canvas);
                }else{
                    if(HAS_CROPPED){
                        FreeformSlicer.DrawPreviewWindow(canvas);
                    }

                    FreeformSlicer.DrawPath(canvas);
                }
            }
        }

        //Draw the dimmed viewport over the image.
        private void DrawViewport(Canvas c){
            Bitmap b = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
            Canvas ViewP = new Canvas(b);
            ViewP.drawRect(0,0,c.getWidth(),c.getHeight(),ViewPortPaint);
            ViewP.drawRect(POINT_X - (PORT_WIDTH/2),POINT_Y - (PORT_HEIGHT / 2),POINT_X + (PORT_WIDTH / 2),POINT_Y + (PORT_HEIGHT / 2),PorterPaint);
            ViewP.drawRect(POINT_X - (PORT_WIDTH/2),POINT_Y - (PORT_HEIGHT / 2),POINT_X + (PORT_WIDTH / 2),POINT_Y + (PORT_HEIGHT / 2),BorderPaint);

            //Draw the viewport handles.
            ViewP.drawCircle(POINT_X,POINT_Y - (PORT_HEIGHT / 2),HANDLE_RADIUS, HandlePaint);
            ViewP.drawCircle(POINT_X - (PORT_WIDTH/2),POINT_Y,HANDLE_RADIUS, HandlePaint);
            ViewP.drawCircle(POINT_X + (PORT_WIDTH/2),POINT_Y,HANDLE_RADIUS, HandlePaint);
            ViewP.drawCircle(POINT_X,POINT_Y + (PORT_HEIGHT/2),HANDLE_RADIUS, HandlePaint);

            if(CROSSHAIRS){
                ViewP.drawLine(POINT_X - (PORT_WIDTH / 4),POINT_Y - (PORT_HEIGHT / 2),POINT_X - (PORT_WIDTH / 4),POINT_Y + (PORT_HEIGHT / 2),CrosshairPaint);
                ViewP.drawLine(POINT_X + (PORT_WIDTH / 4),POINT_Y - (PORT_HEIGHT / 2),POINT_X + (PORT_WIDTH / 4),POINT_Y + (PORT_HEIGHT / 2),CrosshairPaint);
                ViewP.drawLine(POINT_X - (PORT_WIDTH / 2),POINT_Y - (PORT_HEIGHT / 4),POINT_X + (PORT_WIDTH / 2),POINT_Y - (PORT_HEIGHT / 4),CrosshairPaint);
                ViewP.drawLine(POINT_X - (PORT_WIDTH / 2),POINT_Y + (PORT_HEIGHT / 4),POINT_X + (PORT_WIDTH / 2),POINT_Y + (PORT_HEIGHT / 4),CrosshairPaint);
            }

            c.drawBitmap(b,0,0,null);
        }

        private boolean InsideHandle(float x, float y,int radius, float TouchX, float TouchY){
            double center_x = x;
            double center_y = y;
            double DistanceX = TouchX - center_x;
            double DistanceY = TouchY - center_y;
            return (DistanceX * DistanceX) + (DistanceY * DistanceY) < (radius * radius);
        }
        private boolean InsideImage(float x,float y,float imgx,float imgy){

            return false;
        };

        private boolean InsideViewport(){
            Rect bounds = new Rect((int) Math.floor(POINT_X - (PORT_WIDTH/2)),(int) Math.floor(POINT_Y - (PORT_HEIGHT/2)),(int) Math.floor(POINT_X + (PORT_WIDTH/2)),(int) Math.floor(POINT_Y + (PORT_HEIGHT/2)));

            if(bounds.contains((int) POINT_X, (int) POINT_Y)){
                return true;
            }else{
                return false;
            }
        }

        private Bitmap AutoScale(Bitmap b){
            Matrix m = new Matrix();
            m.setScale(CURRENT_SCALE,CURRENT_SCALE);
            return Bitmap.createBitmap(b,0,0,b.getWidth(),b.getHeight(),m,false);
        }

        private Bitmap RotateImage(Bitmap b){
            Matrix m = new Matrix();
            m.setRotate(IMAGE_ROTATION);
            return Bitmap.createBitmap(b,0,0,b.getWidth(),b.getHeight(),m,false);
        }

        private Bitmap MirrorImage(Bitmap b){
            Matrix m = new Matrix();

            if(MIRRORED){
                m.setScale(-1,1);
                m.postTranslate(getWidth(), 0);
            }

            return Bitmap.createBitmap(b,0,0,b.getWidth(),b.getHeight(),m,false);
        }

        //Returns the paint object that will be used for drawing what the user wants to slice onto the canvas.
        private Paint SlicePaint(){
            Paint p = new Paint();

            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.FILL);
            p.setAntiAlias(true);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeCap(Paint.Cap.ROUND);

            return p;
        }
    }

    //Class that will be used to load the selected image from the
    //filesystem, this will usually be a tmp file.
    private class FileService{
        public Bitmap LoadFile(Uri source,Boolean FromMedia){
            InputStream SourceStream;
            Bitmap OutputFile;

            try {
                if(FromMedia){
                    SourceStream = getContentResolver().openInputStream(source);
                    OutputFile = BitmapFactory.decodeStream(SourceStream);
                }else{
                    SourceStream = new FileInputStream(new File(source.getPath()));
                    OutputFile = BitmapFactory.decodeStream(SourceStream);
                }

                return OutputFile;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Error loading file...",Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        //Saves the cropped file to a temporary directory.
        public String SaveFile(Bitmap b, String n){
            File fil = new File(getApplicationContext().getCacheDir(),n);

            try {
                fil.createNewFile();
                FileOutputStream fos = new FileOutputStream(fil);
                b.compress(Bitmap.CompressFormat.PNG,100,fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(),"Error saving temporary file...",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            return fil.getAbsolutePath();
        }
    }

    private class SlicePoint{
        private float x,y;

        public SlicePoint(float x, float y){
            this.x = x;
            this.y = y;
        }

        public float getX(){
            return this.x;
        }

        public float getY(){
            return this.y;
        }

        public void setX(float x){
            this.x = x;
        }

        public void setY(float y){
            this.y = y;
        }
    }

    //Class used to handle painting the cropping indicator on to the canvas.
    private class CropSlicer{
        private Paint SlicePaint;
        private Path p;
        private SliceCanvas c;
        private boolean SelectionFinished = false;
        private SlicePoint startPnt;
        private ArrayList<SlicePoint> points;

        public CropSlicer(SliceCanvas c){
            this.SlicePaint = new Paint();
            this.p = new Path();
            this.c = c;
            this.points = new ArrayList<SlicePoint>();

            Bitmap stripe = BitmapFactory.decodeResource(getResources(),R.drawable.stripe);
            BitmapShader mshade = new BitmapShader(stripe, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

            this.SlicePaint.setShader(mshade);
            this.SlicePaint.setStrokeCap(Paint.Cap.ROUND);
            this.SlicePaint.setAntiAlias(true);
            this.SlicePaint.setStrokeWidth(10);
            this.SlicePaint.setAlpha(200);
            this.SlicePaint.setStrokeJoin(Paint.Join.ROUND);
            this.SlicePaint.setStyle(Paint.Style.STROKE);
        }

        public void StartShape (SlicePoint p){
            this.startPnt = p;
            this.p.moveTo(p.getX(),p.getY());
            this.points.add(p);
        }

        public void AddPoint(SlicePoint p){
            this.p.lineTo(p.getX(),p.getY());
            this.points.add(p);
        }

        public void EndShape (SlicePoint p){
            this.p.moveTo(p.getX(),p.getY());
            this.p.lineTo(this.startPnt.getX(),this.startPnt.getY());
            this.points.add(p);
        }

        public void ResetPath(){
            this.p.reset();
        }

        public void DrawPath(Canvas c){
            c.drawPath(this.p,this.SlicePaint);
        }

        //CROP THE IMAGE OUT OF THE FREEFORM
        public Bitmap CropOut(){
            Bitmap hole = Bitmap.createBitmap(SliceCan.getWidth(),SliceCan.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas portHole = new Canvas(hole);

            //PAINT FOR THE PATH
            Paint PathPaint = new Paint();
            PathPaint.setColor(Color.BLACK);
            PathPaint.setStyle(Paint.Style.FILL);
            portHole.drawPath(this.p,PathPaint);

            this.p.reset();

            //PAINT FOR THE BITMAP
            Paint HolePaint = new Paint();
            HolePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            portHole.drawBitmap(SliceCan.getDrawingCache(),0,0,HolePaint);

            //HERE WE ARE GOING TO WANT TO DETERMINE THE RESOLUTION OF THE FREEFORM CROP AND
            //RETURN THAT INSTEAD THE ENTIRE CANVAS IMAGE RESOLUTION.
            float HighestY = this.points.get(0).getY(),LowestY = this.points.get(0).getY(),HighestX = this.points.get(0).getX(),LowestX = this.points.get(0).getX();

            for(SlicePoint p : this.points) {
                if(p.getY() > HighestY){
                    HighestY = p.getY();
                }

                if(p.getY() < LowestY){
                    LowestY = p.getY();
                }

                if(p.getX() > HighestX){
                    HighestX = p.getX();
                }

                if(p.getX() < LowestX) {
                    LowestX = p.getX();
                }
            }

            //CROP THE NEW IMAGE HERE.
            Bitmap finalHole = Bitmap.createBitmap(hole,(int) Math.floor(LowestX),(int) Math.floor(LowestY),(int) Math.floor(HighestX - LowestX),(int) Math.floor(HighestY - LowestY),null,false);

            return finalHole;
        }

        //STAMPS THE FREEFORM SELECTION INTO A DARK OVERLAY TO DISPLAY WHAT WOULD BE CROPPED.
        public void DrawPreviewWindow(Canvas c){
            Bitmap hole = Bitmap.createBitmap(c.getWidth(),c.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas portHole = new Canvas(hole);

            //PAINT OBJECT FOR DRAWING THE BACKGROUND SHADE.
            Paint pan = new Paint();
            pan.setStyle(Paint.Style.FILL);
            pan.setColor(Color.BLACK);
            pan.setAlpha(160);

            //PAINT USED FOR PUNCHING A HOLE INTO THE BACKGROUND SHADE.
            Paint stamp = new Paint();
            stamp.setStyle(Paint.Style.FILL);
            stamp.setColor(Color.TRANSPARENT);
            stamp.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

            portHole.drawRect(0,0,c.getWidth(),c.getHeight(),pan);
            portHole.drawPath(this.p,stamp);


            c.drawBitmap(hole,0,0,null);
        }
    }
}
