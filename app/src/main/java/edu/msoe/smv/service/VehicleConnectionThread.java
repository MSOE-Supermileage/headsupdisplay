package edu.msoe.smv.service;

import android.os.Bundle;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import edu.msoe.smv.utility.Utility;

/**
 * Created by austin on 1/31/16.
 */
public class VehicleConnectionThread extends ConnectionThreadBase {

    public static final int PORT = 5001;

    public VehicleConnectionThread(MasterPublisher masterPublisher) {
        super(masterPublisher);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try (ServerSocket waitSocket = new ServerSocket(PORT)) {
                // blocking call
                Socket dataCollector = waitSocket.accept();

                masterPublisher.notifyDataCollectorConnectedStatus(true);

                while (!Thread.currentThread().isInterrupted()) {

                    // blocking until receive
                    JsonReader reader = new JsonReader(new InputStreamReader(dataCollector.getInputStream()));

                    final Bundle dataNode = Utility.parseDataNode(reader);

                    // service doesn't care about which car or what's in the data node,
                    // let the client controller determine how to deal with the data.
                    masterPublisher.postDataNode(dataNode);
                }
            } catch (IOException e) {
                connectionError(e.getMessage());
            }
        }
    }
}