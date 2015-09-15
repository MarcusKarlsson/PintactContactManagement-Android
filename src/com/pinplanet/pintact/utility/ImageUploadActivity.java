package com.pinplanet.pintact.utility;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.data.ImageDTO;
import com.pinplanet.pintact.R;

import java.io.FileOutputStream;

public abstract class ImageUploadActivity extends MyActivity {

    private static final int PICK_IMAGE = 1;
    private static final int PICK_Camera_IMAGE = 2;
    private static final int CROP_IMAGE = 3;
    protected String imagePath1;
    protected Uri imageUri;
    protected boolean isUploadingImage = false;
    protected ImageDTO imInfo;
    protected CustomNetworkImageView imageView;
    private View changeButton;
    private View removeButton;
    private TextView imageTextView, imageNextTextView;
    public String initialImagePath;
    private LinearLayout imageButtonsHolder;

    public void init() {
        imageView = (CustomNetworkImageView) findViewById(R.id.pcn_image);
        changeButton = findViewById(R.id.pcn_image_change);
        removeButton = findViewById(R.id.pcn_image_remove);
        imageTextView = (TextView) findViewById(R.id.image_text);
        imageButtonsHolder = (LinearLayout) findViewById(R.id.imageButtonsHolder);
        imageNextTextView = (TextView) findViewById(R.id.imageNextTextView);
        isUploadingImage = false;
    }

    public void initImagePath(String imagePath) {
        this.initialImagePath = imagePath;
        if (initialImagePath != null && initialImagePath.length() > 0) {
            ImageLoader imageLoader = AppController.getInstance().getImageLoader();
            imageView.setImageUrl(imagePath, imageLoader);
            imageView.setDefaultImageResId(R.drawable.add_an_image);
            initImageTextAndButtons(true);
            imInfo = new ImageDTO();
            imInfo.thumbnailPath = initialImagePath;
        } else {
            imageView.setImageResource(R.drawable.add_an_image);
            imageView.setDefaultImageResId(R.drawable.add_an_image);
            initImageTextAndButtons(false);
        }
    }

    public void initImageTextAndButtons(boolean imageExists) {
        if (imageExists) {
            if (changeButton != null) {
                changeButton.setVisibility(View.VISIBLE);
            }
            if (removeButton != null) {
                removeButton.setVisibility(View.VISIBLE);
            }
            if (imageTextView != null) {
                imageTextView.setVisibility(View.GONE);
            }
        } else {
            if (changeButton != null) {
                changeButton.setVisibility(View.GONE);
            }
            if (removeButton != null) {
                removeButton.setVisibility(View.GONE);
            }
            if (imageTextView != null) {
                imageTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void createAttachDialog(View v) {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                handleAttachCameraAction();
                                break;
                            case 1:
                                handleGallery();
                                break;
                            case 2:
                                dialog.dismiss();
                                break;
                            default:
                        }
                    }
                }).show();

    }

    private void handleAttachCameraAction() {
        String fileName = "new-photo-name.jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        try {

            intent.putExtra("return-data", true);
            startActivityForResult(intent, PICK_Camera_IMAGE);

        } catch (ActivityNotFoundException e) {

        }
    }

    private void handleGallery() {
        Intent gintent = new Intent();
        gintent.setType("image/*");
        gintent.setAction(Intent.ACTION_PICK);
        startActivityForResult(
                Intent.createChooser(gintent, "Select Picture"),
                PICK_IMAGE);
    }

    private static final String TEMP_PHOTO_FILE = "temporary_holder.png";

    public String saveBitmap2File(Bitmap bmp) {
        FileOutputStream out;
        String filename;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            filename = Environment.getExternalStorageDirectory() + "/" + TEMP_PHOTO_FILE;
        } else
            return null;

        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            return filename;
        } catch (Exception e) {
            e.printStackTrace();
            return filename;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == PICK_Camera_IMAGE || requestCode == PICK_IMAGE) && resultCode == RESULT_OK) {
            Uri uri;
            if (requestCode == PICK_Camera_IMAGE) {
                uri = imageUri;
            } else {
                uri = data.getData();
            }
            if (uri == null)
                return;

            try {
                /*the user's device may not support cropping*/
                String path = getPath(uri);
                if (path != null && path.length() > 1)
                    System.out.println("Getting image from : " + path);
                cropCapturedImage(uri);
            } catch (ActivityNotFoundException aNFE) {
                //display an error message if user device doesn't support
                String errorMessage = "Sorry - your device doesn't support the crop action!";
                Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        if (requestCode == CROP_IMAGE && resultCode == RESULT_OK && data != null) {

            //Create an instance of bundle and get the returned data
            Bundle extras = data.getExtras();
            //get the cropped bitmap from extras
            Bitmap bm = extras.getParcelable("data");

            //Bitmap bm = getBitmapFromUri(uri);
            if (bm != null) {
                imageView.setLocalImageBitmap(bm);
                //imageView.setImageBitmap(bm);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

            // WRITE Bitmap TO FILE
            imagePath1 = saveBitmap2File(bm);
            imageButtonsHolder.setVisibility(View.VISIBLE);
            imageNextTextView.setText("NEXT");

            System.out.println("file path : " + imagePath1);

            initImageTextAndButtons(true);
        }

    }

    public void removeSearchImage(View view) {
        imInfo = null;
        imageView.setImageResource(R.drawable.add_an_image);
        initImageTextAndButtons(false);
    }

    public boolean uploadImage() {
        Log.d(TAG, "in uploadImage");
        if (imagePath1 != null && (this.initialImagePath == null || !this.initialImagePath.equals(imagePath1))) {
            Log.d(TAG, "imagePath1: " + imagePath1);
            isUploadingImage = true;
            String path = "/api/image.json?" +
                    SingletonLoginData.getInstance().getPostParam();

            SingletonNetworkStatus.getInstance().setActivity(this);
            new HttpConnectionForImage().access(this, path, "", "POST", imagePath1);
            return true;
        }
        else {
            Log.d(TAG, "else uploadImage");
        }
        return false;
    }

    //create helping method cropCapturedImage(Uri picUri)
    public void cropCapturedImage(Uri picUri) {
        //call the standard crop action intent
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        //indicate image type and Uri of image
        cropIntent.setDataAndType(picUri, "image/*");
        //set crop properties
        cropIntent.putExtra("crop", "true");
        //indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        //indicate output X and Y
        cropIntent.putExtra("outputX", 256);
        cropIntent.putExtra("outputY", 256);
        //retrieve data on return
        cropIntent.putExtra("return-data", true);
        //start the activity - we handle returning in onActivityResult
        startActivityForResult(cropIntent, CROP_IMAGE);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    public void onPostNetwork() {
        if (isUploadingImage) {
            isUploadingImage = false;

            Gson gson = new GsonBuilder().create();
            Log.d(TAG, "imInfo json: " + SingletonNetworkStatus.getInstance().getJson());
            imInfo = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), ImageDTO.class);
        }

    }

}
