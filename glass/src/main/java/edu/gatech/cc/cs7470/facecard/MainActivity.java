package edu.gatech.cc.cs7470.facecard;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import edu.gatech.cc.cs7470.facecard.Model.FaceCard;


public class MainActivity extends Activity {
	private List<CardBuilder> mCards;
	private CardScrollView mCardScrollView;
	private FaceCardScrollAdapter mScrollAdapter;

	TextView logTextView;
	AlertDialog connectingDialog;

	final String connectionUUID = "0f3561b9-bda5-4672-84ff-ab1f98e349b6";
	final String ExitAppSignal = "EXIT_APP_SIGNAL";

	private BluetoothSocket mBluetoothSocket;
	private BluetoothAdapter mBluetoothAdapter;

	Timer timerRefresh = new Timer();  // timer to periodically refresh data
	long REFRESH_TIME = 10 * 1000; //time to wait until next refresh, this is not the accurate time because read() blocks until it reads data from phone

	TimerTask timerTaskRefresh = new TimerTask() {
		@Override
		public void run() {
			new phoneCommTask().execute();
		}
	};

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ExitAppSignal)) finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		logTextView = (TextView)findViewById(R.id.logTextView);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ExitAppSignal);
		registerReceiver(mReceiver, filter);
	}

	@Override
	public void onResume() {
		super.onResume();
		checkConnection();
	}

	//this is a block function, it waits until connect with a phone (or get an exception)
	private void checkConnection() {
		if (mBluetoothSocket != null && mBluetoothSocket.isConnected()) return;
		String message =  "Waiting for connection...\n Glass Name: " + mBluetoothAdapter.getName() + "  \nGlass Address: " + mBluetoothAdapter.getAddress();
		connectingDialog = new AlertDialog.Builder(this).setMessage(message).setCancelable(false).create();
		connectingDialog.show();
		new connectTask().execute();
	}

	private class connectTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void ... params) {
			try {
				BluetoothServerSocket mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("MyConnection", UUID.fromString(connectionUUID));
				mBluetoothSocket = mServerSocket.accept();
				Log.v("debug", "connected!");
				connectingDialog.cancel();
				//timer periodically run phoneCommTask to update face cards
				timerRefresh.schedule(timerTaskRefresh, 0, REFRESH_TIME);
			} catch (Exception e) {
				Log.v("exception", e.toString());
				MainActivity.this.finish();
			}
			return null;
		}
	}

	private class phoneCommTask extends AsyncTask<Void, ArrayList<FaceCard>, Void> {
		@Override
		protected Void doInBackground(Void ... params) {
			// Register the BroadcastReceiver
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
			for (FaceCard fc : bean) logTextView.append("\n" + fc.getName() + " " + fc.getBluetoothId() + "\n");

			//update face cards
			createCards(bean);

			mCardScrollView = new CardScrollView(MainActivity.this);
			mScrollAdapter = new FaceCardScrollAdapter();
			mCardScrollView.setAdapter(mScrollAdapter);
			mCardScrollView.activate();
			setContentView(mCardScrollView);
		}
	}

	private void createCards(ArrayList<FaceCard> bean) {
		mCards = new ArrayList<CardBuilder>();
		for (FaceCard fc : bean) {
			mCards.add(new CardBuilder(this, CardBuilder.Layout.TEXT)
					.setText(fc.getName() + " " + fc.getBluetoothId())
					.setFootnote("I'm the footer!"));
		}
	}

	private class FaceCardScrollAdapter extends CardScrollAdapter {

		@Override
		public int getPosition(Object item) {
			return mCards.indexOf(item);
		}

		@Override
		public int getCount() {
			return mCards.size();
		}

		@Override
		public Object getItem(int position) {
			return mCards.get(position);
		}

		@Override
		public int getViewTypeCount() {
			return CardBuilder.getViewTypeCount();
		}

		@Override
		public int getItemViewType(int position){
			return mCards.get(position).getItemViewType();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return mCards.get(position).getView(convertView, parent);
		}
	}

	@Override
	public void onDestroy() {
		try {
			if (mBluetoothSocket != null) mBluetoothSocket.close();
		}catch (Exception e) { Log.v("exception", e.toString());}
		super.onDestroy();
	}

		/*
	protected void onCreate(Bundle savedInstanceState) {
		mPointsGenerator = new Random();

		if (mLiveCard == null) {

			// Get an instance of a live card
			mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

			// Inflate a layout into a remote view
			mLiveCardView = new RemoteViews(getPackageName(),
					R.layout.live_card);

			// Set up initial RemoteViews values
			homeScore = 0;
			awayScore = 0;
			mLiveCardView.setTextViewText(R.id.home_team_name_text_view, "home team");
			mLiveCardView.setTextViewText(R.id.away_team_name_text_view, "away team");
			mLiveCardView.setTextViewText(R.id.footer_text, "game quarter");

			// Set up the live card's action with a pending intent
			// to show a menu when tapped
			Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
			menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
					Intent.FLAG_ACTIVITY_CLEAR_TASK);
			mLiveCard.setAction(PendingIntent.getActivity(
					this, 0, menuIntent, 0));

			// Publish the live card
			mLiveCard.publish(LiveCard.PublishMode.REVEAL);

			// Queue the update text runnable
			mHandler.post(mUpdateLiveCardRunnable);
		}
	}*/

	/**
	 * Runnable that updates live card contents
	 */
	/*private class UpdateLiveCardRunnable implements Runnable{

		private boolean mIsStopped = false;

		public void run(){
			if(!isStopped()){
				// Generate fake points.
				homeScore += mPointsGenerator.nextInt(3);
				awayScore += mPointsGenerator.nextInt(3);

				// Update the remote view with the new scores.
				mLiveCardView.setTextViewText(R.id.home_score_text_view,
						String.valueOf(homeScore));
				mLiveCardView.setTextViewText(R.id.away_score_text_view,
						String.valueOf(awayScore));

				// Always call setViews() to update the live card's RemoteViews.
				mLiveCard.setViews(mLiveCardView);

				// Queue another score update in 30 seconds.
				mHandler.postDelayed(mUpdateLiveCardRunnable, DELAY_MILLIS);
			}
		}

		public boolean isStopped() {
			return mIsStopped;
		}

		public void setStop(boolean isStopped) {
			this.mIsStopped = isStopped;
		}
	}*/

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_, menu);
		return true;
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
}
