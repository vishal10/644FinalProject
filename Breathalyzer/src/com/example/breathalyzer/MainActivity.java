package com.example.breathalyzer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView homescreen_text;
	private Button settings_button;
	private Button drunk_button;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String preNameText = "\t\t\t\t\t\t\t\t\tWelcome ";
		String postNameText = "! \nHope you are having a great time!";
		
		checkSavedData();

		settings_button = (Button) findViewById(R.id.settings_button);
		drunk_button = (Button) findViewById(R.id.drunk_button);
		homescreen_text = (TextView) findViewById(R.id.homescreen_text);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		homescreen_text.setText(preNameText + prefs.getString("breathalyzer_name", "Vishal") + postNameText);
		
		settings_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				openSettings("");
			}
		});
	}

	protected void openSettings(String toastInfo) {
		Intent i = new Intent(MainActivity.this, SettingsActivity.class);
		if(toastInfo.equals("SaveData")){
			showToast("Please setup the application first!");
		}
		startActivity(i);
		finish();
	}

	private void checkSavedData() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(!prefs.getBoolean("breathalyzer_savedData", false)){
			openSettings("SaveData");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void showToast(String msg) {
		Toast toast = new Toast(getApplicationContext());
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 0);
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_layout,
				(ViewGroup) findViewById(R.id.toast_layout_root));
		toast.setView(layout);
		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(msg);
		toast.show();
	}

}
