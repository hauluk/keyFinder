package com.hauer.keyfinder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import com.example.keyfinder.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class KeyFinder extends Activity {
	
	private static final String TAG = KeyFinder.class.getSimpleName();
	private final int ACTION_REQUEST_ENABLE = 1;
	private BluetoothAdapter btAdapter;
	private TextView out;
	
	//Estimote SDK
	private static final String ESTIMOTE_PROXIMITY_UUID = "b9407f30-f5f8-466e-aff9-25556b57fe6d";
	private static final String ESTIMOTE_DEVICE_NAME ="Estimote Beacon";
	private static final String ESTIMOTE_MAC_ADDRESS = "EB:26:A0:11:5A:1C";
	private static final int ESTIMOTE_PROXIMITY_Major = 31390;	//std value 23068
	private static final int ESTIMOTE_PROXIMITY_Minor = 13370;	//std value 40977
	private static final int ESTIMOTE_MEASURED_POWER = -74;
	private static final int ESTIMOTE_RSSI = -34;
	private static final Region MY_ESTIMOTE_iBeacon = new Region("regionID",	ESTIMOTE_PROXIMITY_UUID, 
																				ESTIMOTE_PROXIMITY_Major,
																				ESTIMOTE_PROXIMITY_Minor);
	private BeaconManager beaconManager;
	private Beacon estimoteBeacon = new Beacon(ESTIMOTE_PROXIMITY_UUID,
												ESTIMOTE_DEVICE_NAME, 
												ESTIMOTE_MAC_ADDRESS,
												ESTIMOTE_PROXIMITY_Major, 
												ESTIMOTE_PROXIMITY_Minor, 
												ESTIMOTE_MEASURED_POWER,
												ESTIMOTE_RSSI);	
			
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_finder);
        
        //TextView integrated in a ScrollView
        out = (TextView) findViewById(R.id.out);
        
        //Get BT Adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        
        //Check if Bluetooth is available on the device
        if(btAdapter == null){
        	new AlertDialog.Builder(this) 
            .setMessage("Bluetooth modul is not existing!")
            .setNeutralButton("Ok", null)
            .show();
        }else{
        	out.append("Bluetooth is activated...\n");
        	out.append("Search started!\n");
        	
            //Bluetooth enable if disabled
            btStatus();       
            
            
            beaconManager = new BeaconManager(this);
            beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);
            beaconManager.setRangingListener(new BeaconManager.RangingListener() {
				@Override
				public void onBeaconsDiscovered(Region MY_ESTIMOTE_iBeacon, final List<Beacon> rangedBeacons) {
					for(Beacon rangedBeacon : rangedBeacons){
						if(rangedBeacon.getMacAddress().equals(estimoteBeacon.getMacAddress())){
							if(rangedBeacon.getMajor() == estimoteBeacon.getMajor()){
								if(rangedBeacon.getMinor() == estimoteBeacon.getMinor()){
									estimoteBeacon = rangedBeacon;
									//out.append("\nMac "+rangedBeacon.getMeasuredPower());
									detectKey(estimoteBeacon);
								}
							}
						}
					}
				}
			});
           
            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
				@Override
				public void onServiceReady() {
					try{
						beaconManager.startRanging(MY_ESTIMOTE_iBeacon);
					}catch(RemoteException e){
						Log.e(TAG,"Cannot start ranging!",e);
					}
				}
			});
            
        }
		
       
        
        
    //onCreate End    
    }


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.key_finder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void btStatus(){
    	
    	if(btAdapter.isEnabled() == false){
    		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    		startActivityForResult(enableBtIntent, ACTION_REQUEST_ENABLE);
    	}
    }
    
    private void detectKey(Beacon estimoteBeacon) {
		
    	double accuracy = Utils.computeAccuracy(estimoteBeacon);

    	if(accuracy == -1){
    		out.append("Beacon is not in range!");
    	}
    	else{
    		out.append("\nDistance in meter: "+ (Math.round(accuracy*100.0)/100.0));
    	}
	}      
}

