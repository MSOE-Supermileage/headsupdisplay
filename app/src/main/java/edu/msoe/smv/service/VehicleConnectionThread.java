package edu.msoe.smv.service;

import android.os.Bundle;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import edu.msoe.smv.Utility;

/**
 * Created by austin on 1/31/16.
 */
public class VehicleConnectionThread extends ConnectionThreadBase {

    public static final int PORT = 5001;

    // all in milliseconds
    private static final int ACCEPT_TIMEOUT = 1000;
    private static final int READ_TIMEOUT = 1000;

    public VehicleConnectionThread(ReceiverNotifier notifier) {
        super(notifier);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try (ServerSocket waitSocket = new ServerSocket(PORT)){
                // set a second timeout in case we can't connect
                waitSocket.setSoTimeout(ACCEPT_TIMEOUT);

                // blocking call
                Socket dataCollector = waitSocket.accept();
                // start with a 5 second timeout, we may need to play with this
                dataCollector.setSoTimeout(READ_TIMEOUT);

                notifier.notifyDataCollectorConnectedStatus(true);

                while (!Thread.interrupted()) {
                    final Bundle dataNode;
                    // blocking until receive
                    JsonReader reader;
                    try {
                        long startTime = System.currentTimeMillis();
                        reader = new JsonReader(
                                new InputStreamReader(dataCollector.getInputStream()));

                        // service doesn't care about which car or what's in the data node,
                        // let the client controller determine how to deal with the data.
                        dataNode = Utility.parseDataNode(reader);

                        trackSocketTime(System.currentTimeMillis() - startTime);

                        notifier.postDataNode(dataNode);
                    } catch (IOException e) {
                        connectionError(e.getMessage());
                    }
                }
            } catch (IOException e) {
                connectionError(e.getMessage());
            }
        }
    }
}