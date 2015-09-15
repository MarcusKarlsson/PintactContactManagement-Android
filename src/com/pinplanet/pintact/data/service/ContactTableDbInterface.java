package com.pinplanet.pintact.data.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.data.ContactDTO;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.utility.SingletonLoginData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ContactTableDbInterface {

    private final PintactDbHelper dbHelper;
    private static final String TAG = ContactTableDbInterface.class.getSimpleName();

    private static ContactTableDbInterface contactTableDbInterface;

    public ContactTableDbInterface(PintactDbHelper pintactDbHelper) {
        this.dbHelper = pintactDbHelper;
    }

    public static ContactTableDbInterface getInstance() {
        if (contactTableDbInterface == null) {
            contactTableDbInterface = new ContactTableDbInterface(PintactDbHelper.getInstance());
        }

        return contactTableDbInterface;
    }

    public void addContact(List<ContactDTO> contactDTOs) throws Exception {
        for (ContactDTO contactDTO : contactDTOs) {

            if (contactDTO.isAcontact()) {
                addContact(contactDTO);
            } else if (contactDTO.getContactUser().getId().toString().equals(SingletonLoginData.getInstance().getUserData().id)) {
                addContact(contactDTO);
                ProfileTableDbInterface.getInstance().addProfile(contactDTO.getSharedProfiles());
            } else {
                removeContact(contactDTO.getUserId());
            }

        }
    }

    public void addContact(ContactDTO contactDTO) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactTableContract.USER_ID, contactDTO.getUserId());
        contentValues.put(ContactTableContract.CREATED_AT, new Date().getTime());
        contentValues.put(ContactTableContract.UPDATED_AT, new Date().getTime());
        contentValues.put(ContactTableContract.CONTACT_DTO, new Gson().toJson(contactDTO));
        contentValues.put(ContactTableContract.FIRST_NAME, contactDTO.getFirstName());
        contentValues.put(ContactTableContract.LAST_NAME, contactDTO.getLastName());
        contentValues.put(ContactTableContract.PATH_IMAGE, contactDTO.getPathToImage());
        contentValues.put(ContactTableContract.TITLE, contactDTO.getSubtitle());
        contentValues.put(ContactTableContract.LABELS, new Gson().toJson(contactDTO.getLabels()));
        dbHelper.getWritableDatabase().insert(ContactTableContract.TABLE_NAME, null, contentValues);
    }

    public void removeContact(ContactDTO contactDTO) throws Exception {
        if (!contactDTO.isAcontact()) {
            removeContact(contactDTO.getUserId());
        }
    }

    public void removeContact(Long userId) throws Exception {
        dbHelper.getWritableDatabase().delete(ContactTableContract.TABLE_NAME, ContactTableContract.USER_ID + "=" + userId, null);
    }

    public void addContactAddress(Long userId, String rawAddressBookId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactTableContract.USER_ID, userId);
        contentValues.put(ContactTableContract.ADDRESS_BOOK_ID, rawAddressBookId);
        dbHelper.getWritableDatabase().insert(ContactTableContract.CONTACT_ADDRESS_TABLE_NAME, null, contentValues);
    }

    public void updateContactAddress(Long userId, String rawAddressBookId) {
        dbHelper.getWritableDatabase().delete(ContactTableContract.CONTACT_ADDRESS_TABLE_NAME, ContactTableContract.USER_ID + "=" + userId, null);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactTableContract.USER_ID, userId);
        contentValues.put(ContactTableContract.ADDRESS_BOOK_ID, rawAddressBookId);
        dbHelper.getWritableDatabase().insert(ContactTableContract.CONTACT_ADDRESS_TABLE_NAME, null, contentValues);
    }

    public Set<String> getAllInvitedContacts() {
        Set<String> feeds = new HashSet<String>();

        String selectQuery = "SELECT  * FROM " + InvitedEmailAddressContract.TABLE_NAME;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                feeds.add(cursor.getString(cursor.getColumnIndex(InvitedEmailAddressContract.EMAIL_ID)));
            } while (cursor.moveToNext());
        }
        return feeds;
    }

    public void addEmailsToInvitedList(List<String> emails) {
        for (String email : emails) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(InvitedEmailAddressContract.EMAIL_ID, email);
            dbHelper.getWritableDatabase().insert(InvitedEmailAddressContract.TABLE_NAME, null, contentValues);
        }
    }

    public Set<String> getAllPintactNativeContacts() {
        Set<String> feeds = new HashSet<String>();

        String selectQuery = "SELECT  * FROM " + ContactTableContract.CONTACT_ADDRESS_TABLE_NAME;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                feeds.add(cursor.getString(cursor.getColumnIndex(ContactTableContract.ADDRESS_BOOK_ID)));
            } while (cursor.moveToNext());
        }
        return feeds;
    }

    public String checkIfContactAddedToAddressBook(Long userId) {
        String selectQuery = "SELECT  * FROM " + ContactTableContract.CONTACT_ADDRESS_TABLE_NAME + " where " + ContactTableContract.USER_ID + " = " + userId;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            String rawAddressId = cursor.getString(cursor.getColumnIndex(ContactTableContract.ADDRESS_BOOK_ID));
            return rawAddressId;
        }
        return null;
    }

    public List<ContactDTO> getContacts(Long loggedInUserId) throws Exception {
        List<ContactDTO> feeds = new ArrayList<ContactDTO>();
        String selectQuery = "SELECT  " + ContactTableContract.USER_ID+", "+
                ContactTableContract.LABELS + ", " +
                ContactTableContract.FIRST_NAME + ", " +
                ContactTableContract.LAST_NAME + ", " + ContactTableContract.TITLE + ", " +
                ContactTableContract.PATH_IMAGE + " FROM " + ContactTableContract.TABLE_NAME + " where " + ContactTableContract.USER_ID + " != " + loggedInUserId;
        ;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            int userIdIndex = cursor.getColumnIndex(ContactTableContract.USER_ID);
            int firstNameIndex = cursor.getColumnIndex(ContactTableContract.FIRST_NAME);
            int lastNameIndex = cursor.getColumnIndex(ContactTableContract.LAST_NAME);
            int titleIndex = cursor.getColumnIndex(ContactTableContract.TITLE);
            int pathImageIndex = cursor.getColumnIndex(ContactTableContract.PATH_IMAGE);
            int labelsIndex = cursor.getColumnIndex(ContactTableContract.LABELS);
            do {
                Gson gson = new Gson();
                Type collectionType = new TypeToken<Collection<String>>() {}.getType();
                feeds.add(cursorToFeed(gson, cursor, firstNameIndex, lastNameIndex, titleIndex, pathImageIndex, userIdIndex, labelsIndex, collectionType));
            } while (cursor.moveToNext());
        }
        return feeds;
    }

    public ContactDTO getContact(Long userId){
        try {
            String selectQuery = "SELECT  * FROM " + ContactTableContract.TABLE_NAME + " where " + ContactTableContract.USER_ID + " = " + userId;

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                int c = cursor.getColumnIndex(ContactTableContract.CONTACT_DTO);
                return cursorToFeed(new Gson(), cursor, c);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public ContactDTO cursorToFeed(Gson gson, Cursor cursor, int contactDtoIndex) throws Exception {
        String firstName = cursor.getString(contactDtoIndex);

        ContactDTO contactDTO = gson.fromJson(firstName, ContactDTO.class);

        return contactDTO;
    }

    public ContactDTO cursorToFeed(Gson gson, Cursor cursor, int firstNameIndex, int lastNameIndex, int titleIndex, int pathImageIndex, int userIdIndex, int labelsIndex, Type collectionType) throws Exception {
        String firstName = cursor.getString(firstNameIndex);
        String lastName = cursor.getString(lastNameIndex);
        String title = cursor.getString(titleIndex);
        String pathImage = cursor.getString(pathImageIndex);
        String  labels = cursor.getString(labelsIndex);
        Long userId = cursor.getLong(userIdIndex);
        ContactDTO contactDTO = new ContactDTO();
        contactDTO.setFirstName(firstName);
        contactDTO.setLastName(lastName);
        contactDTO.setPathToImage(pathImage);
        contactDTO.setSubTitle(title);
        contactDTO.setUserId(userId);
        contactDTO.setAcontact(true);
        List<String> labelList = gson.fromJson(labels, collectionType);

        UserDTO contactUser = new UserDTO();
        contactUser.setId(userId);
        contactUser.setFirstName(firstName);
        contactUser.setLastName(lastName);
        contactUser.pathToImage = pathImage;

        contactDTO.setContactUser(contactUser);
        contactDTO.setLabels(labelList);
        return contactDTO;
    }

    public void addIsPintact(String nativeContactId, Long userId) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactTableContract.LOCAL_CONTACT_ID, nativeContactId);
        contentValues.put(ContactTableContract.USER_ID, userId);
        dbHelper.getWritableDatabase().insert(ContactTableContract.IS_PINTACT_TABLE_NAME, null, contentValues);
    }

    public Map<String, Long> loadIsPintact()
    {
        Map<String, Long> isPintactMap = new HashMap<>();

        String selectQuery = "SELECT  * FROM " + ContactTableContract.IS_PINTACT_TABLE_NAME;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                isPintactMap.put(cursor.getString(cursor.getColumnIndex(ContactTableContract.LOCAL_CONTACT_ID)),
                        cursor.getLong(cursor.getColumnIndex(ContactTableContract.USER_ID)));
            } while (cursor.moveToNext());
        }
        return isPintactMap;
    }

}
