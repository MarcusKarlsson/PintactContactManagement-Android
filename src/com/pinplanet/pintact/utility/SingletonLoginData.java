package com.pinplanet.pintact.utility;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.contact.ContactListChangeListner;
import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.ContactShareRequest;
import com.pinplanet.pintact.data.GroupDTO;
import com.pinplanet.pintact.data.NotificationDTO;
import com.pinplanet.pintact.data.PageDTO;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.SignupRequest;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.data.UserProfileChildType;
import com.pinplanet.pintact.data.UserSettings;
import com.pinplanet.pintact.data.service.AppService;

public class SingletonLoginData {

    String accessToken;
    UserDTO userData;
    GroupDTO curGroup;
    String currentLabel;
    ProfileDTO mergedProfile;
    UserDTO contactUser;
    ProfileDTO introducedProfile;
    List<NotificationDTO> originalNotifications;
    PageDTO<NotificationDTO> notifications;
    List<ProfileDTO> userProfiles;
    List<ContactDTO> contactList;
    List<ContactDTO> cloudContactList;
    Collection<ContactDTO> localContactList;
    HashMap<String, ContactDTO> allLocalContacts;
    Map<String, List<ContactDTO>> labelContactMap;
    List<GroupDTO> userGroups;
    List<GroupDTO> createdGroups;
    List<GroupDTO> joinedGroups;
    List<ContactDTO> groupContacts;
    List<String> labels;
    List<String> contactLabels;
    SignupRequest signupRequest;
    UserSettings userSettings;
    ContactShareRequest contactShareRequest;
    boolean isContactLoaded = false;
    boolean isStatusChanged = false;
    Long lastNotificationId = -1L;
    int totalUnseenNoti = 0;
    ContactDTO currentContactDto;
    public String deviceRegistered;
    private ContactListChangeListner contactListChangeListner;
    private Map<String, Long> localContactSearchMap;
    private HashSet<Long> contactuserList;
    public Date lastSyncTime = new Date();

    public boolean shouldSyncNow(){
        if(lastSyncTime != null) {
            Date currentTime = new Date();
            long time = currentTime.getTime() - lastSyncTime.getTime();
            if(time/(60 * 1000) > 15 ){
                return true;
            }else
                return false;
        }else
            return true;
    }

    public HashSet<Long> getContactuserList() {
        return contactuserList;
    }

    public void setContactuserList(HashSet<Long> contactuserList) {
        this.contactuserList = contactuserList;
    }

    public Map<String, Long> getLocalContactSearchMap() {
        return localContactSearchMap;
    }

    public void setLocalContactSearchMap(Map<String, Long> localContactSearchMap) {
        this.localContactSearchMap = localContactSearchMap;
    }

    public ContactListChangeListner getContactListChangeListner() {
        return contactListChangeListner;
    }

    public void setContactListChangeListner(ContactListChangeListner contactListChangeListner) {
        this.contactListChangeListner = contactListChangeListner;
    }

    public HashMap<String, ContactDTO> getAllLocalContacts() {
        return allLocalContacts;
    }

    public void setAllLocalContacts(HashMap<String, ContactDTO> allContacts) {
        this.allLocalContacts = allContacts;
    }

    Map<UserProfileChildType, Map<AttributeType, List<String>>> userProfileChildLabels;

    public ContactDTO getCurrentContactDto() {
        return currentContactDto;
    }

    public void setCurrentContactDto(ContactDTO currentContactDto) {
        this.currentContactDto = currentContactDto;
    }

    public void setContactUser(UserDTO contactUser) {
        this.contactUser = contactUser;
    }

    public UserDTO getContactUser() {
        return contactUser;
    }

    public List<NotificationDTO> getOriginalNotifications() {
        return originalNotifications;
    }

    public void setOriginalNotifications(List<NotificationDTO> originalNotifications) {
        this.originalNotifications = originalNotifications;
    }

    public int getTotalUnseenNoti() {
        return totalUnseenNoti;
    }

    public void setTotalUnseenNoti(int totalUnseenNoti) {
        this.totalUnseenNoti = totalUnseenNoti;
    }

    @SuppressLint("UseSparseArrays")
    TreeMap<Integer, Bitmap> profImages = new TreeMap<Integer, Bitmap>();

    static SingletonLoginData instance = null;

    private SingletonLoginData() {
        // initialize to avoid null point access
        contactList = new ArrayList<ContactDTO>();
        labelContactMap = new HashMap<String, List<ContactDTO>>();
        localContactList = new ArrayList<ContactDTO>();
        cloudContactList = new ArrayList<ContactDTO>();
        userProfiles = new ArrayList<ProfileDTO>();
        notifications = new PageDTO<NotificationDTO>();
        labels = new ArrayList<String>();
        contactLabels = new ArrayList<String>();
        userGroups = new ArrayList<GroupDTO>();
        createdGroups = new ArrayList<GroupDTO>();
        joinedGroups = new ArrayList<GroupDTO>();
        groupContacts = new ArrayList<ContactDTO>();
        userSettings = new UserSettings();
        userProfileChildLabels = new HashMap<UserProfileChildType, Map<AttributeType, List<String>>>();
    }

