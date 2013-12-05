package com.example.breathalyzer;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends Activity {
	
	private EditText yourName;
	private EditText friendsNumber;
	private EditText cabNumber;
	private Button saveButton;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		yourName = (EditText) findViewById(R.id.nameInput);
		friendsNumber = (EditText) findViewById(R.id.friendsNumberInput);
		cabNumber = (EditText) findViewById(R.id.cabNumberInput);
		saveButton = (Button) findViewById(R.id.saveButton);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		initData();
		
		saveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveData();
			}
		});
		
	}

	private void initData() {
		yourName.setText(prefs.getString("breathalyzer_name", ""));
		friendsNumber.setText(prefs.getString("breathalyzer_friendNumber", ""));
		cabNumber.setText(prefs.getString("breathalyzer_cabNumber", "4123218294"));
	}

	protected void saveData() {
		String name = yourName.getText().toString();
		String fnumber = friendsNumber.getText().toString();
		String cnumber = cabNumber.getText().toString();
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("breathalyzer_name", name);
		editor.putString("breathalyzer_friendNumber", fnumber);
		editor.putString("breathalyzer_cabNumber", cnumber);
		editor.putBoolean("breathalyzer_savedData", true);
		editor.commit();
		
		goToMainActivity();
	}

	private void goToMainActivity() {
		Intent i = new Intent(SettingsActivity.this, MainActivity.class);
		startActivity(i);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

}
