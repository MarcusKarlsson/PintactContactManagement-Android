package com.pinplanet.pintact;

import android.app.Application;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.pinplanet.pintact.chat.ChatDataWrapper;
import com.pinplanet.pintact.chat.ChatListener;
import com.pinplanet.pintact.chat.ChatListenerImpl;
import com.pinplanet.pintact.chat.XmppChatManager;
import com.pinplanet.pintact.data.ChatTopicDTO;
import com.pinplanet.pintact.data.service.PintactDbHelper;
import com.pinplanet.pintact.data.service.TopicsTableDbInterface;
import com.pinplanet.pintact.utility.LruBitmapCache;
import com.pinplanet.pintact.utility.SingletonLoginData;

import java.util.List;

public class AppController extends Application {

  public static final String TAG = AppController.class
      .getSimpleName();

  private RequestQueue mRequestQueue;
  private ImageLoader mImageLoader;
  private XmppChatManager chatManager;
  private static AppController mInstance;
  private ChatListener listener;
  private ChatDataWrapper chatData;


  public static final byte LIST_ITEMS_STATE_SHOW_ITEMS=1;

  //singleton save memory 
  private Typeface typeface_bold;
  private Typeface typeface_italic;
  private Typeface typeface_normal;
  private Typeface typeface_light;

  @Override
  public void onCreate() {
    super.onCreate();
    PintactDbHelper.init(getApplicationContext());
    mInstance = this;
  }

  public static synchronized AppController getInstance() {
    return mInstance;
  }

  public Typeface getTypeFaceBold()
  {
	  if(typeface_bold==null)
	  {
		  typeface_bold = Typeface.createFromAsset(getAssets(), "fonts/aller_bd.ttf");
	  }
	  return typeface_bold;	  
  }
  
  public Typeface getTypeFaceItalic()
  {
	  if(typeface_italic==null)
	  {
		  typeface_italic = Typeface.createFromAsset(getAssets(), "fonts/aller_it.ttf");
	  }
	  return typeface_italic;	  
  }
  
  public Typeface getTypeFaceItalicBold()
  {
	  if(typeface_italic==null)
	  {
		  typeface_italic = Typeface.createFromAsset(getAssets(), "fonts/aller_it_bd.ttf");
	  }
	  return typeface_italic;	  
  }
  
  public Typeface getTypeFaceNormal()
  {
	  if(typeface_normal==null)
	  {
		  typeface_normal = Typeface.createFromAsset(getAssets(), "fonts/aller_rg.ttf");
	  }
	  return typeface_normal;	  
  }
  
  
  public Typeface getTypeFaceLightItalic()
  {
	  if(typeface_light==null)
	  {
		  typeface_light = Typeface.createFromAsset(getAssets(), "fonts/aller_lt_it.ttf");
	  }
	  return typeface_light;	  
  }
  
  public Typeface getTypeFaceLight()
  {
	  if(typeface_light==null)
	  {
		  typeface_light = Typeface.createFromAsset(getAssets(), "fonts/aller_lt.ttf");
	  }
	  return typeface_light;	  
  }
  
  public RequestQueue getRequestQueue() {
    if (mRequestQueue == null) {
      mRequestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    return mRequestQueue;
  }

  public ImageLoader getImageLoader() {
    getRequestQueue();
    if (mImageLoader == null) {
      mImageLoader = new ImageLoader(this.mRequestQueue,
          new LruBitmapCache());
    }
    return this.mImageLoader;
  }

  public <T> void addToRequestQueue(Request<T> req, String tag) {
    // set the default tag if tag is empty
    req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
    getRequestQueue().add(req);
  }

  public <T> void addToRequestQueue(Request<T> req) {
    req.setTag(TAG);
    getRequestQueue().add(req);
  }

  public void cancelPendingRequests(Object tag) {
    if (mRequestQueue != null) {
      mRequestQueue.cancelAll(tag);
    }
  }

    public XmppChatManager getChatManager() {
        if(chatManager == null){
            listener = new ChatListenerImpl();
            chatManager = new XmppChatManager(listener, getString(R.string.pintact_chat_server),  getString(R.string.pintact_group_chat_server),
                    SingletonLoginData.getInstance().getUserData().getId().toString(),  SingletonLoginData.getInstance().getAccessToken() );
        }
        return chatManager;
    }

    public void setChatManager(XmppChatManager chatManager) {
        this.chatManager = chatManager;
    }

    public ChatDataWrapper getChatData() {
        if(chatData == null){
            chatData = new ChatDataWrapper();
        }
        return chatData;
    }

    public void setChatData(ChatDataWrapper chatData) {
        this.chatData = chatData;
    }
}
