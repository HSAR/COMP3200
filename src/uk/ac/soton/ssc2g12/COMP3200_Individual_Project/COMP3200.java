package uk.ac.soton.ssc2g12.COMP3200_Individual_Project;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class COMP3200 extends Activity {

    private static final String TAG = "COMP3200";
    private static final long LOCATION_REFRESH_TIME = 0l;
    private static final float LOCATION_REFRESH_DISTANCE = 0;
    // Default 5000 (5s), set to 0 to disable repeat
    private int logInterval = 0;
    private int timerInterval = 83;

    private WifiManager wifiManager;
    private LocationManager locationManager;
    private Handler mHandler;

    private Location lastLocation;

    private int scanStatus;
    private long lastLogTime;



    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            lastLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, locationListener);

        setContentView(R.layout.main);

        final Button button = (Button) findViewById(R.id.manual_capture_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                logRunnable.run();
            }
        });

        TextView initialisedField = (TextView) findViewById(R.id.initialised_field);
        initialisedField.setText("Application initialised.");

        mHandler = new Handler();

        //startRepeatingTasks();
    }

    @Override
     public void onPause() {
        super.onPause();  // Always call the superclass method first

        // Assume user is changing location between movements
        locationManager.removeUpdates(locationListener);
        lastLocation = null;
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        // Re-enable location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, locationListener);
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

    private void stopRepeatingTasks() {
        mHandler.removeCallbacks(logRunnable);
        mHandler.removeCallbacks(timerRunnable);
    }

    private void RequestLocation() {
        lastLocation = null;

        Criteria criteriaForLocationService = new Criteria();
        criteriaForLocationService.setAccuracy(Criteria.ACCURACY_COARSE);
        List<String> acceptableLocationProviders = locationManager.getProviders(criteriaForLocationService, true);
        for (String provider: acceptableLocationProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {

                if (lastLocation == null || location.getAccuracy() < lastLocation.getAccuracy()) {
                    lastLocation = location;
                }
            }
        }
    }

    private final Runnable logRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                //Log.v(TAG, "Running logger.");
                WifiInfo info = wifiManager.getConnectionInfo();

                TextView wifiDataField = (TextView) findViewById(R.id.wifi_data_field);
                StringBuilder wifiDataSB = new StringBuilder();

                TextView gpsDataField = (TextView) findViewById(R.id.gps_data_field);
                StringBuilder gpsDataSB = new StringBuilder();

                // register next log run
                lastLogTime = System.currentTimeMillis();
                if (logInterval != 0) {
                    mHandler.postDelayed(this, logInterval);
                }

                String ssid = info.getSSID();
                //if (false) {
                if (ssid == null) {
                    // don't waste log space with unconnected logging
                    // clear recorded data field
                    if (scanStatus == 1) {
                        wifiDataField.setText("No data to record - not connected to network.");
                        scanStatus++;
                    } else if (scanStatus == 2) {
                        wifiDataField.setText("No data to record - not connected to network..");
                        scanStatus++;
                    } else if (scanStatus == 3) {
                        wifiDataField.setText("No data to record - not connected to network...");
                        scanStatus = 1;
                    }
                } else {
                    // start logging data
                    String stringSSID = "SSID=" + ssid;
                    Log.d(TAG, stringSSID);
                    wifiDataSB.append(stringSSID);
                    wifiDataSB.append("\n");

                    int rssi = info.getRssi();
                    int signalPercent = WifiManager.calculateSignalLevel(rssi, 100);
                    String stringSignalStrength = "SignalStrength=" + signalPercent;
                    Log.d(TAG, stringSignalStrength);
                    wifiDataSB.append(stringSignalStrength);
                    wifiDataSB.append("\n");

                    String bssid = info.getBSSID();
                    String stringBSSID = "BSSID=" + bssid;
                    Log.d(TAG, stringBSSID);
                    wifiDataSB.append(stringBSSID);
                    wifiDataSB.append("\n");

                    int speed = info.getLinkSpeed();
                    String stringLinkSpeed = "LinkSpeed=" + speed;
                    Log.d(TAG, stringLinkSpeed);
                    wifiDataSB.append(stringLinkSpeed);
                    wifiDataSB.append("\n");

                    int ip = info.getIpAddress();
                    String stringIP = "IpAddress=" + ip;
                    Log.d(TAG, stringIP);
                    wifiDataSB.append(stringIP);
                    wifiDataSB.append("\n");

                    // display data
                    wifiDataField.setText(wifiDataSB.toString());

                }

                Location bestResult;
                float bestAccuracy = Float.MAX_VALUE;
                long bestTime = 0l;

                List<String> matchingProviders = locationManager.getAllProviders();
                for (String provider: matchingProviders) {
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        float accuracy = location.getAccuracy();
                        long time = location.getTime();

                        long minTime = 0l;

                        if ((time > minTime && accuracy < bestAccuracy)) {
                            bestResult = location;
                            bestAccuracy = accuracy;
                            bestTime = time;
                        }
                        else if (time < minTime &&
                            bestAccuracy == Float.MAX_VALUE && time > bestTime){
                            bestResult = location;
                            bestTime = time;
                        }
                    }
                }

                RequestLocation();
                //if (false) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    gpsDataField.setText("No data to record - GPS disabled");
                } else if (lastLocation == null) {
                    gpsDataField.setText("No data to record - location unavailable");
                } else {
                    // start logging data
                    String stringLat = "Lat=" + lastLocation.getLatitude();
                    Log.d(TAG, stringLat);
                    gpsDataSB.append(stringLat);
                    gpsDataSB.append("\n");

                    String stringLong = "Long=" + lastLocation.getLongitude();
                    Log.d(TAG, stringLong);
                    gpsDataSB.append(stringLong);
                    gpsDataSB.append("\n");

                    String stringAcc = "Acc=" + lastLocation.getAccuracy();
                    Log.d(TAG, stringAcc);
                    gpsDataSB.append(stringAcc);
                    gpsDataSB.append("\n");

                    String stringSpeed = "Speed=" + lastLocation.getSpeed();
                    Log.d(TAG, stringSpeed);
                    gpsDataSB.append(stringSpeed);
                    gpsDataSB.append("\n");

                    // display data
                    gpsDataField.setText(gpsDataSB.toString());

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
            if (logInterval != 0) {
                mHandler.postDelayed(this, timerInterval);
            }

            // display countdown on device screen
            Long deltaTime = logInterval - (System.currentTimeMillis() - lastLogTime);
            timerField.setText(deltaTime.toString());
        }
    };
}
