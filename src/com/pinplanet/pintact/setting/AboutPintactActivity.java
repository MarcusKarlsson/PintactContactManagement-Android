package com.pinplanet.pintact.setting;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.utility.MyActivity;

public class AboutPintactActivity extends MyActivity{

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_about_main);

    showLeftImage(R.drawable.actionbar_left_arrow);
    View.OnClickListener backLn = new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        finish();
      }
    };
    addLeftClickListener(backLn);
    hideRight();
    TextView tv = (TextView) findViewById(R.id.actionBar);
    tv.setText(getResources().getString(R.string.set_about).toUpperCase());

    TextView vVersion = (TextView) findViewById(R.id.about_info_version);
    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      vVersion.setText("V " + pInfo.versionName);
    } catch (Exception e) {
      // not much we can do here
    }
    
    View.OnClickListener lnAbout = new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent it = new Intent (v.getContext(), AboutFaqActivity.class);
        startActivity(it);
      }
    };

    View vAboutFaq = this.findViewById(R.id.about_faq_wrapper);
    vAboutFaq.setOnClickListener(lnAbout);


    View.OnClickListener tln = new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent it = new Intent (v.getContext(), AboutTermsActivity.class);
        startActivity(it);
      }
    };

    View terms = this.findViewById(R.id.about_terms_wrapper);
    terms.setOnClickListener(tln);


    View.OnClickListener pln = new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent it = new Intent (v.getContext(), AboutPrivacyActivity.class);
        startActivity(it);
      }
    };

    View privacy = this.findViewById(R.id.about_privacy_wrapper);
    privacy.setOnClickListener(pln);
  }
}
