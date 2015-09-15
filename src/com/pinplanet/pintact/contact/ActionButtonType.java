package com.pinplanet.pintact.contact;

import com.pinplanet.pintact.R;

/**
 * Created by pranab on 11/19/14.
 */
public enum ActionButtonType {

    INVITE(R.string.INVITE),
    ADD(R.string.ADD),
    CONNECT(R.string.CONNECT);

    private int labelResourceId;

    private ActionButtonType(int resId) {
        this.labelResourceId = resId;
    }

    public int getLabelResourcesId() {
        return labelResourceId;
    }
}
