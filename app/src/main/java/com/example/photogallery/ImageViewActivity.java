package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

public class ImageViewActivity extends AppCompatActivity {

    private String path;
    private ImageView pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        path = intent.getStringExtra("ImagePath");
        setContentView(R.layout.activity_image_view);

        try {
            Bitmap bmp;
            File image = new File(path);
            bmp = BitmapFactory.decodeFile(image.getAbsolutePath());
            pic = findViewById(R.id.imageLarge);
            pic.setImageBitmap(bmp);
        } catch (Exception e){
            System.out.println("ERROR "+e.getMessage());
        }


    }
}
