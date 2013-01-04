package com.eumetrica.em2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

public class PatientEKGMain extends Activity {

	Button recordButton;
	BluetoothAdapter bluetoothAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_patient_ekgmain);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		recordButton = (Button) findViewById(R.id.record_ekg_button);
		recordButton.setOnClickListener(recordButtonHandler);
	}
	
	View.OnClickListener recordButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			
			//Record the signal here
			
			
		}
	};
	
	public void onSwitchFlipped(View view) {
	    // Is the switch in the connected state?
	    boolean connected = ((ToggleButton) view).isChecked();
	    
	    if (connected) {
	    	//Connect to Bluetooth
//	        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//	        if(bluetoothAdapter == null) {
//	        	// Alert user that Bluetooth is not available
//	        	Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
//	        	finish();
//	        	return;
//	        }  
	    } else {
	        // Disconnect from Bluetooth
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
