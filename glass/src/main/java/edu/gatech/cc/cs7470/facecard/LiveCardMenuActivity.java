package edu.gatech.cc.cs7470.facecard;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import edu.gatech.cc.cs7470.facecard.Model.FaceCard;

/**
 * A transparent {@link Activity} displaying a "Stop" options menu to remove the {@link MainActivity}.
 */
public class LiveCardMenuActivity extends Activity {
	private BluetoothSocket mBluetoothSocket;
	private BluetoothAdapter mBluetoothAdapter;

	Timer timerRefresh = new Timer();  // timer to periodically refresh data
	Timer timerFreeze = new Timer();  //count how long use hasn't an action
	long FREEZE_TIME = 10 * 1000; //time to wait that use hasn't an action to start the update service again
	long REFRESH_TIME = 30 * 1000; //time to wait until next refresh

	TimerTask timerTaskFreeze = new TimerTask() {
		@Override
		public void run() {
			timerRefresh.schedule(timerTaskRefresh, 0, REFRESH_TIME);
		}
	};

	TimerTask timerTaskRefresh = new TimerTask() {
		@Override
		public void run() {
			new phoneCommTask().execute();
		}
	};

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Open the options menu right away.
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.face_card_main, menu);
	    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    return connectToPhone();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_stop:
                // Stop the service which will unpublish the live card.
                timerRefresh.cancel();
	            timerFreeze.schedule(timerTaskFreeze, FREEZE_TIME);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        // Nothing else to do, finish the Activity.
        finish();
    }

	//this is a block function, it waits until connect with a phone (or get an exception)
	private boolean connectToPhone() {
		String message =  "Connecting to phone...\n Glass Name: " + mBluetoothAdapter.getName() + "  \nGlass Address: " + mBluetoothAdapter.getAddress();
		AlertDialog ad1 = new AlertDialog.Builder(this).setMessage(message).setCancelable(false).create();
		ad1.show();
		try {
			BluetoothServerSocket mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("MyConnection", UUID.fromString("0f3561b9-bda5-4672-84ff-ab1f98e349b6"));
			mBluetoothSocket = mServerSocket.accept();
			Log.v("debug", "connected!");
			ad1.cancel();
			timerRefresh.schedule(timerTaskRefresh, 0, REFRESH_TIME);
			return true;
		} catch (Exception e) {
			Log.v("exception", e.toString());
			new AlertDialog.Builder(this).setMessage("Exception occurred while connecting to phone").create().show();
			try {Thread.sleep(1000); } catch (Exception e1) {}
			return false;
		}
	}

	private class phoneCommTask extends AsyncTask<Void, ArrayList<FaceCard>, Void> {
		@Override
		protected Void doInBackground(Void ... params) {
			// Register the BroadcastReceiver
			if (!mBluetoothSocket.isConnected())
				if (!connectToPhone()) LiveCardMenuActivity.this.finish();
			try {
				ObjectInputStream oInStream = new ObjectInputStream(mBluetoothSocket.getInputStream());
				ArrayList<FaceCard> bean = (ArrayList<FaceCard>) oInStream.readObject();
				/*String output = "";
				for (FaceCard fc : bean) output += fc.getName() + " " + fc.getBluetoothId() + "\n";
				publishProgress(output+"\n");*/
				publishProgress(bean);
			} catch (Exception e) {
				//data might be corrupted
				Log.v("exception", e.toString());
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(ArrayList<FaceCard> ... progress) {
			ArrayList<FaceCard> bean = progress[0];
			Log.v("debug", "receive object: " + bean.toString());
			//show it on glass
		}
	}

	@Override
	public void onDestroy() {
		try {
			if (mBluetoothSocket != null) mBluetoothSocket.close();
		}catch (Exception e) { Log.v("exception", e.toString());}
	}
}
