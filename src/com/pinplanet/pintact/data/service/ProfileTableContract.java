package com.pinplanet.pintact.data.service;

import android.provider.BaseColumns;

/**
 * Created by pranab on 7/28/14.
 */
public interface ProfileTableContract extends BaseColumns{

  public static final String PROFILE_ID = "profile_id";
  public static final String PROFILE_DTO = "profile_dto";
  public static final String TABLE_NAME = "profiles";
  public static final String  CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";

}
