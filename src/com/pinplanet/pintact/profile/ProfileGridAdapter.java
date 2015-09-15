package com.pinplanet.pintact.profile;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ProfileDTO;
import com.pinplanet.pintact.utility.CustomNetworkImageView;
import com.pinplanet.pintact.utility.SingletonLoginData;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author manish.s
 *
 */
public class ProfileGridAdapter extends ArrayAdapter<ProfileDTO> {
 Context context;
 int layoutResourceId;
 List<ProfileDTO> data = new ArrayList<ProfileDTO>();

 public ProfileGridAdapter(Context context, int layoutResourceId,
   List<ProfileDTO> data) {
  super(context, layoutResourceId, data);
  this.layoutResourceId = layoutResourceId;
  this.context = context;
  this.data = data;
 }

 @Override
 public View getView(int position, View convertView, ViewGroup parent) {

	   LayoutInflater inflater = ((Activity) context).getLayoutInflater();
	   View row = inflater.inflate(layoutResourceId, parent, false);
	
	   TextView title = (TextView) row.findViewById(R.id.pm_name);
	   TextView name = (TextView) row.findViewById(R.id.pm_profile_id);
       CustomNetworkImageView image = (CustomNetworkImageView) row.findViewById(R.id.pm_image);

	   ProfileDTO item = data.get(position);
	   title.setText(item.getUserProfile().getName());
	   name.setText(SingletonLoginData.getInstance().getUserData().pin);
	   
	   //Bitmap bm = SingletonLoginData.getInstance().getBitmap(position);
	   if ( item.getUserProfile().getPathToImage() != null &&
			item.getUserProfile().getPathToImage().length() > 0)
	   {
           image.setImageUrl(item.getUserProfile().getPathToImage(), AppController.getInstance().getImageLoader());
           image.setScaleType(ImageView.ScaleType.CENTER_CROP);
		   //loadImage(position, item.getUserProfile().getPathToImage(), image);
	   }else {
           //image.setImageResource(R.drawable.silhouette);
           image.setLocalBitmapFromResource(context,R.drawable.silhouette);
       }
	   
	   return row;
 }
 
 public void loadImage(int index, String photo_url_str, ImageView profile_photo) {
	 System.out.println("Loading image from " + photo_url_str);
	 new DownloadProfileImageTask(profile_photo).execute(photo_url_str, Integer.toString(index));
 }
 

 }
