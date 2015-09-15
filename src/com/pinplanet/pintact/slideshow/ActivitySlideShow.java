package com.pinplanet.pintact.slideshow;


import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.group.GroupPinActivity;
import com.pinplanet.pintact.login.LoginActivity;
import com.pinplanet.pintact.login.LoginRegisterActivity;
import com.pinplanet.pintact.login.LoginRegisterOptionsActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.TextViewTypeFace;

import de.late.utils.LogUtil;


public class ActivitySlideShow extends FragmentActivity
{
	private static final String TAG=ActivitySlideShow.class.getName();
    public static final String PARAM_ARRAY_ID="ARRAY_ID",
        PARAM_SHOW_BOTTOM_BUTTONS ="SHOW_BOTTOM_BUTTONS",
        PARAM_SHOW_TOP_BUTTONS ="SHOW_TOP_BUTTONS",
        PARAM_TOUR_TYPE = "TOUR_TYPE";

    //the points
    private ImageView[] pager_points;
    //layout contains the points
    private LinearLayout pagerPoints;
    //this holds the buttons we need it to dynamic enable/disable them
    private View buttonsView;

    public TourType getTourType() {
        return tourType;
    }

    private TourType tourType;


    public enum TourType {
      PINTACT,
      GROUP_PIN,
      SLIDE_SHOW_FIRST_START;
    }

    public void updatePagerPoints(int active, int size) {
        if(pager_points==null || pager_points.length != size) {
            pagerPoints.removeAllViews();
            pager_points = new ImageView[size];
            for(int i=0; i<size; i++) {
                ImageView v=new ImageView(this);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(5, 5, 5, 5); //substitute parameters for left, top, right, bottom
                v.setLayoutParams(lp);

                v.setImageResource(R.drawable.indicator_grey);

                pager_points[i] = v;
                pagerPoints.addView(pager_points[i]);
            }
        }
        setActivePagerPoint(active);
    }

    public void setActivePagerPoint(int active) {
        for(int i=0; i<pager_points.length; i++) {
            pager_points[i].setImageResource(i == active ? R.drawable.indicator_orange : R.drawable.indicator_grey);
        }
    }

	//we have 5 tour images...
	private int max_pages = 0;
    private int[] imageResArray;
    
    //The pager widget, which handles animation and allows swiping horizontally to access previous
    //and next pages.
    private ViewPager mPager;

