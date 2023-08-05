package com.android.translator_hin_eng;

import static android.media.MediaRecorder.VideoSource.CAMERA;
import static androidx.core.app.ActivityCompat.startActivityForResult;
import static com.google.android.material.internal.ContextUtils.getActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Provider;
import java.util.Calendar;
import java.util.Objects;

public class Widget extends Service{
    int LAYOUT_FLAG;
    View mFloatingView;
    WindowManager windowManager;
    ImageView imageClose;
    ImageView tvWidget;
    float height,width;
    ClipboardManager clipboard;

    ClipData clip;
    MDToast mdToast;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint({"ClickableViewAccessibility"})
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            LAYOUT_FLAG= WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            LAYOUT_FLAG= WindowManager.LayoutParams.TYPE_PHONE;
        }


        //inflate widget layout

        mFloatingView= LayoutInflater.from(this).inflate(R.layout.layout_widget,null);
        final WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        layoutParams.gravity = Gravity.TOP|Gravity.RIGHT;
        layoutParams.x = 0;
        layoutParams.y = 100;


        //layout params for close button
        final WindowManager.LayoutParams imageParams = new WindowManager.LayoutParams(140,
                140,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat. TRANSLUCENT);
        imageParams.gravity=Gravity.BOTTOM|Gravity.CENTER;
        imageParams.y=100;


        windowManager = (WindowManager) getSystemService (WINDOW_SERVICE);
        imageClose = new ImageView( this);
        imageClose.setImageResource (R.drawable.close_white);
        imageClose.setVisibility (View. INVISIBLE);
        windowManager.addView(imageClose, imageParams);

        windowManager.addView (mFloatingView, layoutParams);
        mFloatingView.setVisibility(View.VISIBLE);

        height= windowManager.getDefaultDisplay().getHeight();
        width = windowManager.getDefaultDisplay ().getWidth();
        tvWidget = mFloatingView.findViewById (R.id.text_widget);


        tvWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = Bitmap.createBitmap((int)width,
                        (int)height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);
                TextRecognizer textRecognizer = new TextRecognizer.Builder(Widget.this).build();
                Frame frameImage = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> textBlockSparseArray = textRecognizer.detect(frameImage);
                String stringImageText = "";
                for (int i = 0; i < textBlockSparseArray.size(); i++) {
                    TextBlock textBlock = textBlockSparseArray.get(textBlockSparseArray.keyAt(i));
                    stringImageText = stringImageText + " " + textBlock.getValue();
                }
                Toast.makeText(Widget.this, stringImageText, Toast.LENGTH_SHORT).show();
            }
        });

        //drag momments

        tvWidget.setOnTouchListener(new View.OnTouchListener() {

            int initialx, initialy;
            float initialTouchx, initialTouchy;
            long startClickTime;
            int MAX_CLICK_DURATION=200;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        startClickTime=Calendar.getInstance().getTimeInMillis();

                        imageClose.setVisibility(View.VISIBLE);
                        initialx = layoutParams.x;
                        initialy = layoutParams.y;

                        //touch positions
                        initialTouchx = event.getRawX();
                        initialTouchy = event.getRawY();

                        return true;


                    case MotionEvent.ACTION_UP:
                        imageClose.setVisibility(View.GONE);





                        return true;
                    case MotionEvent.ACTION_MOVE:

                        //calculate x and y co_ordinates of view

                        layoutParams.x = initialx+ (int) (initialTouchx-event.getRawX());
                        layoutParams.y = initialy +(int) (event.getRawY()-initialTouchy);

                        //update layout co_ordinates
                        windowManager.updateViewLayout(mFloatingView,layoutParams);

                        if (layoutParams.y>(height*0.8)){
                            imageClose.setImageResource(R.drawable.close);
                            if (mFloatingView != null) windowManager.removeView(mFloatingView);
                            imageClose.setVisibility(View.GONE);
                        }else {
                            imageClose.setImageResource(R.drawable.close_white);
                        }

                        return true;

                }




                return false;
            }
        });
        return START_STICKY;
    }



    public void screenShot(View view) throws IOException {
        try {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                    view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();
            Frame frameImage = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> textBlockSparseArray = textRecognizer.detect(frameImage);
            String stringImageText = "";
            for (int i = 0; i < textBlockSparseArray.size(); i++) {
                TextBlock textBlock = textBlockSparseArray.get(textBlockSparseArray.keyAt(i));
                stringImageText = stringImageText + " " + textBlock.getValue();
            }
        } catch (Exception e) {

        }

        /* textView.setText("click");*/
    }


    void copy(String text) {
        if (!text.equals("")) {
            clip = ClipData.newPlainText("text", text);
            clipboard.setPrimaryClip(clip);
            toast("Text Copied",1);
        } else {
            mdToast = MDToast.makeText(getApplicationContext(), "There is no text", MDToast.LENGTH_SHORT, MDToast.TYPE_WARNING);
        }
        mdToast.show();
    }

    void toast(String message,int type){
        mdToast = MDToast.makeText(getApplicationContext(), message, MDToast.LENGTH_SHORT, type);
        mdToast.show();
    }


    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();
    }
}
