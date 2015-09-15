package com.pinplanet.pintact.setting;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

public class SettingSortActivity extends MyActivity {
	RadioButton rbSort1;
	RadioButton rbSort2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_sort);
		hideRight();

		showLeftImage(R.drawable.actionbar_left_arrow);
		View.OnClickListener backLn = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		};
		addLeftClickListener(backLn);
		
		TextView tv = (TextView)findViewById(R.id.actionBar);
		tv.setText(getResources().getString(R.string.ab_sort_settings));
		
		rbSort1 = (RadioButton)findViewById(R.id.set_sort_kind1);
		rbSort2 = (RadioButton)findViewById(R.id.set_sort_kind2);
		
		if ( SingletonLoginData.getInstance().getUserSettings().sort == 0 ) {
			rbSort1.setChecked(true);
			rbSort2.setChecked(false);
		}
		else { 
			rbSort2.setChecked(true);
			rbSort1.setChecked(false);
		}
		
		rbSort1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if ( isChecked ) {
					((MyActivity)buttonView.getContext()).updatePreferencesSort(0);
					rbSort2.setChecked(false);
					rbSort1.setChecked(true);
				}
			}
		});

		rbSort2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if ( isChecked ) {
					((MyActivity)buttonView.getContext()).updatePreferencesSort(1);
					rbSort1.setChecked(false);
					rbSort2.setChecked(true);
				}
			}
		});
		
	}

	
	public void onDummy(View view) {
	}


}
