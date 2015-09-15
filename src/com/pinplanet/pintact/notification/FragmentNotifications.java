package com.pinplanet.pintact.notification;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.LeftDeckActivity;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.EventType;
import com.pinplanet.pintact.data.NotificationDTO;
import com.pinplanet.pintact.data.PageDTO;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.PostServiceExecuteTask;
import com.pinplanet.pintact.utility.RestServiceAsync;
import com.pinplanet.pintact.utility.SingletonLoginData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.late.gui.ExpandableListViewAdvanced;
import de.late.widget.ExpandableViewListAdapter;
import de.late.widget.ViewListItem;

/**
 * Created by Dennis on 02.10.2014.
 *
 */
public class FragmentNotifications extends Fragment {

    private static final String TAG = FragmentNotifications.class.getName();

    private static final int GROUP_ID_PINVITES=1000,GROUP_ID_UPDATES=1001;

    private static final int SECTIONS=2,PAGESIZE=100;
    private int currentPage=1;
    private boolean isLoading=false;

    private ExpandableViewListAdapter adapterNotifications;
    private ExpandableListViewAdvanced lvNotifications;
    private SwipeRefreshLayout swipeLayout;

    private Handler mainHandler;

    public FragmentNotifications() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainHandler = new Handler(AppController.getInstance().getApplicationContext().getMainLooper());
        updateData();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (swipeLayout!=null) {
            swipeLayout.setRefreshing(false);
            swipeLayout.destroyDrawingCache();
            swipeLayout.clearAnimation();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_notifications, container, false);

        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipeLayout);
        swipeLayout.setColorSchemeResources(R.color.PINTACT_BLUE_COLOR,R.color.PINTACT_GREEN_COLOR,R.color.PINTACT_ORANGE_COLOR,R.color.PINTACT_RED_COLOR);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData();
            }
        });

        ((MyActivity)getActivity()).showLeftImage(R.drawable.actionbar_menu);
        ((MyActivity)getActivity()).addLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LeftDeckActivity)getActivity()).toggleDrawer();
            }
        });

        lvNotifications=(ExpandableListViewAdvanced)v.findViewById(R.id.listViewNotifications);

        adapterNotifications=new ExpandableViewListAdapter(getActivity(),new ExpandableViewListAdapter.initView() {
            @Override
            public void initGroup(ExpandableViewListAdapter ExpandableViewListAdapter, int groupPosition, View convertView)
            {
                convertView.findViewById(R.id.textViewRightGroupText).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<ViewListItem> list = adapterNotifications.getGroupWithKey(GROUP_ID_UPDATES).getChilds();
                        //last item getNotificationId is needed for the call
                        NotificationDTO notificationDTO=((ListViewNotificationItem)list.get(list.size()-1)).getNotification();
                        AppService.markAllNotificationMarked(getActivity(),SingletonLoginData.getInstance().getLastNotificationId(), new NotificationAllClear());
                    }
                });
            }

            @Override
            public void initChild(ExpandableViewListAdapter expandableViewListAdapter, int i, int i2, View view) {}
        });

        lvNotifications.setAdapter(adapterNotifications);
        lvNotifications.setIgnoreGroupClicks(true);

        lvNotifications.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView absListView,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
                //Log.i(TAG,"onScroll firstVisibleItem:"+firstVisibleItem+" visibleItemCount:"+visibleItemCount+" totalItemCount:"+totalItemCount+" calc:"+(firstVisibleItem+visibleItemCount)+" isLoading:"+isLoading);
                if(!isLoading && firstVisibleItem+visibleItemCount==totalItemCount)
                {
                    //TODO: load more list date here...added some vars already:
//                    private static final int SECTIONS=2,PAGESIZE=100;
//                    private int currentPage=0;
                    if(currentPage<totalItemCount/PAGESIZE)
                    updateData();
                }
            }
        });

        lvNotifications.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int group, int child, long l) {
                ListViewNotificationItem item=(ListViewNotificationItem)adapterNotifications.getChild(group,child);
                if(item!=null)
                {
                    NotificationDTO notificationDTO = item.getNotification();

                    FragmentManager fragmentManager =  getActivity().getFragmentManager();
                    FragmentTransaction trans= fragmentManager.beginTransaction();
                    trans.addToBackStack(null);

                    Fragment frg=FragmentNotificationDetails.getInstance(item.getSourcePosition(),notificationDTO);
                    trans.setCustomAnimations( R.anim.fragment_left_in, R.anim.fragment_right_out, R.anim.fragment_right_in,R.anim.fragment_left_out);
                    trans.replace(R.id.content_frame, frg);
                    trans.commit();
                    fragmentManager.executePendingTransactions();

                    return true;
                }

                return false;
            }
        });


      String path = "/api/sortedNotifications.json?pageSize="+PAGESIZE+"&" + SingletonLoginData.getInstance().getPostParam();
      new RestServiceAsync(new PostServiceExecuteTask() {
        @Override
        public void run(int statusCode, String result) {
          if(statusCode == 200) {
            updateList();
          }
        }
      }, this.getActivity() , true).execute(path, "", "GET");

        return v;
    }


    public void updateData()
    {
        Log.d(TAG,"updateData()");
        AppService.fetchMoreNotifications(new PostServiceExecuteTask() {
            @Override
            public void run(int statusCode, String result) {

                Type collectionType = new TypeToken<PageDTO<NotificationDTO>>() {}.getType();
                Gson gson = new GsonBuilder().create();
                final PageDTO<NotificationDTO> notifications = gson.fromJson(result, collectionType);
                SingletonLoginData.getInstance().setNotifications(notifications);

                updateList();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    public void updateList()
    {
        Log.d(TAG,"updateList()");
        isLoading=true;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Activity act = getActivity();
                if(act!= null)
                    act.setProgressBarIndeterminateVisibility(true);
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (adapterNotifications != null) {
                    adapterNotifications.setNotifyOnChange(false);
                    adapterNotifications.clear();

                    final PageDTO<NotificationDTO> mData = SingletonLoginData.getInstance().getNotifications();

                    ListViewGroupNotificationsItem groupPinvites = new ListViewGroupNotificationsItem(R.string.noti_list_pinvites);
                    ListViewGroupNotificationsItem groupUpdates = new ListViewGroupNotificationsItem(R.string.noti_list_updates, true);

                    //Log.d(TAG,"updateList() notfication count:"+mData.getData().size());

                    if (mData.getData() != null) {
                        int position = 0;
                        Long lastNotificationId = 0L;
                        for (NotificationDTO n : mData.getData()) {
                            EventType eType = n.getEventType();

                            //Log.d(TAG, "eType:"+eType);

                            ListViewNotificationItem item = new ListViewNotificationItem(n);
                            item.setSourcePosition(position++);

                            //PINVITES, group
                            if (eType == EventType.CONTACT_INVITE || eType == EventType.CONTACT_INTRODUCE) {
                                //Log.d(TAG,"updateList() add PINVITES item!"+groupPinvites.getChilds().size());
                                groupPinvites.getChilds().add(item);
                                //adapterNotifications.add(new ListViewGroupNotificationsItem(R.string.))
                            } else//"UPDATES", childs
                            {
                                //Log.d(TAG,"updateList() add UPDATES item!"+groupUpdates.getChilds().size());
                                if (lastNotificationId == 0L)
                                    lastNotificationId = n.getNotificationId();

                                groupUpdates.getChilds().add(item);
                            }
                        }
                        if (SingletonLoginData.getInstance().getLastNotificationId() == null || SingletonLoginData.getInstance().getLastNotificationId() < lastNotificationId) {
                            SingletonLoginData.getInstance().setLastNotificationId(lastNotificationId);
                        }

                    }
                    if (groupPinvites.getSize() > 0)
                        adapterNotifications.putGroup(GROUP_ID_PINVITES, groupPinvites);

                    if (groupUpdates.getSize() > 0)
                        adapterNotifications.putGroup(GROUP_ID_UPDATES, groupUpdates);

                    Log.d(TAG, "updateList() sizes end:" + groupPinvites.getSize() + "/" + groupUpdates.getSize());

                    currentPage = (groupPinvites.getSize() + groupUpdates.getSize()) / PAGESIZE;
                    isLoading = false;

                    adapterNotifications.notifyDataSetChanged();
                    lvNotifications.expandAllGroups();

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getActivity().setProgressBarIndeterminateVisibility(false);
                        }
                    });

                }
            }
        });
    }

  @Override
  public void onResume() {
    updateList();
    adapterNotifications.notifyDataSetChanged();
    super.onResume();
  }

  class NotificationAllClear implements PostServiceExecuteTask
  {

    @Override
    public void run(int statusCode, String result) {
      try {
        if(statusCode == 200) {
          Gson gson = new GsonBuilder().create();
          Map lastNotiData = gson.fromJson(result, Map.class);

          String notiCount = (String)lastNotiData.get("notificationCount");
          if(notiCount != null)
            SingletonLoginData.getInstance().setTotalUnseenNoti(Integer.parseInt(notiCount));
          else
            SingletonLoginData.getInstance().setTotalUnseenNoti(0);

          List<NotificationDTO> noti = SingletonLoginData.getInstance().getNotifications().getData();
          for(NotificationDTO notificationDTO : noti)
          {
            if(!(notificationDTO.getEventType().equals(EventType.CONTACT_INTRODUCE) || notificationDTO.getEventType().equals(EventType.CONTACT_INVITE))) {
                notificationDTO.setSeen(true);
            }
          }

          Handler mainHandler = new Handler(AppController.getInstance().getApplicationContext().getMainLooper());

          Runnable myRunnable = new Runnable(){
            public void run(){
              FragmentNotifications.this.updateList();
              FragmentNotifications.this.adapterNotifications.notifyDataSetChanged();
            }
          };
          mainHandler.post(myRunnable);


        }
      }catch (Exception e){
        System.out.println("Error fetching contact updates " +e.getMessage());
        e.printStackTrace();
      }
    }
  }
}




/**
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INTRODUCE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_DELETE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_DELETE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INTRODUCED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INTRODUCED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:null
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:null
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:null
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_CREATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_JOINED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_CREATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_JOINED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_CREATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_JOINED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_CREATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_JOINED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_CREATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_JOINED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_CREATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INTRODUCED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INTRODUCED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INTRODUCED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INTRODUCED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INTRODUCED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INTRODUCED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_JOINED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:GROUP_CREATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_DELETE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITE_ACCEPTED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INVITED
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:UPDATE_PROFILE_SHARE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:PROFILE_UPDATE
 10-02 15:52:02.359  22127-22257/com.pintact D/com.pintact.notification.FragmentNotifications﹕ eType:CONTACT_INTRODUCED
 * */