    public Long getLastNotificationId() {
        return lastNotificationId;
    }

    public void setLastNotificationId(Long lastNotificationId) {
        this.lastNotificationId = lastNotificationId;
    }

    public boolean getIsContactLoaded() {
        return isContactLoaded;
    }

    public void setIsContactLoaded(boolean b) {
        isContactLoaded = b;
    }

    public boolean getIsStatusChanged() {
        return isStatusChanged;
    }

    public void setIsStatusChanged(boolean b) {
        isStatusChanged = b;
    }

    public Bitmap getBitmap(int index) {
        return profImages.get(index);
    }

    public void setBitmap(int index, Bitmap bm) {
        profImages.put(index, bm);
    }

    public void removeBitmap(int index) {
        TreeMap<Integer, Bitmap> newProfImages = new TreeMap<Integer, Bitmap>();
        for (int key : profImages.keySet()) {
            if (key > index) {
                newProfImages.put(key - 1, profImages.get(key));
            } else if (key < index) {
                newProfImages.put(key, profImages.get(key));
            }
        }
        profImages = newProfImages;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String s) {
        accessToken = s;
    }

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettigns(UserSettings s) {
        if (s != null) userSettings = s;
        else userSettings = new UserSettings();
    }

    public ProfileDTO getMergedProfile() {
        return mergedProfile;
    }

    public void setMergedProfile(ProfileDTO d) {
        if (d != null) mergedProfile = d;
        else mergedProfile = new ProfileDTO();
    }

    public GroupDTO getCurGroup() {
        return curGroup;
    }

    public void setCurGroup(GroupDTO d) {
        if (d != null) curGroup = d;
        else curGroup = new GroupDTO();
    }

    public ProfileDTO getIntroducedProfile() {
        return introducedProfile;
    }

    public void setIntroducedProfile(ProfileDTO d) {
        if (d != null) introducedProfile = d;
        else introducedProfile = new ProfileDTO();
    }

    public UserDTO getUserData() {
        return userData;
    }

    public void setUserDTO(UserDTO d) {
        if (d != null) userData = d;
        else userData = new UserDTO();
    }

    public SignupRequest getSignupRequest() {
        return signupRequest;
    }

    public void setSignupRequest(SignupRequest d) {
        if (d != null) signupRequest = d;
        else signupRequest = new SignupRequest();
    }

    public ContactShareRequest getContactShareRequest() {
        return contactShareRequest;
    }

    public void setContactShareRequest(ContactShareRequest d) {
        if (d != null) contactShareRequest = d;
        else contactShareRequest = new ContactShareRequest();
    }

    public PageDTO<NotificationDTO> getNotifications() {
        return notifications;
    }

    public void setNotifications(PageDTO<NotificationDTO> d) {
        if (d != null) notifications = d;
        else notifications = new PageDTO<NotificationDTO>();
    }

    public List<ProfileDTO> getUserProfiles() {
        if (userProfiles == null || userProfiles.size() == 0) {
            userProfiles = AppService.getProfiles();
        }
        return userProfiles;
    }

    public void setUserProfiles(List<ProfileDTO> d) {
        if (d != null) userProfiles = d;
        else userProfiles = new ArrayList<ProfileDTO>();
    }

    public List<ContactDTO> getContactList() {
        return contactList;
    }

    public void setContactList(List<ContactDTO> d) {
        if (d != null) contactList = d;
        else contactList = new ArrayList<ContactDTO>();
    }

    public Collection<ContactDTO> getLocalContactList() {
        return localContactList;
    }

    public void setLocalContactList(Collection<ContactDTO> d) {
        if (d != null) localContactList = d;
        else localContactList = new ArrayList<ContactDTO>();
    }

    public String getCurrentLabel() {
        return currentLabel;
    }

    public void setCurrentLabel(String d) {
        if (d != null) currentLabel = d;
        else currentLabel = new String();
    }

    public Map<String, List<ContactDTO>> getLabelContactMap() {
        if (labelContactMap == null || labelContactMap.isEmpty()) {
            for (ContactDTO contactDTO : contactList) {
                if (contactDTO.getLabels() != null) {
                    for (String label : contactDTO.getLabels()) {
                        if (!labelContactMap.containsKey(label)) {
                            labelContactMap.put(label, new ArrayList<ContactDTO>());
                        }
                        labelContactMap.get(label).add(contactDTO);
                    }
                }
            }
        }
        return labelContactMap;
    }

    public List<ContactDTO> getCloudContactList() {
        return cloudContactList;
    }

