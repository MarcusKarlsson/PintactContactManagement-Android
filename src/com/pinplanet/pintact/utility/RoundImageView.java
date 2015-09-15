package com.pinplanet.pintact.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Created by Dennis on 17.11.2014.
 */
public class RoundImageView extends CustomNetworkImageView {
    private static final String TAG = RoundImageView.class.getName();

    public RoundImageView(Context context) {
        super(context);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    private Bitmap buffer;


    /**
     if (mDrawable == null) {
     return; // couldn't resolve the URI
     }

     if (mDrawableWidth == 0 || mDrawableHeight == 0) {
     return;     // nothing to draw (empty bounds)
     }

     if (mDrawMatrix == null && mPaddingTop == 0 && mPaddingLeft == 0) {
     mDrawable.draw(canvas);
     } else {
     int saveCount = canvas.getSaveCount();
     canvas.save();

     if (mCropToPadding) {
     final int scrollX = mScrollX;
     final int scrollY = mScrollY;
     canvas.clipRect(scrollX + mPaddingLeft, scrollY + mPaddingTop,
     scrollX + mRight - mLeft - mPaddingRight,
     scrollY + mBottom - mTop - mPaddingBottom);
     }

     canvas.translate(mPaddingLeft, mPaddingTop);

     if (mDrawMatrix != null) {
     canvas.concat(mDrawMatrix);
     }
     mDrawable.draw(canvas);
     canvas.restoreToCount(saveCount);
     }
     * */

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);

        final Drawable mDrawable = getDrawable();

        if (mDrawable == null) {
            return; // couldn't resolve the URI
        }

        final int mDrawableWidth = mDrawable.getIntrinsicWidth();
        final int mDrawableHeight = mDrawable.getIntrinsicHeight();

        if (mDrawableWidth == 0 || mDrawableHeight == 0) {
            return;     // nothing to draw (empty bounds)
        }

        final Matrix mDrawMatrix = getImageMatrix();
        final int mPaddingTop = getPaddingTop();
        final int mPaddingLeft = getPaddingLeft();
        final int mPaddingRight= getPaddingRight();
        final int mPaddingBottom = getPaddingBottom();

        final int mRight=0,mLeft=0,mBottom=0,mTop=0;

        final boolean mCropToPadding = getCropToPadding();

        if (mDrawMatrix == null && mPaddingTop == 0 && mPaddingLeft == 0) {
            mDrawable.draw(canvas);
        } else {
            int saveCount = canvas.getSaveCount();
            canvas.save();

            if (mCropToPadding) {
                final int scrollX = getScrollX();
                final int scrollY = getScrollY();
                canvas.clipRect(scrollX + mPaddingLeft, scrollY + mPaddingTop,
                        scrollX + mRight - mLeft - mPaddingRight,
                        scrollY + mBottom - mTop - mPaddingBottom);
            }

            canvas.translate(mPaddingLeft, mPaddingTop);

            if (mDrawMatrix != null) {
                canvas.concat(mDrawMatrix);
            }
            mDrawable.draw(canvas);
            canvas.restoreToCount(saveCount);
        }

/*

        Log.d(TAG,"Drawable exits:"+(d!=null));
        if (d != null && d instanceof BitmapDrawable)
        {
            Log.d(TAG,"instanceof BitmapDrawable:true");
            BitmapDrawable bd=(BitmapDrawable) d;
            if(buffer==null && bd.getBitmap()!=null)
            {
                buffer=bd.getBitmap().copy(Bitmap.Config.ARGB_8888,true);


            }

            if(buffer!=null)
            {
               // Paint p=new Paint();
                //final Canvas c = new Canvas (buffer);
                //c.drawColor(0, PorterDuff.Mode.CLEAR); // this makes your whole Canvas transparent
                canvas.drawColor(0, PorterDuff.Mode.CLEAR); // this makes your whole Canvas transparent
                //canvas.drawColor(Color.WHITE);  // this makes it all white on another canvas
                canvas.drawBitmap (buffer, 0,  0,null); // this draws your bitmap on another canvas
            }
        }
        else
        {
            Log.d(TAG,"instanceof BitmapDrawable:false");
            super.onDraw(canvas);
        }

*/

    }
}
