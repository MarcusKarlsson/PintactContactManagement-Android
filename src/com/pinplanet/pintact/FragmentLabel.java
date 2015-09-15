package com.pinplanet.pintact;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.PostServiceExecuteTask;
import com.pinplanet.pintact.utility.RestServiceAsync;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FragmentLabel extends Fragment {

  private static final String TAG = FragmentProfile.class.getName();

  public static final String ARG_PLANET_NUMBER = "planet_number";
  View mLabelView;
  ArrayList<View> mLabelItems = new ArrayList<View> ();
  MyActivity mActivity;
  View mRootView;

  public FragmentLabel() {

  }

  public void deleteLabelItem() {
    TextView textView = (TextView) mLabelView.findViewById(R.id.textView1);
    textView.setText(R.string.lm_edit_instruct);

    View.OnClickListener btnClk = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        View addView = (View) v.getParent();
        //addView.setTranslationX(addView.getResources().getDimension(R.dimen.label_delete_shift_negative));
        //addView.animate();

        addView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            //v.setTranslationX(0);
            //v.animate();

            View addView = (View) v.getParent();
            View deleteConfirm = addView.findViewById(R.id.lli_delete_confirm_container);
            deleteConfirm.setVisibility(View.INVISIBLE);

            v.setOnClickListener(null);
            v.setClickable(false);
          }
        });

        View deleteConfirm = ((View) addView.getParent()).findViewById(R.id.lli_delete_confirm_container);
        deleteConfirm.setMinimumHeight(addView.getHeight());
        deleteConfirm.setVisibility(View.VISIBLE);

        deleteConfirm.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            View addView = (View) v.getParent();
            TextView labelTV = (TextView)addView.findViewById(R.id.label_view);

            sendLabel2Server(labelTV.getText().toString(), 2, addView);
          }
        });

      }
    };

    for (int i = 0; i < mLabelItems.size(); i++) {
      View iv = mLabelItems.get(i);
      View tvDel = iv.findViewById(R.id.view_delete);
      tvDel.setVisibility(View.VISIBLE);
      tvDel.setOnClickListener(btnClk);

      iv.findViewById(R.id.lil_arrow).setVisibility(View.INVISIBLE);

      float dimen = mLabelView.getContext().getResources().getDimension(R.dimen.label_left_margin_expand);
      View tvLabel = iv.findViewById(R.id.label_text_container);
      RelativeLayout.LayoutParams layoutParams =
          (RelativeLayout.LayoutParams)tvLabel.getLayoutParams();
      layoutParams.setMargins(Math.round(dimen), 0, 0, 0);
      tvLabel.setLayoutParams(layoutParams);
    }

  }

  public void showLabelContacts(String label) {

    try {
      System.out.println("Label is " + label + " url:" + Uri.encode(label, "utf-8"));

      ((LeftDeckActivity)(this.getActivity())).loadLabel(label);

    } catch (Exception e) {

    }

  }

  public void showAllLabels () {

    List<String> labels = SingletonLoginData.getInstance().getLabels();

    LinearLayout container = (LinearLayout)mLabelView.findViewById(R.id.lm_llo);

    for (int i =0 ; i < labels.size(); i ++ ) {
      List<ContactDTO> labelContacts = SingletonLoginData.getInstance().getLabelContactMap().get(labels.get(i));
      int labelMemberCount = labelContacts == null ? 0 : labelContacts.size();

      final View addView = this.getActivity().getLayoutInflater().inflate(R.layout.label_list_item, null);
      container.addView(addView);
      mLabelItems.add(addView);
      if (labelMemberCount > 0) {
        addView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            TextView tv = (TextView ) v.findViewById(R.id.label_view);
            showLabelContacts(tv.getText().toString());
          }
        });
        View arrow = addView.findViewById(R.id.lil_arrow);
        arrow.setVisibility(View.VISIBLE);
      }

      TextView labelTV = (TextView)addView.findViewById(R.id.label_view);
      labelTV.setText(labels.get(i));
      labelTV.setVisibility(View.VISIBLE);

      if (labelMemberCount > 0) {
        TextView labelMembers = (TextView)addView.findViewById(R.id.label_members);
        labelMembers.setText(labelMemberCount + " Pintacts");
      }

      View addTV = addView.findViewById(R.id.view_add);
      EditText inputET = (EditText)addView.findViewById(R.id.label_input);
      inputET.setVisibility(View.GONE);
      addTV.setVisibility(View.INVISIBLE);

      if (i == labels.size() - 1) {
        View separator = addView.findViewById(R.id.label_separator);
        RelativeLayout.LayoutParams layoutParams =
            (RelativeLayout.LayoutParams)separator.getLayoutParams();
        layoutParams.setMargins(0, 0, 0, 0);
        separator.setLayoutParams(layoutParams);
      }
    }

  }

  public void addLabelItem() {

    final LinearLayout container = (LinearLayout)mLabelView.findViewById(R.id.lm_llo);
      final View addView = this.getActivity().getLayoutInflater().inflate(R.layout.label_list_item, null);
    container.addView(addView);
    RelativeLayout rlo = (RelativeLayout) mLabelView.findViewById(R.id.lm_clkLO);
    ImageView rIV = (ImageView) mLabelView.findViewById(R.id.lm_icon);
    TextView  rTV = (TextView) mLabelView.findViewById(R.id.lm_btn);
    rlo.setClickable(false);
    rIV.setClickable(false);
    rTV.setClickable(false);

    View addTV = addView.findViewById(R.id.view_add);
    addTV.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        View addView = (View) v.getParent();
        EditText inputET = (EditText)addView.findViewById(R.id.label_input);
        
        if (inputET.length() == 0) {
          inputET.setError("Value is required");
          return;
        }

        if (inputET.getError() != null) {
          ((MyActivity)getActivity()).myDialog(R.string.generic_error_dialog_title, R.string.dialog_fix_errors_message);
          return;
        }

        sendLabel2Server(inputET.getText().toString(), 1, addView);
      }
    });


    View tvDel = addView.findViewById(R.id.view_delete);
    tvDel.setVisibility(View.VISIBLE);
    tvDel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        View addView = (View)((View) v.getParent()).getParent();
        container.removeView(addView);
      }
    });

    float dimen = addView.getContext().getResources().getDimension(R.dimen.label_left_margin_expand);
    View labelText = addView.findViewById(R.id.label_text_container);
    RelativeLayout.LayoutParams layoutParams =
        (RelativeLayout.LayoutParams)labelText.getLayoutParams();
    layoutParams.setMargins(Math.round(dimen), 0, 0, 0);
    labelText.setLayoutParams(layoutParams);

    final EditText inputET = (EditText)addView.findViewById(R.id.label_input);
    inputET.addTextChangedListener(new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        LinearLayout container = (LinearLayout)mLabelView.findViewById(R.id.lm_llo);
        if (s.length() > 0) {
          ((TextView)container.getChildAt(container.getChildCount()-1).findViewById(R.id.view_add)).setTextColor(container.getResources().getColor(R.color.PINTACT_BLUE_COLOR));
        } else {
          ((TextView)container.getChildAt(container.getChildCount()-1).findViewById(R.id.view_add)).setTextColor(container.getResources().getColor(R.color.profile_title));
        }
        if (s.length() > 50) {
          inputET.setError("Value cannot exceed 50 characters");
        } else {
          inputET.setError(null);
        }
      }
    });
  }

  public void sendLabel2Server(String value, int op, View v) {
    String path, params;
    if ( op == 1) {
      path = "/api/labels.json?" + SingletonLoginData.getInstance().getPostParam();
      params = "{\"label\":\"" + value + "\"}";
      ((LeftDeckActivity)this.getActivity()).setLabelOpPost(1, mLabelView, v, mLabelItems);
    } else {
      value = value.replaceAll(" ", "%20");
      path = "/api/labels/" + value + "/delete.json?" + SingletonLoginData.getInstance().getPostParam();
      params = "";
      ((LeftDeckActivity)this.getActivity()).setLabelOpPost(2, mLabelView, v, mLabelItems);
    }

    SingletonNetworkStatus.getInstance().setActivity(this.getActivity());
    new HttpConnection().access(this.getActivity(), path, params, "POST");
  }

  public View showLabelLayout(LayoutInflater inflater, ViewGroup container)
  {
    View rootView = inflater.inflate(R.layout.label_main, container, false);
    mLabelView = rootView;

    return mLabelView;
  }



  @SuppressLint("NewApi")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

      Log.i(TAG, "onCreateView()");

    View result = null;
    mActivity = (MyActivity)this.getActivity();
    result = showLabelLayout(inflater, container);
    mRootView = result;

    String path = "/api/labels.json?" + SingletonLoginData.getInstance().getPostParam();
    new RestServiceAsync(new PostServiceExecuteTask() {
      @Override
      public void run(int statusCode, final String result) {
        if(statusCode == 200) {
          Type collectionType = new TypeToken<Collection<String>>(){}.getType();
          Gson gson = new GsonBuilder().create();
          Collection<String> labels = null;
          if(result != null) {
            labels = gson.fromJson(result, collectionType);
          }else{
            labels = new ArrayList<String>();
          }
          AppService.addLabels(new ArrayList<String>(labels));

          Handler mainHandler = new Handler(AppController.getInstance().getApplicationContext().getMainLooper());

          Runnable myRunnable = new Runnable(){
            public void run(){
              if(FragmentLabel.this.getActivity() != null) {
                  View.OnClickListener btnClk = new View.OnClickListener() {

                      @Override
                      public void onClick(View v) {
                          mActivity.showRightText(getResources().getString(R.string.ab_done));
                          deleteLabelItem();
                          addLabelItem();
                      }
                  };
                  RelativeLayout rlo = (RelativeLayout) mLabelView.findViewById(R.id.lm_clkLO);
                  ImageView rIV = (ImageView) mLabelView.findViewById(R.id.lm_icon);
                  TextView rTV = (TextView) mLabelView.findViewById(R.id.lm_btn);
                  rIV.setOnClickListener(btnClk);
                  rlo.setOnClickListener(btnClk);
                  rTV.setOnClickListener(btnClk);

                  showAllLabels();

                  View.OnClickListener editClk = new View.OnClickListener() {

                      @Override
                      public void onClick(View v) {
                          if (mActivity.getRightText() == getResources().getString(R.string.ab_edit)) {
                              mActivity.showRightText(getResources().getString(R.string.ab_done));
                              deleteLabelItem();
                          } else {
                              TextView textView = (TextView) mLabelView.findViewById(R.id.textView1);
                              textView.setText(R.string.lm_main_instruct);

                              RelativeLayout rlo = (RelativeLayout) mLabelView.findViewById(R.id.lm_clkLO);
                              ImageView rIV = (ImageView) mLabelView.findViewById(R.id.lm_icon);
                              TextView rTV = (TextView) mLabelView.findViewById(R.id.lm_btn);
                              rlo.setClickable(true);
                              rIV.setClickable(true);
                              rTV.setClickable(true);

                              LinearLayout container = (LinearLayout) mLabelView.findViewById(R.id.lm_llo);
                              View lastView = container.getChildAt(container.getChildCount() - 1);
                              if (lastView.findViewById(R.id.view_add).getVisibility() == View.VISIBLE) {
                                  container.removeViewAt(container.getChildCount() - 1);
                              }

                              mActivity.showRightText(getResources().getString(R.string.ab_edit));
                              for (int i = 0; i < mLabelItems.size(); i++) {
                                  View iv = mLabelItems.get(i);
                                  View tvDel = iv.findViewById(R.id.view_delete);
                                  tvDel.setVisibility(View.INVISIBLE);

                                  TextView labelTV = (TextView) iv.findViewById(R.id.label_view);
                                  if (SingletonLoginData.getInstance().getLabelContactMap()
                                          .containsKey(labelTV.getText().toString())) {
                                      iv.findViewById(R.id.lil_arrow).setVisibility(View.VISIBLE);
                                  }

                                  float dimen = mLabelView.getContext().getResources().getDimension(R.dimen.label_left_margin);
                                  View tvLabel = iv.findViewById(R.id.label_text_container);
                                  RelativeLayout.LayoutParams layoutParams =
                                          (RelativeLayout.LayoutParams) tvLabel.getLayoutParams();
                                  layoutParams.setMargins(Math.round(dimen), 0, 0, 0);
                                  tvLabel.setLayoutParams(layoutParams);
                              }

                              mActivity.hideSoftKeyboard(mActivity);
                          }
                      }
                  };
                  mActivity.hideRight();
                  mActivity.showRightText(getResources().getString(R.string.ab_edit));
                  mActivity.addRightTextClickListener(editClk);
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
