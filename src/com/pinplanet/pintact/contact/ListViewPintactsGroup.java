package com.pinplanet.pintact.contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pinplanet.pintact.R;


import de.late.widget.ViewListItemSection;

/**
 * Created by Dennis on 18.08.2014.
 */
public class ListViewPintactsGroup extends ViewListItemSection {

  private String text;
  public ListViewPintactsGroup(String text)
  {
    super();
    this.text=text;
  }

  class ViewHandle {
    public TextView entryChar;
  }

    @Override
    public boolean isEnabled() {
        return false;
    }

  @Override
  public View createView(Context context) {
    LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = vi.inflate(getLayoutID(), null);

    final ViewHandle vh = new ViewHandle();
    vh.entryChar = (TextView) v.findViewById(R.id.textViewChar);

    v.setTag(vh);

    return v;
  }

  @Override
  public void fillView(View v) {
    ViewHandle vh = (ViewHandle) v.getTag();
    vh.entryChar.setText(text);
  }

  @Override
  public String getSectionString() {
    return text;
  }

  @Override
  public Class<?> getViewHandle() {
    return ViewHandle.class;
  }

  @Override
  public int getLayoutID() {
    return R.layout.list_view_pintact_group_header;
  }

}
