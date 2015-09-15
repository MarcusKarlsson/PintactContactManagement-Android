package com.pinplanet.pintact.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pinplanet.pintact.R;

import de.late.widget.ViewListItem;


/**
 * Created by Dennis on 06.10.2014.
 */
public class ListViewTextItem extends ViewListItem {

    private static final String TAG = ListViewTextItem.class.getName();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String text;

    public ListViewTextItem(String text) {
        super();
        this.text=text;
    }

    class ViewHandle {
        public TextView textViewText;
    }


    @Override
    public View createView(Context context) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(getLayoutID(), null);

        final ViewHandle vh = new ViewHandle();
        vh.textViewText = (TextView) v.findViewById(R.id.textViewText);

        v.setTag(vh);

        return v;
    }

    @Override
    public void fillView(View v) {
        ViewHandle vh = (ViewHandle) v.getTag();
        vh.textViewText.setText(text);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public Class<?> getViewHandle() {
        return ViewHandle.class;
    }

    @Override
    public int getLayoutID() {
        return R.layout.list_view_text_item;
    }
}
