package com.pinplanet.pintact.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.UserProfileAddress;

import java.io.StringWriter;

import de.late.widget.ViewListItem;

/**
 * Created by Dennis on 04.08.2014.
 */
public class ListViewItemShowAddress extends ViewListItem implements ListViewItemType {

    private String title;
    private String street;
    private String address;

    private String city,state,zip;

    private UserProfileAddress userProfileAddress;

    public ListViewItemShowAddress(String title, String street,String city,String state,String zip,
                                   UserProfileAddress userProfileAddress)
    {
        this.title=title;
        this.street=street;
        this.city=city;
        this.state=state;
        this.zip=zip;
        this.userProfileAddress = userProfileAddress;

        StringWriter sw=new StringWriter();

        boolean added=false;
        if(city!=null)
        {
            if(added)sw.append(", ");
            added=true;
            sw.append(city);
        }
        if(state!=null)
        {
            if(added)sw.append(", ");
            added=true;
            sw.append(state);
        }
        if(zip!=null)
        {
            if(added)sw.append(", ");
            added=true;
            sw.append(zip);
        }

        this.address=sw.toString();

        try {
            sw.close();
        }
        catch(Exception e){}


    }

    public String getTitle() {
        return title;
    }
    public String getStreet() {
        return street;
    }

    public String getZip() {
        return zip;
    }
    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public UserProfileAddress getUserProfileAddress() {
      return userProfileAddress;
    }

    public void setUserProfileAddress(UserProfileAddress userProfileAddress) {
      this.userProfileAddress = userProfileAddress;
    }

    @Override
    public ITEM_TYPE getListItemType() {
        return ITEM_TYPE.ADDRESS;
    }

    class ViewHandle {
        public TextView entryTitle;
        public TextView entryStreet,entryAddress;
    }

    @Override
    public View createView(Context context) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(getLayoutID(), null);

        final ViewHandle vh = new ViewHandle();
        vh.entryTitle = (TextView) v.findViewById(R.id.textViewEntryTitle);
        vh.entryStreet = (TextView) v.findViewById(R.id.textViewStreet);
        vh.entryAddress = (TextView) v.findViewById(R.id.textViewAddress);

        v.setTag(vh);

        return v;
    }

    @Override
    public void fillView(View v) {
        ViewHandle vh = (ViewHandle) v.getTag();
        vh.entryTitle.setText(title);
        vh.entryStreet.setText(street);
        vh.entryAddress.setText(address);
    }

    private void enableEditText(EditText ed,boolean enabled)
    {
        ed.setFocusable(enabled);
        ed.setEnabled(enabled);

        if( enabled )
        {ed.setFocusableInTouchMode(enabled);}
    }

    @Override
    public Class<?> getViewHandle() {
        return ViewHandle.class;
    }

    @Override
    public int getLayoutID() {
        return R.layout.list_view_profile_item_show_address;
    }


}
