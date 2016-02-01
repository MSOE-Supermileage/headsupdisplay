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
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import edu.msoe.smv.Utility;
import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by austin on 1/31/16.
 */
public class DataNodeHandler extends Handler {

    public static final String URL = "http://supermileage.azurewebsites.net/";

    private long lastSendTime;

    // default starts at half a second
    private int movingPublishPeriod = 500;

    private final FileWriter logFileWriter;
    private List<String> csvHeaders = new LinkedList<>();

    private Socket webSocket;

    public DataNodeHandler() throws IOException {

        lastSendTime = System.currentTimeMillis();

        String fileName = "daq-data.csv";
        String logPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator
                + fileName;
        File logFile = new File(logPath);
        logFileWriter = new FileWriter(logFile, true);

        try {
            webSocket = IO.socket(URL);
            webSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleMessage(Message message) {
        long curTime = System.currentTimeMillis();
        // publish to pit view
        Bundle receive = message.getData();
        JSONObject objectSend = new JSONObject();
        for (String key : receive.keySet()) {
            try {
                objectSend.put(key, receive.getDouble(key));
            } catch (JSONException e) {
                // why is this a checked exception???
                // rethrow unchecked. It's not like we're letting the user build the object.
                throw new UnsupportedOperationException(e.getMessage());
            }
        }
        if (curTime - lastSendTime >= movingPublishPeriod) {
            webSocket.emit(objectSend.toString());
        }

        try {
            writeToCSV(receive);
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
