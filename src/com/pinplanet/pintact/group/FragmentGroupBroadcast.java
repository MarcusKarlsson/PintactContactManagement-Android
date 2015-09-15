package com.pinplanet.pintact.group;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.GroupDTO;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

public class FragmentGroupBroadcast extends DialogFragment {

    private GroupDTO groupDTO;
    private DialogFragment dialogToCloseOnSucess;

    public FragmentGroupBroadcast() {
    }

    public FragmentGroupBroadcast(GroupDTO groupDTO, DialogFragment dialogToCloseOnSucess) {
        this.groupDTO = groupDTO;
        this.dialogToCloseOnSucess = dialogToCloseOnSucess;
    }

    public FragmentGroupBroadcast(GroupDTO groupDTO) {
        this.groupDTO = groupDTO;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View inputView = inflater.inflate(R.layout.send_text, null);
        builder.setTitle(R.string.group_broadcast_dialog_title)
                .setView(inputView)
                .setPositiveButton(R.string.send_text_button_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Map<String, String> smsRequest = new HashMap<String, String>();
                        String message = ((EditText) inputView.findViewById(R.id.send_text_input)).getText().toString();
                        smsRequest.put("body", message);

                        Gson gson = new GsonBuilder().create();
                        String params = gson.toJson(smsRequest);
//                        String apiPath = "/api/group/" + Uri.encode(groupDTO.getGroupPin(), "utf-8") + "/sendSMS";
                        String apiPath = "/api/group/" + groupDTO.getGroupPin() + "/sendSMS";
                        SingletonNetworkStatus.getInstance().setActivity(FragmentGroupBroadcast.this.getActivity());
                        String path = apiPath + ".json?" + SingletonLoginData.getInstance().getPostParam();
                        new HttpConnection().access(FragmentGroupBroadcast.this.getActivity(), path, params, "POST");

                        if (dialogToCloseOnSucess != null) {
                            dialogToCloseOnSucess.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
