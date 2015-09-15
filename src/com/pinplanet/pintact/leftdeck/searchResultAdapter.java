package com.pinplanet.pintact.leftdeck;

import java.util.List;

import com.pinplanet.pintact.R;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class searchResultAdapter  extends ArrayAdapter<searchResult> {

	  private final List<searchResult> list;
	  private final Activity context;

	  public searchResultAdapter(Activity context, List<searchResult> list) {
	    super(context, R.layout.search_left_result, list);
	    this.context = context;
	    this.list = list;
	  }

	  static class ViewHolder {
	    protected TextView textName;
	    protected TextView textTitle;
	  }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    View view = null;
	    if (convertView == null) {
	      LayoutInflater inflator = context.getLayoutInflater();
	      view = inflator.inflate(R.layout.search_left_result, null);
	      final ViewHolder viewHolder = new ViewHolder();
	      viewHolder.textName = (TextView) view.findViewById(R.id.searchName);
	      viewHolder.textTitle = (TextView) view.findViewById(R.id.searchTitle);
	      /*
	      viewHolder.checkbox
	          .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

	            @Override
	            public void onCheckedChanged(CompoundButton buttonView,
	                boolean isChecked) {
	              Model element = (Model) viewHolder.checkbox
	                  .getTag();
	              element.setSelected(buttonView.isChecked());

	            }
	          });
	          */
	      view.setTag(viewHolder);
	      //viewHolder.checkbox.setTag(list.get(position));
	    } else {
	      view = convertView;
	      //((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
	    }
	    ViewHolder holder = (ViewHolder) view.getTag();
	    holder.textName.setText(list.get(position).getName());
	    holder.textTitle.setText(list.get(position).getTitle());
	    return view;
	  }	
	
}
