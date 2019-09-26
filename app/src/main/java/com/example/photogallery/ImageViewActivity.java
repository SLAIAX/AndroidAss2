package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

public class ImageViewActivity extends AppCompatActivity {

    private String path;
    private ImageView pic;
    private int orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        path = intent.getStringExtra("ImagePath");
        setContentView(R.layout.activity_image_view);
        orientation = intent.getIntExtra("ImageOrientation", 0);
        System.out.println("The orientation of this image is originally " + orientation);
        Matrix matrix = new Matrix();
        switch(orientation){
            case 90:
                matrix.postRotate(90);
                break;
            case 270:
                matrix.postRotate(270);
                break;
        }

        try {
            Bitmap bmp;
            File image = new File(path);
            bmp = BitmapFactory.decodeFile(image.getAbsolutePath());
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            pic = findViewById(R.id.imageLarge);
            pic.setImageBitmap(bmp);
        } catch (Exception e){
            System.out.println("ERROR "+e.getMessage());
        }


    }
}
