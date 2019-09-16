package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.provider.MediaStore;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Images";
    // Number of tiles
    // this must be devisible by 8 for the initialization code to work.
    private static final int NTILES=32;         //Change
    // Number of columns in the gridview
    private static final int NCOLS=4;

    GridView images;
    ImageAdapter adapter;
    ImageButton reload;
    Cursor imageCursor;


    // list of camera URLs, anything that returns a jpeg will work here.
    String[] urls = {
            "https://www.surf2surf.com/reports/freecams/MW",
            "https://www.surf2surf.com/reports/freecams/PH",
            "https://www.surf2surf.com/reports/freecams/NP",
            "https://www.surf2surf.com/reports/freecams/TA",
            "https://www.surf2surf.com/reports/freecams/MB",
            "https://www.surf2surf.com/reports/freecams/WG",
            "https://www.surf2surf.com/reports/freecams/MM",
            "https://www.surf2surf.com/reports/freecams/MC",
            "https://www.surf2surf.com/reports/freecams/GS",
            "https://www.surf2surf.com/reports/freecams/WA",
            "https://www.surf2surf.com/reports/freecams/DN",
            "http://www.takapunabeach.com/netcam.jpg",
            "http://www.windsurf.co.nz/webcams/orewa.jpg",
            "https://www.surf2surf.com/reports/freecams/MW",
            "https://www.surf2surf.com/reports/freecams/PH",
            "https://www.surf2surf.com/reports/freecams/NP",
            "https://www.surf2surf.com/reports/freecams/TA",
            "https://www.surf2surf.com/reports/freecams/MB",
            "https://www.surf2surf.com/reports/freecams/WG",
            "https://www.surf2surf.com/reports/freecams/MM",
            "https://www.surf2surf.com/reports/freecams/MC",
            "https://www.surf2surf.com/reports/freecams/GS",
            "https://www.surf2surf.com/reports/freecams/WA",
            "https://www.surf2surf.com/reports/freecams/DN",
            "http://www.takapunabeach.com/netcam.jpg",
            "http://www.windsurf.co.nz/webcams/orewa.jpg",
            "https://www.surf2surf.com/reports/freecams/MW",
            "https://www.surf2surf.com/reports/freecams/PH",
            "https://www.surf2surf.com/reports/freecams/NP",
            "https://www.surf2surf.com/reports/freecams/TA",
            "https://www.surf2surf.com/reports/freecams/MB",
            "https://www.surf2surf.com/reports/freecams/WG",
            "https://www.surf2surf.com/reports/freecams/MM",
            "https://www.surf2surf.com/reports/freecams/MC",
            "https://www.surf2surf.com/reports/freecams/GS",
            "https://www.surf2surf.com/reports/freecams/WA",
            "https://www.surf2surf.com/reports/freecams/DN",
            "http://www.takapunabeach.com/netcam.jpg",
            "http://www.windsurf.co.nz/webcams/orewa.jpg",
            "http://www.windsurf.co.nz/webcams/orewa2.jpg",
            "https://www.surf2surf.com/reports/freecams/MW",
            "https://www.surf2surf.com/reports/freecams/PH",
            "https://www.surf2surf.com/reports/freecams/NP",
            "https://www.surf2surf.com/reports/freecams/TA",
            "https://www.surf2surf.com/reports/freecams/MB",
            "https://www.surf2surf.com/reports/freecams/WG",
            "https://www.surf2surf.com/reports/freecams/MM",
            "https://www.surf2surf.com/reports/freecams/MC",
            "https://www.surf2surf.com/reports/freecams/GS",
            "https://www.surf2surf.com/reports/freecams/WA",
            "https://www.surf2surf.com/reports/freecams/DN",
            "http://www.takapunabeach.com/netcam.jpg",
            "http://www.windsurf.co.nz/webcams/orewa.jpg",
            "https://www.surf2surf.com/reports/freecams/MW",
            "https://www.surf2surf.com/reports/freecams/PH",
            "https://www.surf2surf.com/reports/freecams/NP",
            "https://www.surf2surf.com/reports/freecams/TA",
            "https://www.surf2surf.com/reports/freecams/MB",
            "https://www.surf2surf.com/reports/freecams/WG",
            "https://www.surf2surf.com/reports/freecams/MM",
            "https://www.surf2surf.com/reports/freecams/MC",
            "https://www.surf2surf.com/reports/freecams/GS",
            "https://www.surf2surf.com/reports/freecams/WA",
            "https://www.surf2surf.com/reports/freecams/DN",
            "http://www.takapunabeach.com/netcam.jpg",
            "http://www.windsurf.co.nz/webcams/orewa.jpg",
            "https://www.surf2surf.com/reports/freecams/MW",
            "https://www.surf2surf.com/reports/freecams/PH",
            "https://www.surf2surf.com/reports/freecams/NP",
            "https://www.surf2surf.com/reports/freecams/TA",
            "https://www.surf2surf.com/reports/freecams/MB",
            "https://www.surf2surf.com/reports/freecams/WG",
            "https://www.surf2surf.com/reports/freecams/MM",
            "https://www.surf2surf.com/reports/freecams/MC",
            "https://www.surf2surf.com/reports/freecams/GS",
            "https://www.surf2surf.com/reports/freecams/WA",
            "https://www.surf2surf.com/reports/freecams/DN",
            "http://www.takapunabeach.com/netcam.jpg",
            "http://www.windsurf.co.nz/webcams/orewa.jpg",
            "http://www.windsurf.co.nz/webcams/orewa2.jpg"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        images=findViewById(R.id.gridview);
        adapter=new ImageAdapter();
        images.setAdapter(adapter);
        reload=findViewById(R.id.button);
        reload.setOnClickListener(view -> adapter.notifyDataSetChanged());



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
            return NTILES;
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
            convertView.setMinimumHeight(200);              //adjust
            //Set position
            vh.position = i;
            //Erase old image
            vh.image.setImageBitmap(null);



            // make an AsyncTask to load the image
            new AsyncTask<ViewHolder,Void, Bitmap>() {
                private ViewHolder vh;
                @Override
                protected Bitmap doInBackground(ViewHolder... params) {
                    vh=params[0];


                    // get the string for the url
                    String address=urls[vh.position%urls.length];
                    Bitmap bmp=null;
                    try {
                        Log.i(TAG,"Loading:"+address);
                        URL url = new URL(address);
                        // open network connection
                        URLConnection connection=url.openConnection();
                        // vh position might have changed
                        if(vh.position!=i)
                             return null;
                        // decode the jpeg into a bitmap
                        bmp = BitmapFactory.decodeStream(connection.getInputStream());
                    } catch (Exception e) {
                        Log.i(TAG,"Error Loading:" + i + " " +address);
                        e.printStackTrace();
                    }
                    // return the bitmap (might be null)
                    return bmp;
                }
                @Override
                protected void onPostExecute(Bitmap bmp) {
                    // only set the imageview if the position hasn't changed.
                    if(vh.position==i) {
                        vh.image.setImageBitmap(bmp);
                    }
                }
            }.execute(vh);//executeOnExecutor(mExecutor,vh);

            //Set onClickListener

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openImageViewActivity();
                }
            });


            return convertView;
        }
    }

    public void openImageViewActivity(){
        Intent intent = new Intent(this, ImageViewActivity.class);
        startActivity(intent);
    }
}
