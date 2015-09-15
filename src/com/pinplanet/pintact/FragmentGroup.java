package com.pinplanet.pintact;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.contact.ContactFindActivity;
import com.pinplanet.pintact.data.GroupDTO;
import com.pinplanet.pintact.group.CreateGroupActivity;
import com.pinplanet.pintact.group.GroupPinActivity;
import com.pinplanet.pintact.slideshow.ActivitySlideShow;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.PostServiceExecuteTask;
import com.pinplanet.pintact.utility.RestServiceAsync;
import com.pinplanet.pintact.utility.SingletonLoginData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by pranab on 10/25/14.
 */
public class FragmentGroup extends Fragment {

  private static final String TAG = FragmentProfile.class.getName();

  public static final String ARG_PLANET_NUMBER = "planet_number";
  ExpandableListView expListView;
  View mLabelView;
  ArrayList<View> mLabelItems = new ArrayList<View> ();
  MyActivity mActivity;
  View mRootView;

  public FragmentGroup() {

  }

  public void onNewGroup() {
    Intent myIntent = new Intent(this.getActivity(), CreateGroupActivity.class);
    startActivity(myIntent);

  }

  public void loadGroups() {
    mActivity.showRightImage(R.drawable.info_circle_button);
    mActivity.addRightImageClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if(FragmentGroup.this.getActivity() != null) {
            Intent it = ActivitySlideShow.getInstance(FragmentGroup.this.getActivity(), ActivitySlideShow.TourType.GROUP_PIN,
                    R.array.ARRAY_TOUR_GROUP_PIN, true, false);
            startActivity(it);
            FragmentGroup.this.getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        }
      }
    });


    final LinearLayout list = (LinearLayout)mRootView.findViewById(R.id.gmn_list);
    final List<GroupDTO> allGroups = SingletonLoginData.getInstance().getGroups();
      Log.d(TAG, "Groups: " + allGroups.toString());
    list.removeAllViews();
    for (int i = 0; i < allGroups.size(); i ++) {
      GroupDTO group = allGroups.get(i);
      View addView = this.getActivity().getLayoutInflater().inflate(R.layout.group_item_list, null);
      addView.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          int num = list.getChildCount();
          for ( int i = 0; i < num; i ++ ) {
            if ( list.getChildAt(i) == v ) {
              System.out.println("Found child at index " + i);
              SingletonLoginData.getInstance().setCurGroup(allGroups.get(i));
              ((LeftDeckActivity)v.getContext()).loadingGroup(allGroups.get(i).getGroupPin());
              return;
            }
          }
          System.out.println("=== ERROR: could not find this child view");
        }
      });
      TextView groupName = (TextView) addView.findViewById(R.id.gil_name);
      TextView groupPin = (TextView) addView.findViewById(R.id.gil_title);
      ImageView editableImage = (ImageView) addView.findViewById(R.id.gil_editable);
      groupName.setText(group.getGroupName());
      groupPin.setText(group.getGroupPin());
      if (!SingletonLoginData.getInstance().getUserData().id.toString().equals(group.getCreatedBy())) {
        editableImage.setVisibility(View.GONE);
      }
      if (i == allGroups.size()-1) {
        View separator = addView.findViewById(R.id.gil_sep);
        separator.setVisibility(View.GONE);
      }
      list.addView(addView);
    }


  }

  public View showGroupLayout(LayoutInflater inflater, ViewGroup container)
  {
    View rootView = inflater.inflate(R.layout.group_main, container, false);
    mRootView = rootView;

    mRootView.findViewById(R.id.gmn_add_button).setOnClickListener(null);

    mRootView.findViewById(R.id.gmn_join_button).setOnClickListener(null);

    return rootView;
  }

  @SuppressLint("NewApi")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    int i = getArguments().getInt(ARG_PLANET_NUMBER);
    View result = null;
    result = showGroupLayout(inflater, container);
    mRootView = result;

    String path = "/api/groupListMemberships.json?" + SingletonLoginData.getInstance().getPostParam();
    new RestServiceAsync(new PostServiceExecuteTask() {
      @Override
      public void run(int statusCode, final String result) {
        if(statusCode == 200) {
          Type collectionType = new TypeToken<Collection<GroupDTO>>(){}.getType();
          Gson gson = new GsonBuilder().create();
          Collection<GroupDTO> groups = gson.fromJson(result, collectionType);
          SingletonLoginData.getInstance().setGroups(new ArrayList<GroupDTO>(groups));
          SingletonLoginData.getInstance().setCreatedGroups(new ArrayList<GroupDTO>(groups));
          System.out.println("Total " + groups.size() + " Created Groups.");

          Handler mainHandler = new Handler(AppController.getInstance().getApplicationContext().getMainLooper());

          Runnable myRunnable = new Runnable(){
            public void run(){
                 MyActivity act = FragmentGroup.this.mActivity;
               if(FragmentGroup.this.getActivity() != null) {
                   // load groups created
                   loadGroups();

                   /// show right
                   View.OnClickListener newLn = new View.OnClickListener() {

                       @Override
                       public void onClick(View v) {
                           SingletonLoginData.getInstance().setCurGroup(null);
                           onNewGroup();
                       }
                   };

                   mRootView.findViewById(R.id.gmn_add_button).setOnClickListener(newLn);

                   mRootView.findViewById(R.id.gmn_join_button).setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View v) {
                           Intent myIntent = new Intent(getActivity(), ContactFindActivity.class);
                           startActivity(myIntent);
                       }
                   });
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
    loadGroups();
  }
  
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mActivity = (MyActivity)activity;
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
