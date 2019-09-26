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
import android.widget.ImageView;
import android.provider.MediaStore;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Images";

    private GridView images;                        //< Gridview
    private ImageAdapter adapter;                   //< Adapter
    private Cursor imageCursor;                     //< Cursor to access all images
    private int imageCount;                         //< Number of images found
    private int position;                           //< Position in Gridview, used for opening and closing app
    private LruCache<String, Bitmap> memoryCache;   //< Cache storage
    private ThreadPoolExecutor executor;            //< ThreadPool for multithreaded version. CURRENTLY NOT USED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets the layout and gets GridView reference
        setContentView(R.layout.activity_main);
        images=findViewById(R.id.gridview);

        //Requests permission to read external storage
        if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        } else {
            init();
        }
    }

    /*
     * Adds a key, image pair to the cache
     */
    public void addImageToCache(String key, Bitmap imageBitmap) {
        if (getImageFromCache(key) == null) {
            memoryCache.put(key, imageBitmap);
        }
    }

    /*
     * Retrieves an image from the cache given a key
     */
    public Bitmap getImageFromCache(String key) {
        return memoryCache.get(key);
    }

    /*
     * If permission is requested, determines whether it was granted or not.
     * If granted, now call init
     * If not granted, close the app
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            init();
        } else{
            finish();
        }
    }
    /*
     * Init function initializes:
     *                  image adapter
     *                  onClickListener
     *                  Cursor
     *                  Cache
     */
    public void init(){
        adapter=new ImageAdapter();
        images.setAdapter(adapter);
        //onClickListener for each item. Calls function to open new activity
        images.setOnItemClickListener((parent, view, position, id) -> openImageViewActivity(position));

        ContentResolver cr = getContentResolver();
        imageCursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,MediaStore.Images.Media.DATE_ADDED + " DESC");
        imageCursor.moveToFirst();
        imageCount = imageCursor.getCount();

        //Retrieves amount of memory available to device
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //Specifies memory used by this application. Temporarily all.
        final int cacheSize = maxMemory;

        //Initializes cache to specified size
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
        //Initializes multiple threads. NOT CURRENTLY USED
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
    }

    //Populates gridview
    public class ImageAdapter extends BaseAdapter {
        class ViewHolder {
            int position;
            ImageView image;
        }
        @Override
        public int getCount() {
            return imageCount;
        }
        //Not used
        @Override
        public Object getItem(int i) {
            return null;
        }
        //Not used
        @Override
        public long getItemId(int i) {
            return i;
        }

        //Populates a view
        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {
            ViewHolder vh;
            if (convertView == null) {
                //If not recycled, inflate from xml
                convertView = getLayoutInflater().inflate(R.layout.image,  viewGroup, false);
                //Create Viewholder for it
                vh=new ViewHolder();
                vh.image=convertView.findViewById(R.id.galleryImage);
                convertView.setTag(vh);
            } else
                vh=(ViewHolder)convertView.getTag();        //Otherwise get the viewHolder
            //Specifies tile size
            convertView.setLayoutParams(new GridView.LayoutParams(images.getColumnWidth(),images.getColumnWidth()));
            //Sets position
            vh.position = i;
            //Erases old image
            vh.image.setImageBitmap(null);
            //Sets key to just be it's position
            final String imageKey = String.valueOf(i);
            //Tries to initally load image from the cache
            final Bitmap bitmap = getImageFromCache(imageKey);
            if (bitmap != null) {
                //if successful
                vh.image.setImageBitmap(bitmap);
            } else {
                //Make an AsyncTask to load the image
                new AsyncTask<ViewHolder, Void, Bitmap>() {
                    private ViewHolder vh;
                    @Override
                    protected Bitmap doInBackground(ViewHolder... params) {
                        vh = params[0];
                        imageCursor.moveToPosition(i);
                        Bitmap bmp = null;
                        try {
                            //Create file path
                            String path = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                            File image = new File(path);
                            //Adds sampling options to scale down
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            bmOptions.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                            int scaleFactor = 4;

                            bmOptions.inJustDecodeBounds = false;
                            bmOptions.inSampleSize = scaleFactor;
                            bmOptions.inPurgeable = true;
                            //Create Thumbnail
                            bmp = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions),images.getColumnWidth(),images.getColumnWidth());
                        } catch (Exception e) {
                            Log.i(TAG, e.getMessage());
                        }

                        //Get rotation and adjust such that it's zero degrees
                        int orientation = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
                        Matrix matrix = new Matrix();
                        switch(orientation){
                            case(90):
                                matrix.postRotate(90);
                                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                                break;
                            case(270):
                                matrix.postRotate(270);
                                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                                break;
                        }
                        return bmp;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bmp) {
                        //Only set the imageview if the position hasn't changed.
                        if (vh.position == i) {
                            vh.image.setImageBitmap(bmp);
                        }
                        //Add image to cache for later retrieval
                        addImageToCache(Integer.toString(i),bmp);
                    }
                }.execute(vh);//executeOnExecutor(executor,vh);
            }
            return convertView;
        }
    }

    /*
     * Finds and adds both orientation to the intent before opening the new activity
     */
    public void openImageViewActivity(int position){
        imageCursor.moveToPosition(position);
        int orientation = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
        String path = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
        Intent intent = new Intent(this, ImageViewActivity.class);
        intent.putExtra("ImagePath", path);
        intent.putExtra("ImageOrientation", orientation);
        startActivity(intent);
    }


    @Override
    public void onPause() {
        super.onPause();
        //Save the list position
        position=images.getFirstVisiblePosition();
        //Close the cursor
        imageCursor.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Clear the cache in-case new images
        memoryCache.evictAll();
        //Re-init in case things have changed
        init();
        //Set the list position
        images.setSelection(position);
    }
}
