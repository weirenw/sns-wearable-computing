package edu.gatech.cc.cs7470.facecard.View.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.SignInButton;

import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;


import edu.gatech.cc.cs7470.facecard.Constants;
import edu.gatech.cc.cs7470.facecard.R;

/**
 * Created by miseonpark on 2/24/15.
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = "FaceCard LoginActivity";

    private Activity activity;
    private SignInButton btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        activity = this;

        btnSignIn = (SignInButton) findViewById(R.id.sign_in_button);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!mGoogleApiClient.isConnecting()) {
                    mSignInProgress = STATE_SIGN_IN;
                    Log.d("","ZZZZZZZ connecting");
                    mGoogleApiClient.connect();
                }
            }
        });

/*

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resolveSignInError();
            }
        });
*/
        if (savedInstanceState != null) {
            mSignInProgress = savedInstanceState
                    .getInt(SAVED_PROGRESS, STATE_DEFAULT);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint){
        Log.d(TAG, "onConnectedZZZ");
        Log.d(TAG, "starting main activity");
        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        Log.d(TAG, currentUser.getDisplayName());
        saveAccountPreference(currentUser.getId());
        mSignInProgress = STATE_DEFAULT;
        Intent i = new Intent(activity, MainActivity.class);
        startActivity(i);
	    finish();
        mSignInProgress = STATE_DEFAULT;
    }

    @Override
    protected void onSignedOut() {
        //do nothing
    }

    private void saveAccountPreference(String id){
        SharedPreferences prefs = getSharedPreferences(Constants.PACKAGE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.SHARED_PREFERENCES_ACCOUNT, id);
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    // If the error resolution was successful we should continue
                    // processing errors.
                    mSignInProgress = STATE_SIGN_IN;
                } else {
                    // If the error resolution was not successful or the user canceled,
                    // we should stop processing errors.
                    mSignInProgress = STATE_DEFAULT;
                }

                if (!mGoogleApiClient.isConnecting()) {
                    // If Google Play services resolved the issue with a dialog then
                    // onStart is not called so we need to re-attempt connection here.
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy do nothing");
    }

}
