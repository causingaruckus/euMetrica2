package com.eumetrica.em2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class PatientMain extends Activity {

	Button notifyButton;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_main);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        notifyButton = (Button) findViewById(R.id.patient_notifications);
        notifyButton.setOnClickListener(patientNotificationsHandler);
	}

	View.OnClickListener patientNotificationsHandler = new View.OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(PatientMain.this, PatientNotifications.class);
			startActivity(i);
		}
	};
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_patient_main, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
