package com.eumetrica.em2;

import com.neurosky.thinkgear.TGDevice;
import com.neurosky.thinkgear.TGEegPower;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class PatientEKGMain extends Activity {

	Button recordButton;
	TextView ekgValue;
	TGDevice tgDevice;
	BluetoothAdapter btAdapter;
	private final static int REQUEST_ENABLE_BT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_patient_ekgmain);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		recordButton = (Button) findViewById(R.id.record_ekg_button);
		recordButton.setOnClickListener(recordButtonHandler);
		ekgValue = (TextView) findViewById(R.id.ekg_value);
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TGDevice.MSG_STATE_CHANGE:
				switch (msg.arg1) {
				case TGDevice.STATE_IDLE:
					break;
				case TGDevice.STATE_CONNECTING:
					break;
				case TGDevice.STATE_CONNECTED:
					tgDevice.start();
					break;
				case TGDevice.STATE_DISCONNECTED:
					break;
				case TGDevice.STATE_NOT_FOUND:
				case TGDevice.STATE_NOT_PAIRED:
				default:
					break;
				}
				break;
			case TGDevice.MSG_POOR_SIGNAL:
				Log.v("HelloEEG", "PoorSignal: " + msg.arg1);
			case TGDevice.MSG_ATTENTION:
				Log.v("HelloEEG", "Attention: " + msg.arg1);
				break;
			case TGDevice.MSG_RAW_DATA:
				int rawValue = msg.arg1;
				ekgValue.setText(rawValue);
				break;
			case TGDevice.MSG_EEG_POWER:
				//TGEegsPower ep = (TGEegPower) msg.arg1;
				//Log.v("HelloEEG", "Delta: " + ep.delta);
			default:
				break;
			}
		}
	};

	View.OnClickListener recordButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {

			tgDevice.connect(true);
			// Record the signal here
		}
	};

	public void onSwitchFlipped(View view) {
		// Is the switch in the connected state?
		boolean wantToConnect = ((ToggleButton) view).isChecked();

		if (wantToConnect) {
			// Initializing the Bluetooth adapter
			btAdapter = BluetoothAdapter.getDefaultAdapter();
			if (!btAdapter.isEnabled()) {
			    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
			if (btAdapter != null) {
				tgDevice = new TGDevice(btAdapter, handler);
			} else{
				 // Alert user that Bluetooth is not available
				 Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
				 finish();
				 return;
			}
		} else {
			tgDevice.close();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_patient_ekgmain, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
