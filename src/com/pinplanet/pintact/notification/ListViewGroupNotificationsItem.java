package com.pinplanet.pintact.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pinplanet.pintact.R;

import de.late.widget.ViewListItemGroup;


/**
 * Created by Dennis on 02.10.2014.
 */
public class ListViewGroupNotificationsItem extends ViewListItemGroup {

    private static final String TAG = ListViewGroupNotificationsItem.class.getName();

    private boolean showRight=false;
    private int resGroupText;
    private String groupText;

    public ListViewGroupNotificationsItem(int resGroupText) {
        this.resGroupText=resGroupText;
    }

    public ListViewGroupNotificationsItem(String groupText) {
        this.groupText=groupText;
    }


    public ListViewGroupNotificationsItem(int resGroupText,boolean showRight) {
        this(resGroupText);
        this.showRight=showRight;
    }

    class ViewHandle {
        public TextView leftGroupText,rightGroupText;
    }


    @Override
    public View createView(Context context) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(getLayoutID(), null);

        final ViewHandle vh = new ViewHandle();
        vh.leftGroupText = (TextView) v.findViewById(R.id.textViewLeftGroupText);
        vh.rightGroupText = (TextView)v.findViewById(R.id.textViewRightGroupText);

        v.setTag(vh);

        return v;
    }

    @Override
    public void fillView(View v) {
        ViewHandle vh = (ViewHandle) v.getTag();

        if(groupText!=null)
            vh.leftGroupText.setText(groupText);
        else
            vh.leftGroupText.setText(resGroupText);

        vh.rightGroupText.setVisibility(showRight?View.VISIBLE:View.GONE);
    }

    @Override
    public Class<?> getViewHandle() {
        return ViewHandle.class;
    }

    @Override
    public int getLayoutID() {
        return R.layout.list_view_notification_group;
    }
}
