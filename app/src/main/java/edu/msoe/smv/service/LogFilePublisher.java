package edu.msoe.smv.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import edu.msoe.smv.utility.Utility;

/**
 * Created by austin on 1/31/16.
 */
public class LogFilePublisher extends Handler {

    private final FileWriter logFileWriter;
    private List<String> csvHeaders = new LinkedList<>();

    public LogFilePublisher() throws IOException {
        String fileName = "daq-data.csv";
        String logPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator
                + fileName;
        File logFile = new File(logPath);
        logFileWriter = new FileWriter(logFile, true);
    }

    @Override
    public void handleMessage(Message message) {
        // publish to pit view
        Bundle receiveData = message.getData();
        JSONObject objectSend = new JSONObject();
        for (String key : receiveData.keySet()) {
            try {
                objectSend.put(key, receiveData.getDouble(key));
            } catch (JSONException e) {
                // why is this a checked exception???
                // rethrow unchecked. It's not like we're letting the user build the object.
                throw new UnsupportedOperationException(e.getMessage());
            }
        }

        try {
            writeToCSV(receiveData);
        } catch (IOException e) {
            Log.e("I/O Error", e.getMessage());
        }
    }

    /**
     * logs the data point to csv file, will include headers if headers haven't
     * been written yet for this key set of values
     * @throws IOException
     */
    private void writeToCSV(Bundle object) throws IOException {
        if (!csvHeaders.containsAll(object.keySet())) {
            csvHeaders = new LinkedList<>(object.keySet());
            logFileWriter.write(Utility.stringJoin(csvHeaders, ",") + "\n");
        }

        List<String> values = new LinkedList<>();
        for (String key : csvHeaders) {
            values.add("" + object.getDouble(key));
        }

        logFileWriter.write(Utility.stringJoin(values, ",") + "\n");
    }
}
