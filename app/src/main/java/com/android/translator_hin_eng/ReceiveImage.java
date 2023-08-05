package com.android.translator_hin_eng;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;

public class ReceiveImage extends AppCompatActivity {
    ImageView imgView;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_image);
        imgView=findViewById(R.id.image);
        text=findViewById(R.id.text);


        Intent intent=getIntent();
        if (intent!=null){
            String action=intent.getAction();
            String type =intent.getType();
            if (Intent.ACTION_SEND.equals(action)&&type!=null){
                 if (type.startsWith("image/")){
                    handleImage(intent);
                }
            }
        }
    }



    private void handleImage(Intent intent) {
        Uri image=intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (image!=null){
/*            Toast.makeText(this, image.getPath(), Toast.LENGTH_SHORT).show();*/
            imgView.setImageURI(image);
            text.setVisibility(View.GONE);

        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
}