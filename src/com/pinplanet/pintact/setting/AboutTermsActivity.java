package com.pinplanet.pintact.setting;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.utility.MyActivity;


public class AboutTermsActivity extends MyActivity{

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.about_terms);

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
    tv.setText(getResources().getString(R.string.about_setting_terms));

    WebView myWebView = (WebView) findViewById(R.id.webview);
    myWebView.loadUrl("file:///android_asset/html/terms_conditions.html");
  }
}
