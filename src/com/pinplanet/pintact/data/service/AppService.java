package com.pinplanet.pintact.data.service;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.contact.ContactInviteActivity;
import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.ContactUpdateDTO;
import com.pinplanet.pintact.data.NotificationDTO;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.SearchUserDTO;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.data.UserProfile;
import com.pinplanet.pintact.data.UserProfileAddress;
import com.pinplanet.pintact.data.UserProfileAttribute;
import com.pinplanet.pintact.data.UserProfileChildLabelDTO;
import com.pinplanet.pintact.data.UserProfileChildType;
import com.pinplanet.pintact.utility.HttpConnection;
import com.pinplanet.pintact.utility.PostServiceExecuteTask;
import com.pinplanet.pintact.utility.RestServiceAsync;
import com.pinplanet.pintact.utility.SingletonLoginData;
import com.pinplanet.pintact.utility.SingletonNetworkStatus;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AppService {

  private static Long lastSeenNotification = 0L;

  public static void reInit(){
    lastSeenNotification = 0L;
    PintactDbHelper.getInstance().reinitDB();
  }

  public static void setLabels(){
    try {
      SingletonLoginData.getInstance().setLabels(LabelTableDbInterface.getInstance().getLabels());
    }catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void addLabels(List<String> labels){
    try {
      LabelTableDbInterface.getInstance().addLabel(labels);
      SingletonLoginData.getInstance().setLabels(LabelTableDbInterface.getInstance().getLabels());
    }catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void addLabels(String label){
    try {

      LabelTableDbInterface.getInstance().addLabel(label);
      SingletonLoginData.getInstance().setLabels(LabelTableDbInterface.getInstance().getLabels());
    }catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void checkIfLocalContactsRegistered(){
      Collection<ContactDTO> contactDTOs = SingletonLoginData.getInstance().getLocalContactList();
      List<SearchUserDTO> searchUserDTOs = new ArrayList<>();

      for(ContactDTO contactDTO : contactDTOs)
      {
        List<ProfileDTO> profileDTOs = contactDTO.getSharedProfiles();
        if(profileDTOs != null && profileDTOs.size() > 0)
        {
            ProfileDTO profileDTO = profileDTOs.get(0);
            if(profileDTO.getUserProfileAttributes() != null && profileDTO.getUserProfileAttributes().size() > 0)

            {
                String email = null;
                String mobile = null;
                for(UserProfileAttribute profileAttribute : profileDTO.getUserProfileAttributes())
                {
                        switch (profileAttribute.getType())
                        {
                            case EMAIL:
                                email = profileAttribute.getValue();
                                break;
                            case PHONE_NUMBER:
                                mobile = profileAttribute.getValue();
                                break;
                            default:

                        }
                }
                if(email != null || mobile != null)
                {
                    SearchUserDTO searchUserDTO = new SearchUserDTO();
                    searchUserDTO.setMobileNumber(mobile);
                    searchUserDTO.setEmail(email);
                    searchUserDTO.setClientRefId(contactDTO.localContactId);
                    searchUserDTOs.add(searchUserDTO);
                }
            }
        }

      }
      if(searchUserDTOs.size() > 0)
      {
          String path = "/api/users/search.json?" + SingletonLoginData.getInstance().getPostParam();
          new RestServiceAsync(new LocalContactMatchingHandler()).execute(path, new Gson().toJson(searchUserDTOs), "POST");
      }

  }
  
  public static void removeLabel(String label) {
    try {
      LabelTableDbInterface.getInstance().removeLabel(label);
      SingletonLoginData.getInstance().setLabels(LabelTableDbInterface.getInstance().getLabels());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void setUserProfileChildLabels(){
    try {
      Map<UserProfileChildType, Map<AttributeType, List<String>>> userProfileChildLabelMap =
          UserProfileChildLabelTableDbInterface.getInstance().getLabels();
      if (userProfileChildLabelMap.isEmpty()) {
        PintactDbHelper.getInstance().initUserProfileChildLabelTable(PintactDbHelper.getInstance().getWritableDatabase());
        userProfileChildLabelMap =
            UserProfileChildLabelTableDbInterface.getInstance().getLabels();
      }
      SingletonLoginData.getInstance().setUserProfileChildLabels(userProfileChildLabelMap);
    }catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void addUserProfileChildLabels(UserProfileChildType userProfileChildType, AttributeType attributeType,
      List<String> labels){
    try {
      UserProfileChildLabelTableDbInterface.getInstance().addUserProfileChildLabel(userProfileChildType, attributeType, labels);
      SingletonLoginData.getInstance().setUserProfileChildLabels(UserProfileChildLabelTableDbInterface.getInstance().getLabels());
    }catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void addUserProfileChildLabels(UserProfileChildType userProfileChildType, AttributeType attributeType,
      String label){
    try {
      UserProfileChildLabelTableDbInterface.getInstance().addLabel(userProfileChildType, attributeType, label);
      SingletonLoginData.getInstance().setUserProfileChildLabels(UserProfileChildLabelTableDbInterface.getInstance().getLabels());
    }catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public static void removeUserProfileChildLabel(UserProfileChildType userProfileChildType, AttributeType attributeType,
      String label) {
    try {
      UserProfileChildLabelTableDbInterface.getInstance().removeLabel(userProfileChildType, attributeType, label);
      SingletonLoginData.getInstance().setUserProfileChildLabels(UserProfileChildLabelTableDbInterface.getInstance().getLabels());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static void removeProfile(ProfileDTO profileDTO) {
    try {
      ProfileTableDbInterface.getInstance().removeProfile(profileDTO);
      SingletonLoginData.getInstance().setUserProfiles(ProfileTableDbInterface.getInstance().getProfiles());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void fetchMoreNotifications(PostServiceExecuteTask postServiceExecuteTask)
  {
    String path = "/api/sortedNotifications.json?" + SingletonLoginData.getInstance().getPostParam();
    path=path+"&pageSize=100";
    new RestServiceAsync(postServiceExecuteTask).execute(path, "", "GET");
  }

  public static ProfileDTO getMergedProfile(List<ProfileDTO> profileDTOs){
    ContactDTO contactDTO = new ContactDTO();
    contactDTO.setSharedProfiles(profileDTOs);
    return getMergedProfile(contactDTO);
  }

  public static ProfileDTO getMergedProfile(ContactDTO selectedItem){
    if(selectedItem!= null) {
      ProfileDTO merged = new ProfileDTO();
      merged.setUserProfileAttributes(new ArrayList<UserProfileAttribute>());
      merged.setUserProfileAddresses(new ArrayList<UserProfileAddress>());

      UserProfile profile;
      if (selectedItem.getSharedProfiles() != null && selectedItem.getSharedProfiles().size() > 0)
        profile = selectedItem.getSharedProfiles().get(0).getUserProfile();
      else {
        profile = new UserProfile();
        if (selectedItem.getContactUser() != null) {
          profile.setFirstName(selectedItem.getContactUser().getFirstName());
          profile.setLastName(selectedItem.getContactUser().getLastName());
        } else {
          profile.setFirstName(selectedItem.getFirstName());
          profile.setLastName(selectedItem.getLastName());
        }
      }
      merged.setUserProfile(profile);
      merged.setUserId(selectedItem.getUserId());

      int num = (selectedItem.getSharedProfiles()== null)? 0 : selectedItem.getSharedProfiles().size();
      for (int k = 0; k < num; k++) {
        List<UserProfileAttribute> attrs = selectedItem.getSharedProfiles().get(k).getUserProfileAttributes();
        List<UserProfileAddress> addrs = selectedItem.getSharedProfiles().get(k).getUserProfileAddresses();
        if(attrs != null) {
            for (int item = 0; item < attrs.size(); item++) {
                if (!merged.getUserProfileAttributes().contains(attrs.get(item)))
                    merged.getUserProfileAttributes().add(attrs.get(item));
            }
        }
          if(addrs != null) {
              for (int item = 0; item < addrs.size(); item++) {
                  if (!merged.getUserProfileAddresses().contains(addrs.get(item)))
                      merged.getUserProfileAddresses().add(addrs.get(item));
              }
          }
      }
      return merged;
    }else{
      return null;
    }
  }


  public static void handleGetContactResponse(){
    try {
      Gson gson = new GsonBuilder().create();
      ContactUpdateDTO contactUpdateDTO = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), ContactUpdateDTO.class);
      if(contactUpdateDTO != null) {
        SingletonLoginData.getInstance().setTotalUnseenNoti(contactUpdateDTO.getTotalUnseenNotifications());
        lastSeenNotification = contactUpdateDTO.getLastSeenNotificationId();
        addContact(contactUpdateDTO.getContacts());
        initContactList(true);
      }
    }catch (Exception e){
      e.printStackTrace();
    }
  }


  public static List<ContactDTO> initContactList(boolean reInit){
    List<ContactDTO> list = SingletonLoginData.getInstance().getContactList();
      if(list == null || list.size() == 0 || reInit) {
          list = AppService.fetchContacts();
          HashSet<Long> contactUserList = new HashSet<>();
          for(ContactDTO contactDTO : list)
          {
              contactUserList.add(contactDTO.getUserId());
          }
          SingletonLoginData.getInstance().setContactuserList(contactUserList);
          list.addAll(SingletonLoginData.getInstance().getLocalContactList());
          Collections.sort(list, new MyContactComp(SingletonLoginData.getInstance().getUserSettings().sort));
          System.out.println("Cloud: " + SingletonLoginData.getInstance().getCloudContactList().size() +
                  "Local: " + SingletonLoginData.getInstance().getLocalContactList().size() +
                  "Total: " + list.size());
          SingletonLoginData.getInstance().setContactList(list);
          SingletonLoginData.getInstance().setIsStatusChanged(true);
      }
    return list;
  }

  public static void handleUpdateContactResponse(){
    try {
      Gson gson = new GsonBuilder().create();
      ContactDTO contactDTO = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), ContactDTO.class);
      if (contactDTO != null && contactDTO.isAcontact()) {
        addContact(contactDTO);
        initContactList(true);
        SingletonLoginData.getInstance().getLabelContactMap().clear();
        SingletonLoginData.getInstance().setIsStatusChanged(true);
      }
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  public static void handleDeleteContactResponse(Long userId){
    try {
      ContactTableDbInterface.getInstance().removeContact(userId);
      initContactList(true);
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  public static void handleGetProfileResponse(String json){
    try {
      Gson gson = new GsonBuilder().create();
      Type collectionType = new TypeToken<Collection<ProfileDTO>>() {
      }.getType();
      List<ProfileDTO> profiles = gson.fromJson(json, collectionType);
      if(profiles!= null) {
        ProfileTableDbInterface.getInstance().removeProfiles();
        ProfileTableDbInterface.getInstance().addProfile(profiles);
      }
    }catch (Exception e){
      throw new RuntimeException(e);
    }
  }

  public static void handleGetSingleProfileResponse(){
    try {
      Gson gson = new GsonBuilder().create();
      ProfileDTO profiles = gson.fromJson(SingletonNetworkStatus.getInstance().getJson(), ProfileDTO.class);
      if(profiles != null)
        ProfileTableDbInterface.getInstance().addProfile(profiles);
    }catch (Exception e){
        Crashlytics.setString("error AppService - handleGetSingleProfileResponse()", "json: " + SingletonNetworkStatus.getInstance().getJson());
        Crashlytics.log("error AppService - handleGetSingleProfileResponse() json: " + SingletonNetworkStatus.getInstance().getJson());
       throw new RuntimeException(e);
    }
  }

  public static void getLabels(Activity activity) {
    String path = "/api/labels.json?" + SingletonLoginData.getInstance().getPostParam();
    new HttpConnection().access(activity, path, "", "GET");
  }

  public static void getProfiles(Activity activity) {
    String path = "/api/profiles.json?" + SingletonLoginData.getInstance().getPostParam();
    new HttpConnection().access(activity, path, "", "GET");
  }

  public static List<ProfileDTO> getProfiles(){
    try {
      return ProfileTableDbInterface.getInstance().getProfiles();
    }catch (Exception e){
      throw new RuntimeException(e);
    }
  }

  public static void fetchContacts(Activity activity){
    SingletonNetworkStatus.getInstance().setActivity(activity);
    String path = "/api/notifications/getContactUpdates.json?" + SingletonLoginData.getInstance().getPostParam();
    path = path+"&lastSeenNotificationId="+lastSeenNotification;
    path = path+"&includeSelf=true";
    new HttpConnection().access(activity, path, "", "GET");
  }

  public static void getProfilesAsync(){
    String path = "/api/profiles.json?" + SingletonLoginData.getInstance().getPostParam();
    new RestServiceAsync(new ProfilesAsyncResponseHandler()).execute(path, "", "GET");
  }

  public static void getLabelsAsync(){
    String path = "/api/labels.json?" + SingletonLoginData.getInstance().getPostParam();
    new RestServiceAsync(new LabelsAsyncResponseHandler()).execute(path, "", "GET");
  }
  
  public static void getUserProfileChildLabelsAsync() {
    String path = "/api/profiles/attributes/customLabels.json?" + SingletonLoginData.getInstance().getPostParam();
    new RestServiceAsync(new UserProfileChildLabelAsyncResponseHandler()).execute(path, "", "GET");
  }

  public static void markAllNotificationMarked(Context context , Long lastNotificationId, PostServiceExecuteTask executeTask){
    String path = "/api/clearNotifications.json?" + SingletonLoginData.getInstance().getPostParam();

    Map data = new HashMap();
    data.put("lastNotificationId", lastNotificationId);
    String json = new Gson().toJson(data);
    new RestServiceAsync(executeTask, context , true).execute(path, json, "POST");
  }

  public static void addListContactToPintact(Context context , Long userId, Long groupId, Long profileId, PostServiceExecuteTask executeTask){
    String path = "/api/contacts/addFromList.json?" + SingletonLoginData.getInstance().getPostParam();

    Map data = new HashMap();
    data.put("destinationUserId", userId);
    data.put("groupId", groupId);
    data.put("userProfileIdsShared", Arrays.asList(profileId));
    String json = new Gson().toJson(data);
    new RestServiceAsync(executeTask, context , true).execute(path, json, "POST");
  }

  public static void inviteContact(Context context , ContactDTO contactDTO, PostServiceExecuteTask callback){
    if(!contactDTO.isAlreadyInvited()) {
      Map<String, List<ContactInviteActivity.InviteContactDTO>> data = new HashMap<String, List<ContactInviteActivity.InviteContactDTO>>();
      List<ContactInviteActivity.InviteContactDTO> list = new ArrayList<ContactInviteActivity.InviteContactDTO>();

      ContactInviteActivity.InviteContactDTO inviteContactDTO = new ContactInviteActivity.InviteContactDTO();
      inviteContactDTO.name = contactDTO.getContactUser().getName();
      inviteContactDTO.email = contactDTO.getContactUser().getEmailId();
      list.add(inviteContactDTO);
      data.put("data", list);
      String json = new Gson().toJson(data);
      String path = "/api/users/invite.json?" + SingletonLoginData.getInstance().getPostParam();
      new RestServiceAsync(callback, context, true).execute(path, json, "POST");
    }
  }

  public static void checkIfThereIsAnyContacts(){
    String path = "/api/notifications/getContactUpdates.json?" + SingletonLoginData.getInstance().getPostParam();
    path = path+"&lastSeenNotificationId="+lastSeenNotification;
    new RestServiceAsync(new ContactsAsyncResponseHandler()).execute(path, "", "GET");
  }

  public static void markSeenNotification(final NotificationDTO notificationDTO){
    notificationDTO.setSeen(true);
    String path = "/api/notifications/"+notificationDTO.getNotificationId()+"/seen.json?" + SingletonLoginData.getInstance().getPostParam();
    new RestServiceAsync(new MarkSeenNotificationCallback(notificationDTO)).execute(path, "", "POST");
  }

  public void addContactToAddressBook(){

  }

  public static String checkIfContactAddedToAddressBook(Long userId){
    return ContactTableDbInterface.getInstance().checkIfContactAddedToAddressBook(userId);
  }

  public static List<ContactDTO> fetchContacts(){
    try {
        SingletonLoginData.getInstance().checkLoginDataLoaded();
        if(SingletonLoginData.getInstance().getUserData() != null && SingletonLoginData.getInstance().getUserData().id != null) {
            return ContactTableDbInterface.getInstance().getContacts(Long.valueOf(SingletonLoginData.getInstance().getUserData().id));
        }else{
            return new ArrayList<ContactDTO>();
        }
    }catch (Exception e){
      throw new RuntimeException(e);
    }
  }

  public static void addContact(List<ContactDTO> contactDTOs) throws Exception {
    ContactTableDbInterface.getInstance().addContact(contactDTOs);
    for(ContactDTO contactDTO : contactDTOs){
      if(contactDTO.isAcontact()) {
        boolean shouldSync = SingletonLoginData.getInstance().shouldSyncNow();

        insertContact(AppService.getMergedProfile(contactDTO), !shouldSync);
        if(shouldSync) {
          SingletonLoginData.getInstance().lastSyncTime = new Date();
        }
      }
    }
  }

  public static void addContact(ContactDTO contactDTO) throws Exception {
    ContactTableDbInterface.getInstance().addContact(contactDTO);
    insertContact(AppService.getMergedProfile(contactDTO), false);
  }

  public static int insertContact(ProfileDTO mProfile , boolean updateIfFound) {
    String contactId = checkIfContactAddedToAddressBook(mProfile.getUserId());
    boolean found = false;
    if(contactId != null)
    {
      found = true;
    }

    if ( ! found && !updateIfFound) {
      AppService.insertNewContact(mProfile, 0);
      return 1;
    } else if (found)	{
      AppService.updateContact(mProfile,Integer.parseInt(contactId));
      return 2;
    }else{
      return 0;
    }

  }

  public static void updateContact(ProfileDTO mProfile, int rawContactId) {
    String contactId = String.valueOf(rawContactId);
    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

    ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
        .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? and " + ContactsContract.Data.MIMETYPE + "=?", new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
        .build());

    ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
        .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? and " + ContactsContract.Data.MIMETYPE + "=?", new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE})
        .build());

    ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
        .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? and " + ContactsContract.Data.MIMETYPE + "=?", new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE})
        .build());

    ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
        .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? and " + ContactsContract.Data.MIMETYPE + "=?", new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE})
        .build());

    ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
        .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? and " + ContactsContract.Data.MIMETYPE + "=?", new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE})
        .build());
    try {
      ContentProviderResult[] results = AppController.getInstance().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
      Cursor cursor = AppController.getInstance().getContentResolver().query(ContactsContract.Data.CONTENT_URI,null,
          ContactsContract.RawContacts.Data.RAW_CONTACT_ID +" = ? and "+ContactsContract.RawContacts.Data.MIMETYPE +" = ? ",
          new String[]{ String.valueOf(rawContactId), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE}, null);

      if(cursor.moveToNext())
      {
        insertNewContact(mProfile, rawContactId);
      }else{
        insertNewContact(mProfile, 0);
      }

    }catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private static ContentProviderOperation.Builder addRawContactIdToOps(ContentProviderOperation.Builder builder , int rawContactId)
  {
    if(rawContactId == 0)
      builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
    else
      builder.withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

    return builder;
  }

  public static String insertNewContact(ProfileDTO mProfile, int rawContactId) {

    List<UserProfileAttribute> attr = mProfile.getUserProfileAttributes();
    List<UserProfileAddress> addr = mProfile.getUserProfileAddresses();
    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    int rawContactInsertIndex = rawContactId;

    if(rawContactId == 0) {
      ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
          .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
          .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
    }

    String name = mProfile.getUserProfile().getFirstName() + " " + mProfile.getUserProfile().getLastName();
    name = name +" [Pintact]";

    if(rawContactId == 0) {

      ops.add(ContentProviderOperation
          .newInsert(ContactsContract.Data.CONTENT_URI)
          .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID,
              rawContactInsertIndex)
          .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
          .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
          .build());
    }else{
      ops.add(ContentProviderOperation
          .newUpdate(ContactsContract.Data.CONTENT_URI)
          .withSelection(ContactsContract.RawContacts.Data.RAW_CONTACT_ID + " = ? and " + ContactsContract.RawContacts.Data.MIMETYPE + " = ? ", new String[]{
              String.valueOf(rawContactInsertIndex), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
          .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
          .build());
    }


    for ( int i = 0; i < attr.size(); i ++ )
    {
      String value = attr.get(i).getValue();
      String label = attr.get(i).getLabel();
      if ( attr.get(i).getType() == AttributeType.PHONE_NUMBER ) {

        ops.add(addRawContactIdToOps(ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, value)
            .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, label)
            .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM), rawContactInsertIndex).build());

      }

      else if ( attr.get(i).getType() == AttributeType.EMAIL ) {
        ops.add(addRawContactIdToOps(ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, value)
            .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Email.LABEL, label)
            .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM), rawContactInsertIndex).build());

      }

      else if ( attr.get(i).getType() == AttributeType.SERVICE_ID) {
        ops.add(addRawContactIdToOps(ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Im.DATA, value)
            .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Im.LABEL, label)
            .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE )
            .withValue(ContactsContract.CommonDataKinds.Im.TYPE, ContactsContract.CommonDataKinds.Im.TYPE_CUSTOM), rawContactInsertIndex)
            .build());
      } else if ( attr.get(i).getType() == AttributeType.PRIVATE_NOTE && attr.get(i).getValue() != null && attr.get(i).getValue().trim().length() > 0) {
        ops.add(addRawContactIdToOps(ContentProviderOperation
            .newInsert(ContactsContract.Data.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Note.NOTE, attr.get(i).getValue()), rawContactInsertIndex)
            .build());
      }
    }

    ops.add(addRawContactIdToOps(ContentProviderOperation
        .newInsert(ContactsContract.Data.CONTENT_URI)
        .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, AppController.getInstance().getResources().getString(R.string.pintact_manage_note)), rawContactInsertIndex)
        .build());


    for ( int i = 0; i < addr.size(); i ++ )
    {
      ops.add(addRawContactIdToOps(ContentProviderOperation
          .newInsert(ContactsContract.Data.CONTENT_URI)
          .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE )
          .withValue(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, addr.get(i).getLabel())
          .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE )
          .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, addr.get(i).getAddressLine1())
          .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE )
          .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, addr.get(i).getCity())
          .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE )
          .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, addr.get(i).getPostalCode())
          .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE )
          .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, addr.get(i).getState())
          .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE )
          .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM), rawContactInsertIndex)
          .build());
    }

    try {
      ContentProviderResult[] res = AppController.getInstance().getContentResolver().applyBatch(
          ContactsContract.AUTHORITY, ops);
      System.out.println("Insert contact successfully.");
      if (res != null &&  res.length > 0 && res[0] != null && res[0].uri != null) {
        int raw_contact_id = Integer.parseInt(res[0].uri.getLastPathSegment());
        rawContactId = raw_contact_id;
      }

      AppService.insertImageInContact(mProfile, rawContactId);
      ContactTableDbInterface.getInstance().updateContactAddress(mProfile.getUserId(), String.valueOf(rawContactId));
      return String.valueOf(rawContactId);

    } catch (RemoteException e) {
      System.out.println("RemoteException");
      e.printStackTrace();
    } catch (OperationApplicationException e) {
      System.out.println("OperationApplicationException");
      e.printStackTrace();
    }


    return null;

  }

  public static String getContactIdOfRawContactid(String rawContactId)
  {
    Uri uri1 = ContactsContract.RawContacts.CONTENT_URI;
    String[] projection1    = new String[] { ContactsContract.RawContacts.CONTACT_ID };
    String conds = ContactsContract.RawContacts._ID + " = " + rawContactId;
    Cursor phones = AppController.getInstance().getContentResolver().query(uri1, projection1, conds, null, null);
    ArrayList<String> result = new ArrayList<String> ();
    if ( phones != null && phones.moveToNext()) {
      return phones.getString(phones.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
    }
    return null;
  }

  public static ArrayList<String> queryAllPhones(int id) {
    Uri uri1 = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    String[] projection1    = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
    String conds = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id;
    Cursor phones = AppController.getInstance().getContentResolver().query(uri1, projection1, conds, null, null);
    int indexNM = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
    ArrayList<String> result = new ArrayList<String> ();
    while ( phones != null && phones.moveToNext()) {
      result.add(phones.getString(indexNM));
      System.out.println("Found Phone:" + phones.getString(indexNM));
    }
    return result;
  }

  public static ArrayList<String> queryAllEmails(int id) {
    Uri uri1 = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
    String[] projection1    = new String[] { ContactsContract.CommonDataKinds.Email.ADDRESS };
    String conds = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id;
    Cursor phones = AppController.getInstance().getContentResolver().query(uri1, projection1, conds, null, null);
    int indexNM = phones.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
    ArrayList<String> result = new ArrayList<String> ();
    while ( phones != null && phones.moveToNext()) {
      result.add(phones.getString(indexNM));
      System.out.println("Found Email:" + phones.getString(indexNM));
    }
    return result;
  }

  public static ArrayList<String> queryAllUrls(int id) {
    Uri uri1 = ContactsContract.Data.CONTENT_URI;
    String[] projection1    = new String[] { ContactsContract.CommonDataKinds.Im.DATA };
    String conds = ContactsContract.Data.CONTACT_ID + " = " + id;
    Cursor phones = AppController.getInstance().getContentResolver().query(uri1, projection1, conds, null, null);
    int indexNM = phones.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA);
    ArrayList<String> result = new ArrayList<String> ();
    while ( phones != null && phones.moveToNext()) {
      result.add(phones.getString(indexNM));
      System.out.println("Found URL:" + phones.getString(indexNM));
    }
    return result;
  }

  public static ArrayList<String> queryAllNotes(int id) {
    Uri uri1 = ContactsContract.Data.CONTENT_URI;
    String[] projection1    = new String[] { ContactsContract.CommonDataKinds.Note.NOTE };
    String conds = ContactsContract.Data.CONTACT_ID + " = " + id;
    Cursor phones = AppController.getInstance().getContentResolver().query(uri1, projection1, conds, null, null);
    int indexNM = phones.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE);
    ArrayList<String> result = new ArrayList<String> ();
    while ( phones != null && phones.moveToNext()) {
      result.add(phones.getString(indexNM));
      System.out.println("Found Note:" + phones.getString(indexNM));
    }
    return result;
  }

  public static ArrayList<String> queryAllAddrs(int id) {
    Uri uri1 = ContactsContract.Data.CONTENT_URI;
    String[] projection1    = new String[] { ContactsContract.CommonDataKinds.StructuredPostal.STREET };
    String conds = ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = " + id;
    Cursor phones = AppController.getInstance().getContentResolver().query(uri1, projection1, conds, null, null);
    int indexNM = phones.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
    ArrayList<String> result = new ArrayList<String> ();
    while ( phones.moveToNext()) {
      result.add(phones.getString(indexNM));
      System.out.println("Found Address:" + phones.getString(indexNM));
    }
    return result;
  }

  static public class MyContactComp implements Comparator<ContactDTO> {

    private int mode = 0;

    public MyContactComp(int m) {
      mode = m;
    }

    @Override
    public int compare(ContactDTO c1, ContactDTO c2) {
      String s2FN = c2.getFirstName();
      String s1FN = c1.getFirstName();
      String s2LN = c2.getLastName();
      String s1LN = c1.getLastName();

      int value;

      s1FN = s1FN == null ? "" : s1FN;
      s1LN = s1LN == null ? "" : s1LN;
      s2FN = s2FN == null ? "" : s2FN;
      s2LN = s2LN == null ? "" : s2LN;
      if ( mode == 0 ) {
        value = s1FN.compareToIgnoreCase(s2FN);
        if ( value == 0 ) {
          return s1LN.compareToIgnoreCase(s2LN);
        }
      } else {
        if(s2LN.length() == 0)
            s2LN = s2FN;
          if(s1LN.length() == 0)
              s1LN = s1FN;

        value = s1LN.compareToIgnoreCase(s2LN);
        if ( value == 0 ) {
          return s1FN.compareToIgnoreCase(s2FN);
        }
      }
      return value;
    }
  }

  static class ContactsAsyncResponseHandler implements PostServiceExecuteTask
  {
    @Override
    public void run(int statusCode, String result) {
      try {
        if(statusCode == 200) {
          Gson gson = new GsonBuilder().create();
          Type collectionType = new TypeToken<Collection<ProfileDTO>>() {
          }.getType();
          ContactUpdateDTO contactUpdateDTO = gson.fromJson(result, ContactUpdateDTO.class);
          SingletonLoginData.getInstance().setTotalUnseenNoti(contactUpdateDTO.getTotalUnseenNotifications());
          addContact(contactUpdateDTO.getContacts());
          initContactList(true);
        }
      }catch (Exception e){
        System.out.println("Error fetching contact updates " +e.getMessage());
        e.printStackTrace();
      }
    }
  }

  static class LabelsAsyncResponseHandler implements PostServiceExecuteTask
  {
    @Override
    public void run(int statusCode, String result) {
      try {
        if(statusCode == 200) {
          Gson gson = new GsonBuilder().create();
          Type collectionType = new TypeToken<Collection<String>>() {
          }.getType();
          List<String> profiles = gson.fromJson(result, collectionType);
          addLabels(profiles);
        }
      }catch (Exception e){
        System.out.println("Error fetching contact updates " +e.getMessage());
        e.printStackTrace();
      }
    }
  }
  
  static class UserProfileChildLabelAsyncResponseHandler implements PostServiceExecuteTask
  {
    @Override
    public void run(int statusCode, String result) {
      try {
        if(statusCode == 200) {
          Gson gson = new GsonBuilder().create();
          Type collectionType = new TypeToken<Collection<UserProfileChildLabelDTO>>() {
          }.getType();
          List<UserProfileChildLabelDTO> labels = gson.fromJson(result, collectionType);
          for (UserProfileChildLabelDTO userProfileChildLabel : labels)
          addUserProfileChildLabels(userProfileChildLabel.getUserProfileChildType(),
              userProfileChildLabel.getAttributeType(), userProfileChildLabel.getLabel());
        }
      }catch (Exception e){
        System.out.println("Error fetching user profile child label updates " +e.getMessage());
        e.printStackTrace();
      }
    } 
  }

  static class ProfilesAsyncResponseHandler implements PostServiceExecuteTask
  {
    @Override
    public void run(int statusCode, String result) {
      try {
        if(statusCode == 200) {
          Gson gson = new GsonBuilder().create();
          Type collectionType = new TypeToken<Collection<ProfileDTO>>() {
          }.getType();
          List<ProfileDTO> profiles = gson.fromJson(result, collectionType);
          ProfileTableDbInterface.getInstance().addProfile(profiles);
        }
      }catch (Exception e){
        System.out.println("Error fetching contact updates " +e.getMessage());
        e.printStackTrace();
      }
    }
  }

  static class LocalContactMatchingHandler implements PostServiceExecuteTask{
      @Override
      public void run(int statusCode, String result) {
          try {
              if(statusCode == 200) {
                  Gson gson = new GsonBuilder().create();
                  Type collectionType = new TypeToken<Map<String, UserDTO>>() {
                  }.getType();
                  Map<String, UserDTO> resultMap = gson.fromJson(result, collectionType);
                  Map<String, Long> userMap = new HashMap<>();
                  for(Map.Entry<String, UserDTO> entry : resultMap.entrySet())
                  {
                      ContactTableDbInterface.getInstance().addIsPintact(entry.getKey(), entry.getValue().getId());
                      userMap.put(entry.getKey(), entry.getValue().getId());
                  }
                  SingletonLoginData.getInstance().setLocalContactSearchMap(userMap);
              }
          }catch (Exception e){
              System.out.println("Error fetching contact updates " +e.getMessage());
              e.printStackTrace();
          }
      }
  }

  static class MarkSeenNotificationCallback implements PostServiceExecuteTask{

    NotificationDTO notificationDTO;

    MarkSeenNotificationCallback(NotificationDTO notificationDTO){
      this.notificationDTO = notificationDTO;
    }
    @Override
    public void run(int statusCode, String result) {
      notificationDTO.setSeen(true);
      int count = SingletonLoginData.getInstance().getTotalUnseenNoti();
      SingletonLoginData.getInstance().setTotalUnseenNoti((count > 1)? count - 1 : 0);
    }
  }

  static class DownloadImageTask extends AsyncTask<Object, Void, Bitmap> {

    public DownloadImageTask() {
    }

    private String contactId;
    private ProfileDTO profileDTO;

    protected Bitmap doInBackground(Object... urls) {
      String urldisplay = (String)urls[0];
      contactId = (String)urls[1];
      profileDTO = (ProfileDTO)urls[2];

      Bitmap mIcon11 = null;
      try {
        InputStream in = new java.net.URL(urldisplay).openStream();
        mIcon11 = BitmapFactory.decodeStream(in);
      } catch (Exception e) {
        System.out.println("Error" + e.getMessage());
      }
      return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
      ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

      if(profileDTO.getUserProfile().getPathToImage() != null && result != null)
      {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.PNG, 100, baos);

        ContentResolver c = AppController.getInstance().getContentResolver();


        ContentValues values = new ContentValues();
        int photoRow = -1;
        String where = ContactsContract.Data.RAW_CONTACT_ID + " = " + contactId + " AND " + ContactsContract.Data.MIMETYPE + " = '" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";
        Cursor cursor = c.query(ContactsContract.Data.CONTENT_URI, null, where, null, null);
        int idIdx = cursor.getColumnIndexOrThrow(ContactsContract.Data._ID);
        if (cursor.moveToFirst()) {
          photoRow = cursor.getInt(idIdx);
        }
        cursor.close();


        values.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
        values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);
        values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, baos.toByteArray());
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);

        if (photoRow >= 0) {
          c.update(ContactsContract.Data.CONTENT_URI, values, ContactsContract.Data._ID + " = " + photoRow, null);
        } else {
          c.insert(ContactsContract.Data.CONTENT_URI, values);
        }

      }
    }
  }

  public static  void insertImageInContact(ProfileDTO mProfile, int contactId){
    if(mProfile.getUserProfile().getPathToImage() != null) {
      new DownloadImageTask().execute(mProfile.getUserProfile().getPathToImage(), String.valueOf(contactId), mProfile);
    }
  }
}
