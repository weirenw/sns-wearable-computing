package edu.gatech.cc.cs7470.facecard.View.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.PersonBuffer;

import edu.gatech.cc.cs7470.facecard.Constants;
import edu.gatech.cc.cs7470.facecard.Model.Profile;
import edu.gatech.cc.cs7470.facecard.R;
import edu.gatech.cc.cs7470.facecard.View.activities.MainActivity;

/**
 * Created by miseonpark on 3/16/15.
 */
public class FriendListFragment extends Fragment implements ResultCallback<People.LoadPeopleResult> {

    private static String TAG = "FriendListFragment";

    private static MainActivity activity;

    private GoogleApiClient mGoogleApiClient;

    public static Fragment newInstance() {
        FriendListFragment mFragment = new FriendListFragment();
        return mFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        activity = (MainActivity) this.getActivity();
        mGoogleApiClient = activity.getGoogleApiClient();

        View rootView = inflater.inflate(R.layout.fragment_friend_list, container, false);
        populateFriendInfo();
        return rootView;
    }

    private void populateFriendInfo(){
//        try {
//            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
//                Plus.PeopleApi.loadVisible(mGoogleApiClient, null)
//                        .setResultCallback(this);
//
//            } else {
//                    Toast.makeText(activity,
//                            "Person information is null", Toast.LENGTH_LONG).show();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onResult(People.LoadPeopleResult peopleData) {
        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = peopleData.getPersonBuffer();
            try {
                int count = personBuffer.getCount();
                for (int i = 0; i < count; i++) {
                    Log.d(TAG, "Display name: " + personBuffer.get(i).getDisplayName());
                }
            } finally {
                personBuffer.close();
            }
        } else {
            Log.e(TAG, "Error requesting visible circles: " + peopleData.getStatus());
        }
    }
}
