package com.example.photogallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.provider.MediaStore;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Images";

    private GridView images;
    private ImageAdapter adapter;
    private ImageButton reload;
    private Cursor imageCursor;
    private int imageCount;
    private int position;
    private LruCache<String, Bitmap> memoryCache;/////////////
    private ThreadPoolExecutor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        images=findViewById(R.id.gridview);
        adapter=new ImageAdapter();
        images.setAdapter(adapter);

        if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        } else {
            init();
        }
    }

    ////////////////////////////
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }



    /////////////////////////////

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            init();
        } else{
            finish();
        }
    }

    public void init(){

        ContentResolver cr = getContentResolver();
        imageCursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,MediaStore.Images.Media.DATE_ADDED + " DESC");
        imageCursor.moveToFirst();
        //adapter.notifyDataSetChanged();
        imageCount = imageCursor.getCount();



        ///////////////////////////////////////////////////
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;        //ADJUST. Was divided by 8

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
        /////////////////////////////////////////////////



        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
//        reload=findViewById(R.id.button);
//        reload.setOnClickListener(view -> adapter.notifyDataSetChanged());
    }

    // gets view data
    public class ImageAdapter extends BaseAdapter {
        class ViewHolder {
            int position;
            ImageView image;
        }
        // how many tiles
        @Override
        public int getCount() {
            return imageCount;
        }
        // not used
        @Override
        public Object getItem(int i) {
            return null;
        }
        // not used
        @Override
        public long getItemId(int i) {
            return i;
        }

        // populate a view
        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {
            ViewHolder vh;
//            //ImageView image;
            if (convertView == null) {
                // if it's not recycled, inflate it from xml
                convertView = getLayoutInflater().inflate(R.layout.image,  viewGroup, false);
//                // convertview will be a LinearLayout
                //Create Viewholder for it
                vh=new ViewHolder();
                vh.image=convertView.findViewById(R.id.galleryImage);
                // and set the tag to it
                convertView.setTag(vh);
            } else
                vh=(ViewHolder)convertView.getTag();        //otherwise get the viewHolder
            convertView.setMinimumHeight(images.getColumnWidth());

            convertView.setLayoutParams(new GridView.LayoutParams(images.getColumnWidth(),images.getColumnWidth()));              //adjust
            //Set position
            vh.position = i;
            //Erase old image
            vh.image.setImageBitmap(null);


            final String imageKey = String.valueOf(i);

            final Bitmap bitmap = getBitmapFromMemCache(imageKey);
            if (bitmap != null) {
                vh.image.setImageBitmap(bitmap);
            } else {
                // make an AsyncTask to load the image
                new AsyncTask<ViewHolder, Void, Bitmap>() {

                    private ViewHolder vh;

                    @Override
                    protected Bitmap doInBackground(ViewHolder... params) {

                        vh = params[0];

                        imageCursor.moveToPosition(i);
                        Bitmap bmp = null;
                        try {
                            String path = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
                            File image = new File(path);

                            bmp = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(image.getAbsolutePath()),200,200);
                        } catch (Exception e) {
                            Log.i(TAG, e.getMessage());
                        }
                        int orientation = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
                        if(orientation != 0){
                            System.out.println("ERROR "+orientation);
                            //Matrix matrix = new Matrix();
                            //matrix.setRotate(orientation - 90);
                            //bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

                        }

                        return bmp;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bmp) {
                        // only set the imageview if the position hasn't changed.
                        if (vh.position == i) {
                            vh.image.setImageBitmap(bmp);
                        }
                        addBitmapToMemoryCache(Integer.toString(i),bmp);
                    }
                }.executeOnExecutor(executor,vh);
            }
            //Set onClickListener
            convertView.setOnClickListener(v -> openImageViewActivity(vh));


            return convertView;
        }
    }

    public void openImageViewActivity(ImageAdapter.ViewHolder vh){
        imageCursor.moveToPosition(vh.position);
        String path = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
        Intent intent = new Intent(this, ImageViewActivity.class);
        intent.putExtra("ImagePath", path);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        // save the list position
        position=images.getFirstVisiblePosition();
        // close the cursor (will be opened again in init() during onResume())
        imageCursor.close();
    }
    @Override
    public void onResume() {
        super.onResume();
        // reinit in case things have changed
        init();
        //Clear the cache in-case new images
        memoryCache.evictAll();
        // set the list position
        images.setSelection(position);
    }
}
