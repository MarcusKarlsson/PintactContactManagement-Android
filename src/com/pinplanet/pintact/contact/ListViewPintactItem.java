package com.pinplanet.pintact.contact;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ListableEntity;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.UiControllerUtil;

import de.late.widget.ViewListItem;

/**
 * Created by Dennis on 18.08.2014.
 */
public class ListViewPintactItem extends ViewListItem {

    private static final String TAG = ListViewPintactItem.class.getName();
    private ListableEntity entity;

  public ListViewPintactItem(ListableEntity entity){
    this.entity = entity;
  }

  public ListableEntity getEntity() {
    return entity;
  }

  class ViewHandle {
    public TextView entryName, subTitle, textViewAction, textViewInitial;
    public CustomNetworkImageView imageView;
    public ImageView imageViewPictureLocal;
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
    vh.imageViewPictureLocal = (ImageView)v.findViewById(R.id.imageViewPictureLocal);

    v.setTag(vh);

    return v;
  }

  @Override
  public void fillView(View v) {
    ViewHandle vh = (ViewHandle) v.getTag();
    vh.entryName.setText(entity.getName());

    String firstName = entity.getFirstName();
    String lastName = entity.getLastName();

    vh.textViewAction.setVisibility(entity.isShowAction() ? View.VISIBLE : View.INVISIBLE);
    vh.textViewAction.setText(entity.getActionLabel().getLabelResourcesId());

    vh.textViewInitial.setText(UiControllerUtil.getInitial(firstName , lastName));

    String cn = entity.getSubtitle();
    vh.subTitle.setText(cn == null ? "" : cn);

    //Log.d("XXX","getPathToImage() :"+contact.getSharedProfiles().get(0).getUserProfile().getPathToImage());

    if(entity.isLocalContact() && entity.getPathToImage() != null)
    {
      vh.imageView.setVisibility(View.GONE);
      vh.imageViewPictureLocal.setVisibility(View.VISIBLE);
        Uri uri=null;
        try {
            uri=Uri.parse(entity.getPathToImage());
        } catch (Exception e) {
            Crashlytics.setString("error ListViewPintactItem local contact image","firstName:"+firstName+" lastName:"+lastName+" image:"+entity.getPathToImage());
            Crashlytics.log("error ListViewPintactItem local contact image firstName:"+firstName+" lastName:"+lastName+" image:"+entity.getPathToImage());
            Log.d(TAG,"firstName:"+firstName+" lastName:"+lastName+" image:"+entity.getPathToImage());
            throw new RuntimeException(e);
        }
        vh.imageViewPictureLocal.setImageURI(uri);
    }else {
      vh.imageView.setVisibility(View.VISIBLE);
      vh.imageView.setImageUrl(entity.getPathToImage(), AppController.getInstance().getImageLoader());
      vh.imageViewPictureLocal.setVisibility(View.GONE);
    }
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
