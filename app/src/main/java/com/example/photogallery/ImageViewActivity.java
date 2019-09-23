package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;

public class ImageViewActivity extends AppCompatActivity {

    private int position;
    private Cursor imageCursor;
    private ImageView pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        position = intent.getIntExtra("ImagePosition", 0);
        System.out.println("Position: "+position);
        ContentResolver cr = getContentResolver();
        imageCursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,MediaStore.Images.Media.DATE_ADDED);
        imageCursor.moveToPosition(position);


        Bitmap bmp = null;
        String path = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
        File image = new File(path);
        bmp = BitmapFactory.decodeFile(image.getAbsolutePath());

        pic = findViewById(R.id.imageLarge);
        pic.setImageBitmap(bmp);



    }
}
