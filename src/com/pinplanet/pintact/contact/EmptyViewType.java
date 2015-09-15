package com.pinplanet.pintact.contact;

import com.pinplanet.pintact.R;

/**
 * Created by pranab on 11/19/14.
 */
public enum EmptyViewType {

    PINTACT(R.id.emptyPintactLayout),
    EMPTY(R.id.emptyLayout);

    private int viewId;

    private EmptyViewType(int viewId) {
        this.viewId = viewId;
    }

    public int getViewId() {
        return viewId;
    }
}
