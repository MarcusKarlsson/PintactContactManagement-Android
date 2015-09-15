package com.pinplanet.pintact.label;



import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.pinplanet.pintact.R;

import java.util.ArrayList;

import de.late.widget.SimpleViewListAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentSelectLabel#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class FragmentSelectLabel extends Fragment {

    private static final String PARAM_LABEL_TYPE_TO_LOAD="label_type";

    public static final int LABEL_TYPE_PHONE=1,LABEL_TYPE_MAIL=2,LABEL_TYPE_ADDRESS=3,LABEL_TYPE_NOTES=4,LABEL_TYPE_SOCIAL=5;

    private ArrayList<Label> labels;
    private ListView lv;
    private SimpleViewListAdapter vla;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param type PARAM_LABEL_TYPE_TO_LOAD
     * @return A new instance of fragment SelectLabelFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentSelectLabel newInstance(int type) {
        FragmentSelectLabel fragment = new FragmentSelectLabel();
        Bundle args = new Bundle();
        args.putInt(PARAM_LABEL_TYPE_TO_LOAD,type);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentSelectLabel() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        labels=new ArrayList<Label>();

        if (getArguments() != null) {
            final int type = getArguments().getInt(PARAM_LABEL_TYPE_TO_LOAD, 0);

            //TODO: load labels here, cloud and DB
            //for now we use the resource array-labels to fill the list and add new ones in storage

            int arrayResId=0;
            int defaultIndexResID=0;//we use a s selected index
            switch(type)
            {
                case LABEL_TYPE_PHONE:
                    arrayResId=R.array.ARRAY_CREATE_PROFILE_PHONE;
                    defaultIndexResID=R.integer.ARRAY_CREATE_PROFILE_PHONE_DEFAULT;
                    break;
                case LABEL_TYPE_MAIL:
                    arrayResId=R.array.ARRAY_CREATE_PROFILE_EMAIL;
                    defaultIndexResID=R.integer.ARRAY_CREATE_PROFILE_EMAIL_DEFAULT;
                    break;
                case LABEL_TYPE_SOCIAL:
                    arrayResId=R.array.ARRAY_CREATE_PROFILE_SOCIAL;
                    defaultIndexResID=R.integer.ARRAY_CREATE_PROFILE_SOCIAL_DEFAULT;
                    break;

                case LABEL_TYPE_ADDRESS:
                    arrayResId=R.array.ARRAY_CREATE_PROFILE_ADDRESS;
                    defaultIndexResID=R.integer.ARRAY_CREATE_PROFILE_ADDRESS_DEFAULT;
                    break;
            }

            String labelArray[]=getResources().getStringArray(arrayResId);
            int selectedIndex=getResources().getInteger(defaultIndexResID);

            int index=0;
            for(String s:labelArray)
            {
                boolean selected=(index==selectedIndex);

                labels.add(new Label(selected,s));

                index++;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        final View v= inflater.inflate(R.layout.fragment_select_label, container, false);

        lv=(ListView)v.findViewById(R.id.listViewLabelChooser);
        vla=new SimpleViewListAdapter(getActivity());
        lv.setAdapter(vla);

        fillList(labels);

        return v;
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
