package com.pinplanet.pintact.contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pinplanet.pintact.R;

import java.util.List;
import java.util.Locale;

public class ContactInviteLVAdapter extends BaseAdapter {

  private Context mContext;
  List<ContactInviteActivity.InviteContactDTO> inviteContactDTOs;

  public ContactInviteLVAdapter(Context context, List<ContactInviteActivity.InviteContactDTO> inviteContactDTOs) {
    super();
    mContext = context;
    this.inviteContactDTOs = inviteContactDTOs;
  }

  public int getCount() {
    // return the number of records in cursor
    return inviteContactDTOs.size();
  }

  // getView method is called for each item of ListView
  public View getView(int position, View view, ViewGroup parent) {
    // inflate the layout for each item of listView
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    //view = inflater.inflate(R.layout.contact_invite_list, null);
      view = inflater.inflate(R.layout.list_view_item_contact_invite, null);

    ContactInviteActivity.InviteContactDTO inviteContactDTO = inviteContactDTOs.get(position);

    // get the reference of textViews
    TextView name = (TextView) view.findViewById(R.id.im_name);
    TextView email = (TextView) view.findViewById(R.id.im_email);
    
    name.setText(inviteContactDTO.name);
    email.setText(inviteContactDTO.email);
    
    {
      String[] nameArr = getFNLN(inviteContactDTO);
      String fn = nameArr[0];
      String ln = nameArr[1];

      char ab[] = "ab".toCharArray();

      if (fn == null || fn.length() < 1) {
        ab[0] = ab[1] = ln.charAt(0);
        fn = "";
      } else if (ln == null || ln.length() < 1) {
        ab[0] = ab[1] = fn.charAt(0);
        ln = "";
      } else {
        ab[0] = fn.charAt(0);
        ab[1] = ln.charAt(0);
      }

      String init = new String(ab);
      init = init.toUpperCase(Locale.US);
      TextView initIV = (TextView)view.findViewById(R.id.textViewInitial);
      initIV.setText(init);
    }

    // set image
    ListView lvContact = (ListView) parent;
    ImageView ivSelect = (ImageView) view.findViewById(R.id.im_check);
    if (lvContact.isItemChecked(position)) {
      ivSelect.setImageResource(R.drawable.circle_check_orange);
    } else {
      ivSelect.setImageResource(R.drawable.circle);
    }

    if(inviteContactDTO.alreadyInvited)
    {
      view.setEnabled(false);
    }

    return view;
  }

  public boolean isEnabled(int position)
  {
    ContactInviteActivity.InviteContactDTO inviteContactDTO = inviteContactDTOs.get(position);
    return !inviteContactDTO.alreadyInvited;
  }

  public Object getItem(int position) {
    return position;
  }

  public long getItemId(int position) {
    return position;
  }
  
  public String[] getFNLN(ContactInviteActivity.InviteContactDTO contactDTO) {
    String[] nameArr = new String[4];
    StringBuilder labels = new StringBuilder("");
    String name = contactDTO.name;
    String[] names = name.split("\\s+");
    String fn = "";
    String ln = "";
    if(names.length > 0)
      fn = names[0];

    if(names.length > 1)
      ln = names[1];

    nameArr[0] = (fn==null)? "" : fn;
    nameArr[1] = (ln == null)? "" : ln;
    nameArr[3] = labels.toString();

    return nameArr;
  }
}
