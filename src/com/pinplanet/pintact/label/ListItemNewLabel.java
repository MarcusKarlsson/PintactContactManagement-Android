package com.pinplanet.pintact.label;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pinplanet.pintact.R;

import de.late.widget.ViewListItem;


/**
 * Created by Dennis on 16.10.2014.
 */
public class ListItemNewLabel extends ViewListItem {

    private static final String TAG = ListItemNewLabel.class.getName();

    public String getTextEntry() {
        return textEntry;
    }

    private String textEntry;

    public ListItemNewLabel() {
    }

    class ViewHandle {
        public EditText entryText;
        public TextView textViewAddLabel;
    }

    @Override
    public View createView(Context context) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(getLayoutID(), null);

        final ViewHandle vh = new ViewHandle();

        vh.entryText = (EditText) v.findViewById(R.id.textViewChooserEntry);
        vh.textViewAddLabel = (TextView)v.findViewById(R.id.textViewAddLabel);

        vh.entryText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                textEntry = editable.toString();
                if (textEntry.length() > 500) {
                    vh.entryText.setError("Value cannot exeed 500 characters");
                }
            }
        });

        v.setTag(vh);

        return v;
    }

    @Override
    public void fillView(View v) {
        ViewHandle vh = (ViewHandle) v.getTag();

        vh.entryText.setText(textEntry);
    }


    @Override
    public Class<?> getViewHandle() {
        return ViewHandle.class;
    }

    @Override
    public int getLayoutID() {
        return R.layout.list_view_label_add_item;
    }
}
