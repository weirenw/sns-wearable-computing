package edu.gatech.cc.cs7470.facecard.View.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.plus.Plus;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import edu.gatech.cc.cs7470.facecard.Constants;
import edu.gatech.cc.cs7470.facecard.Controller.tasks.RegisterBluetoothTask;
import edu.gatech.cc.cs7470.facecard.Controller.utils.BluetoothUtil;
import edu.gatech.cc.cs7470.facecard.Model.Bluetooth;
import edu.gatech.cc.cs7470.facecard.Model.FaceCard;
import edu.gatech.cc.cs7470.facecard.Model.Profile;
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

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private int currentNavigationFragment;

    private Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        currentNavigationFragment = 0;

        if (mGoogleApiClient.isConnected() && Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            profile = new Profile(Plus.PeopleApi.getCurrentPerson(mGoogleApiClient),
                    Plus.AccountApi.getAccountName(mGoogleApiClient));
        }

        //check for bluetooth registration
        SharedPreferences prefs = getSharedPreferences(Constants.PACKAGE_NAME, MODE_PRIVATE);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment()).commit();
        }

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

	//------------------------------ bluetooth part --------------------------------------------

	@Override
	protected void onResume() {  //runs when app is either re-opened from backend or launched
		//update necessary UI here

		//check bluetooth condition
		super.onResume();
		blueToothWork();
	}

	private BluetoothAdapter mBluetoothAdapter;
	private HashMap<String, BluetoothDevice> btMap;
	private BluetoothSocket mBluetoothSocket;
	boolean isBtRegistered = false;
	boolean isPaired = false;
	AlertDialog ad1;

	private void blueToothWork() {
		//check if bluetooth is already connected with glass
		if (mBluetoothSocket != null && mBluetoothSocket.isConnected()) {
			AlertDialog ad2 = new AlertDialog.Builder(MainActivity.this).setMessage("There's a connected Glass yet.")
					.setPositiveButton("OK", null).setCancelable(false)
					.create();
			ad2.show();
			return;
		}
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Log.d("","device does not support bluetooth");
			finish();
		}
		//enable bluetooth
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
		} else {
			if (!isBtRegistered) 	registerBluetooth();
			if (!isPaired) pairToGlass();
		}
	}

	private void pairToGlass() {  //bluetooth has to be enabled
		// Register the BroadcastReceiver
		registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND)); // Don't forget to unregister during onDestroy
		registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

		//record all bluetooth devices
		btMap = new HashMap<String, BluetoothDevice>();
		//start discover process, it lasts 12 seconds. results are handled in mReceiver
		mBluetoothAdapter.startDiscovery();
		//reminder it to user
		ad1 = new AlertDialog.Builder(MainActivity.this).setMessage("Detecting bluetooth devices...").setCancelable(false).create();
		ad1.show();
	}

	// this receiver is only used when detecting glass
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		BluetoothDevice[] bdArray;
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				btMap.put(device.getAddress(), device);
				Log.v("debug", "found device: " + device.getName() + " " + device.getAddress());
			}else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				mBluetoothAdapter.cancelDiscovery();
				unregisterReceiver(this);
				ad1.cancel();
				bdArray = btMap.values().toArray(new BluetoothDevice[0]);
				String[] items = new String[bdArray.length];
				for (int i = 0; i < bdArray.length; ++i) {
					items[i] = bdArray[i].getName() + "\n" + bdArray[i].getAddress();
				}

				AlertDialog chooseBluetoothDialog = new AlertDialog.Builder(MainActivity.this)
						.setTitle("Please select your Google Glass")
						.setItems(items, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// The 'which' argument contains the index position
								// of the selected item
								try {
									mBluetoothSocket = bdArray[which].createRfcommSocketToServiceRecord(UUID.fromString("0f3561b9-bda5-4672-84ff-ab1f98e349b6"));
									mBluetoothSocket.connect();
								} catch (Exception e) {
									Log.v("exception", e.toString());
									MainActivity.this.finish();
								}

								isPaired = true;
								Log.v("debug", bdArray[which].getName() + which);
								AlertDialog d = new AlertDialog.Builder(MainActivity.this)
										.setMessage("Connection Succeeded!")
										.setPositiveButton("OK", null)
										.setCancelable(false)
										.create();
								d.show();

								//use an AsynTask to detect bt devices nearby and send data to glass periodically
								new glassCommTask().execute();
							}
						})
						.setNegativeButton("Cancel", null)
						.setCancelable(false)
						.create();
				chooseBluetoothDialog.show();
			}
		}
	};

	public class glassCommTask extends AsyncTask<Void, Void, Void> {
		private HashMap<String, BluetoothDevice> btMap2;
		ObjectOutputStream oOutStream;
		private final BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				// When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					btMap2.put(device.getAddress(), device);
					Log.v("debug", "found device: " + device.getName() + " " + device.getAddress());
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {  //finish detecting, get data from backend and send to glass
					mBluetoothAdapter.cancelDiscovery();

					//request backend for data here

					//send data to glass
					ArrayList<FaceCard> bean = new ArrayList<FaceCard>();
					for (BluetoothDevice device : btMap2.values()) bean.add(new FaceCard(device.getAddress(), "", device.getName(), ""));
					btMap2.clear();
					Log.v("debug", bean.toString());
					try {
						oOutStream.writeObject(bean);
						oOutStream.flush();
					} catch (Exception e) {
						Log.v("exception", e.toString());
					}
				}
			}
		};

		@Override
		protected Void doInBackground(Void ... params) {
			// Register the BroadcastReceiver
			registerReceiver(mReceiver2, new IntentFilter(BluetoothDevice.ACTION_FOUND)); // Don't forget to unregister during onDestroy
			registerReceiver(mReceiver2, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
			try {
				oOutStream = new ObjectOutputStream(mBluetoothSocket.getOutputStream());
			}catch (Exception e) {Log.v("exception", e.toString());}

			while (true) {
				//record all bluetooth devices
				btMap2 = new HashMap<String, BluetoothDevice>();
				//start discover process, it lasts 12 seconds
				mBluetoothAdapter.startDiscovery();

				try {
					while (true) {
						Thread.sleep(5 * 1000);
						if (!mBluetoothAdapter.isDiscovering()) break;
					}
				}catch (Exception e) {}

				publishProgress();
			}
		}

		@Override
		protected void onProgressUpdate(Void ... progress) {
		}

		@Override
		public void finalize() {
			unregisterReceiver(mReceiver2);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				if (!isBtRegistered) 	registerBluetooth();
				if (!isPaired) pairToGlass();
			}
		}
	}



