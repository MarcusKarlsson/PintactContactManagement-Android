package com.pinplanet.pintact.profile;

/**
 * Created by Dennis on 13.08.2014.
 */
public interface ListViewItemType {

    public static enum ITEM_TYPE{PHONE,MOBILE_PHONE,EMAIL,SOCIAL,ADDRESS,LABEL};

    public ITEM_TYPE getListItemType();
}
