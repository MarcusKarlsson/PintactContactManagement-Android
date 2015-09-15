package com.pinplanet.pintact.data.service;

import android.provider.BaseColumns;

public interface ContactTableContract extends BaseColumns{

  public static final String USER_ID = "user_id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String PATH_IMAGE = "path_image";
    public static final String TITLE = "title";
    public static final String LABELS = "labels";
  public static final String CONTACT_DTO = "contact_dto";
  public static final String TABLE_NAME = "contacts";
  public static final String  CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";
  public static final String ADDRESS_BOOK_ID = "address_book_id";

  public static final String CONTACT_ADDRESS_TABLE_NAME = "contacts_address_rel";
    public static final String LOCAL_CONTACT_ID = "local_contact_id";

    public static final String IS_PINTACT_TABLE_NAME = "is_pintact";

}
