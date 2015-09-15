package com.pinplanet.pintact.contact;

import android.content.Context;
import android.util.Log;
import android.widget.Filter;

import com.pinplanet.pintact.data.ListableEntity;
import com.pinplanet.pintact.utility.SingletonLoginData;

import java.util.ArrayList;
import java.util.List;

import de.late.widget.SimpleViewListAdapter;

/**
 * Created by Dennis on 18.08.2014.
 */
public class SectionAdapter extends SimpleViewListAdapter {

  private static final String TAG = SectionAdapter.class.getName();

  private List<? extends ListableEntity> entities;

  public SectionAdapter(Context context, List<? extends ListableEntity> entities, initView listener) {
    super(context,listener);
    setFilter(new EntryFilter());
    this.entities = entities;
    createListItems(entities);
  }

  public void setEntities(List<? extends ListableEntity> entities) {
    this.entities = entities;
    createListItems(entities);
  }

  private void createListItems(List<? extends ListableEntity> entities) {

      Log.d(TAG,"createListItems() start entities:"+entities.size());
    try {
      char lastChar=' ';

      setNotifyOnChange(false);//speed up
      clear();

      for(ListableEntity entity : entities)
      {
        char first = ' ';
        String st;
        if (SingletonLoginData.getInstance().getUserSettings().sort == 0) {
            st = entity.getFirstName();
            if(st == null || st.length() ==0)
                st = entity.getLastName();
        } else {
            st = entity.getLastName();
            if(st == null || st.length() ==0)
                st = entity.getFirstName();
        }
          if(st != null && st.length() > 0)
          {
              first = Character.toUpperCase(st.charAt(0));
          }else{
              first = ' ';
          }
        if(lastChar != first)
        {
          add(new ListViewPintactsGroup(""+first));
          lastChar=first;
        }
        add(new ListViewPintactItem(entity));
      }

      notifyDataSetChanged();
    } catch (Exception e){
      e.printStackTrace();
    }

      Log.d(TAG,"createListItems() end");
  }
  
  public class EntryFilter extends Filter {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      FilterResults results = new FilterResults();
      if (constraint == null || constraint.length() == 0) {
        results.values = new ArrayList<ListableEntity>(entities);
        results.count = entities.size();
        return results;
      } else {
        ArrayList<ListableEntity> entryList = new ArrayList<ListableEntity>();
        constraint = constraint.toString().toLowerCase();

        for (int i = 0; i < entities.size(); i++) {
          String[] namsArr = getFNLNCN(entities.get(i));
          String fn = namsArr[0];
          String ln = namsArr[1];
          String cn = namsArr[2];
          String[] tokens = constraint.toString().split("\\s+");

          String name = fn + " " + ln + " " + cn + " ";
          boolean found = true;
          for (int j = 0; j < tokens.length; j++) {
            if (tokens[j].length() > 0) {
              if (!(fn.toLowerCase().startsWith(tokens[j]) ||
                  ln.toLowerCase().startsWith(tokens[j]) ||
                  cn.toLowerCase().startsWith(tokens[j]))) {
                found = false;
                break;
              }
            }
          }
          if (found) {
            entryList.add(entities.get(i));
            Log.d(TAG, "Found " + name);
          }
        }

        results.values = entryList;
        results.count = entryList.size();
      }
      return results;
    }


    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      if (results.count == 0) {
        Log.d(TAG,"results.count == 0");
        //createListItems(new ArrayList<ContactDTO>());
        notifyDataSetInvalidated();
      } else {
        Log.d(TAG,"results.count != 0");
        createListItems( (ArrayList<ListableEntity>) results.values );
      }
    }
  }

  public String[] getFNLNCN(ListableEntity entity) {
    String[] nameArr = new String[4];
    
    String fn = entity.getFirstName();
    String ln = entity.getLastName();
    String cn = entity.getSubtitle();

    nameArr[0] = (fn==null)? "" : fn;
    nameArr[1] = (ln == null)? "" : ln;
    nameArr[2] = (cn == null)? "" : cn;

    return nameArr;
  }

}

