package com.example.breathalyzer;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DrunkActivity extends Activity {

	private Button textFriendButton;
	private Button textCabButton;
	private SharedPreferences prefs;
	private String friendNumber;
	private String cabNumber;
	private Address address;
	private BroadcastReceiver singleUpdateReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drunk);
		
		textFriendButton = (Button) findViewById(R.id.textFriend_button);
		textCabButton = (Button) findViewById(R.id.textCab_button);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		checkSavedData();
		
		friendNumber = prefs.getString("breathalyzer_friendNumber", "");
		cabNumber = prefs.getString("breathalyzer_cabNumber", "");
		
		textFriendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				textFriend();
			}
		});
		
		textCabButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				textCab();
			}
		});
	}
	
	protected void textCab() {
		getLocationUpdate(cabNumber, "");
	}

	protected void textFriend() {
		String message = "Hi Friend! I am currently drunk and will be unable to " + 
				"drive back home. Can you please pick me up from ";
		
		getLocationUpdate(friendNumber, message);
	}
	
	protected String getAddressString(Address address){
		String add = "";
		for(int i=0; i< address.getMaxAddressLineIndex(); i++){
			add += address.getAddressLine(i) + ", ";
		}
		
		add = add.substring(0,add.length()-2);
		return add;
	}
	
	protected void getLocationUpdate(final String phoneNumber, final String message){
		final LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String REQUESTED = "LOCATION_REQUESTED";
		
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(REQUESTED), PendingIntent.FLAG_UPDATE_CURRENT);
		
		singleUpdateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				 context.unregisterReceiver(singleUpdateReceiver);
			      String key = LocationManager.KEY_LOCATION_CHANGED;
			      Location location = (Location)intent.getExtras().get(key);
			      
			      if (location != null){
			    	  convertLocationToAddress(location);
			    	  
			    	  if(address == null){
			  			showToast("Unable to find current address");
			    	  } else{
			    		  String finalMessage = message + getAddressString(address);
			    		  sendSMS(phoneNumber, finalMessage);
			    	  }
			      }

			      mlocManager.removeUpdates(pendingIntent);
			}
		};
		
		registerReceiver(singleUpdateReceiver, new IntentFilter(REQUESTED));
		
		mlocManager.requestSingleUpdate(mlocManager.getBestProvider(criteria, true), pendingIntent);
		
	}
	
	protected void convertLocationToAddress(Location loc) {
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		
		if(loc == null){
			address = null;
			return;
		}
		
		Address currentAddress = null;
		List<Address> addresses;
		try {
			addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
			if(addresses == null){
				address = null;
				return;
			}
			currentAddress = addresses.get(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		address = currentAddress;
	}

	/**
	 * This function implements the functionality of getting the geo location
	 * and sending it to the phone number entered by the user.
	 */
	protected void sendSMS(final String phoneNumber, final String message) {
		String SENT = "SMS_SENT";

		PendingIntent sent = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(getBaseContext(), "SMS Sent!", Toast.LENGTH_SHORT).show();
					ContentValues values = new ContentValues();

					values.put("address", phoneNumber);
					values.put("body", message); 

					getApplicationContext().getContentResolver().insert(Uri.parse("content://sms/sent"),    values);
					return;
				}

			}
		}, new IntentFilter(SENT));

		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phoneNumber.toString(), null,message, sent, null);
	}

	private void checkSavedData() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(!prefs.getBoolean("breathalyzer_savedData", false)){
			openSettings("SaveData");
		}
	}
	
	protected void openSettings(String toastInfo) {
		Intent i = new Intent(DrunkActivity.this, SettingsActivity.class);
		if(toastInfo.equals("SaveData")){
			showToast("Please setup the application first!");
		}
		startActivity(i);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.drunk, menu);
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
