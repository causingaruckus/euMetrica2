package com.eumetrica.em2;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends Activity {

	Button patientLogin;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		patientLogin = (Button) findViewById(R.id.patient_login);
		patientLogin.setOnClickListener(patientLoginHandler);
	}

	View.OnClickListener patientLoginHandler = new View.OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(LoginActivity.this, PatientMain.class);
			startActivity(i);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

}
