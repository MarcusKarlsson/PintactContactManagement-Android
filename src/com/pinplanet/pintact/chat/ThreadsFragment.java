package com.pinplanet.pintact.chat;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.ChatTopicDTO;
import com.pinplanet.pintact.data.service.TopicsTableDbInterface;
import com.pinplanet.pintact.utility.PostServiceExecuteTask;
import com.pinplanet.pintact.utility.RestServiceAsync;
import com.pinplanet.pintact.utility.SingletonLoginData;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;


public class ThreadsFragment extends Fragment {

    private ListView feedsList;
    private ThreadsAdaptor topicAdaptor;
    private View.OnClickListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_group_threads, container, false);
        feedsList = (ListView) rootView.findViewById(R.id.ThreadsList);

        listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatTopicDTO dto = (ChatTopicDTO)view.getTag();
                openChat(dto);
            }
        };

        setTopics();

        return rootView;
    }

    private void setTopics(){
        if(SingletonLoginData.getInstance().getCurGroup() != null) {
            String path = "/api/groups/" + SingletonLoginData.getInstance().getCurGroup().getId()+ "/chatTopics.json?" + SingletonLoginData.getInstance().getPostParam();
            new RestServiceAsync(new PostServiceExecuteTask() {
                @Override
                public void run(int statusCode, final String result) {
                    if (statusCode == 200) {
                        Type collectionType = new TypeToken<Collection<ChatTopicDTO>>(){}.getType();
                        Gson gson = new GsonBuilder().create();
                        final List<ChatTopicDTO> chatTopics = gson.fromJson(result, collectionType);
                        Handler mainHandler = new Handler(AppController.getInstance().getApplicationContext().getMainLooper());

                        for(ChatTopicDTO chatTopicDTO: chatTopics){
                            if( !AppController.getInstance().getChatData().isLastSeenTimeUpdated(chatTopicDTO.getId().toString()))
                            {
                                AppController.getInstance().getChatManager().join(chatTopicDTO.getId().toString());
                                AppController.getInstance().getChatData().updateLastMessageSeenTime(chatTopicDTO.getId().toString(), null);
                            }
                        }
                        Runnable myRunnable = new Runnable() {
                            public void run() {
                                if (ThreadsFragment.this.getActivity() != null && chatTopics != null) {
                                    setAdaptor(chatTopics);
                                }
                            }

                            ;
                        };
                        mainHandler.post(myRunnable);

                        for(ChatTopicDTO chatTopicDTO: chatTopics){
                            TopicsTableDbInterface.getInstance().addTopic(chatTopicDTO.getId(), chatTopicDTO.getGroupId(), AppController.getInstance().getChatData().getLastMessageSeentime(chatTopicDTO.getId().toString()));
                        }
                    }
                }
            }, this.getActivity(), true).execute(path, "", "GET");
        }
    }

    private void setAdaptor(List<ChatTopicDTO> chats){
        topicAdaptor = new ThreadsAdaptor(getActivity().getApplicationContext(), chats, listener);
        feedsList.setAdapter(topicAdaptor);
    }

    public void openChat(ChatTopicDTO dto){
        Log.d("AVINASH", "starting chat activity");
        Intent it = new Intent(getActivity(), ChatActivity.class);
        it.putExtra("topicDto", dto);
        startActivity(it);
    }
    @Override
    public void onResume() {
        super.onResume();
        setTopics();
    }
}
