package com.pinplanet.pintact.label;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.AttributeType;
import com.pinplanet.pintact.data.UserProfileChildType;
import com.pinplanet.pintact.data.service.AppService;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.SingletonLoginData;

import java.util.ArrayList;
import java.util.List;

import de.late.widget.SimpleViewListAdapter;
import de.late.widget.ViewListItem;

public class ActivitySelectLabel extends MyActivity {

    private static final String PARAM_LABEL_TYPE ="label_type";
    private static final String PARAM_CURRENT_LABEL ="label_string";
    public static final String PARAM_SELECTED_LABEL="label_string";
    public static final String PARAM_LABEL_SOURCE_INDEX="label_index";

    public static final int LABEL_TYPE_PHONE=101,LABEL_TYPE_MAIL=102,LABEL_TYPE_ADDRESS=103,LABEL_TYPE_NOTES=104,LABEL_TYPE_SOCIAL=105;

    private ArrayList<Label> labels;
    private ListView lv;
    private SimpleViewListAdapter vla;
    private int type,selectedLabelIndex,labelSourceIndex;
    private String selectedLabel;

    public static Intent getInstance(Context context,int labelType,String currentLabel,int sourceIndex)
    {
        Bundle b=new Bundle();
        b.putInt(PARAM_LABEL_TYPE,labelType);
        b.putString(PARAM_CURRENT_LABEL, currentLabel);
        b.putInt(PARAM_LABEL_SOURCE_INDEX,sourceIndex);//we return this (back) to the waiting activity
        Intent i=new Intent(context,ActivitySelectLabel.class);
        i.putExtras(b);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_label);

        showTitle(R.string.LABEL_AB_TITLE);

