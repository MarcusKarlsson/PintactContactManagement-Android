package com.pinplanet.pintact.profile;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.UserProfileAttribute;

import de.late.utils.StringHelper;
import de.late.widget.ViewListItem;


/**
 * Created by Dennis on 04.08.2014.
 */
public class ListViewItemEditDefault extends ViewListItem  implements ListViewItemType {

    private static final String TAG = ListViewItemEditDefault.class.getName();
    private String title,text;
    
    private UserProfileAttribute userProfileAttribute;

    private int spinnerArrayResId=-1,hintResId=-1,defaultIndexResID=-1;
    private int textInputType = InputType.TYPE_CLASS_TEXT;
    private ITEM_TYPE itemType;



    /**
     * we check the label..if contains mobile...we accept its a mobile number
     * */
    public boolean isMobileNumber()
    {
        return (!StringHelper.isNullOrEmpty(title) && title.toLowerCase().contains("mobile"));
    }

    /**
     *
     * */
    public String getUriPrefix()
    {
        if(!StringHelper.isNullOrEmpty(title))
        {
            String lowerTitle=title.toLowerCase();

            if(lowerTitle.startsWith("http") || lowerTitle.startsWith("www"))
                return "";
            if(lowerTitle.contains("twitter"))
                return "twitter://user?user_id=";
            if(lowerTitle.contains("skype"))
                return "skype:";
            if(lowerTitle.contains("facebook"))
                return "fb://profile/";
        }

        return null;
    }

    public ListViewItemEditDefault(String title,String text,ITEM_TYPE itemType, UserProfileAttribute userProfileAttribute)
    {
        this.title=title;
        this.text=text;
        this.itemType=itemType;
        this.userProfileAttribute = userProfileAttribute;

        switch(itemType)
        {
            case PHONE:
                spinnerArrayResId=R.array.ARRAY_CREATE_PROFILE_PHONE;
                hintResId = R.string.HINT_PROFILE_ENTER_PHONE_NUMBER;
                defaultIndexResID=R.integer.ARRAY_CREATE_PROFILE_PHONE_DEFAULT;
                textInputType = InputType.TYPE_CLASS_PHONE;
            break;

            case EMAIL:
                spinnerArrayResId=R.array.ARRAY_CREATE_PROFILE_EMAIL;
                hintResId = R.string.HINT_PROFILE_ENTER_EMAIL;
                defaultIndexResID=R.integer.ARRAY_CREATE_PROFILE_EMAIL_DEFAULT;
                textInputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                break;

            default:
            case SOCIAL:
                spinnerArrayResId=R.array.ARRAY_CREATE_PROFILE_SOCIAL;
                hintResId = R.string.HINT_PROFILE_ENTER_SOCIAL;
                defaultIndexResID=R.integer.ARRAY_CREATE_PROFILE_SOCIAL_DEFAULT;
                break;
        }

    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public ITEM_TYPE getListItemType() {
        return itemType;
    }

    public UserProfileAttribute getUserProfileAttribute() {
      return userProfileAttribute;
    }

    public void setUserProfileAttribute(UserProfileAttribute userProfileAttribute) {
      this.userProfileAttribute = userProfileAttribute;
    }

    class ViewHandle {
        //public LabelSpinner spinnerTitle;
        public TextView entryTitle;
        public EditText entryText;

        //the arrow right of the label, we need to hide him
        public ImageView blueArrow;

        public ImageView deleteImage,actionOneImage,actionTwoImage;
    }

    @Override
    public View createView(Context context) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(getLayoutID(), null);

        final ViewHandle vh = new ViewHandle();
        //vh.spinnerTitle = (Spinner) v.findViewById(R.id.spinnerTitle);
        vh.entryTitle = (TextView) v.findViewById(R.id.textViewEntryTitle);

        vh.blueArrow = (ImageView)v.findViewById(R.id.imageViewBlueArrow);

        vh.entryText = (EditText) v.findViewById(R.id.editTextEntry);
        vh.deleteImage = (ImageView)v.findViewById(R.id.imageView);
        vh.actionOneImage = (ImageView)v.findViewById(R.id.imageViewActionOne);
        vh.actionTwoImage = (ImageView)v.findViewById(R.id.imageViewActionTwo);
        
        if (itemType == ITEM_TYPE.EMAIL || itemType == ITEM_TYPE.SOCIAL) {
          vh.entryText.setInputType(vh.entryText.getInputType() | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }

        vh.entryText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                text = editable.toString();
                if (text.length() > 500) {
                  vh.entryText.setError("Value cannot exeed 500 characters");
                } else {
                  vh.entryText.setError(null);
                }
                if (itemType == ITEM_TYPE.PHONE) {
                  text = text.replaceAll("[-() ]", "");
                  if (text.length() > 3 && text.length() <= 10) {
                    StringBuilder formattedPhone = new StringBuilder();
                    formattedPhone.append("(" + text.substring(0, 3) + ") ");
                    if (text.length() > 6) {
                      formattedPhone.append(text.substring(3, 6) + "-" + text.substring(6));
                    } else {
                      formattedPhone.append(text.substring(3));
                    }
                    text = formattedPhone.toString();
                  }
                  vh.entryText.removeTextChangedListener(this);
                  editable.replace(0, editable.toString().length(), text);
                  vh.entryText.addTextChangedListener(this);
                }
            }
        });


        if(StringHelper.isNullOrEmpty(title)) {
            int index = context.getResources().getInteger(defaultIndexResID);
            title = context.getResources().getStringArray(spinnerArrayResId)[index];
        }



//        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,context.getResources().getStringArray(spinnerArrayResId) );
//        vh.spinnerTitle.setAdapter(spinnerArrayAdapter);
//
//        int index=context.getResources().getInteger(defaultIndexResID);
//        if(!StringHelper.isNullOrEmpty(title))
//        {
//            int labelIndex=spinnerArrayAdapter.getPosition(title);
//            if(labelIndex!=-1)
//                index=labelIndex;
//        }
//        vh.spinnerTitle.setSelection(index);
//        title=spinnerArrayAdapter.getItem(index).toString();
//
//        vh.spinnerTitle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                title=adapterView.getItemAtPosition(i).toString();
//                vh.entryTitle.setText(title);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {  }
//        });

        v.setTag(vh);

        return v;
    }

    @Override
    public void fillView(View v) {
        ViewHandle vh = (ViewHandle) v.getTag();
        boolean editMode=isEditMode();
        vh.entryTitle.setText(title);
        vh.entryTitle.setEnabled(editMode);

        vh.blueArrow.setVisibility(editMode ? View.VISIBLE: View.GONE);
        vh.deleteImage.setVisibility(editMode ? View.VISIBLE: View.GONE);

        enableEditText(vh.entryText,editMode);
        vh.entryText.setText(text);
        vh.entryText.setHint(hintResId);
        vh.entryText.setInputType(textInputType);
    }

    private void enableEditText(EditText ed,boolean enabled)
    {
        ed.setFocusable(enabled);
        ed.setEnabled(enabled);

        if( enabled )
        {ed.setFocusableInTouchMode(enabled);}
    }

    @Override
    public Class<?> getViewHandle() {
        return ViewHandle.class;
    }

    @Override
    public int getLayoutID() {
        return R.layout.list_view_profile_item_edit_default;
    }


}
