package com.pinplanet.pintact.chat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.pinplanet.pintact.R;

public class ChatImageFullScreenActivity extends Activity {
    static final String TAG = "Debugging";

    Intent callingIntent;
    String fileData;
    ImageView fullImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_image_full_screen);

        fullImageView = (ImageView) findViewById(R.id.fullImageView);

        callingIntent = getIntent();
        fileData = callingIntent.getStringExtra("FileData");

        if (fileData != null) {
            Runtime.getRuntime().maxMemory();
            byte[] decodedString = Base64.decode(fileData, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            int height = decodedByte.getHeight();
            int width = decodedByte.getWidth();
//                double ratio;
//                //Scale images
//                int maxHeight = 1200;
//                int maxWidth = 800;
//                if (height > maxHeight) {
//                    ratio = (double)maxHeight / (double)height;
//                    Log.d(TAG, "Ratio: " + Double.toString(ratio));
//                    height = maxHeight;
//                    width = (int)(width * ratio);
//                }
//                if (width > maxWidth) {
//                    ratio = (double)maxWidth / (double)width;
//                    Log.d(TAG, "Ratio: " + Double.toString(ratio));
//                    width = maxWidth;
//                    height = (int)(height * ratio);
//                }
            Log.d(TAG, "Height: " + Integer.toString(height) + " Width: " + Integer.toString(width));
            fullImageView.setImageBitmap(Bitmap.createScaledBitmap(decodedByte, width, height, false));
        }
    }
}