        showRightText(getResources().getString(R.string.ab_edit));
        addRightTextClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vla.setEditMode(!vla.isEditMode());
                if(vla.isEditMode())
                {
                    showRightText(getResources().getString(R.string.ab_done));
                }
                else
                {
                    showRightText(getResources().getString(R.string.ab_edit));
                    //clears also all not added labels
                    fillList(labels);
                }
            }
        });

        showLeftImage(R.drawable.actionbar_left_arrow);
        addLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    finish();
            }
        });

        type = getIntent().getIntExtra(PARAM_LABEL_TYPE, 0);
        labelSourceIndex = getIntent().getIntExtra(PARAM_LABEL_SOURCE_INDEX, 0);
        selectedLabel = getIntent().getStringExtra(PARAM_CURRENT_LABEL);

        lv=(ListView)findViewById(R.id.listViewLabelChooser);
        vla=new SimpleViewListAdapter(this,new SimpleViewListAdapter.initView() {
            @Override
            public void init(SimpleViewListAdapter simpleViewListAdapter, int i, final View convertView) {
                ViewListItem item=vla.getItem(i);
                if(item instanceof ListItemNewLabel)//action for the new item ...add
                {
                    convertView.findViewById(R.id.textViewAddLabel).setTag(item);
                    convertView.findViewById(R.id.textViewAddLabel).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ListItemNewLabel li=(ListItemNewLabel)view.getTag();
                            String newLabel=li.getTextEntry();
                            //remove placeholder list-item
                            vla.remove(li.getPosition());
                            
                            if (newLabel != null && newLabel.length() > 0) {
                              UserProfileChildType userProfileChildType = null;
                              AttributeType attributeType = null;
                              switch(type) {
                                case LABEL_TYPE_ADDRESS:
                                  userProfileChildType = UserProfileChildType.ADDRESS;
                                  break;
                                case LABEL_TYPE_MAIL:
                                  userProfileChildType = UserProfileChildType.ATTRIBUTE;
                                  attributeType = AttributeType.EMAIL;
                                  break;
                                case LABEL_TYPE_PHONE:
                                  userProfileChildType = UserProfileChildType.ATTRIBUTE;
                                  attributeType = AttributeType.PHONE_NUMBER;
                                  break;
                                case LABEL_TYPE_SOCIAL:
                                  userProfileChildType = UserProfileChildType.ATTRIBUTE;
                                  attributeType = AttributeType.SERVICE_ID;
                                  break;
                                default:
                                  // ?
                              }
                              AppService.addUserProfileChildLabels(userProfileChildType, attributeType, newLabel);
                              vla.add(new ListItemSelectableLabel(newLabel,false));
                              labels.add(new Label(false,newLabel));
                            }
                            hideSoftKeyboard(ActivitySelectLabel.this);
                        }
                    });
                }
                else//add actions for default label items e.g. delete...
                {
                    convertView.findViewById(R.id.imageView).setTag(vla.getItem(i));
                    convertView.findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final ListItemSelectableLabel itemTag=(ListItemSelectableLabel)view.getTag();

                            Animation button_right_in = AnimationUtils.loadAnimation(ActivitySelectLabel.this,R.anim.anim_button_right_in);
                            convertView.findViewById(R.id.textViewDeleteLabel).startAnimation(button_right_in);

                            convertView.findViewById(R.id.textViewDeleteLabel).setVisibility(View.VISIBLE);
                            convertView.findViewById(R.id.imageView).setVisibility(View.INVISIBLE);

                            convertView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View view) {
                                    if(convertView.findViewById(R.id.imageView).getVisibility()==View.VISIBLE) return;

                                    Animation button_right_out = AnimationUtils.loadAnimation(ActivitySelectLabel.this, R.anim.anim_button_right_out);
                                    convertView.findViewById(R.id.textViewDeleteLabel).startAnimation(button_right_out);
                                    itemTag.setLabelDeleteVisible(true);
                                    convertView.findViewById(R.id.textViewDeleteLabel).setVisibility(View.VISIBLE);

                                    button_right_out.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {
                                            convertView.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
                                        }
                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            itemTag.setLabelDeleteVisible(false);
                                            convertView.findViewById(R.id.textViewDeleteLabel).setVisibility(View.INVISIBLE);
                                        }
                                        @Override
                                        public void onAnimationRepeat(Animation animation) { }
                                    });
                                }
                            });

                            convertView.findViewById(R.id.textViewDeleteLabel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    itemTag.setLabelDeleteVisible(false);
                                    convertView.findViewById(R.id.textViewDeleteLabel).setVisibility(View.INVISIBLE);
                                    
                                    UserProfileChildType userProfileChildType = null;
                                    AttributeType attributeType = null;
                                    switch(type) {
                                      case LABEL_TYPE_ADDRESS:
                                        userProfileChildType = UserProfileChildType.ADDRESS;
                                        break;
                                      case LABEL_TYPE_MAIL:
                                        userProfileChildType = UserProfileChildType.ATTRIBUTE;
                                        attributeType = AttributeType.EMAIL;
                                        break;
                                      case LABEL_TYPE_PHONE:
                                        userProfileChildType = UserProfileChildType.ATTRIBUTE;
                                        attributeType = AttributeType.PHONE_NUMBER;
                                        break;
                                      case LABEL_TYPE_SOCIAL:
                                        userProfileChildType = UserProfileChildType.ATTRIBUTE;
                                        attributeType = AttributeType.SERVICE_ID;
                                        break;
                                      default:
                                        // ?
                                    }
                                    AppService.removeUserProfileChildLabel(userProfileChildType, attributeType,
                                        itemTag.getTitle()); 
                                    vla.remove(itemTag.getPosition());
                                    labels.remove(itemTag.getPosition());
                                }
                            });

                        }
                    });
                }
            }
        });

        View footer = getLayoutInflater().inflate(R.layout.list_view_label_add_footer, null);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vla.add(new ListItemNewLabel());
                vla.notifyDataSetChanged();
                lv.invalidateViews();
            }
        });

        lv.addFooterView(footer);
        lv.setAdapter(vla);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListItemSelectableLabel label = (ListItemSelectableLabel)vla.getItem(i);
                if(label!=null)
                {
                    label.setSelected(true);
                    vla.getItem(selectedLabelIndex).setSelected(false);
                    selectedLabel=label.getTitle();
                    selectedLabelIndex=i;
                    lv.invalidateViews();
                    onBackPressed();
                }
            }
        });

        labels=new ArrayList<Label>();

        List<String> labelList;
        switch(type)
        {
            case LABEL_TYPE_PHONE:
                labelList=SingletonLoginData.getInstance().getUserProfileChildLabels()
                  .get(UserProfileChildType.ATTRIBUTE).get(AttributeType.PHONE_NUMBER);
                break;
            case LABEL_TYPE_MAIL:
                labelList=SingletonLoginData.getInstance().getUserProfileChildLabels()
                  .get(UserProfileChildType.ATTRIBUTE).get(AttributeType.EMAIL);
                break;
            case LABEL_TYPE_SOCIAL:
                labelList=SingletonLoginData.getInstance().getUserProfileChildLabels()
                  .get(UserProfileChildType.ATTRIBUTE).get(AttributeType.SERVICE_ID);
                break;

            case LABEL_TYPE_ADDRESS:
                labelList=SingletonLoginData.getInstance().getUserProfileChildLabels()
                  .get(UserProfileChildType.ADDRESS).get(null);
                break;
            default:
                labelList = new ArrayList<String>();
        }

        int selectedIndex=0;
        selectedLabelIndex=-1;

        int index=0;
        //check if we can find the existing label string in the array to get the selected index
        if(selectedLabel!=null)
        {
            for(String s:labelList)
            {
                if(s.equalsIgnoreCase(selectedLabel))
                {
                    selectedLabelIndex=index;
                    break;
                }
                index++;
            }
        }

        if(selectedLabelIndex==-1)
        selectedLabelIndex=selectedIndex;

        index=0;
        for(String s:labelList)
        {
            boolean selected=(index==selectedLabelIndex);

            labels.add(new Label(selected,s));

            index++;
        }

        fillList(labels);
    }

    private void fillList(ArrayList<Label> labels)
    {
        vla.setNotifyOnChange(false);
        vla.clear();

        for(Label l:labels)
        {
            vla.add(new ListItemSelectableLabel(l.label,l.isSelected));
        }

        vla.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if(isFinishing())return;

        if(selectedLabel!=null)
        {
            Log.d(TAG, "onBackPressed - selectedLabel:"+selectedLabel);
            Intent i=new Intent();
            i.putExtra(PARAM_SELECTED_LABEL,selectedLabel);
            i.putExtra(PARAM_LABEL_SOURCE_INDEX,labelSourceIndex);
            setResult(RESULT_OK, i);
        }

        finish();
        overridePendingTransition( R.anim.slide_in_right, R.anim.slide_out_right );
    }

    private class Label
    {
        boolean isSelected=false;
        String label;

        private Label(boolean isSelected, String label) {
            this.isSelected = isSelected;
            this.label = label;
        }
    }

}
