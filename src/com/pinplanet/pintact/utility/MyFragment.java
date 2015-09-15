package com.pinplanet.pintact.utility;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.pinplanet.pintact.R;

/**
 * Created by Dennis on 08.10.2014.
 */
public class MyFragment extends Fragment{

    protected final String TAG = this.getClass().getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
    }

    public void onPostNetwork() {
    }

    public void myDialog(int title, int info)
    {
        myDialog(getString(title), getString(info));
    }

    public void myDialog(String title, String info) {
        AlertDialog.Builder builder = new AlertDialog.Builder( new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(info);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
