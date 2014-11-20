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
    private int mInterval = 5000; // 5 seconds by default, can be changed later

    private WifiManager wifiManager;
    private Handler mHandler;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        TextView initialisedField = (TextView) findViewById(R.id.initialised_field);
        initialisedField.setText("Application initialised.");

        mHandler = new Handler();

        startRepeatingTask();
    }

    void startRepeatingTask() {
        logRunnable.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(logRunnable);
    }

    private final Runnable logRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                Log.v(TAG, "Running logger");
                WifiInfo info = wifiManager.getConnectionInfo();

                TextView recordedDataField = (TextView) findViewById(R.id.recorded_data_field);
                StringBuilder recordedDataSB = new StringBuilder();

                // register next log run
                mHandler.postDelayed(logRunnable, mInterval);

                String ssid = info.getSSID();
                if (ssid == null) {
                    // don't waste log space with unconnected logging
                    // clear recorded data field
                    recordedDataField.setText("No data to record - not connected to network.");
                } else {
                    // start logging data
                    String stringSSID = "SSID=" + ssid;
                    Log.d(TAG, stringSSID);
                    recordedDataSB.append(stringSSID);

                    int rssi = info.getRssi();
                    int signalPercent = WifiManager.calculateSignalLevel(rssi, 100);
                    String stringSignalStrength = "SignalStrength=" + signalPercent;
                    Log.d(TAG, stringSignalStrength);
                    recordedDataSB.append(stringSignalStrength);

                    String bssid = info.getBSSID();
                    String stringBSSID = "BSSID=" + bssid;
                    Log.d(TAG, stringBSSID);
                    recordedDataSB.append(stringBSSID);

                    int speed = info.getLinkSpeed();
                    String stringLinkSpeed = "LinkSpeed=" + speed;
                    Log.d(TAG, stringLinkSpeed);
                    recordedDataSB.append(stringLinkSpeed);

                    int ip = info.getIpAddress();
                    String stringIP = "IpAddress=" + ip;
                    Log.d(TAG, stringIP);
                    recordedDataSB.append(stringIP);

                    // display recorded data on device screen
                    recordedDataField.setText(recordedDataSB.toString());

                }
            } catch (Exception e) {
                Log.e(TAG, "LogFailed=", e);
            }
        }
    };
}