    //The pager adapter, which provides the pages to the view pager widget.
    private PagerAdapter mPagerAdapter;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, "onCreate()");
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_slideshow);

        pagerPoints = (LinearLayout) findViewById(R.id.pagerPoints);

        Bundle b = this.getIntent().getExtras();

        tourType = TourType.values()[b.getInt(PARAM_TOUR_TYPE)];

        buttonsView=findViewById(R.id.bottomButtons);

        if(b.getBoolean(PARAM_SHOW_BOTTOM_BUTTONS,false))
        {
            buttonsView.setVisibility(View.VISIBLE);
        }

        TypedArray icons = getResources().obtainTypedArray(b.getInt(PARAM_ARRAY_ID));
        max_pages=icons.length();
        imageResArray=new int[max_pages];
        for(int a=0;a<max_pages;a++)
        {
            imageResArray[a]=icons.getResourceId(a,0);
        }

        icons.recycle();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.tourpager);
        mPagerAdapter = new ActivityTourPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int pos) {
            	setActivePagerPoint(pos);
                if(tourType==TourType.SLIDE_SHOW_FIRST_START)
                {
                    buttonsView.setVisibility((pos+1)==max_pages?View.VISIBLE:View.INVISIBLE);
                }
            }
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        updatePagerPoints(0, max_pages);

        mPager.setCurrentItem(0);
        
        // set button labels/style
        TextViewTypeFace leftButton = (TextViewTypeFace)findViewById(R.id.buttonBottomLeft);
        TextViewTypeFace rightButton = (TextViewTypeFace)findViewById(R.id.buttonBottomRight);
        LinearLayout.LayoutParams params = null;
        switch (tourType) {
          case PINTACT:
                leftButton.setText(R.string.SIGN_IN);
                leftButton.setTextColor(getResources().getColor(R.color.PINTACT_ORANGE_COLOR));
                leftButton.setBackgroundResource(R.drawable.pintact_round_transparent_orange_button_selector);
                params = (LinearLayout.LayoutParams)leftButton.getLayoutParams();
                params.weight = 0.5f;
                leftButton.setLayoutParams(params);
                rightButton.setText(R.string.JOIN_PINTACT);
                params = (LinearLayout.LayoutParams)rightButton.getLayoutParams();
                params.weight = 0.5f;
                rightButton.setLayoutParams(params);
            break;
          case GROUP_PIN:
                leftButton.setText(R.string.group_tour_create);
                leftButton.setTextColor(getResources().getColor(R.color.PINTACT_WHITE_COLOR));
                leftButton.setBackgroundResource(R.drawable.pintact_round_orange_rect);
                params = (LinearLayout.LayoutParams)leftButton.getLayoutParams();
                params.weight = 0.65f;
                leftButton.setLayoutParams(params);
                rightButton.setText(R.string.group_tour_cancel);
                params = (LinearLayout.LayoutParams)rightButton.getLayoutParams();
                params.weight = 0.35f;
                rightButton.setLayoutParams(params);
            break;
            case SLIDE_SHOW_FIRST_START:
                leftButton.setText(R.string.SIGN_IN);
                leftButton.setTextColor(getResources().getColor(R.color.PINTACT_ORANGE_COLOR));
                leftButton.setBackgroundResource(R.drawable.pintact_round_transparent_orange_button_selector);
                params = (LinearLayout.LayoutParams)leftButton.getLayoutParams();
                params.weight = 0.5f;
                leftButton.setLayoutParams(params);
                rightButton.setText(R.string.JOIN_PINTACT);
                params = (LinearLayout.LayoutParams)rightButton.getLayoutParams();
                params.weight = 0.5f;
                rightButton.setLayoutParams(params);
                break;
          default:
            // unexpected; don't do anything
        }
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            finish();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }    

    
    public static Intent getInstance(Activity activity, TourType tourType,
        int tourImageArrayResId,boolean showBottomButtons,boolean showTopButtons)
    {
        Bundle b=new Bundle();
        b.putBoolean(PARAM_SHOW_BOTTOM_BUTTONS,showBottomButtons);
        b.putBoolean(PARAM_SHOW_TOP_BUTTONS,showTopButtons);
        b.putInt(PARAM_ARRAY_ID,tourImageArrayResId);
        b.putInt(PARAM_TOUR_TYPE, tourType.ordinal());

        Intent intent = new Intent(activity, ActivitySlideShow.class);
        intent.putExtras(b);

        return intent;
    }
    

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
	private class ActivityTourPagerAdapter extends FragmentStatePagerAdapter {
        public ActivityTourPagerAdapter(FragmentManager fm) {
            super(fm);
        }

		@Override
        public Fragment getItem(int position) {
            return ActivitySlideShowPageFragment.create(imageResArray[position]);
        }

        @Override
        public int getCount() {
            return max_pages;
        }
    }


    public void onClick(View v) {
        if(v.getId()==R.id.buttonBottomLeft) {
          switch (tourType) {
          case PINTACT: // sign in
            onLogin();
            break;
          case GROUP_PIN:
            onCreateGroup();
            break;
          case SLIDE_SHOW_FIRST_START: // sign in
              onLogin();
              break;
          default:
              // unknown type; take no action
          }
        } else {
          switch (tourType) {
          case PINTACT:
            onRegister();
            break;
          case GROUP_PIN:
            onCancel();
            break;
          case SLIDE_SHOW_FIRST_START:
              onRegister();
              break;
          default:
            // unknown type; take no action
          }
        }
    }

    public void onRegister() {
        Intent it = new Intent(this, LoginRegisterOptionsActivity.class);
        startActivity(it);
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        finish();
    }

    public void onLogin() {
        Intent it = new Intent(this, LoginActivity.class);
        startActivity(it);
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        finish();
    }
    
    public void onCreateGroup() {
      SingletonLoginData.getInstance().setCurGroup(null);
      Intent it = new Intent(this, GroupPinActivity.class);
      startActivity(it);
      overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
      finish();
    }
    
    public void onCancel() {
      overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
      finish();
    }
}
