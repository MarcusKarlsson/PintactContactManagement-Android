package com.pinplanet.pintact.slideshow;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.pinplanet.pintact.R;


/**
 * A simple fragment class to display images
 * */
public class ActivitySlideShowPageFragment extends Fragment {

    	private static final String IMG_NR = "IMG_NR";
		private int imageNr=0;
    	
        /**
         * Factory method for this fragment class. Constructs a new fragment for the given page number.
         */
        public static ActivitySlideShowPageFragment create(int imageNr) {
            ActivitySlideShowPageFragment fragment = new ActivitySlideShowPageFragment();
            Bundle args = new Bundle();
            args.putInt(IMG_NR, imageNr);
            fragment.setArguments(args);
            return fragment;
        }    	
    	
    	
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            imageNr = getArguments().getInt(IMG_NR);
        }
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate( R.layout.activity_slideshow_screen_fragment, container, false);
            final ImageView img = (ImageView) rootView.findViewById(R.id.imageViewTourContent);
            
            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            Bitmap bitmap = decodeSampledBitmapFromResource(getResources(), imageNr, size.x, size.y);

            img.setImageBitmap(bitmap);
            
            return rootView;
        }


        public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;
        
            if (height > reqHeight || width > reqWidth) {
        
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
        
                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }
        
            return inSampleSize;
        }
        
        public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
            int reqWidth, int reqHeight) {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(res, resId, options);
    
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
    
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(res, resId, options);
        }
    
}
