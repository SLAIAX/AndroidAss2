package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;

public class ImageViewActivity extends AppCompatActivity {

    private String path;
    private Cursor imageCursor;
    private ImageView pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        path = intent.getStringExtra("ImagePath");
        setContentView(R.layout.activity_image_view);
//        ContentResolver cr = getContentResolver();
//        imageCursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,MediaStore.Images.Media.DATE_ADDED + " ASC");
//        imageCursor.moveToPosition(position);

        try {
            Bitmap bmp;
            File image = new File(path);
            bmp = BitmapFactory.decodeFile(image.getAbsolutePath());
            //bmp = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(image.getAbsolutePath()),200,200);
            pic = findViewById(R.id.imageLarge);
            pic.setImageBitmap(bmp);
        } catch (Exception e){
            System.out.println("ERROR "+e.getMessage());
        }


    }
}
