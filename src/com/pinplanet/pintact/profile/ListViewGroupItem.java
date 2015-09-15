package com.pinplanet.pintact.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.pinplanet.pintact.R;

import de.late.widget.ViewListItemGroup;


/**
 * Created by Dennis on 02.09.2014.
 */
public class ListViewGroupItem extends ViewListItemGroup {

    private int iconResId;

    public ListViewGroupItem(int iconResId) {
        super();
        this.iconResId=iconResId;

    }

    class ViewHandle {
        public ImageView iconImage;
        public CheckBox checkBox;
        public ToggleButton toggleButton;
    }


    @Override
    public View createView(Context context) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(getLayoutID(), null);

        final ViewHandle vh = new ViewHandle();
        vh.checkBox = (CheckBox) v.findViewById(R.id.checkBoxHeader);
        vh.iconImage = (ImageView)v.findViewById(R.id.imageViewHeader);
        vh.toggleButton = (ToggleButton)v.findViewById(R.id.toggleButtonEditNote);

        v.setTag(vh);

        return v;
    }

    @Override
    public void fillView(View v) {
        ViewHandle vh = (ViewHandle) v.getTag();

        vh.checkBox.setChecked(isExpanded());
        vh.toggleButton.setChecked(isEditMode());
        vh.iconImage.setImageResource(iconResId);
    }

    @Override
    public Class<?> getViewHandle() {
        return ViewHandle.class;
    }

    @Override
    public int getLayoutID() {
        return R.layout.list_view_profile_group_header;
    }
}
