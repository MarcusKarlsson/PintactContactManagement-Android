package com.pinplanet.pintact.label;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinplanet.pintact.R;

import de.late.widget.ViewListItem;


/**
 * Created by Dennis on 16.10.2014.
 */
public class ListItemSelectableLabel extends ViewListItem {

    private static final String TAG = ListItemSelectableLabel.class.getName();

    private static final byte STATE_LABEL_VISIBLE=1;
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ListItemSelectableLabel(String title, boolean isSelected) {
        this.title = title;
        setSelected(isSelected);
    }

    class ViewHandle {
        public TextView entryTitle;
        public TextView labelDelete;
        public ImageView deleteImage;
        public ImageView entryCheckImage;
    }

    @Override
    public View createView(Context context) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(getLayoutID(), null);

        final ViewHandle vh = new ViewHandle();
        vh.entryTitle = (TextView) v.findViewById(R.id.textViewEntryTitle);
        vh.labelDelete = (TextView) v.findViewById(R.id.textViewDeleteLabel);
        vh.deleteImage = (ImageView)v.findViewById(R.id.imageView);
        vh.entryCheckImage = (ImageView)v.findViewById(R.id.imageViewChooserCheck);
        v.setTag(vh);

        return v;
    }


    @Override
    public void fillView(View v) {
        ViewHandle vh = (ViewHandle) v.getTag();
        vh.entryTitle.setText(title);
        vh.entryTitle.setEnabled(isEditMode());

        vh.entryCheckImage.setImageResource(isSelected() ? R.drawable.pin_check_ok:R.drawable.pin_check_empty);
        vh.labelDelete.setVisibility(isLabelDeleteVisible() ? View.VISIBLE:View.INVISIBLE);
        vh.deleteImage.setVisibility(isDeleteImageVisible() ? View.VISIBLE: View.GONE);
    }

    public boolean isDeleteImageVisible()
    {
        return (isEditMode() && !getStateBitsSet(STATE_LABEL_VISIBLE));
    }

    public boolean isLabelDeleteVisible()
    {
        return (isEditMode() && getStateBitsSet(STATE_LABEL_VISIBLE));
    }

    public void setLabelDeleteVisible(boolean enabled)
    {
        setStateBits(enabled,STATE_LABEL_VISIBLE);
    }

    @Override
    public Class<?> getViewHandle() {
        return ViewHandle.class;
    }

    @Override
    public int getLayoutID() {
        return R.layout.list_view_label_selectable_item;
    }

}
