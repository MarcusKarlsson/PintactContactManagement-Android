package com.pinplanet.pintact.profile;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.data.UserProfileAddress;

import de.late.utils.StringHelper;
import de.late.widget.ViewListItem;

/**
 * Created by Dennis on 04.08.2014.
 */
public class ListViewItemEditAddress extends ViewListItem implements ListViewItemType {

    private String title;
    private String street;
    private String city;
    private String state;
    private String zip;
    private UserProfileAddress userProfileAddress;

    public ListViewItemEditAddress(String title, String street,String city,String state,String zip,
        UserProfileAddress userProfileAddress)
    {
        this.title=title;
        this.street=street;
        this.city=city;
        this.state=state;
        this.zip=zip;
        this.userProfileAddress = userProfileAddress;
    }

    public String getTitle() {
        return title;
    }
    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public UserProfileAddress getUserProfileAddress() {
      return userProfileAddress;
    }

    public void setUserProfileAddress(UserProfileAddress userProfileAddress) {
      this.userProfileAddress = userProfileAddress;
    }

    @Override
    public ITEM_TYPE getListItemType() {
        return ITEM_TYPE.ADDRESS;
    }

    class ViewHandle {
        public Spinner spinnerTitle;
        public TextView entryTitle;
        public EditText entryStreet,entryCity,entryState,entryZip;
        public ImageView deleteImage,blueArrow;
    }

    @Override
    public View createView(Context context) {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(getLayoutID(), null);

        final ViewHandle vh = new ViewHandle();
        vh.entryTitle = (TextView) v.findViewById(R.id.textViewEntryTitle);
        vh.spinnerTitle = (Spinner) v.findViewById(R.id.spinnerTitle);

        vh.blueArrow = (ImageView)v.findViewById(R.id.imageViewBlueArrow);

        vh.entryStreet = (EditText) v.findViewById(R.id.editTextStreet);
        vh.entryStreet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
            @Override
            public void afterTextChanged(Editable editable) {
                street=editable.toString();
                if (street.length() > 250) {
                  vh.entryStreet.setError("Value cannot exeed 250 characters");
                } else {
                  vh.entryStreet.setError(null);
                }
            }
        });

        vh.entryCity = (EditText) v.findViewById(R.id.editTextCity);
        vh.entryCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                city = editable.toString();
                if (city.length() > 100) {
                  vh.entryCity.setError("Value cannot exeed 100 characters");
                } else {
                  vh.entryCity.setError(null);
                }
            }
        });

        vh.entryState = (EditText) v.findViewById(R.id.editTextState);
        vh.entryState.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                state = editable.toString();
                if (state.length() > 100) {
                  vh.entryState.setError("Value cannot exeed 100 characters");
                } else {
                  vh.entryState.setError(null);
                }
            }
        });

        vh.entryZip = (EditText) v.findViewById(R.id.editTextZip);
        vh.entryZip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                zip = editable.toString();
                if (zip.length() > 10) {
                  vh.entryZip.setError("Value cannot exeed 10 characters");
                } else {
                  vh.entryZip.setError(null);
                }
            }
        });

        vh.deleteImage = (ImageView)v.findViewById(R.id.imageView);


//        vh.entryTitle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                vh.spinnerTitle.performClick();
//            }
//        });

        if(StringHelper.isNullOrEmpty(title)) {
            int index = context.getResources().getInteger(R.integer.ARRAY_CREATE_PROFILE_ADDRESS_DEFAULT);
            title = context.getResources().getStringArray(R.array.ARRAY_CREATE_PROFILE_ADDRESS)[index];
        }

//        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, context.getResources().getStringArray(R.array.ARRAY_CREATE_PROFILE_ADDRESS));
//        vh.spinnerTitle.setAdapter(spinnerArrayAdapter);
//
//        int index=context.getResources().getInteger(R.integer.ARRAY_CREATE_PROFILE_ADDRESS_DEFAULT);
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
        vh.entryTitle.setText(title);
        boolean editMode=isEditMode();

        vh.entryTitle.setEnabled(editMode);

        enableEditText(vh.entryStreet,editMode);
        vh.entryStreet.setText(street);

        enableEditText(vh.entryCity,editMode);
        vh.entryCity.setText(city);

        enableEditText(vh.entryState,editMode);
        vh.entryState.setText(state);

        enableEditText(vh.entryZip,editMode);
        vh.entryZip.setText(zip);

        vh.blueArrow.setVisibility(editMode ? View.VISIBLE: View.GONE);
        vh.deleteImage.setVisibility(editMode ? View.VISIBLE: View.GONE);
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
        return R.layout.list_view_profile_item_edit_address;
    }


}
