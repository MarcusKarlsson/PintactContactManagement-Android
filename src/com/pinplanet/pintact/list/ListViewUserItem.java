package com.pinplanet.pintact.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.UserDTO;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.UiControllerUtil;

import de.late.widget.ViewListItem;

/**
 * Created by Dennis on 08.10.2014.
 */
public class ListViewUserItem extends ViewListItem {

  private UserDTO user;

  public ListViewUserItem(UserDTO user){
    this.user = user;
  }

  public UserDTO getUser() {
    return user;
  }

  class ViewHandle {
    public TextView entryName, subTitle, textViewAction, textViewInitial;
    public CustomNetworkImageView imageView;
  }

  @Override
  public View createView(Context context) {
    LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = vi.inflate(getLayoutID(), null);

    final ViewHandle vh = new ViewHandle();
    vh.entryName = (TextView) v.findViewById(R.id.textViewName);
    vh.subTitle = (TextView) v.findViewById(R.id.textViewSubTitle);
    vh.imageView = (CustomNetworkImageView) v.findViewById(R.id.imageViewPicture);
    vh.textViewAction  = (TextView) v.findViewById(R.id.textViewAction);
    vh.textViewInitial = (TextView) v.findViewById(R.id.textViewInitial);

    v.setTag(vh);

    return v;
  }

  @Override
  public void fillView(View v) {
    ViewHandle vh = (ViewHandle) v.getTag();
    vh.entryName.setText(user.getName());

    String firstName = user.getFirstName();
    String lastName = user.getLastName();

    //vh.textViewAction.setVisibility(entity.isShowAction() ? View.VISIBLE : View.INVISIBLE);

    vh.textViewInitial.setText(UiControllerUtil.getInitial(firstName , lastName));

    String cn = user.getPin();
    vh.subTitle.setText(cn == null ? "" : cn);

    //Log.d("XXX","getPathToImage() :"+contact.getSharedProfiles().get(0).getUserProfile().getPathToImage());
    vh.imageView.setImageUrl(user.getPathToImage(), AppController.getInstance().getImageLoader());
  }


    @Override
  public Class<?> getViewHandle() {
    return ViewHandle.class;
  }

  @Override
  public int getLayoutID() {
    return R.layout.list_view_pintact_item;
  }
}
