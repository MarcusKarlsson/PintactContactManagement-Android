package com.pinplanet.pintact.utility;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;


public class SingletonNetworkStatus {

	String json;
	String message;
	boolean ready;
	boolean noshow = false;
	boolean nodismiss = false;
	int code;
	Context activity;
    Fragment fragment;
	ProgressDialog dialog;
	
	static SingletonNetworkStatus instance = null;
	private SingletonNetworkStatus () 
	{
	}
	
	public String getJson() { return json; }
	public void setJson( String s ) { json = s; }
	
	public Context getActivity() { return activity; }
	public void setActivity( Context s ) { activity = s; }

    //sometimes we use fragments to callback the result
    public Fragment getFragment(){ return fragment; }
    public void setFragment(Fragment f) {fragment=f;}
	
	public ProgressDialog getWaitDialog() { return dialog; }
	public void setWaitDialog( ProgressDialog s ) { dialog = s; }
	
	public String getErrMsg() {
    	Gson gson = new GsonBuilder().create();
    	if ( json == null )
    		return "";

        errCode ec=null;

        try {
            ec = gson.fromJson(json, errCode.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    if(ec == null)
    {
      return AppController.getInstance().getResources().getString(R.string.unknown_error);
    }
		return ec.message;
	}
	public String getMsg() { return message; }
	public void setMsg( String s ) { message = s; }
	
	public int getCode() { return code; }
	public void setCode( int c ) { code = c; }
	
	public boolean isReady() { return ready; }
	public void setReady(boolean b) { ready = b; }
	
	public boolean getDoNotShowStatus() { return noshow; }
	public void setDoNotShowStatus(boolean b) { noshow = b; }

	public boolean getDoNotDismissDialog() { return nodismiss; }
	public void setDoNotDismissDialog(boolean b) { nodismiss = b; }
	
	public static SingletonNetworkStatus getInstance() {
		if ( instance == null )
			instance = new SingletonNetworkStatus();
		
		return instance;
	}
	
	public void clean() {
		if ( dialog != null ) {
			dialog.dismiss();
			dialog = null;
		}
		
		nodismiss = false;
		noshow = false;
		activity = null;
        fragment=null;
	}
	
	public class errCode {
		public String code;
		public String message;
	}
}
