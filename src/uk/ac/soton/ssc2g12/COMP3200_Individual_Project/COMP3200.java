package uk.ac.soton.ssc2g12.COMP3200_Individual_Project;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

public class COMP3200 extends Activity {

    private static final String TAG = "COMP3200";
    private static final long LOCATION_REFRESH_TIME = 0l;
    private static final float LOCATION_REFRESH_DISTANCE = 0;

    // Default 5000 (5s), set to 0 to disable repeat
    // Wifi card samples at 0.5Hz, Nyquist sampling rate 1Hz (1000ms/sample)
    private static final int logInterval = 1000;
    private static final int timerInterval = 83;
    private static final int locationInterval = 500;

    private WifiManager wifiManager;
    private LocationManager locationManager;
    private Handler mHandler;

    private Location lastLocation;

    private int scanStatus;
    private String displayString;

    public int logNumber = 0;

    private final LogRunnable logRunnable = new LogRunnable();

    private final File logFile = new File(Environment.getExternalStorageDirectory(), "COMP3200/COMP3200_data.log");
    private static final Logger logger = Logger.getLogger(TAG);
    private ExecutorService threadExecutor;

    private class LogRunnable implements Runnable {
        public int groupNumber = 0;
        public long lastLogTime;

        COMP3200 master;

        public void setApplication(COMP3200 master) {
            this.master = master;
        }

        @Override
        public void run() {
            try {
                logger.info("--START--");
                // register next log run
                lastLogTime = System.currentTimeMillis();
                if (logInterval != 0) {
                    //mHandler.postDelayed(this, logInterval);
                }

                // record time - this serves as a unique ID for the record
                String stringTime = "Time=" + lastLogTime;
                logger.info(stringTime);

                // obtain connection info
                WifiInfo info = wifiManager.getConnectionInfo();

                // find user interface objects
                //TextView wifiDataField = (TextView) findViewById(R.id.wifi_data_field);
                StringBuilder wifiDataSB = new StringBuilder();

                //TextView gpsDataField = (TextView) findViewById(R.id.gps_data_field);
                //StringBuilder gpsDataSB = new StringBuilder();

                //TextView initialisedField = (TextView) findViewById(R.id.initialised_field);

                String ssid = info.getSSID();
                //if (false) {
                if (ssid == null) {
                    // don't waste log space with unconnected logging
                    // clear recorded data field
                    if (scanStatus == 1) {
                        wifiDataSB.append("No data to record - not connected to network.");
                        scanStatus++;
                    } else if (scanStatus == 2) {
                        wifiDataSB.append("No data to record - not connected to network..");
                        scanStatus++;
                    } else if (scanStatus == 3) {
                        wifiDataSB.append("No data to record - not connected to network...");
                        scanStatus = 1;
                    }
                } else {
                    // start logging data
                    String stringSSID = "SSID=" + ssid;
                    logger.info(stringSSID);
                    wifiDataSB.append(stringSSID);
                    wifiDataSB.append("\n");

                    int rssi = info.getRssi();
                    int signalPercent = WifiManager.calculateSignalLevel(rssi, 100);
                    String stringSignalDB = "SignalDB=" + rssi;
                    logger.info(stringSignalDB);
                    wifiDataSB.append(stringSignalDB);
                    wifiDataSB.append("\n");
                    String stringSignalStrength = "SignalStrength=" + signalPercent;
                    logger.info(stringSignalStrength);
                    wifiDataSB.append(stringSignalStrength);
                    wifiDataSB.append("\n");

                    String bssid = info.getBSSID();
                    String stringBSSID = "BSSID=" + bssid;
                    logger.info(stringBSSID);
                    wifiDataSB.append(stringBSSID);
                    wifiDataSB.append("\n");

                    int speed = info.getLinkSpeed();
                    String stringLinkSpeed = "LinkSpeed=" + speed;
                    logger.info(stringLinkSpeed);
                    wifiDataSB.append(stringLinkSpeed);
                    wifiDataSB.append("\n");

                    int ip = info.getIpAddress();
                    // convert to standard human-readable format
                    InetAddress currentAddress = InetAddress.getByAddress(BigInteger.valueOf(ip).toByteArray());
                    String stringIP = "IpAddress=" + currentAddress.getHostAddress();
                    logger.info(stringIP);
                    wifiDataSB.append(stringIP);
                    wifiDataSB.append("\n");

                    String stringDataGroup = "DataGroup=" + groupNumber;
                    logger.info(stringDataGroup);
                    wifiDataSB.append(stringDataGroup);
                    wifiDataSB.append("\n");

                    double bandwidth = TestBandwidth();
                    String stringBandwith = "Bandwidth=" + bandwidth;
                    logger.info(stringBandwith);
                    wifiDataSB.append(stringBandwith);
                    wifiDataSB.append("\n");
                    // display data
                    master.setDisplayText(wifiDataSB.toString());
                    //wifiDataField.setText(wifiDataSB.toString());

                }

                /*Location bestResult;
                float bestAccuracy = Float.MAX_VALUE;
                long bestTime = 0l;

                List<String> matchingProviders = locationManager.getAllProviders();
                for (String provider : matchingProviders) {
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        float accuracy = location.getAccuracy();
                        long time = location.getTime();

                        long minTime = 0l;

                        if ((time > minTime && accuracy < bestAccuracy)) {
                            bestResult = location;
                            bestAccuracy = accuracy;
                            bestTime = time;
                        } else if (time < minTime &&
                                bestAccuracy == Float.MAX_VALUE && time > bestTime) {
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
                    logger.info(stringLat);
                    gpsDataSB.append(stringLat);
                    gpsDataSB.append("\n");

                    String stringLong = "Long=" + lastLocation.getLongitude();
                    logger.info(stringLong);
                    gpsDataSB.append(stringLong);
                    gpsDataSB.append("\n");

                    String stringAcc = "Acc=" + lastLocation.getAccuracy();
                    logger.info(stringAcc);
                    gpsDataSB.append(stringAcc);
                    gpsDataSB.append("\n");

                    String stringSpeed = "Speed=" + lastLocation.getSpeed();
                    logger.info(stringSpeed);
                    gpsDataSB.append(stringSpeed);
                    gpsDataSB.append("\n");

                    // display data
                    gpsDataField.setText(gpsDataSB.toString());

                }*/
            } catch (Exception e) {
                Log.e(TAG, "LogFailed=", e);
            } finally {
                logger.info("---END---");
            }
        }
    }

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