    public void setCloudContactList(List<ContactDTO> d) {
        if (d != null) cloudContactList = d;
        else cloudContactList = new ArrayList<ContactDTO>();
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> d) {
        if (d != null) labels = d;
        else labels = new ArrayList<String>();
    }

    public List<String> getContactLabels() {
        return contactLabels;
    }

    public void setContactLabels(List<String> d) {
        if (d != null) contactLabels = d;
        else contactLabels = new ArrayList<String>();
    }

    public List<GroupDTO> getGroups() {
        return userGroups;
    }

    public void setGroups(List<GroupDTO> d) {
        if (d != null) userGroups = d;
        else userGroups = new ArrayList<GroupDTO>();
    }

    public List<GroupDTO> getCreatedGroups() {
        return createdGroups;
    }

    public void setCreatedGroups(List<GroupDTO> d) {
        if (d != null) createdGroups = d;
        else createdGroups = new ArrayList<GroupDTO>();
    }

    public List<GroupDTO> getJoinedGroups() {
        return joinedGroups;
    }

    public void setJoinedGroups(List<GroupDTO> d) {
        if (d != null) joinedGroups = d;
        else joinedGroups = new ArrayList<GroupDTO>();
    }

    public List<ContactDTO> getGroupContacts() {
        return groupContacts;
    }

    public void setGroupContacts(List<ContactDTO> d) {
        if (d != null) {
            groupContacts = d;
            Collections.sort(groupContacts, new AppService.MyContactComp(SingletonLoginData.getInstance().getUserSettings().sort));
        } else {
            groupContacts = new ArrayList<ContactDTO>();
        }
    }

    public Map<UserProfileChildType, Map<AttributeType, List<String>>> getUserProfileChildLabels() {
        return userProfileChildLabels;
    }

    public void setUserProfileChildLabels(Map<UserProfileChildType, Map<AttributeType, List<String>>> d) {
        if (d != null) userProfileChildLabels = d;
        else
            userProfileChildLabels = new HashMap<UserProfileChildType, Map<AttributeType, List<String>>>();
    }

    public static SingletonLoginData checkInstance() {
        return instance;
    }

    public static SingletonLoginData getInstance() {
        if (instance == null)
            instance = new SingletonLoginData();

        return instance;
    }

    public ContactDTO getLocalContactDetail(String localContactId, Context context) {
        ContactDTO contactDTO = null;
        if (allLocalContacts != null) {
            contactDTO = null;//allLocalContacts.get(localContactId);
        }
        if (contactDTO == null) {
            contactDTO = UiControllerUtil.loadLocalContact(context, localContactId);
        }

        return contactDTO;
    }

    public void checkLoginDataLoaded() {
        if (userData == null || accessToken == null) {
            loadLoginData();
        }
    }

    public String getPostParam() {

        checkLoginDataLoaded();
        String stringUrl = "";
        try {
            Log.d("Debugging", "userId: " + userData.id);
            Log.d("Debugging", "accessToken: " + accessToken);
            stringUrl = "userId=" + URLEncoder.encode(userData.id.toString(), "UTF-8") +
                    "&accessToken=" + URLEncoder.encode(accessToken, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("wrong");
        }

        return stringUrl;
    }

    public void clean() {
        userData = new UserDTO();
        if (profImages != null)
            profImages.clear();
        if (userProfiles != null)
            userProfiles.clear();
        if (contactList != null)
            contactList.clear();
        if (labelContactMap != null)
            labelContactMap.clear();
        if (localContactList != null)
            localContactList.clear();
        if (cloudContactList != null)
            cloudContactList.clear();
        if (labels != null)
            labels.clear();
        if (contactLabels != null)
            contactLabels.clear();
        if (userGroups != null)
            userGroups.clear();
        if (createdGroups != null)
            createdGroups.clear();
        if (joinedGroups != null)
            joinedGroups.clear();
        if (groupContacts != null)
            groupContacts.clear();
        if (notifications != null)
            setNotifications(null);
        if (userProfileChildLabels != null)
            userProfileChildLabels.clear();

        accessToken = "";
    }

    public NotificationManager mNotificationManager;

    public void loadLoginData() {
        Context context = AppController.getInstance().getApplicationContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String mUserId = sharedPref.getString(context.getString(R.string.login_username), null);
        String mAccessToken = sharedPref.getString(context.getString(R.string.access_token), null);
        String userJson = sharedPref.getString(context.getString(R.string.login_user), null);

        if (userJson != null && mAccessToken != null && mUserId != null) {
            UserDTO user = new Gson().fromJson(userJson, UserDTO.class);
            SingletonLoginData.getInstance().setAccessToken(mAccessToken);
            SingletonLoginData.getInstance().setUserDTO(user);
            String deviceRegistered = sharedPref.getString("deviceRegistered", null);
            SingletonLoginData.getInstance().deviceRegistered = deviceRegistered;
        }
    }

}
