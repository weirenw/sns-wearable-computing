package edu.gatech.cc.cs7470.facecard.View.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import com.google.android.gms.plus.Plus;

import java.util.ArrayList;

import edu.gatech.cc.cs7470.facecard.Constants;
import edu.gatech.cc.cs7470.facecard.Controller.tasks.GetRecommendedUsersInfo;
import edu.gatech.cc.cs7470.facecard.Controller.tasks.RegisterBluetoothTask;
import edu.gatech.cc.cs7470.facecard.Controller.utils.BluetoothUtil;
import edu.gatech.cc.cs7470.facecard.View.activities.Model.Bluetooth;
import edu.gatech.cc.cs7470.facecard.View.activities.Model.FaceCard;
import edu.gatech.cc.cs7470.facecard.View.activities.Model.Profile;
import edu.gatech.cc.cs7470.facecard.R;
import edu.gatech.cc.cs7470.facecard.View.fragments.FriendListFragment;
import edu.gatech.cc.cs7470.facecard.View.fragments.MainFragment;
import edu.gatech.cc.cs7470.facecard.View.fragments.NavigationDrawerFragment;

public class MainActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String TAG = "FaceCard MainActivity";

    /**
     * Fragment
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private BroadcastReceiver mReceiver;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<String> discoveredBluetoothMACs = new ArrayList<String>();
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private int currentNavigationFragment;

    private Profile mProfile;
    private ArrayList<FaceCard> discoveredFacecards = new ArrayList<FaceCard>(); //!!!!!!! the discovered facecards are stored here !!!!!!!!!! Qiang this is what you need
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        currentNavigationFragment = 0;

        if (mGoogleApiClient.isConnected() && Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            mProfile = new Profile(Plus.PeopleApi.getCurrentPerson(mGoogleApiClient),
                    Plus.AccountApi.getAccountName(mGoogleApiClient));

        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG,"device does not support bluetooth");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        }

        //check for bluetooth registration
        SharedPreferences prefs = getSharedPreferences(Constants.PACKAGE_NAME, MODE_PRIVATE);

        //if(!prefs.contains(Constants.SHARED_PREFERENCES_BLUETOOTH)){
            registerBluetooth();
       // }

        //discover nearby bluetooth enabled devices
        mBluetoothAdapter.startDiscovery();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment()).commit();
        }
        discoveredBluetoothMACs.add(mBluetoothAdapter.getAddress()); //adding source address as element 0
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent                    BluetoothDevice device  = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    discoveredBluetoothMACs.add(device.getAddress());   //adding target addresses as elements 1 through n
                    Log.d(TAG, "Device found, device name is: " + device.getName() + ", Mac address is: " + device.getAddress());
                }
                if(mBluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    Log.d(TAG,"discovery finished, sending the following info to getrecommendedusersinfo");
                    for(String i:discoveredBluetoothMACs){
                        Log.d(TAG,i);
                    }
                    new GetRecommendedUsersInfo(MainActivity.this).execute(discoveredBluetoothMACs);

                }

            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(mBluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
//        tv_profile_description = (TextView)findViewById(R.id.profile_description);
//        tv_profile_organization = (TextView)findViewById(R.id.profile_organization);
//        iv_profile_picture = (ImageView)findViewById(R.id.profile_picture);
//        ll_profile_background = (LinearLayout)findViewById(R.id.profile_background);

        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        mNavigationDrawerFragment.setMenuVisibility(false);

        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onSignedOut() {
        Log.d(TAG, "onSignedOut Start");
        finish();
        Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(myIntent);
        Log.d(TAG, "onSignedOut End");
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Log.d(TAG, "navigation position " + position);
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch(position){
            case 0:
                if(currentNavigationFragment!=position) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, MainFragment.newInstance())
                            .commit();
                    currentNavigationFragment = position;
                }
                break;
            case 1:
                if(currentNavigationFragment!=position) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, FriendListFragment.newInstance())
                            .commit();
                    currentNavigationFragment = position;
                }
                break;
            case 2:
                if(currentNavigationFragment!=position) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.container, MainFragment.newInstance())
                            .commit();
                    currentNavigationFragment = position;
                }
            case 3:
                //logout
                if(signOutFromGplus()){
                    onSignedOut();
                }else{
                    //Throw Error Message
                }
                break;
            default:
                break;
        }

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.profile);
                break;
            case 2:
                mTitle = getString(R.string.friends);
                break;
            case 3:
                mTitle = getString(R.string.settings);
            case 4:
                //logout
                if(signOutFromGplus()){
                    onSignedOut();
                }else{
                    //Throw Error Message
                }
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Profile getProfile(){
        return this.mProfile;
    }

    private void registerBluetooth(){

        final String mEmail=mProfile.getEmail();
        final String mName = mProfile.getName();
        final String mTagline = mProfile.getTagline();



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bluetooth Registration");
        builder.setMessage("You have to register your Bluetooth device to use the application.\n");
        //Yes
        builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String uuid = (new BluetoothUtil()).getBluetoothId();
                String macAddress = mBluetoothAdapter.getAddress();
                mProfile.setBluetoothInfo(new Bluetooth(macAddress, mEmail));
                final String mBluetoothID = mProfile.getBluetoothInfo().getBluetoothId();
                Log.d(TAG,"Bluetooth id of user's device is " + mBluetoothID);
                new RegisterBluetoothTask().execute(mEmail,mBluetoothID, mName, mTagline);

                //save
                SharedPreferences prefs = getSharedPreferences(Constants.PACKAGE_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Constants.SHARED_PREFERENCES_BLUETOOTH, uuid);
                editor.commit();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(mReceiver);

        Log.d(TAG, "onDestroy GoogleApiClient disconnected");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    public void recommenderCallBack(ArrayList<FaceCard> facecards){
        discoveredFacecards = facecards;
        for(FaceCard i : discoveredFacecards){
            Log.d(TAG,"bluetoothId " + i.getBluetoothId());
            Log.d(TAG,"accountId/email " + i.getAccountId());
            Log.d(TAG,"major " + i.getMajor());
            Log.d(TAG,"name " + i.getName());
            Log.d(TAG,"tag " + i.getTag());
        }
    }

}