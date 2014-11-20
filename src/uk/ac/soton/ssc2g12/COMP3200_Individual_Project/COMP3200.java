package uk.ac.soton.ssc2g12.COMP3200_Individual_Project;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class COMP3200 extends Activity {

    private static final String TAG = "COMP3200";
    private int logInterval = 5000;
    private int timerInterval = 83;

    private WifiManager wifiManager;
    private Handler mHandler;

    private int scanStatus;
    private long lastLogTime;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        TextView initialisedField = (TextView) findViewById(R.id.initialised_field);
        initialisedField.setText("Application initialised. Time to next log:");

        mHandler = new Handler();

        startRepeatingTasks();
    }

    @Override
    public void onDestroy() {
        stopRepeatingTasks();
        Log.v(TAG, "Shutdown complete.");
        super.onDestroy();
    }

    void startRepeatingTasks() {
        scanStatus = 1;
        logRunnable.run();
        timerRunnable.run();
    }

    void stopRepeatingTasks() {
        mHandler.removeCallbacks(logRunnable);
        mHandler.removeCallbacks(timerRunnable);
    }

    private final Runnable logRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                Log.v(TAG, "Running logger.");
                WifiInfo info = wifiManager.getConnectionInfo();

                TextView recordedDataField = (TextView) findViewById(R.id.recorded_data_field);
                StringBuilder recordedDataSB = new StringBuilder();

                // register next log run
                lastLogTime =  System.currentTimeMillis();
                mHandler.postDelayed(this, logInterval);

                String ssid = info.getSSID();
                if (false) {
                //if (ssid == null) {
                    // don't waste log space with unconnected logging
                    // clear recorded data field
                    if (scanStatus == 1) {
                        recordedDataField.setText("No data to record - not connected to network.");
                        scanStatus++;
                    } else if (scanStatus == 2) {
                        recordedDataField.setText("No data to record - not connected to network..");
                        scanStatus++;
                    } else if (scanStatus == 3) {
                        recordedDataField.setText("No data to record - not connected to network...");
                        scanStatus = 1;
                    }
                } else {
                    // start logging data
                    String stringSSID = "SSID=" + ssid;
                    Log.d(TAG, stringSSID);
                    recordedDataSB.append(stringSSID);
                    recordedDataSB.append("\n");

                    int rssi = info.getRssi();
                    int signalPercent = WifiManager.calculateSignalLevel(rssi, 100);
                    String stringSignalStrength = "SignalStrength=" + signalPercent;
                    Log.d(TAG, stringSignalStrength);
                    recordedDataSB.append(stringSignalStrength);
                    recordedDataSB.append("\n");

                    String bssid = info.getBSSID();
                    String stringBSSID = "BSSID=" + bssid;
                    Log.d(TAG, stringBSSID);
                    recordedDataSB.append(stringBSSID);
                    recordedDataSB.append("\n");

                    int speed = info.getLinkSpeed();
                    String stringLinkSpeed = "LinkSpeed=" + speed;
                    Log.d(TAG, stringLinkSpeed);
                    recordedDataSB.append(stringLinkSpeed);
                    recordedDataSB.append("\n");

                    int ip = info.getIpAddress();
                    String stringIP = "IpAddress=" + ip;
                    Log.d(TAG, stringIP);
                    recordedDataSB.append(stringIP);
                    recordedDataSB.append("\n");

                    // display recorded data on device screen
                    recordedDataField.setText(recordedDataSB.toString());

                }
            } catch (Exception e) {
                Log.e(TAG, "LogFailed=", e);
            }
        }
    };


    private final Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            TextView timerField = (TextView) findViewById(R.id.countdown_field);

            // register next timer run
            mHandler.postDelayed(this, timerInterval);

            // display countdown on device screen
            Long deltaTime = logInterval - (System.currentTimeMillis() - lastLogTime);
            timerField.setText(deltaTime.toString());
        }
    };
}
