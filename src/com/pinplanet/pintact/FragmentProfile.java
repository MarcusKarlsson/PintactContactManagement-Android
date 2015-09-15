package com.pinplanet.pintact;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.GridView;

import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.profile.ProfileCreateEditActivity;
import com.pinplanet.pintact.profile.ProfileGridAdapter;
import com.pinplanet.pintact.profile.ProfileShowActivity;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.PostServiceExecuteTask;
import com.pinplanet.pintact.utility.RestServiceAsync;
import com.pinplanet.pintact.utility.SingletonLoginData;

import java.util.ArrayList;
import java.util.Collection;

public class FragmentProfile extends Fragment {

  private static final String TAG = FragmentProfile.class.getName();

  public static final String ARG_PLANET_NUMBER = "planet_number";
  ExpandableListView expListView;
  View mLabelView;
  ArrayList<View> mLabelItems = new ArrayList<View> ();
  MyActivity mActivity;
  View mRootView;

  public FragmentProfile() {

  }

  public void onProfileNew() {
    Log.i(TAG, "onProfileNew()");
    Intent myIntent = ProfileCreateEditActivity.getInstance(getActivity(), ProfileCreateEditActivity.MODE_NEW_PROFILE);
    startActivity(myIntent);
  }

  public void onProfileShow(int i) {
    Log.i(TAG,"onProfileShow(int i)");

    Intent myIntent = ProfileShowActivity.getInstance(getActivity(), i);
    startActivity(myIntent);
    this.getActivity().overridePendingTransition( R.anim.slide_in_left, R.anim.slide_out_left );
  }

  public View showProfileLayout(LayoutInflater inflater, ViewGroup container)
  {
    View v = inflater.inflate(R.layout.profile_main, container, false);
    ((MyActivity)this.getActivity()).showRightImage(R.drawable.actionbar_plus_orange);
    View.OnClickListener newLn = new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        onProfileNew();
      }
    };
    ((MyActivity)this.getActivity()).addRightImageClickListener(newLn);

    return v;
  }

  @SuppressLint("NewApi")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    int i = getArguments().getInt(ARG_PLANET_NUMBER);
    View result = null;
    mActivity = (MyActivity)this.getActivity();
    result = showProfileLayout(inflater, container);
    mRootView = result;

    String path = "/api/profiles.json?" + SingletonLoginData.getInstance().getPostParam();
    new RestServiceAsync(new PostServiceExecuteTask() {
      @Override
      public void run(int statusCode, String result) {
        if(statusCode == 200) {
          AppService.handleGetProfileResponse(result);
          Collection<ProfileDTO> profiles = AppService.getProfiles();
          SingletonLoginData.getInstance().setUserProfiles(new ArrayList<ProfileDTO>(profiles));

          Handler mainHandler = new Handler(AppController.getInstance().getApplicationContext().getMainLooper());

          Runnable myRunnable = new Runnable(){
            public void run() {
                try {
                    if (FragmentProfile.this != null && FragmentProfile.this.getActivity() != null && ((MyActivity) FragmentProfile.this.getActivity()).isActive()) {
                        GridView gridview = (GridView) mRootView.findViewById(R.id.grid_profiles);
                        ProfileGridAdapter customGridAdapter = new ProfileGridAdapter(FragmentProfile.this.getActivity(),
                                R.layout.profile_main_item,
                                SingletonLoginData.getInstance().getUserProfiles());
                        gridview.setAdapter(customGridAdapter);

                        gridview.setOnItemClickListener(new GridView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                onProfileShow(position);
                            }
                        });
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
          };
          mainHandler.post(myRunnable);


        }
      }
    }, this.getActivity() , true).execute(path, "", "GET");

    return result;
  }

  @Override
  public void onResume() {
    super.onResume();
    System.out.println("Onresume - Fragment.");

    int i = getArguments().getInt(ARG_PLANET_NUMBER);

    GridView gridview = (GridView) mRootView.findViewById(R.id.grid_profiles);
    ProfileGridAdapter customGridAdapter = new ProfileGridAdapter(this.getActivity(),
        R.layout.profile_main_item,
        SingletonLoginData.getInstance().getUserProfiles());
    gridview.setAdapter(customGridAdapter);

  }

  // Convert pixel to dip
  @SuppressLint("NewApi")
  public int getDipsFromPixel(float pixels) {
    // Get the screen's density scale
    final float scale = getResources().getDisplayMetrics().density;
    // Convert the dps to pixels, based on density scale
    return (int) (pixels * scale + 0.5f);
  }
}