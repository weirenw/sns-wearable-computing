package edu.gatech.cc.cs7470.facecard;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.glass.timeline.LiveCard;

/**
 * A transparent {@link Activity} displaying a "Stop" options menu to remove the {@link LiveCard}.
 */
public class LiveCardMenuActivity extends Activity {
	final String ExitAppSignal = "EXIT_APP_SIGNAL";

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		// Open the options menu right away.
		openOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.live_card, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_stop:
				// Stop the app
				Intent intent = new Intent(ExitAppSignal);
				sendBroadcast(intent);
				finish();
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
}
