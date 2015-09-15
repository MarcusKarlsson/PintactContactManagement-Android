package com.pinplanet.pintact.utility;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.ContextThemeWrapper;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.MainActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.data.UserProfileAddress;
import com.pinplanet.pintact.data.UserProfileAttribute;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.data.service.ContactTableDbInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UiControllerUtil {

    public static String sanitizeMobileNumber(String mobile)
    {
        if(mobile != null) {
            StringBuffer stb = new StringBuffer();
            char[] chars = mobile.toCharArray();
            for(int i=0;i<chars.length;i++)
            {
                if(Character.isDigit(chars[i]) || chars[i] == '+')
                    stb.append(chars[i]);
            }
            return stb.toString();
        }
        return mobile;
    }

    public static ContactDTO loadLocalContact(Context context, String id, Cursor cur) {
        ContactDTO contact;
        HashMap<String, ContactDTO> allContacts = new HashMap<String, ContactDTO>(100);
        System.out.println("Count : " + cur.getCount());
        //Log.i(TAG,"loadLocalContacts() count:"+cur.getCount());

        if (cur.getCount() > 0) {

            int contactidIndex = cur.getColumnIndex(ContactsContract.Data.CONTACT_ID);
            int visibleGroupIndex = cur.getColumnIndex(ContactsContract.Data.IN_VISIBLE_GROUP);
            int mimeIndex = cur.getColumnIndex(ContactsContract.Data.MIMETYPE);
            int displayNameIndex = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            int phoneLabelIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
            int phoneNumberIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int emailLabelIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL);
            int emailDataIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
            int noteColumnIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE);
            int protocolIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL);
            int customProtocolIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL);
            int imDataIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA);
            int photoUriIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI);

            while (cur.moveToNext()) {

                //String id = cur.getString(contactidIndex);
                String groupType = cur.getString(visibleGroupIndex);
                if (groupType.equals("1")) {
                    String mimeType = cur.getString(mimeIndex);
                    List<UserProfileAttribute> attributes = null;
                    UserProfile userProfile = null;
                    UserDTO user = null;
                    if (allContacts.containsKey(id)) {
                        // update contact
                        contact = allContacts.get(id);
                        ProfileDTO profileDTO = contact.getSharedProfiles().get(0);
                        attributes = profileDTO.getUserProfileAttributes();
                        userProfile = profileDTO.getUserProfile();
                        user = contact.getContactUser();
                    } else {
                        contact = new ContactDTO();
                        contact.isLocalContact = true;
                        List<ProfileDTO> profileDTOs = new ArrayList<ProfileDTO>();
                        ProfileDTO profileDTO = new ProfileDTO();
                        attributes = new ArrayList<UserProfileAttribute>();
                        userProfile = new UserProfile();
                        user = new UserDTO();
                        profileDTO.setUserProfile(userProfile);
                        profileDTO.setUserProfileAttributes(attributes);
                        profileDTO.setUserProfileAddresses(new ArrayList<UserProfileAddress>());
                        profileDTOs.add(profileDTO);
                        contact.setSharedProfiles(profileDTOs);
                        contact.setContactUser(user);
                        allContacts.put(id, contact);
                    }

                    if (mimeType.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                        String dName = cur.getString(displayNameIndex);
                        int pos = dName.lastIndexOf(' ');
                        if (pos == -1) {
                            userProfile.setFirstName(dName);
                            user.setFirstName(dName);
                        } else {
                            String fn = dName.substring(0, pos);
                            String ln = "";
                            // in case the last name was followed by an extra ' ';
                            if (pos != dName.length() - 1)
                                ln = dName.substring(pos + 1, dName.length());
                            // set name
                            user.setFirstName(fn);
                            user.setLastName(ln);
                            userProfile.setFirstName(fn);
                            userProfile.setLastName(ln);
                        }
                    } else if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                        // set phone munber
                        attributes.add(new UserProfileAttribute(AttributeType.PHONE_NUMBER, cur.getString(phoneLabelIndex), cur.getString(phoneNumberIndex)));
                    } else if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                        // set email
                        attributes.add(new UserProfileAttribute(AttributeType.EMAIL, cur.getString(emailLabelIndex), cur.getString(emailDataIndex)));
                    } else if (mimeType.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                        userProfile.setPathToImage(cur.getString(photoUriIndex));
                    } else if (mimeType.equals(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)) {
                        attributes.add(new UserProfileAttribute(AttributeType.PRIVATE_NOTE, null, cur.getString(noteColumnIndex)));
                    } else if (mimeType.equals(ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)) {
                        attributes.add(new UserProfileAttribute(AttributeType.SERVICE_ID, ContactsContract.CommonDataKinds.Im.getProtocolLabel(context.getResources(), cur.getInt(protocolIndex), cur.getString(customProtocolIndex)).toString(), cur.getString(imDataIndex)));
                    }

                }
            }
        }
        cur.close();
        return allContacts.get(id);
    }

    public static ContactDTO loadLocalContact(Context context, String id) {
        System.out.println("Loading data from db");
        Cursor cur = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]{
                ContactsContract.Data.CONTACT_ID, ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.DATA, ContactsContract.CommonDataKinds.Email.LABEL,
                ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.LABEL, ContactsContract.CommonDataKinds.Photo.PHOTO_URI
                , ContactsContract.CommonDataKinds.Note.NOTE, ContactsContract.CommonDataKinds.Im.DATA,
                ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, ContactsContract.Data.IN_VISIBLE_GROUP}, ContactsContract.Data.CONTACT_ID + " = '" + id + "'", new String[]{}, null);

        return loadLocalContact(context, id, cur);
    }

    public static String getVersionName() {
        AppController appController = AppController.getInstance();
        if (appController != null)
            return appController.getResources().getString(R.string.version_name);
        else
            return "";
    }

    public static String getCountryZipCode(Context context) {
        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        if (manager != null) {
            CountryID = manager.getSimCountryIso().toUpperCase();
            String[] rl = context.getResources().getStringArray(R.array.CountryCodes);
            for (int i = 0; i < rl.length; i++) {
                String[] g = rl[i].split(",");
                if (g[1].trim().equals(CountryID.trim())) {
                    CountryZipCode = "+" + g[0];
                    break;
                }
            }
        }

        if (CountryZipCode.length() == 0) {
            CountryZipCode = "+1";
        }

        return CountryZipCode;
    }

    public static void openPreviewShareActivity(Long[] profileIds) {
        List<ProfileDTO> profileDTOs = new ArrayList<ProfileDTO>();
        for (Long id : profileIds) {
            for (ProfileDTO profileDTO : SingletonLoginData.getInstance().getUserProfiles()) {
                if (profileDTO.getUserProfile().getId().equals(id)) {
                    profileDTOs.add(profileDTO);
                }
            }
        }
        SingletonLoginData.getInstance().setMergedProfile(AppService.getMergedProfile(profileDTOs));
        SingletonLoginData.getInstance().setContactUser(SingletonLoginData.getInstance().getUserData());
    }

    public static boolean checkStaticDataPresent(Activity activity) {
        if (SingletonLoginData.checkInstance() == null) {
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return false;
        }

        return true;
    }

    public static boolean goToMainActivity(Activity activity) {

        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        return true;
    }


    public static void openContactDetail(ContactDTO contact) {
        if(!contact.isLocalContact)
        {
            ContactDTO contact1 = ContactTableDbInterface.getInstance().getContact(contact.getUserId());
            if(contact1 != null)
            {
                contact = contact1;
            }
        }
        SingletonLoginData.getInstance().setMergedProfile(AppService.getMergedProfile(contact));
        SingletonLoginData.getInstance().setCurrentContactDto(contact);
        SingletonLoginData.getInstance().setContactUser(contact.getContactUser());
        if (contact.getLabels() == null) {
            contact.setLabels(new ArrayList<String>());
        }
        SingletonLoginData.getInstance().setContactLabels((ArrayList<String>) contact.getLabels());
    }

    public static String getInitial(String firstName, String lastName) {
        String initial = "";
        if (firstName != null && firstName.length() > 0) {
            initial += firstName.charAt(0);
        }

        if (lastName != null && lastName.trim().length() > 0) {
            initial += lastName.charAt(0);
        }
        return initial.toUpperCase(Locale.US);
    }

    public static String getName(String firstName, String lastName) {
        String initial = "";
        if (firstName != null && firstName.length() > 0) {
            initial += firstName;
        }

        if (lastName != null && lastName.trim().length() > 0) {
            initial += " " + lastName;
        }
        return initial;
    }

    public static String getTitleCompanyName(UserDTO userDTO) {
        String title = "";
        if (userDTO.getTitle() != null && userDTO.getTitle().length() > 0) {
            title += userDTO.getTitle();
        }

        if (userDTO.getCompanyName() != null && userDTO.getCompanyName().trim().length() > 0) {
            title += "," + userDTO.getCompanyName();
        }

        if (title.length() == 0) {
            title = userDTO.pin;
        }
        return title;
    }

    public static String safeString(String st) {
        return (st == null) ? "" : st;
    }


    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }


    public static boolean validField(Activity activity, String str, String info) {
        if (str == null || str == "" || str.length() == 0) {
            myDialog(activity, "Field Empty", "Please enter your " + info);
            return false;
        }

        return true;
    }

    public static void myDialog(Activity activity, String title, String info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.AlertDialogCustom));
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(info);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton(R.string.generic_error_dialog_dismiss,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