//------------------------------------------- bluetooth work ends here ---------------------------------------------------------------------


    @Override
    protected void onSignedOut() {
        Log.d(TAG, "onSignedOut Start");
        Intent myIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(myIntent);
	    finish();
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

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		mNavigationDrawerFragment.setMenuVisibility(true);
		return super.onPrepareOptionsMenu(menu);
	}

    public Profile getProfile(){
        return this.profile;
    }

    private void registerBluetooth(){
        final String mEmail=profile.getEmail();
        final String addr = (new BluetoothUtil()).getBluetoothId();
        final String mName = profile.getName();
        final String mTagline = profile.getTagline();
        profile.setBluetoothInfo(new Bluetooth(addr,mEmail));
        final String mBluetoothID = profile.getBluetoothInfo().getBluetoothId();


	    new RegisterBluetoothTask().execute(mEmail,mBluetoothID, mName, mTagline);
	    isBtRegistered = true;

	    //save
	    SharedPreferences prefs = getSharedPreferences(Constants.PACKAGE_NAME, MODE_PRIVATE);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(Constants.SHARED_PREFERENCES_BLUETOOTH, addr);
	    editor.commit();


        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bluetooth Registration");
        builder.setMessage("You have to register your Bluetooth device to use the application.\n" + addr);
        //Yes
        builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                new RegisterBluetoothTask().execute(mEmail,mBluetoothID, mName, mTagline);

                //save
                SharedPreferences prefs = getSharedPreferences(Constants.PACKAGE_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Constants.SHARED_PREFERENCES_BLUETOOTH, addr);
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
		*/
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBluetoothAdapter.cancelDiscovery();
		unregisterReceiver(mReceiver);
		Log.d(TAG, "onDestroy GoogleApiClient disconnected");
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
		try {
			if (mBluetoothSocket != null) mBluetoothSocket.close();
		}catch (IOException e) { Log.v("exception", e.toString());}
		super.onDestroy();
	}

}