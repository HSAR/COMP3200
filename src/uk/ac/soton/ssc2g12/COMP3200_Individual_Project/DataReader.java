package uk.ac.soton.ssc2g12.COMP3200_Individual_Project;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by HSAR on 01/12/2014.
 */
public class DataReader {

    public DataReader() {
        System.out.println("Enter file name:");

        //  open up standard input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String fileName = null;

        try {
            fileName = br.readLine();
        } catch (IOException ioe) {
            System.out.println("Critical failure.");
            System.exit(1);
        }

        File dataFile = new File("./" + fileName);
        if (!dataFile.exists()) {
            System.out.println("No data file.");
            System.exit(1);
        }

        // Gather and process data into structured classes
        ArrayList<String> rawLines = null;
        ArrayList<DataRecord> dataRecords = null;
        try {
            rawLines = getAllLines(dataFile);
            dataRecords = DataRecord.parseRecords(rawLines);
            System.out.println("DEBUG: Parsed " + dataRecords.size() + " records.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (dataRecords != null && dataRecords.size() > 0) {
            // get the current date in short string format to use as filename
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            String saveFileName = dateformat.format((new Date()).getTime()) + ".txt";
            // declare savefile object
            File saveFile = new File(saveFileName);
            if (!saveFile.exists()) {
                try {
                    // tell Java to create the file
                    saveFile.createNewFile();
                } catch (IOException e) {
                    System.err.println(e.toString());
                }
            }

            // create and initialise PrintStream object
            PrintStream printstream;
            try {
                printstream = new PrintStream(
                        new FileOutputStream(saveFileName, true));

                // Write recorded data out into CSV format
                if (dataRecords != null) {
                    for (DataRecord record : dataRecords) {
                        printstream.println(record.toString());
                    }
                }
                // close file to reduce memory leaks
                printstream.flush();
                printstream.close();
            } catch (FileNotFoundException e) {
                // this is a non-critical method and so does not stack-trace on fail
                System.err.println(e.toString());
            }
        }
    }

    private ArrayList<String> getAllLines(File file) throws IOException {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // instantiate the reader object
        BufferedReader reader = new BufferedReader(fileReader);

        ArrayList<String> fileLines = new ArrayList<>();
        String tempLine = null;
        do {
            tempLine = reader.readLine();
            fileLines.add(tempLine);
            // when templine returns null it is assumed we have reached EOF
        } while (tempLine != null);
        // when read complete close file
        try {
            reader.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
        // return the arraylist
        System.out.println("DEBUG: Read " + fileLines.size() + " lines.");
        return fileLines;
    }

    public static void main(String[] args) {
        new DataReader();
    }
}
