package com.pinplanet.pintact.contact;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinplanet.pintact.R;
import com.pinplanet.pintact.utility.MyActivity;
import com.pinplanet.pintact.utility.MyFragment;

/**
 * Created by Dennis on 14.01.2015.
 */
public class FragmentConnectContact extends MyFragment {

    private static final String TAG = FragmentConnectContact.class.getName();


    public FragmentConnectContact() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_connect_contact, container, false);

        getMyActivity().showTitle(R.string.TITLE_CONNECT);

        View buttonOne=v.findViewById(R.id.buttonOne);
        View buttonTwo=v.findViewById(R.id.buttonTwo);
        View buttonThree=v.findViewById(R.id.buttonThree);

        //TODO: start the Activities here
        View.OnClickListener listener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.buttonOne)
                {
                    getMyActivity().startActivity(new Intent(getMyActivity(), ContactFindActivity.class));
                }
                else if(v.getId()==R.id.buttonTwo)
                {
                    getMyActivity().startActivity(new Intent(getMyActivity(), ContactFindActivity.class));
                }
                else // button three
                {
                    FragmentManager fragmentManager =  getActivity().getFragmentManager();
                    FragmentTransaction trans= fragmentManager.beginTransaction();
                    trans.addToBackStack(null);

                    Fragment frg=new FragmentConnectAddContactManually();
                    trans.setCustomAnimations( R.anim.fragment_left_in, R.anim.fragment_right_out, R.anim.fragment_right_in,R.anim.fragment_left_out);
                    trans.replace(R.id.content_frame, frg);
                    trans.commit();
                    fragmentManager.executePendingTransactions();
                }
            }
        };

        buttonOne.setOnClickListener(listener);
        buttonTwo.setOnClickListener(listener);
        buttonThree.setOnClickListener(listener);

        ((TextView)buttonOne.findViewById(R.id.textViewName)).setText(R.string.CONNECT_SEARCH_CONTACT);
        ((TextView)buttonTwo.findViewById(R.id.textViewName)).setText(R.string.CONNECT_SEARCH_GROUP_PIN);
        ((TextView)buttonThree.findViewById(R.id.textViewName)).setText(R.string.CONNECT_ADD_CONTACT_MANUALLY);

        ((ImageView)buttonOne.findViewById(R.id.imageViewPicture)).setImageResource(R.drawable.connect_search_contact);
        ((ImageView)buttonTwo.findViewById(R.id.imageViewPicture)).setImageResource(R.drawable.connect_search_group_pin);
        ((ImageView)buttonThree.findViewById(R.id.imageViewPicture)).setImageResource(R.drawable.connect_add_manually);

        return v;
    }

    public MyActivity getMyActivity()
    {
        return (MyActivity)getActivity();
    }
}

