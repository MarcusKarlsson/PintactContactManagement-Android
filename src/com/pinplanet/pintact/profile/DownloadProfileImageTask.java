package com.pinplanet.pintact.profile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.pinplanet.pintact.utility.SingletonLoginData;

import java.io.InputStream;

public class DownloadProfileImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    int position;

    public DownloadProfileImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        position = Integer.parseInt(urls[1]);
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            System.out.println("Error" + e.getMessage());
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        SingletonLoginData.getInstance().setBitmap(position, result);
        if(bmImage != null) {
            bmImage.setImageBitmap(result);
            bmImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
}
