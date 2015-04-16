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
import android.os.StrictMode;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.plus.Plus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import edu.gatech.cc.cs7470.facecard.Constants;
import edu.gatech.cc.cs7470.facecard.Controller.tasks.RegisterBluetoothTask;
import edu.gatech.cc.cs7470.facecard.Model.Bluetooth;
import edu.gatech.cc.cs7470.facecard.Model.FaceCard;
import edu.gatech.cc.cs7470.facecard.Model.Profile;
import edu.gatech.cc.cs7470.facecard.R;
import edu.gatech.cc.cs7470.facecard.View.fragments.FriendListFragment;
import edu.gatech.cc.cs7470.facecard.View.fragments.MainFragment;
import edu.gatech.cc.cs7470.facecard.View.fragments.NavigationDrawerFragment;

public class MainActivity extends BaseActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
	final String connectionUUID = "0f3561b9-bda5-4672-84ff-ab1f98e349b6";
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

    private Profile mProfile;

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

	//------------------------------ bluetooth part ---------------------------------------------------------

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				if (!isBtRegistered) 	registerBluetooth();
				if (!bluetoothConnected) pairToGlass();
			}
		}
	}

	@Override
	protected void onResume() {  //runs when app is either re-opened from backend or launched
		//update necessary UI here

		//check bluetooth condition
		super.onResume();
		blueToothWork();
	}

	private BluetoothAdapter mBluetoothAdapter;
	private ObjectOutputStream oOutStream;
	private boolean bluetoothConnected = false;
	boolean isBtRegistered = false;

	private void blueToothWork() {
		//check if bluetooth is already connected with glass
		if (bluetoothConnected) {
			Toast.makeText(MainActivity.this, "There's a connected Glass yet.", Toast.LENGTH_SHORT).show();
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
			if (!bluetoothConnected) pairToGlass();
		}
	}

	private void pairToGlass() {  //bluetooth has to be enabled
		final BluetoothDevice[] bdArray = mBluetoothAdapter.getBondedDevices().toArray(new BluetoothDevice[0]);
		String[] items = new String[bdArray.length];
		for (int i = 0; i < bdArray.length; ++i) {
			items[i] = bdArray[i].getName() + "\n" + bdArray[i].getAddress();
		}

		final AlertDialog chooseBluetoothDialog = new AlertDialog.Builder(MainActivity.this)
				.setTitle("Please select your Google Glass")
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// The 'which' argument contains the index position
						// of the selected item
						try {
							BluetoothSocket mBluetoothSocket = bdArray[which].createRfcommSocketToServiceRecord(UUID.fromString(connectionUUID));
							mBluetoothSocket.connect();
							bluetoothConnected = true;
							oOutStream = new ObjectOutputStream(mBluetoothSocket.getOutputStream());
							Log.v("debug", bdArray[which].getName() + which);
							Toast.makeText(MainActivity.this, "Connection Succeeded!", Toast.LENGTH_SHORT).show();
							//use an AsynTask to detect bt devices nearby and send data to glass periodically
							new glassCommTask().execute();
						} catch (Exception e) {
							Log.v("exception", e.toString());
							Toast.makeText(MainActivity.this, "please turn on FaceCard on Glass and try again", Toast.LENGTH_SHORT).show();
							pairToGlass();
						}
					}
				})
				.setNegativeButton("Cancel", null)
				.setCancelable(false)
				.create();
		chooseBluetoothDialog.show();
	}

	private String sendHttpRequest(String url) {
		StrictMode.ThreadPolicy tp = StrictMode.ThreadPolicy.LAX;
		StrictMode.setThreadPolicy(tp);
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			int responseCode = con.getResponseCode();
			//read response
			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
		}catch (Exception e) {
			Log.v("exception", e.toString());
			return "";
		}
	}

	private ArrayList<FaceCard> askBackendForBean(Collection<BluetoothDevice> btDevices) {  //this is a block function, it waits until receive response of http request
		ArrayList<FaceCard> bean = new ArrayList<FaceCard>();
		try {
			StringBuffer sb = new StringBuffer();
			for (BluetoothDevice device : btDevices)
				sb.append(device.getAddress() + ",");
			sb.deleteCharAt(sb.length()-1);
			//send http request
			String url = "http://54.68.110.119/facecard/recommender.php?source=" + URLEncoder.encode(mBluetoothAdapter.getAddress(),"UTF-8")
					+ "&target=" + URLEncoder.encode(sb.toString(),"UTF-8");
			String response = sendHttpRequest(url);

			//generate bean
			JSONObject jo = new JSONObject(response.toString());
			JSONArray ja = jo.getJSONArray("target_list");
			for (int i = 0; i < ja.length(); i++) {
				JSONObject profile = ja.getJSONObject(i);
				bean.add(new FaceCard(profile.getString("bluetooth_id"), profile.getString("google_account"),
						profile.getString("name"), profile.getString("personal_tags"), profile.getString("major")));
			}
			return bean;
		}catch (Exception e) {
			Log.v("exception", e.toString());
			return bean;
		}
	}

	public class glassCommTask extends AsyncTask<Void, Void, Void> {
		private HashMap<String, BluetoothDevice> btMap2;
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
					Toast.makeText(MainActivity.this, "discovery finished", Toast.LENGTH_SHORT).show();
					//request backend for data here

					//send data to glass
					ArrayList<FaceCard> bean = askBackendForBean(btMap2.values());
					btMap2.clear();
					Log.v("debug", bean.toString());
					try {
						oOutStream.writeObject(bean);
						oOutStream.flush();
						Toast.makeText(MainActivity.this, "sent to glass!", Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						Log.v("exception", e.toString());
						bluetoothConnected = false;
					}
				}
			}
		};

		@Override
		protected Void doInBackground(Void... params) {
			// Register the BroadcastReceiver
			registerReceiver(mReceiver2, new IntentFilter(BluetoothDevice.ACTION_FOUND)); // Don't forget to unregister during onDestroy
			registerReceiver(mReceiver2, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

			while (true) {
				//record all bluetooth devices
				btMap2 = new HashMap<String, BluetoothDevice>();
				//start discover process, it lasts 12 seconds
				mBluetoothAdapter.startDiscovery();

				try {
					while (true) {
						Thread.sleep(30 * 1000);
						if (!mBluetoothAdapter.isDiscovering()) break;
					}
				} catch (Exception e) {
					Log.v("exception", e.toString());
				}

				if (!bluetoothConnected) return null;
			}
		}

		@Override
		protected void onPostExecute(Void result) {
			pairToGlass();
		}

		@Override
		public void finalize() {
			unregisterReceiver(mReceiver2);
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
        return this.mProfile;
    }

    private void registerBluetooth(){
	    final String mEmail=mProfile.getEmail();
	    final String mName = mProfile.getName();
	    final String mTagline = mProfile.getTagline();

	    String macAddress = mBluetoothAdapter.getAddress();
	    mProfile.setBluetoothInfo(new Bluetooth(macAddress, mEmail));
	    Log.d(TAG,"Bluetooth id of user's device is " + macAddress);
	    new RegisterBluetoothTask().execute(mEmail, macAddress, mName, mTagline);
    }

	@Override
	protected void onDestroy() {
		mBluetoothAdapter.cancelDiscovery();
		Log.d(TAG, "onDestroy GoogleApiClient disconnected");
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
		super.onDestroy();
	}

}