        if (!logFile.exists()) {
            try {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "Fatal Log Exception", e);
            }
        }
        String logFilePathName = Environment.getExternalStorageDirectory() + "/COMP3200/COMP3200_data.log";

        try {
            FileHandler logHandler = new FileHandler(logFilePathName, Integer.MAX_VALUE, 1, true);
            logHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord r) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
                    StringBuilder sb = new StringBuilder(dateFormat.format(new Date(r.getMillis())));
                    sb.append("|");
                    sb.append(r.getMessage());
                    sb.append("\n");
                    return sb.toString();
                }
            });
            logger.setLevel(Level.ALL);
            logger.addHandler(logHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, locationListener);

        setContentView(R.layout.main);

        final Button button = (Button) findViewById(R.id.new_data_group_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // On button press, run a burst of three data points across 4 seconds
                //mHandler.postDelayed(logRunnable, 2000l);
                //mHandler.postDelayed(logRunnable, 4000l);
                //logRunnable.run();
                logRunnable.groupNumber++;
            }
        });

        TextView initialisedField = (TextView) findViewById(R.id.initialised_field);
        initialisedField.setText("Application initialised.");

        mHandler = new Handler();

        logRunnable.setApplication(this);
        // initialise threadExecutor
        threadExecutor = Executors.newFixedThreadPool(2);
        startRepeatingTasks();
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        // Reduce battery usage by removing location requests while in background
        locationManager.removeUpdates(locationListener);
        // Assume user is changing location between movements
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
        super.onDestroy();
        stopRepeatingTasks();
        threadExecutor.shutdownNow();
        for (java.util.logging.Handler h : logger.getHandlers()) {
            h.close();   //must call h.close or a .LCK file will remain.
        }
    }

    public void setDisplayText(String displayString) {
        this.displayString = displayString;
    }

    public void updateViews() {
        TextView wifiDataField = (TextView) findViewById(R.id.wifi_data_field);
        TextView countdownField = (TextView) findViewById(R.id.countdown_field);

        // show log number
        logNumber++;
        countdownField.setText("Record #" + logNumber);
        wifiDataField.setText(displayString);
    }

    void startRepeatingTasks() {
        if (logInterval != 0) {
            scanStatus = 1;
           taskRunnable.run();
            //logRunnable.run();
            //timerRunnable.run();
        }
        locationRunnable.run();
    }

    private void stopRepeatingTasks() {
        mHandler.removeCallbacks(logRunnable);
        mHandler.removeCallbacks(timerRunnable);
        locationManager.removeUpdates(locationListener);
    }

    private void RequestLocation() {
        lastLocation = null;

        Criteria criteriaForLocationService = new Criteria();
        criteriaForLocationService.setAccuracy(Criteria.ACCURACY_COARSE);
        List<String> acceptableLocationProviders = locationManager.getProviders(criteriaForLocationService, true);
        for (String provider : acceptableLocationProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                if (lastLocation == null || location.getAccuracy() < lastLocation.getAccuracy()) {
                    lastLocation = location;
                }
            }
        }
    }


    private final Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            TextView timerField = (TextView) findViewById(R.id.countdown_field);

            // register next timer run
            if (logInterval != 0) {
                mHandler.postDelayed(this, timerInterval);
            }

            // display countdown on device screen
            Long deltaTime = logInterval - (System.currentTimeMillis() - logRunnable.lastLogTime);
            timerField.setText(deltaTime.toString());
        }
    };

    private final Runnable taskRunnable = new Runnable() {

        @Override
        public void run() {
            if (logInterval != 0) {
                mHandler.postDelayed(this, logInterval);
            }
            threadExecutor.execute(logRunnable);
            updateViews();
        }
    };

    private final Runnable locationRunnable = new Runnable() {

        @Override
        public void run() {
            TextView initialisedField = (TextView) findViewById(R.id.initialised_field);

            if (lastLocation == null) {
                // if there is no location data to record, let the user know
                initialisedField.setText("Not ready to record - no location data");
                initialisedField.setTextColor(Color.RED);
                // check again in X seconds
                mHandler.postDelayed(this, locationInterval);
            } else {
                initialisedField.setText("Ready to record - location data available");
                initialisedField.setTextColor(Color.GREEN);
                // no need to readd the delayed action
            }
        }
    };

    public static double TestBandwidth() {
        try {
            // randomise the non-unique part of the URL to avoid cache hits that might throw off measurements
            String uuid = UUID.randomUUID().toString();
            // this url loads the same image no matter what the final part of the URL is
            //URL url = new URL("https://robertsspaceindustries.com/media/fif8480g2red0r/slideshow_pager/" + uuid + ".jpg");
            //URL url = new URL("https://robertsspaceindustries.com/media/fif8480g2red0r/slideshow/" + uuid + ".jpg");
            //URL url = new URL("https://robertsspaceindustries.com/media/fif8480g2red0r/source/" + uuid + ".jpg");
            URL url = new URL("https://www.google.co.uk/images/nav_logo195.png");

            // begin timer
            final long startTime = System.nanoTime();


            // check size of image
            final URLConnection connection = url.openConnection();
            final int size = sizeOfInputStream(connection.getInputStream());

            // end timer
            final long finishTime = System.nanoTime();

            // size of bitmap / time taken to download = bandwidth available
            double result = ((double) size) / ((finishTime - startTime) * 0.000000001d);
            //Log.i("BandwidthDataSize", ""+size);
            return result;
        } catch (IOException e) {
            //Log.e("BandwidthTestFail=", e.getMessage());
            return -1.0d;
        }
    }

    public static int sizeOfInputStream(InputStream is) {
        int len = 0;
        try {
            while (is.read() >= 0) {
                len += 1;
            }
            is.close();
            return len;
        } catch (IOException e) {
            return -1;
        }
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println("Bandwidth = " + TestBandwidth() + " bytes/sec");
        }
    }
}
