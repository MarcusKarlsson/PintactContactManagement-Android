package com.pinplanet.pintact.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.pinplanet.pintact.R;

import de.late.widget.ViewListItem;


/**
 * Created by Dennis on 06.10.2014.
 */
public class ListViewEditTextItem extends ViewListItem {

    private static final String TAG = ListViewEditTextItem.class.getName();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String text;

    public ListViewEditTextItem(String text) {
        super();
        this.text=text;
    }

    class ViewHandle {
        public EditText editTextView;
    }



    @Override
    public View createView(Context context) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(getLayoutID(), null);

        final ViewHandle vh = new ViewHandle();
        vh.editTextView = (EditText) v.findViewById(R.id.editTextView);

        v.setTag(vh);

        return v;
    }

    @Override
    public void fillView(View v) {
        ViewHandle vh = (ViewHandle) v.getTag();
        vh.editTextView.setText(text);
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
        return R.layout.list_view_edit_text_item;
    }
}
