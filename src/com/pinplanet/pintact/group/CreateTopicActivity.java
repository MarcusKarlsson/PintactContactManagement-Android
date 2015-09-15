package com.pinplanet.pintact.group;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pinplanet.pintact.AppController;
import com.pinplanet.pintact.R;
import com.pinplanet.pintact.contact.ContactFindActivity;
import com.pinplanet.pintact.data.ChatTopicDTO;
import com.pinplanet.pintact.data.GroupDTO;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.PostServiceExecuteTask;
import com.pinplanet.pintact.utility.RestServiceAsync;
import com.pinplanet.pintact.utility.SingletonLoginData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Avinash on 14/3/15.
 */
public class CreateTopicActivity extends MyActivity {

    private EditText topicNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_topic);
        hideRight();
        showLeftImage(R.drawable.actionbar_left_arrow);

        View.OnClickListener backLn = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
        addLeftClickListener(backLn);


        TextView create_new_topic =  (TextView)findViewById(R.id.start_topic_button);
        topicNameText = (EditText)findViewById(R.id.topic_name);

        View.OnClickListener createTopicLink = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SingletonLoginData.getInstance().getCurGroup() != null) {
                    if(topicNameText.getText() == null || topicNameText.getText().toString().trim().length() == 0){
                        topicNameText.setError(getString(R.string.ERROR_INPUT_INVALID));
                        topicNameText.requestFocus();
                        return;
                    }else {
                        ChatTopicDTO chatTopic = new ChatTopicDTO();
                        chatTopic.setGroupId(Long.parseLong(SingletonLoginData.getInstance().getCurGroup().getId()));
                        chatTopic.setName(topicNameText.getText().toString());
                        Gson gson = new Gson();
                        String data = gson.toJson(chatTopic);
                        String path = "/api/chatTopics.json?" + SingletonLoginData.getInstance().getPostParam();
                        new RestServiceAsync(new PostServiceExecuteTask() {
                            @Override
                            public void run(int statusCode, final String result) {
                                if (statusCode == 200) {
                                    finish();
                                }
                            }
                        }, CreateTopicActivity.this, true).execute(path, data, "POST");
                    }
                }else{

                }
            }
        };
        create_new_topic.setOnClickListener(createTopicLink);
    }
}
