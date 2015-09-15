package com.pinplanet.pintact.utility;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;



/**
 * @author Dennis
 * 
 * this TextView uses custom fonts instead of the default font
 * */
public class TextViewTypeFace extends TextView {
	
	private static final String TAG=TextViewTypeFace.class.getName();

	public TextViewTypeFace(Context context) {
		super(context);
	}


	public TextViewTypeFace(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFont(context, attrs);
	}

	public TextViewTypeFace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFont(context, attrs);
	}

	private void setFont(Context context, AttributeSet attrs) {
        if(isInEditMode())return;
		try {
            TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.TextTypeFace);
            final int fontStyle = a.getInt(R.styleable.TextTypeFace_FontStyles, -1);

			switch(fontStyle)
			{
				case 6://bold|italic 
					setTypeface(AppController.getInstance().getTypeFaceItalicBold());
				break;
			
				case 5://light|italic 
					setTypeface(AppController.getInstance().getTypeFaceLightItalic());
				break;
			
				case 4://normal|italic == italic
					setTypeface(AppController.getInstance().getTypeFaceItalic());
				break;
				
				case 2://normal|bold == bold
					setTypeface(AppController.getInstance().getTypeFaceBold());
				break;
				
				case 1://light
					setTypeface(AppController.getInstance().getTypeFaceLight());
				break;	
				
				case 0://normal
					setTypeface(AppController.getInstance().getTypeFaceNormal());
				break;
				
				default://we don't set another font in this case
				break;	
			}

		} catch (Exception e) {
			Log.d(TAG, "setFont failed",e);
		}
		 
    }


}
