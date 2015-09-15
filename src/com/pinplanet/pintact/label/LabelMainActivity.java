package com.pinplanet.pintact.label;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class LabelMainActivity extends MyActivity {
	
	boolean isAddingLabel = false;
	String newLabel;
	View mSubLabelView;
    ScrollView sv;
	ArrayList<String> mLabels;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.label_main);

		showLeftImage(R.drawable.actionbar_left_arrow);
		View.OnClickListener backLn = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		};
		addLeftClickListener(backLn);

        sv=(ScrollView)findViewById(R.id.label_main_view);
		
		mLabels = new ArrayList<String>(SingletonLoginData.getInstance().getContactLabels());
		AppService.addLabels(mLabels);
        AppService.setLabels();
		// add Done on right
		showRightText(getResources().getString(R.string.ab_done));
		View.OnClickListener doneLn = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendUpdatedLabels();
			}
		};
		addRightTextClickListener(doneLn);		
		
    	View.OnClickListener btnClk = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addLabelItem();				
			}
		};


    	RelativeLayout rlo = (RelativeLayout) findViewById(R.id.lm_clkLO);
    	ImageView rIV = (ImageView) findViewById(R.id.lm_icon);
    	TextView  rTV = (TextView) findViewById(R.id.lm_btn);
    	rIV.setOnClickListener(btnClk);
    	rlo.setOnClickListener(btnClk);
    	rTV.setOnClickListener(btnClk);
    	
    	showAllLabels();
	}

	public void showAllLabels () {
	  List<String> labels = SingletonLoginData.getInstance().getLabels();

	  LinearLayout container = (LinearLayout)findViewById(R.id.lm_llo);

	  View.OnClickListener selectLn = new View.OnClickListener() {
	    @Override
			public void onClick(View v) {
				String label = ((TextView)v.findViewById(R.id.label_view)).getText().toString();
				ImageView check = (ImageView)v.findViewById(R.id.label_check);
				SingletonLoginData.getInstance().getUserSettings().selectedLabel = label;
				
				if ( mLabels.contains(label)) {
					mLabels.remove(label);
					check.setImageResource(R.drawable.circle);
				} else {
					mLabels.add(label);
					check.setImageResource(R.drawable.circle_check_orange);
				}
			}
		};
		
		for (int i =0 ; i < labels.size(); i ++ ) {
			
		    final View addView = this.getLayoutInflater().inflate(R.layout.label_list_item, null);
    	    container.addView(addView);
		    
			TextView labelTV = (TextView)addView.findViewById(R.id.label_view);
			labelTV.setText(labels.get(i));
			labelTV.setVisibility(View.VISIBLE);
			addView.setOnClickListener(selectLn);
			
		    TextView addTV = (TextView) addView.findViewById(R.id.view_add);
			EditText inputET = (EditText)addView.findViewById(R.id.label_input);
			inputET.setVisibility(View.GONE);
			addTV.setVisibility(View.INVISIBLE);

			ImageView check = (ImageView)addView.findViewById(R.id.label_check);
			check.setVisibility(View.VISIBLE);
			
			if ( SingletonLoginData.getInstance().getContactLabels() != null &&
				 SingletonLoginData.getInstance().getContactLabels().contains(labels.get(i))) {
				check.setImageResource(R.drawable.circle_check);
			}
			
		}
	    
    }
    
	public void addLabelItem() {

		final LinearLayout container = (LinearLayout)findViewById(R.id.lm_llo);
	    final View addView = this.getLayoutInflater().inflate(R.layout.label_list_item, null);
	    container.addView(addView);
		RelativeLayout rlo = (RelativeLayout) findViewById(R.id.lm_clkLO);
    	ImageView rIV = (ImageView) findViewById(R.id.lm_icon);
    	TextView  rTV = (TextView) findViewById(R.id.lm_btn);
		rlo.setClickable(false);
		rIV.setClickable(false);
		rTV.setClickable(false);
		mSubLabelView = addView;
		
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

	    TextView addTV = (TextView) addView.findViewById(R.id.view_add);
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
          LabelMainActivity.this.myDialog(R.string.generic_error_dialog_title, R.string.dialog_fix_errors_message);
          return;
        }
				
				sendLabel2Server(inputET.getText().toString(), 1, addView);
			}
		});
	    
	    
	}
	
    
	public void sendLabel2Server(String value, int op, View v) {
		isAddingLabel = true;
		newLabel = value;
		String path = "/api/labels.json?" + SingletonLoginData.getInstance().getPostParam();
		String params = "{\"label\":\"" + value + "\"}";
		
		SingletonNetworkStatus.getInstance().setActivity(this);
		new HttpConnection().access(this, path, params, "POST");
	}
	
    
	public void sendUpdatedLabels() {
		String path = "/api/contacts/" + SingletonLoginData.getInstance().getMergedProfile().getUserId() + "/labels/update.json?" + SingletonLoginData.getInstance().getPostParam();
		SingletonNetworkStatus.getInstance().setActivity(this);
		
		Gson gson = new GsonBuilder().create();
		String params = gson.toJson(mLabels);
		
		params = "{\"label\":" + params + "}";
		new HttpConnection().access(this, path, params, "POST");
		
	}
	

	public void onPostNetwork() {
	  if ( SingletonNetworkStatus.getInstance().getCode() != 200 ) {
	    myDialog(SingletonNetworkStatus.getInstance().getMsg(), 
	        SingletonNetworkStatus.getInstance().getErrMsg());
	    SingletonNetworkStatus.getInstance().setCode(0);
	    return;
	  }

	  if ( isAddingLabel ) {
	    isAddingLabel = false;

	    // add it to global labels
	    // add it to local too
	    mLabels.add(newLabel);
	    AppService.addLabels(newLabel);

	    // update GUI
      EditText inputET = (EditText)mSubLabelView.findViewById(R.id.label_input);
      TextView labelTV = (TextView)mSubLabelView.findViewById(R.id.label_view);
      TextView addTV = (TextView)mSubLabelView.findViewById(R.id.view_add);
      labelTV.setText(inputET.getText().toString());
      inputET.setVisibility(View.GONE);
      labelTV.setVisibility(View.VISIBLE);
      addTV.setVisibility(View.INVISIBLE);

      // hide Add, show checked.
      RelativeLayout rlo = (RelativeLayout) findViewById(R.id.lm_clkLO);
      rlo.setClickable(true);

      ImageView check = (ImageView)mSubLabelView.findViewById(R.id.label_check);
      check.setVisibility(View.VISIBLE);
      check.setImageResource(R.drawable.circle_check);

	    return;
	  }else{
      // update labels
      AppService.handleUpdateContactResponse();
      SingletonLoginData.getInstance().getContactLabels().clear();
      SingletonLoginData.getInstance().getContactLabels().addAll(mLabels);
      finish();
    }


	}

	
	
	public void onDummy(View view) {
	}


}
