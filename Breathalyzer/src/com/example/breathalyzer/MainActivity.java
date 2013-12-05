package com.example.breathalyzer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView homescreen_text;
	private TextView myLabel;
	private Button settings_button;
	private Button drunk_button;
	// Button to connect and listen for BT stuff
	private Button connect_button;
	private SharedPreferences prefs;
	
	//BT stuff
	private int REQUEST_ENABLE_BT = 5;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    private int bytesAvailable;
    private int bytesRead;
    
    // Global counter for debugging purposes
    private int count = 0;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String preNameText = "\t\t\t\t\t\t\t\t\tWelcome ";
		String postNameText = "! \nHope you are having a great time!";
		
		checkSavedData();

		settings_button = (Button) findViewById(R.id.settings_button);
		connect_button = (Button) findViewById(R.id.connect_button);
		drunk_button = (Button) findViewById(R.id.drunk_button);
		homescreen_text = (TextView) findViewById(R.id.homescreen_text);
        myLabel = (TextView)findViewById(R.id.label);

		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		homescreen_text.setText(preNameText + prefs.getString("breathalyzer_name", "Vishal") + postNameText);
		
		settings_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				openSettings("");
			}
		});
		connect_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//makeNotification();
				try{
					findBT();
					openBT();
				}
				catch(IOException e){
					myLabel.setText("IOEXception");
				}
			}
		});
		drunk_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				gotoDrunkPage();
				/*try {
					//closeBT();
					//gotoDrunkPage();
					//sendData();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					myLabel.setText("Failed to close bt");
				}*/
			}
		});
	}
	
	protected void gotoDrunkPage() {
		Intent i = new Intent(MainActivity.this, DrunkActivity.class);
		startActivity(i);
	}
	
    private void sendData() throws IOException
    {
        String msg = "Android";
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Data Sent");
    }

	private void findBT() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			System.out.println("Does not support Bluetooth");
		    // Device does not support Bluetooth
		}
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("v")) //Note, you will need to change this to match the name of your device
                {
                    mmDevice = device;
                    myLabel.setText("Number of open devices: " + pairedDevices.size() + " Connected to: " + mmDevice.getName());
                    break;
                }
            }
        }
	}
	
	private void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);        
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        
        beginListenForData();
        
        myLabel.setText("Bluetooth Opened");

	}
	
    private void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
    }

    private void beginListenForData()
    {
        final Handler handler = new Handler(); 
        final byte delimiter = 10; //This is the ASCII code for a newline character
        
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {                
               while(!Thread.currentThread().isInterrupted() && !stopWorker)
               {
                    try 
                    {
                    	//count++;
                        bytesAvailable = mmInputStream.available(); 
                        bytesRead = mmInputStream.read(readBuffer);
                        byte[] encodedBytes = new byte[bytesRead];
                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                        final String data = new String(encodedBytes, "US-ASCII");

                        handler.post(new Runnable()
                        {
                            public void run()
                            {
                                myLabel.setText(bytesRead + " Bytes Read, and " + bytesAvailable + " Bytes available, and Data: " + data);
                                makeNotification();
                                // Close BT since we want to ignore future calls
                                /*try {
									closeBT();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}*/
                            }
                        }); 
                    }
                    catch (IOException ex) 
                    {
                        stopWorker = true;
                        myLabel.setText("IOException on Listenting");
                    }
               }
            }
        });

        workerThread.start();
    }


	protected void openSettings(String toastInfo) {
		Intent i = new Intent(MainActivity.this, SettingsActivity.class);
		if(toastInfo.equals("SaveData")){
			showToast("Please setup the application first!");
		}
		startActivity(i);
	}

	private void makeNotification() {
        // Create Notification
        NotificationCompat.Builder mBuilder =
        	    new NotificationCompat.Builder(MainActivity.this)
        	    .setVibrate(new long[3])
        	    .setSmallIcon(R.drawable.ic_launcher)
        	    .setContentTitle("You're Drunk!!")
        	    .setContentText("JacketBuddy think you may have had one too many brewskis, bro. Might want to text a friend/cab to pick you up");
        Intent resultIntent = new Intent(this, DrunkActivity.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
            PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = 
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
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
