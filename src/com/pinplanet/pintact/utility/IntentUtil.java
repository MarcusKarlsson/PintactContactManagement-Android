package com.pinplanet.pintact.utility;

import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * Utility class for handling common intents
 *
 * @author Erik Hayes
 */
public class IntentUtil {

    private static final String TAG = "debugging";

    /**
     * Trigger e-mail intent with optional specification of recipients, subject, and body
     *
     * @param emailAddressArray
     * @param subject
     * @param body
     */
    public static void sendEmail(DialogFragment dialogParent, String[] emailAddressArray, String subject, String body) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/html");
        i.putExtra(Intent.EXTRA_EMAIL, emailAddressArray);
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, body);
        try {
            dialogParent.startActivity(Intent.createChooser(i, "Send mail..."));
            dialogParent.dismiss();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(dialogParent.getActivity(),
                    "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void sendEmail(Activity dialogParent, String[] emailAddressArray, String subject, String body) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/html");
        i.putExtra(Intent.EXTRA_EMAIL, emailAddressArray);
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, body);
        try {
            dialogParent.startActivity(Intent.createChooser(i, "Send mail..."));
//            dialogParent.dismiss();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(dialogParent,
                    "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Trigger SMS intent with optional specification of recipients and body
     *
     * @param dialogParent
     * @param recipient    Mobile number(s), can be semi-colon separated list
     * @param body
     */
    public static void sendSms(DialogFragment dialogParent, List<String> recipientList, String body) {
        StringBuilder joinedRecipient = new StringBuilder();
        String separator = ";";
        if (android.os.Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            separator = ",";
        }
        for (String recipient : recipientList) {
            if (joinedRecipient.length() > 0) {
                joinedRecipient.append(separator);
            }
            joinedRecipient.append(recipient.replaceAll("[\\W_]", ""));
        }
        sendSms(dialogParent, joinedRecipient.toString(), body);
    }

    /**
     * Trigger SMS intent with optional specification of recipients and body
     *
     * @param dialogParent
     * @param recipient    Mobile number(s), can be semi-colon separated list
     * @param body
     */
    public static void sendSms(DialogFragment dialogParent, String recipient, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        //or this...fixes whats app but the chooser-list is getting very long
        //Intent intent = new Intent(Intent.ACTION_SEND);
        //intent.setType("text/plain");
        intent.setData(Uri.parse("smsto:" + (recipient == null ? "" : recipient)));
        intent.putExtra("sms_body", body);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            dialogParent.startActivity(Intent.createChooser(intent, "Send SMS..."));
            dialogParent.dismiss();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(dialogParent.getActivity(),
                    "There are no SMS clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Trigger SMS intent with optional specification of recipients and body
     *
     * @param dialogParent
     * @param recipient    Mobile number(s), can be semi-colon separated list
     * @param body
     */
    public static void sendSms(Activity dialogParent, List<String> recipientList, String body) {
        StringBuilder joinedRecipient = new StringBuilder();
        String separator = ";";
        if (android.os.Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            separator = ",";
        }
        for (String recipient : recipientList) {
            if (joinedRecipient.length() > 0) {
                joinedRecipient.append(separator);
            }
            joinedRecipient.append(recipient.replaceAll("[\\W_]", ""));
        }
        Log.d(TAG, "joinedRecipient: " + joinedRecipient.toString());
        sendSms(dialogParent, joinedRecipient.toString(), body);
    }


    public static void sendSms(Activity dialogParent, String recipient, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        //or this...fixes whats app but the chooser-list is getting very long
        //Intent intent = new Intent(Intent.ACTION_SEND);
        //intent.setType("text/plain");
        Log.d(TAG, "recipient: " + recipient);
        intent.setData(Uri.parse("smsto:" + (recipient == null ? "" : recipient)));
        intent.putExtra("sms_body", body);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            if (dialogParent != null) {
                dialogParent.startActivity(Intent.createChooser(intent, "Send SMS..."));
            } else {
                Log.d(TAG, "dialogParent null");
            }
            //dialogParent.dismiss();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(dialogParent,
                    "There are no SMS clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

}
