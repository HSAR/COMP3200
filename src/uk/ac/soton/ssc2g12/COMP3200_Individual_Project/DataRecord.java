package uk.ac.soton.ssc2g12.COMP3200_Individual_Project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by HSAR on 01/12/2014.
 */
public class DataRecord {

    protected Date date;
    protected String SSID;
    protected int signalStrength;
    protected String BSSID;
    protected int linkSpeed;
    protected String IP;
    protected double latitude;
    protected double longitude;
    protected float accuracy;
    protected float speed;

    public DataRecord(ArrayList<String[]> data) throws ParseException {
        if (data == null || data.size() < 1) {
            throw new IllegalArgumentException();
        } else {
            // Parse data into fields

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            // Set the date from the first entry
            date = dateFormat.parse(data.get(0)[0]);
            for (String[] entry : data) {
                // Check all records have the same date
                /*Date tempDate = dateFormat.parse(entry[0]);
                if (date.compareTo(tempDate) != 0) {
                    throw new IllegalArgumentException();
                }*/

                // Check field name and set value accordingly
                String fieldName = entry[1];
                String value = entry[2];
                if (fieldName.equalsIgnoreCase("SSID")) {
                    // SSIDs can be in quotes
                    if (value.contains("\"")) {
                        SSID = value.substring(1, value.length() - 1);
                    } else {
                        SSID = value;
                    }
                } else if (fieldName.equalsIgnoreCase("SignalStrength")) {
                    signalStrength = Integer.parseInt(value);
                } else if (fieldName.equalsIgnoreCase("BSSID")) {
                    BSSID = value;
                } else if (fieldName.equalsIgnoreCase("LinkSpeed")) {
                    linkSpeed = Integer.parseInt(value);
                } else if (fieldName.equalsIgnoreCase("IpAddress")) {
                    IP = value;
                } else if (fieldName.equalsIgnoreCase("Lat")) {
                    latitude = Double.parseDouble(value);
                } else if (fieldName.equalsIgnoreCase("Long")) {
                    longitude = Double.parseDouble(value);
                } else if (fieldName.equalsIgnoreCase("Acc")) {
                    accuracy = Float.parseFloat(value);
                } else if (fieldName.equalsIgnoreCase("Speed")) {
                    speed = Float.parseFloat(value);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(date.getTime());
        sb.append(",");
        sb.append(SSID);
        sb.append(",");
        sb.append(signalStrength);
        sb.append(",");
        sb.append(BSSID);
        sb.append(",");
        sb.append(linkSpeed);
        sb.append(",");
        sb.append(IP);
        sb.append(",");
        sb.append(latitude);
        sb.append(",");
        sb.append(longitude);
        sb.append(",");
        sb.append(accuracy);
        sb.append(",");
        sb.append(speed);
        return sb.toString();
    }

    public static ArrayList<DataRecord> parseRecords(ArrayList<String> data) throws ParseException {
        // Parse a continuous string of records into discrete DataRecord objects
        ArrayList<DataRecord> result = new ArrayList<DataRecord>();
        ArrayList<String[]> recordData = new ArrayList<String[]>();
        boolean recording = false;
        for (int i = 0; i < data.size(); i++) {

            final String line = data.get(i);
            if (line == null) {
                // I don't even know how this can happen, but it does
            } else if (line.contains("START") || line.contains("BEGIN")) {
                recording |= true;
            } else if (line.contains("END")) {
                // record complete, create DataRecord object
                result.add(new DataRecord(recordData));
                recordData = new ArrayList<>();
            } else {
                // if the line is neither BEGIN nor END, add to record
                // sanity check, must contain | and = dividing date, field name and value
                if (recording && line.contains("|") && line.contains("=")) {
                    recordData.add(line.split("=|\\|"));
                }
            }

        }
        return result;
    }
}
