package com.eumetrica.em2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.neurosky.thinkgear.TGDevice;

public class PatientEKGMain extends Activity {

	Button connectButton;
	Button startRecordingButton;
	Button stopRecordingButton;
	Button syncButton;
	TextView signalValue;
	TextView connectionStatus;
	TGDevice tgDevice;
	BluetoothAdapter btAdapter;

	public static final String ACCESS_KEY_ID = "";
	public static final String SECRET_KEY = "";
	
	private AmazonS3Client s3Client = new AmazonS3Client( new BasicAWSCredentials( ACCESS_KEY_ID, SECRET_KEY ) );
	
	private String bucketName = "droidbucket";
	
	private String logFileName = "/euMetricaLog1.txt";
	private int i;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_patient_ekgmain);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//Create the buttons on the screen and assign handlers
		connectButton = (Button) findViewById(R.id.connect_button);
		connectButton.setOnClickListener(connectButtonHandler);
		startRecordingButton = (Button) findViewById(R.id.start_recording_button);
		startRecordingButton.setOnClickListener(startRecordingButtonHandler);
		stopRecordingButton = (Button) findViewById(R.id.stop_recording_button);
		stopRecordingButton.setOnClickListener(stopRecordButtonHandler);
		syncButton = (Button) findViewById(R.id.syncButton);
		syncButton.setOnClickListener(syncButtonHandler);
		//Create the text-views on the screen
		signalValue = (TextView) findViewById(R.id.signal_value);
		connectionStatus = (TextView) findViewById(R.id.connectionStatusTextView);
	}

	//Handle incoming messages from the device
	//Update the connection state and log the status
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TGDevice.MSG_STATE_CHANGE:
				switch (msg.arg1) {
				case TGDevice.STATE_IDLE:
					Log.i ("info", "idle");
					connectionStatus.setText("Idle");
					break;
				case TGDevice.STATE_CONNECTING:
					Log.i ("info", "connecting");
					connectionStatus.setText("Connecting...");
					break;
				case TGDevice.STATE_CONNECTED:
					Log.i ("info", "connected");
					connectionStatus.setText("Connected");
					//tgDevice.start();
					break;
				case TGDevice.STATE_DISCONNECTED:
					Log.i ("info", "disconnected");
					connectionStatus.setText("Disconnected");
					break;
				case TGDevice.STATE_NOT_FOUND:
					Log.i ("info", "not found");
					connectionStatus.setText("Not found");
				case TGDevice.STATE_NOT_PAIRED:
					Log.i ("info", "not paired");
					connectionStatus.setText("Not paired");
				default:
					break;
				}
				break;
			case TGDevice.MSG_POOR_SIGNAL:
				Log.v("info", "PoorSignal: " + msg.arg1);
				connectionStatus.setText("Poor signal");
			case TGDevice.MSG_ATTENTION:
				Log.v("info", "Attention: " + msg.arg1);
				connectionStatus.setText("Attention");
				break;
			case TGDevice.MSG_RAW_DATA:
				int rawADCValue = msg.arg1;
				Log.i ("info", "Got RAW DATA ****** " + i*0.002 + "ms, " + rawADCValue);
				i++;
				//Append cloud log with own timestamp value (seconds) and ADC value
				appendLog(i*0.002 + "," + rawADCValue);
				//ekgValue.setText(rawValue);
				break;
			case TGDevice.MSG_EEG_POWER:
				//TGEegsPower ep = (TGEegPower) msg.arg1;
				//Log.v("HelloEEG", "Delta: " + ep.delta);
			default:
				break;
			}
		}
	};
	

	//Handler for the ON button
	View.OnClickListener connectButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			
			// Initializing the Bluetooth adapter
			btAdapter = BluetoothAdapter.getDefaultAdapter();

			//Check if Bluetooth is enabled
			if (!btAdapter.isEnabled()) {
				signalValue.setText("Bluetooth disabled");
			}
			//Check that the Bluetooth adapter is not null
			if (btAdapter != null) {
				signalValue.setText("Bluetooth not null");
				//Get the paired devices
				Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
				//Go through paired Devices and get the names
				String[] deviceNames = new String[pairedDevices.size()];
				for (int j=0; j < pairedDevices.size(); j++){
					if (pairedDevices.iterator().hasNext()){
						deviceNames[j] = pairedDevices.iterator().next().getName();
						signalValue.setText(deviceNames[j]);
					}
				}
				//Get a new NeuroSky device and connect to it
				Log.i ("info", "Getting new TGDevice");
				tgDevice = new TGDevice(btAdapter, handler);
				Log.i ("info", "Got new TGDevice - connecting");
				tgDevice.connect(true);
				Log.i ("info", "Connected to new TGDevice");
			}
			
			Log.i("info", "Connected to the cloud");
		}
	};
	
	//Handler for the START button
	View.OnClickListener startRecordingButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			//Keep this here - otherwise late packets will come in at 0 ms twice
			i = 0;
			Log.i ("info", "Starting recording");
			tgDevice.start();
		}
	};
	
	//Handler for the STOP button
	View.OnClickListener stopRecordButtonHandler = new View.OnClickListener() {
		public void onClick(View v) {
			Log.i ("info", "Stopping recording");
			tgDevice.stop();
		}
	};
	
	//Handler for the Cloud Sync button
	View.OnClickListener syncButtonHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			uploadLog();
		}
	};

	public void appendLog(String text){       
	   File logFile = new File(Environment.getExternalStorageDirectory() + logFileName);
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}
//	
	public void uploadLog(){
        // Put the image data into S3.
		new LongOperation().execute("");
	}
	
	//Create separate thread for the upload process
	private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
            	CreateBucketIfDoesntExist();
            }
            catch ( Exception exception ) {
            	if (exception.getMessage() != null){
            		Log.i("Upload Info", exception.getMessage() );
            	}
            } try {
            	String fullPathName = Environment.getExternalStorageDirectory() + logFileName;
            	Log.i("Upload Info", "Path name is " + fullPathName);
            	PutObjectRequest por = new PutObjectRequest(bucketName, GetUniqueFilename(), new java.io.File(fullPathName) );  // Content type is determined by file extension.
            	Log.i("Upload Info", "Unique object name is " + GetUniqueFilename());
            	s3Client.putObject( por );
            } catch (Exception exception){
            	if (exception.getMessage() != null){
            		Log.i("Upload Info", exception.getMessage() );
            	}
            }    		
    		File logFile = new File(Environment.getExternalStorageDirectory() + logFileName);
    		String uploadFilename = GetUniqueFilename();
    		s3Client.putObject(bucketName, uploadFilename, logFile);     	
              return "Executed";
        }      

        @Override
        protected void onPostExecute(String result) {
              Log.i("Upload Info", "Thread Executed"); // txt.setText(result);
              //might want to change "executed" for the returned string passed into onPostExecute() but that is up to you
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
  }   
	
    // Display an Alert message for an error or failure.
    protected void displayAlert( String title, String message ) {
        AlertDialog.Builder confirm = new AlertDialog.Builder( this );
        confirm.setTitle( title);
        confirm.setMessage( message );
        confirm.setNegativeButton( "OK", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog, int which ) {
            	PatientEKGMain.this.finish();
            }
        } );
        confirm.show().show();                
    }
    
	public String GetUniqueFilename(){
		String fname = "log_" + GetDatetimeString();// make file name
		int appendInt = 1;
		
//		while(true) {
//			try // Throws exception if object doesn't exist
//			{
//				S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, fname));
//				if (object == null)
//				{
//					break;
//				}
//			} catch (AmazonS3Exception ex) {
//				break;
//			}
//			fname = String.format(fname+"-%d", appendInt);
//			appendInt++;
//		}
		fname = String.format(fname+"-%d", appendInt);
		appendInt++;
		return fname + ".txt";
	}
//	
	public String GetDatetimeString(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();
        return dateFormat.format(date);
	}
//	
	public void CreateBucketIfDoesntExist(){
		if (!s3Client.doesBucketExist(bucketName));
		{
			s3Client.createBucket(bucketName);
        	Log.i("Upload Info", "Created bucket: " + bucketName);
		}
	}

}
