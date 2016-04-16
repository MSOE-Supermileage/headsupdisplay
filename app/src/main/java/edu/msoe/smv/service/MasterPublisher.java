package edu.msoe.smv.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.util.Log;

/**
 * Created by austin on 1/31/16.
 * god object that publishes data to everyone concerned
 * is this a controller? maybe?
 */
public class MasterPublisher {

    private final ResultReceiver receiver;
    private final WebPublisher pitViewPublisher;
    private final LogFilePublisher logFilePublisher;

    public MasterPublisher(ResultReceiver receiver, WebPublisher pitViewPublisher,
                           LogFilePublisher logFilePublisher) {
        this.receiver = receiver;
        this.pitViewPublisher = pitViewPublisher;
        this.logFilePublisher = logFilePublisher;
    }

    /**
     * send a message to the receiver
     * @param message the message to send
     */
    public void postMessage(String message) {
        Bundle send = new Bundle();
        send.putString(VehicleConnectionService.MESSAGE, message);
        receiver.send(VehicleConnectionService.MESSAGE_CODE, send);
    }

    /**
     * sends a data node to clients
     * @param node bundle containing raw data from data collector
     */
    public void postDataNode(Bundle node) {
        // send to the result receive (probably the activity that started the service)
        receiver.send(VehicleConnectionService.DATA_NODE_CODE, node);

        // send to the pit view publisher
        Message send = pitViewPublisher.obtainMessage();
        send.setData(node);
        pitViewPublisher.sendMessage(send);
        logFilePublisher.sendMessage(send);
    }

    /**
     * sends a message to clients that the data collector is or is not connected
     * @param connected true if the data collector appears to be connected and healthy
     */
    public void notifyDataCollectorConnectedStatus(boolean connected) {
        Bundle send = new Bundle();
        send.putBoolean(VehicleConnectionService.PI_CONNECTED, connected);
        receiver.send(VehicleConnectionService.PI_CONNECTED_CODE, send);
    }
}
