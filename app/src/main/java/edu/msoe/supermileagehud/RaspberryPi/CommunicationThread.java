package edu.msoe.supermileagehud.RaspberryPi;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import edu.msoe.supermileagehud.Calculations.Calculator;
import edu.msoe.supermileagehud.Cloud.CloudConnection;
import edu.msoe.supermileagehud.ConnectionActivity;

/**
 * Created by Connor on 10/16/2015.
 */
public class CommunicationThread extends Thread {
    private ConnectionActivity activity;

    private CloudConnection cloud;
    private Socket socket;

    private UIUpdater uiUpdater;

    /**
     * The thread that communicates to the raspberry pi
     * Receive and send data to and from the pi
     */
    public CommunicationThread(ConnectionActivity activity, Socket socket) {
        this.activity = activity;

        this.cloud = new CloudConnection();
        this.socket = socket;

        uiUpdater = new UIUpdater(activity);
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            String line;
            Calculator calculator = new Calculator();

            while ((line = in.readLine()) != null) {
                try {
                    JSONObject data = new JSONObject(line);

                    calculator.updateTime(Integer.parseInt(data.get("time").toString()));

                    uiUpdater.updateUI(calculator);
                    cloud.sendData(calculator.getJSON());
                } catch (JSONException e) {
                    Toast.makeText(activity, "Could not parse data from RPi", Toast.LENGTH_LONG).show();
                }
            }

            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
